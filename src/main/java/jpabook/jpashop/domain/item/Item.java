package jpabook.jpashop.domain.item;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Inheritance(strategy=InheritanceType.JOINED)
@DiscriminatorColumn(name="dtype")
public abstract class Item {
    @Id @GeneratedValue
    @Column(name="item_id")
    private Long id;

    private String name;
    private int price;
    private int stockQuantity;

}
