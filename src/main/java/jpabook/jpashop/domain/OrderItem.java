package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jpabook.jpashop.domain.item.Item;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Data
public class OrderItem {
    @Id @GeneratedValue
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    /*
    OrderSimpleApiController에서 ordersV1 메서드를 호출할 때 발생하는 무한루프를 막기 위함
     */
    @JsonIgnore
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice; //주문 가격

    private int count; //주문 수량

    //이것도 롬복으로 줄일수 있음 @NoArgsConstructor(access=AcessLevel.PROTECTECT)
//    protected OrderItem(){
//
//    }


    //==생성 메서드==//
    //매개변수로 Item item 안에 price가 있으니까 굳이 int orderPrice로 안 해도 되지 않나?
    //답: 그런데 할인 될수도 있다. 바뀔 수 있기 때문에 따로 가져가는 게 맞다 지금은 할인 같은 건 없다.
    public static OrderItem createOrderItem(Item item, int orderPrice, int count){
        OrderItem orderItem=new OrderItem();
        orderItem.setItem(item);
        orderItem.setOrderPrice(orderPrice);
        orderItem.setCount(count);
        item.removeStock(count);
        return orderItem;
    }

    //==비즈니스 로직==//
    public void cancel() {
        getItem().addStock(count);
    }

    //==조회 로직==//
    /**
     * 주문상품 전체 가격 조회
     */
    public int getTotalPrice() {
        return getOrderPrice() * getCount();
    }
}
