package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     *
     * 이것을 PostMan에서 돌리면 가장 겉 껍데기가 배열로 감싸져 있는데 이러면 안된다. Object로 감싸줘야 한다. MemberApiController.java 확인하기
     * 그리고 이 방법은 좋은 방법이 아니다. 항상 엔티티를 그대로 노출하면 안된다고 말을 했었다(반환형이 List<Order>에서 Order 부분)
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> all=orderRepository.findAllByString(new OrderSearch());

        //객체 그래프를 초기화한다.
        //OrderSimpleApiController.java의 주석 확인하기
        for (Order order : all) {
            order.getMember().getName();
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o->o.getItem().getName());
        }
        return all;
    }
    /**
     * V1처럼 Order를 직접적으로 노출하지 않고 V2에서는 DTO로 노출하는 것을 해본다.
     * 이것도 마찬가지로 postman에서 확인하면 가장 겉에가 배열로 감싸져 있는데 클래스로 한번 감싸는 게 맞다
     * MemberApiController.java 주석 확인하기

     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }
    // @Data나 @Getter가 없으면 No serializer found for class 에러가 발생한다.
    //이는 serializer 하는 과정에서 기본으로 접근 제한자가 public이거나 getter/setter를 이용하기 때문에 인스턴스 필드를 private
    //등으로 선언하면 json으로 변환 과정에서 에러가 발생하는 것이다 그래서 @Data나 @Getter이 필요하다.
      /* 또한 이렇게 DTO를 만들었다고 다된 것이 아니다. 외부로 또 엔티티가 노출된다는 단점이 있다.
         OrderDto static class에서 private List<OrderItem> orderItems;를 보면 List의 타입이 OrderItem이다. 즉 엔티티를 노출하는 것이다.
         이렇게 단순히 DTO로 감싸서 보내라는 것이 아니라 엔티티와 완전히 의존을 끊어야 되는 것이다. 이렇게 그대로 OrderItem를 반환하면 안된다!
         이것또한 아래에 static class OrderItemDto가 필요하다다*/
    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        //윗 주석 참고(엔티티 노출로 인해서 안됨)
       // private List<OrderItem> orderItems;
        private List<OrderItemDto> orderItems;
        public OrderDto(Order order) {
            this.orderId=order.getId();
            this.name=order.getMember().getName();
            this.orderDate=order.getOrderDate();
            this.orderStatus=order.getStatus();
            this.address=order.getDelivery().getAddress();
            //프록시 초기화를 해야지 안 하면 orderItem:null이 postman에서 보여지게 된다. ordersV1() 메서드 확인해 보기
            //참고로 여기서 o.getItem().getPrice()를 하든 o.getItem().getName()를 하든 o.getItem().getStockQuantity()를 하든 상관없다.
            //하지만 이 방법은 OrderItem 엔티티를 그대로 노출시킨다 바로 이 메서드 선언부 쪽에 주석 참고하기
            //order.getOrderItems().stream().forEach(o->o.getItem().getPrice());
            //Category까지 조회하기 위해서 프록시 초기화
            //order.getOrderItems().stream().forEach(o->o.getItem().getCategories().forEach(m->m.getName()));

            //OrderItem으로 매핑하는 것이 아니라 OrderItemDto로 매핑을 해줘야 한다.
            orderItems=order.getOrderItems().stream().map(orderItem -> new OrderItemDto(orderItem)).collect(toList());

        }
    }

    @Getter
    static class OrderItemDto{

        //이 정보만 필요하다고 가정했을 때
        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            this.itemName=orderItem.getItem().getName();
            this.orderPrice=orderItem.getOrderPrice();
            this.count=orderItem.getCount();
        }
    }

    /**
     * V2에서는 쿼리가 여러개 나갔는데 여기서는 쿼리가 1번만 나가게 된다.
     * 여기서 fetch join을 findAllWithItem()에서 해주고 있다. 사실상 코드는 V2와 거의 똑같다.
     */
    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        //findAllWithItem() 메서드 영역 부분에 주석 꼭 확인하고 강의 듣기(이대로 하면 데이터가 뻥튀기 된다)
        List<Order> orders=orderRepository.findAllWithItem();
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return collect;
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value="offset",defaultValue="0") int offset,
                                        @RequestParam(value="limit", defaultValue="100")int limit)
    {
        //Order와 Member, Delivery를 fetch join한다.
        /**
         * 여기서는 V3에서의 한계를 돌파해본다. 즉, 페이징도 가능하고 페치 조인도 가능하며 성능도 최적화시킬 수 있는 방법을 알아본다.(강의 보는 거 추천)
         *  첫 번째 최적화 방법: *ToOne 관계에 있는 것은 모두 fetch join를 한다(findAllWithMemberDelivery())
         *  resources 폴더 아래 application.yml에서 default_batch_fetch_size: 100라고 설정을 했다
         *  이것은 만약 Item이 1000개이면 루프가 10번을 돈다는 의미이다. in(...) query를 100개씩 날리는 것이다.
         *  지금 Item에 4개가 저장되어 있다 만약 batch_fetch_size:3으로 해두면 2번 루프가 되는데 한번은 3번 in(...) query를 날리고 한번은 1번 in(...) query를 날리게 될 것이다.
         *  즉 이걸로 인해서 1+N+M이였던 쿼리 개수가 1+1+1이 되는 것이다. 즉 페이징도 되고 성능최적화를 시켰다. 물론 V3에서는 쿼리가 한번에 나간거에 비해 좀 더 나간건 맞다.
         *  하지만! 이게 정말 느릴까? 이게 장단점이 있다. 이전 V3에서의 문제는 fetch join를 하지만 중복이 너무 많다.(뻥튀기) 이것은 DB에서 이미 다 데이터를 가져오고 앱 단에서 걸러주는데
         *  그래서 쿼리는 한방에 나가지만 데이터 전송량 자체는 많아지는 것이다. 하지만 이 V3.1은 쿼리가 3개 나가지만 데이터가 최적화되서 나간다.
         *  자 그러면 batch_fetch_size를 몇으로 걸어주면 되나? minimum은 없는데 maximum은 있다. 맥시멈 1000개이다.
         *  왜냐면 in query가 천개를 넘어가면 오류를 일으키는 것들이 있어서 그것이 max라고 보면 된다.
         */
        //List<Order> orders=orderRepository.findAllWithMemberDelivery();

        List<Order> orders=orderRepository.findAllWithMemberDelivery(offset, limit);
        List<OrderDto> result = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());
        return result;
    }


    /**
     * V3.1은 엔티티를 조회해서 데이터를 가져오는 방변에 여기서는 원하는 데이터를 모두 조인해서 한번에 필요한 데이터만 조회하는 방식이다.
     * 이러한 방식을 "DTO로 조회하는 방식"이라고 일반적으로 이야기한다.
     * 이렇게 DTO로 조회하게 되면 엔티티가 아니다. 따라서 지연로딩, Fetch join등을 사용할 수 없다.
     * 이렇게 DTO로 조회하려면 SQL의 JOIN문을 사용해서 처음부터 원하는 데이터를 모두 선택해서 조회해야 한다.
     */
    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4(){
        /**
         * 이렇게 하면 order, member, deliver를 조인해서 한 쿼리로 가져오고(fetch join아님 그냥 join임)
         * orderitem이랑 item를 가져온다. 이것은 findOrderQueryDtos() 메서드 안에 findOrderItems()할때 발생한다.
         * 그래서 총 쿼리는 Order 한번 그리고 Item 각각 하나씩(2개) 해서 총 3번 발생한다.
         * 하지만 이것도 결과적으로 N+1 문제이다.
         */
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * Order가 많으면 그걸 in() 절로 한번에 가져온 다음에 메모리에 올려놓은 다음 조립
     * V4에서 발생한 N+1 문제 해결
     * Query: 루트 1번, 컬렉션 1번
     * *ToOne 관계들을 먼저 조회하고 여기서 얻은 식별자 orderId로 *ToMany 관계인 OrderItem을 한꺼번에 조회한다.
     * MAP을 사용해서 매칭 성능 향상(O(1))
     * 강사님은 V6보다 V5 방식을 많이 사용하나 항상 상황을 봐야한다.
     */
    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5(){

        return orderQueryRepository.findAllByDto_optimization();
    }


    //반환형이 List<OrderFlatDto>가 아닌 우리가 원하는 스펙이 OrderQueryDto라고 해보자(원하는 데이터 스펙이 OrderQueryDto)
    //이 방법은 쿼리는 한번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터가 추가되므로 상황에 따라 V5보다 더 느릴 수 있다.
    //즉, 상황에 따라서 V5에 따라 느릴 수 있는데 데이터가 엄청 클 때 이야기이다. 데이터가 많지 않으면 이게 빠르다.
    //그리고 애플리케이션에서 추가 작업이 크다
    //그리고 페이징이 불가능하다.(출력된 쿼리 h2에서 직접 출력해보기 그럼 알거임)
    //이 버전은 Order를 기준으로 페이징이 불가능하다. 왜냐면 limit를 걸게되면 Order를 기준으로 묶는 게 아니라 OrderItem 기준으로 묶기 때문이다.
    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> ordersV6(){
        

        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();


        //이건 그냥 아래 리턴 문을 이해하려고 작성한 코드인다.
        //https://www.baeldung.com/java-groupingby-collector 여기를 보면 groupingBy가 3가지로 오버로딩된 것을 볼 수 있음, 문서에서 두번째 꺼임
//        Map<OrderQueryDto, List<OrderItemQueryDto>> collect = flats.stream()
//                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
//                        mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
//                ));
        //그리고 저렇게 만들어진 Map를 entrySet()으로 하나하나 조회하고 그걸로 OrderQueryDto 스펙으로 초기화 한다음 List화 해주는 것이다.
        //이렇게 최종적으로 OrderQueryDto를 반환해 주게 되는 것이다.
        // 그리고 @EqualsAndHashCode(of="orderId") 부분 유의하기 이것은 OrderQueryDto.java에서의 주석 확인하기
        /**
         * entrySet()이하는 Map<OrderQueryDto, List<OrderItemQueryDto>> collect로 뽑는다. 그리고 각 Map를 순회하게 된다.
         * 순회하면서 OrderQueryDto.java에서 orderItem 필드를 포함하는 생성자로 초기화해주게 되는 것이다.
         */
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),e.getKey().getAddress(), e.getValue()))
                .collect(toList());

    }

}
