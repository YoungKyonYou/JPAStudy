package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Delivery {
    @Id @GeneratedValue
    @Column(name="delivery_id")
    private Long id;

    /*
        OrderSimpleApiController에서 ordersV1 메서드를 호출할 때 발생하는 무한루프를 막기 위함
         */
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy="delivery")
    private Order order;

    @Embedded
    private Address address;

    //ORDINAL 절대 쓰면 안됨.. 중간에 뭐 들어오면 망함
    @Enumerated(EnumType.STRING)
    private DeliveryStatus status; //READY, COMP
}
