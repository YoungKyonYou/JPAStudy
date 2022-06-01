package jpabook.jpashop.repository.order.query;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

//OrderApiController.java에서 ordersV6()부분에서 리턴문의 groupingBy를 보면
//어떤 것으로 묶을지 알려줘야 한다. 그런데 지금 OrderQueryDto가 객체이다.
//이것을 묶을 때 equals 그리고 hash 코드를 알려줘야 한다 그래서 이 롬복을 사용한 것이다.
//이렇게 orderId를 써주면 이것을 기준으로 묶는다.
@EqualsAndHashCode(of="orderId")
@Data
public class OrderQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemQueryDto> orderItems;

    //여기서 orderItems 초기화 부분을 뺐다 이유는 OrderQueryRepository.java의 findOrderQueryDtos() 메서드 주석 참고
    public OrderQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
        this.orderItems = orderItems;
    }

    public OrderQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address, List<OrderItemQueryDto> orderItems) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
        this.orderItems = orderItems;
    }
}
