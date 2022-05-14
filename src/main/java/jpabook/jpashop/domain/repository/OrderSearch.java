package jpabook.jpashop.domain.repository;

import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

@Data
public class OrderSearch {
    private String memberName; //회원 이름
    private OrderStatus orderStatus;
}
