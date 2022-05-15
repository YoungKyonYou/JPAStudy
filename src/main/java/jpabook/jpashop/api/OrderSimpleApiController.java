package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * xToOne 관계에 관해 살펴봄 (ManyToOne, OneToOne의 성능 최적화)
 * Order 조회
 * Order -> Member 연관 (Order와 Member는 ManyToOne 관계이다)
 * Order -> Delivery 연관 (Order와 Delivery는 OneToOne 관계이다)
 * 위 관계의 성능 최적화에 대해서 알아본다.
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {
    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    /*
    해당 api를 실행하면 무한 루프가 발생한다. 왜냐면 이는 양방향 관계이기 때문에
    처음에 Order클래스를 가고 Order클래스에 있는 Member 클래스에 가게 되면 또 List<Order>에서 Order로 가게되고
    이렇게 무한 루프가 발생한다. 그래서 양방향에 걸리는 Member, OrderItems, Delivery 부분에 @JsonIgnore를 걸어줘야 한다.
    그래도 문제가 발생한다 이 문제는 Member 쪽에서 설명을 해보면 Order 도메인 클래스에서 @ManyToOne 관계에 Member는 fetch 타입이 LAZY이다.
    즉, 지연로딩이라서 Member 객체를 안 가져온다 즉 DB에서 안 끌고 온다. 그리고 Order에 데이터만 가져오게 된다.
    그래서 hibernate에서는 이를 해결하기 위해 Order 도메인 클래스에 선언된
    private Member member=new Member(); 이부분을
    프록시 라이브러리를 사용해서 프록시를 상속 받아서 넣어둔다
    private Member member=new ProxyMember() 이런식으로. ProxyMember를 이 Member를 상속받는 것이다. 그것이 바로 에러 로그에 나오는 bytebuddy이다.
    즉 ByteBuddyInterceptor라는 게 대신 들어간다.(프록시 기술 중 하나)
    이 프록시 객체를 가짜로 넣어두고 뭔가 이 Member 객체를 손대면 그때 db에 Member 객체에 sql를 날려서 이 Member 객체에 값을 채워준다 그것을 이제
    proxy를 초기화한다고 한다. 근데 문제는 Json, 즉 Jackson라이브러리가 Member 객체를 봤는데 순수한 Member 객체가 아니라 bytebuddy로 되어 있어서
    이것을 어떻게 할 수 없다고 에러가 난 것이다. 그래서 이 문제를 해결하는 방법은 이렇게 지연 로딩인 경우엔 Json 라이브러리한테 뿌리지 말라고 할 수 있다.
    이것은 hibernate5 모듈을 설치해야 한다.
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-hibernate5'
    그리고 JpaBookApplication.java에 빈 등록하기.
    이제 실행하면 Order만 조회하게 된다.
    */
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());

        /*JpaBookApplication.java에 있는 Hibernate5Module의 FORCE_LAZY_LOADING를 쓰지 않고
        LAZY한 데이터를 FORCLY 가져오려면 이렇게 한다.
        이렇게 하면 우리가 원하는 정보인 Member, Delivery가 Postman에 제대로 출력된다. 그 외에 orderItems가 null로 표시된다.
        그런데 Postman 출력을 보면 가장 끝이 배열로 감싸져 있는데 이건 좋지 않다고 말했었다. MemberApiController의 memberV2 주석 참고하기
        이렇게 원하는 정보가 출력되었으나.. 사실 깔끔하진 않다. API를 만들 때 이렇게 복잡하게 안 만든다.
        요구사항이 만약 Member의 name과 Delivery의 주소 정보만 필요하다고 해보자. 즉 쓸데없는 정보들이 다같이 노출이 된다.
        이렇게 데이터를 다 노출해 버리면 운영하기가 어렵다. zipcode가 필요없는데 노출이 된다. 나중에 가서 누군가 쓴다면 협의해야 하는
        비용이 또 생기게 된다. 그래서 서로 다 바꿔줘야 하는 것이다. Hibernate5Module를 사용하기 보다는 DTO로 변환해서 반환하는 것이 좋다.*/
        for (Order order : all) {
            //Member에 있는 아무 필드나 get하면 강제초기화 됨.
            order.getMember().getName(); //Lazy 강제 초기화
            //Delivery에 있는 아무 필드나 get하면 강제초기화 됨.
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    //참고로 원래는 이렇게 List로 반환하면 안 된다. 한번 Result로 감싸야 한다 MemberApiController의 memberV2처럼
    //지금 현재 버전은 감싸지 않은 것이다.!!!!!!
   public List<SimpleOrderDto> oderV2(){
        //Order 2개의 쿼리를 조회하게 된다 (즉 한개의 쿼리로 조회)
        List<Order> orders=orderRepository.findAllByString(new OrderSearch());

        //여기서 쿼리 2개가 나온다. Member를 조회하는 것과 Delivery를 조회하는 쿼리가 각각 나옴 즉 2번 루프를 돌게 된다.
        List<SimpleOrderDto> result=orders.stream()
                .map(o->new SimpleOrderDto(o))
                .collect(Collectors.toList());
        return result;
        /*return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(o->new SimpleOrderDto(o))
                .collect(toList());
        //map(SimpleOrderDto::new)처럼 람다로 바꿔도 무방*/
        /**
         * 우리 데이터에서 Order가 2개가 있다.
         * 그래서 이 API를 사용하게 되면 총 쿼리가 5개가 나간다 (1개-order로 조회) (1개 order에 2개 쿼리 또다른 1개 order에 2개 쿼리)
         * 그래서 5쿼리가 나가게 된다. 쿼리가 어마어마하게 나간 것이다. 이것이 N+1 문제인 것이다. 여기서 1은 Order 쿼리를 의미한다.
         * N은 Order 데이터의 개수인 2개이다. 그러면 첫 번쨰 쿼리의 결과로 N번 만큼 추가로 실행되는 것이 N+1 문제인 것이다.
         * 즉, N+1 => 1(Order)+N(회원)+N(배송) = 5인 것이다. 그렇다고 Order 도메인 클래스에 Member와 Delivery를 EAGER로
         * 바꾼다고 쿼리가 최적화되지 않는다!!!!(쿼리 예측도 안 됨)
         */
    }

    /**
     * v2와 다르게 쿼리 한방에 모든 정보를 가져온다.!!!!! 성능도 매우 빠르다. fetch join은 정말 적극적으로 사용해야 한다.
     * 하지만 여기조차 단점이 좀 존재하여 최적화가 필요하다. 이 API를 실행시켜서 쿼리를 보면 select 절에서 다 끌고(모든 필드, 사용하지 않는 필드) 온다
     * Order, Member, Delivery를 다 끌고(모든 필드를 끌고옴) 온다. 이게 약간 엔티티를 찍어서 줘야하는 단점이 있는데 다음 버전은 이것을 최적화 해본다.
     * 즉, 여기서는 엔티티를 조회해서 중간에 dto로 변환을 했는데 이런 거 없고 다음 버전에서는 바로 jpa에서 dto로 끄집어 내겠끔
     * 성능 최적화를 해본다.
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        List<Order> orders=orderRepository.findAllWithMemberDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }

    /**
     * 여기서는 v3과는 다르게 필요한 필드만 뽑아서 select해서 끌고 온다. v3와 같이 모든 필드를 select하지 않는다.
     * 조인하는 것은 v3와 같아도 v4에서는 적은 필드를 select함으로써 네트워크 용량(생각보다 미비, 요즘
     * 네트워크 매우 좋음)을 적게 쓰는 이점이 있다. 그럼 V4가 더 좋은가 라고 생각할 수 있지만
     * V3와 V4의 우열을 가릴 수 없다. trade-off가 존재한다. 뭐냐면 V4 같은 경우 화면에 최적화해서 가져오긴 했지만 재사용성이 없다!
     * V4는 이 dto를 쓸때만 쓸 수 있는 것이다. 반면에 V3는 재사용성이 좋다. 공용으로 좀 더 쓸 수 있는 것이다. 그러니까 많은 곳에서 이 V3
     * API를 활용할 수 있게 되는 것이다. V4는 feat하게 만들었기 때문에 로직을 재사용할 수 없다는 단점이 있다. 성능은 나을지 몰라도
     * 재사용성이 부족하다.
     * 엄밀히 말해서 강사님 말로는 V4는 물리적으로는 계층이 나눠져 있지만 논리적으로는 계층이 다 깨져 있는 형태이다 사실 V3정도는 괜찮다.(엔티티를 조회하는 거)
     * V4는 API 스펙이 바뀌면 다 뜯어 고쳐야하는 단점이 존재한다 그래서 trade-off인 것이다. 이제 선택을 해야 하는 것이다
     * 그런데 뭘 선택하는 것이 좋은가? 그런데 V3와 V4의 성능 차이가 많이 날까? 대부분의 시스템에서는 성능 차이가 많이 나지 않는다.(제일 정확한 것은 테스트 하는 것)
     * 대부분의 성능은 join 부분에서 성능을 다 먹어버린다. 전체적인 관점에서 보면 select 필드가 좀 는다고 성능에 영향을 거의 받지 않는다.
     * 중요한 건 join하는 부분에서 비용이 많다.(하지만 데이터 개수에 따라 다를 수 있다 데이터가 엄청 많으면 또 이야기가 달라진다.)
     * 강사님은 이런 경우 repository 밑에 패키지를 또 만든다. 쿼리용, 이렇게 성능 최적화된 쿼리용을 뽑아서 하위에 만든다.
     * 현재 repository/order/simplequery 패키지에 findOrderDtos를 넣는 것이 맞지만 지금은 일단 그냥 가겠다.
     * 중요한건 강사님은 쿼리용을 별도로 이렇게 뽑아낸다.
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimpleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; //주문시간
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
        }
    }
}
