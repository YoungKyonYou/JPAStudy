package jpabook.jpashop.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
//protected 생성자 생성한것과 동일
@NoArgsConstructor(access= AccessLevel.PROTECTED)
@Data
public class Order {
    @Id @GeneratedValue
    @Column(name="order_id")
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="member_id")
    private Member member;

    //cascade를 설정하는 이유는 원래는 OrderItem이 있으면 다 persist해주고
    //마지막으로 persist(order)를 해줘야 하는데 이것을 설정하게 되면
    //persist(order)만 하면 OrderItem를 따로 persist 안 해줘도 된다.
    @OneToMany(mappedBy = "order", cascade=CascadeType.ALL)
    //resources아래 application.yml 파일에 적은 default_batch_fetch_size: 100은 글로벌하게 적용하는 것인데
    //이렇게 @BatchSize를 하는 것은 좀 더 디테일 적용하게 하는 것이다. 컬렉션을 사용할 경우 이렇게 적용하면 되는데 컬렉션이 아닌경우도 있다.(예:OrderItem.java의 Item부분 거긴 *ToOne 관계이다.)
    //그럴때는 Item.java의 class 위에다가 적는다. 하지만 이것도 이렇게 적는 게 크게 의미가 없다. 상황에 따라 달라지기 때문이다 그래서 강사님은 yml에다가 글로벌하게 자주 적용하신다.
    //@BatchSize(size=1000)
    private List<OrderItem> orderItems=new ArrayList<>();


    //order 저장할 때 delivery만 세팅해두면 따로 delivery를 persist할 필요가 없음(cascade)
    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name="delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문 상태 [ORDER, CANCEL]

    //==연관관계 메서드(편의 메서드)==//
    public void setMember(Member member){
        //Order이 N이고 Member가 1인데
        //여기서 1인 Member를 this.member로 초기화하고
        //member의 orders를 추가한다
        this.member=member;
        member.getOrders().add(this);
    }
    public void addOrderItem(OrderItem orderItem){
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
    public void setDelivery(Delivery delivery){
        this.delivery=delivery;
        delivery.setOrder(this);
    }

    //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems){
        Order order=new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for(OrderItem orderItem:orderItems){
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //==비즈니스 로직==//
    /**
     * 주문취소
     */
    public void cancel(){
        if(delivery.getStatus()==DeliveryStatus.COMP){
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);
        for(OrderItem orderItem:orderItems){
            orderItem.cancel();
        }
    }

    //==조회 로직==//
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice(){
        int totalPrice=0;
        for(OrderItem orderItem:orderItems){
            totalPrice+=orderItem.getTotalPrice();
        }
        return totalPrice;
    }
}
