package jpabook.jpashop.domain;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Delivery {
    @Id @GeneratedValue
    @Column(name="delivery_id")
    private Long id;


    @OneToOne(mappedBy="delivery")
    private Order order;

    @Embedded
    private Address address;

    //ORDINAL 절대 쓰면 안됨.. 중간에 뭐 들어오면 망함
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; //READY, COMP
}
