package jpabook.jpashop.domain;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="orders")
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
    private List<OrderItem> orderItems=new ArrayList<>();


    //order 저장할 때 delivery만 세팅해두면 따로 delivery를 persist할 필요가 없음(cascade)
    @OneToOne(fetch=FetchType.LAZY, cascade=CascadeType.ALL)
    @JoinColumn(name="delivery_id")
    private Delivery delivery;

    private LocalDateTime orderDate; //주문 시간

    @Enumerated(EnumType.STRING)
    private OrderStatus status; //주문 상태 [ORDER, CANCEL]

    //==연관관계 메서드(편의 메서드)
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
}
