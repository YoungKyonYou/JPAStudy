package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderRepository orderRepository;

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
                .collect(Collectors.toList());
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
            orderItems=order.getOrderItems().stream().map(orderItem -> new OrderItemDto(orderItem)).collect(Collectors.toList());

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

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return collect;
    }









}
