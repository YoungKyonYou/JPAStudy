package jpabook.jpashop.domain;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Member {
    @Id @GeneratedValue
    @Column(name="member_id")
    private Long id;

    private String name;

//    내장 타입임을 선언
    @Embedded
    private Address address;

    @OneToMany(mappedBy="member")
    private List<Order> orders=new ArrayList<>();


}
