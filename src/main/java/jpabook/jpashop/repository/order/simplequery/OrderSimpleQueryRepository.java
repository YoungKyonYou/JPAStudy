package jpabook.jpashop.repository.order.simplequery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderSimpleQueryRepository {
    private final EntityManager em;
    public List<OrderSimpleQueryDto> findOrderDtos() {
        //select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto()에서 주의할 점은 엔티티를 인자로 넘기면 안된다.
        //그렇게 되면 엔티티가 식별자로 들어가기 때문이다 그래서 OrderSimpleQueryDto에서 생성자 파라미터를 일일이 다시 만들어준 것이다.
        //하지만 d.address처럼 Address는 가능하다. Address 클래스에 들어가보면 엔티티가 아니라 value타입이기 때문이다. 이런 건 값으로 동작하기 때문이다.
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"+
                                " from Order o"+
                                " join o.member m"+
                                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }

}
