package jpabook.jpashop.repository.order.query;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class OrderQueryRepository {
    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos(){
        //제일 처음에 findOrders를 가져온다 여기서 OrderQueryDto의 필드 중 orderItems를 제외한 나머지를 초기화한다.
        List<OrderQueryDto> result=findOrders(); // N+1인 이유 여기서 1번으로 2개가 나온다.(N개)

        //여기서 orderItems를 다 가져오게 됨
        result.forEach(o->{
            List<OrderItemQueryDto> orderItems=findOrderItems(o.getOrderId()); //바로 위에서 1번으로 2개가 나옴 그래서 여기서 2번 실행된다.(N번)
            o.setOrderItems(orderItems);
        });
        return result;
    }
    public List<OrderItemQueryDto> findOrderItems(Long orderId){
        return em.createQuery(
                //여기보면 join를 했는데 OrderItem과 item를 조인하는 것을 볼 수 있다
                //orderItem 입장에서 item은 *ToOne 관계이기 때문에 데이터가 증가해도 row에서 컬럼 형식으로 증가를 하지 데이터 row 자체가 증가하지 않는다
                //그래서 그냥 join를 함으로써 최적화를 한 것이다.
                "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"+
                        " from OrderItem oi"+
                        " join oi.item i"+
                        " where oi.order.id=:orderId", OrderItemQueryDto.class)
                .setParameter("orderId",orderId)
                .getResultList();

    }


    /*
    이것을 별도로 만든 이유는 OrderQueryRepository에서 OrderQueryDto가 OrderApiController.java에 있는 OrderDto 클래스와
    필드가 동일함에도 불구하고 List<OrderQueryDto> 이렇게 가져오고
    OrderApiController.java에 있는 OrderDto 클래스 즉, List<OrderDto>를 안 한 이유는 그렇게 OrderDto를 참조해버리면
     Repository가 Controller를 참조하는 의존관계가 생긴다 즉 순환이 되버리는 문제가 생긴다. 그래서 OrderQueryDto를 OrderQueryRepository와 같은
     패키지에 생성하였다.*/
    public List<OrderQueryDto> findOrders() {
        return em.createQuery(
                //일단 orderItems는 생성자로 안 넘겨줬다 그리고 OrderQueryDto의 생성자에도 해당 인자 받는 부분을 뺐다 이유는 jpql에서 컬렉션을 넣을 수가 없기 때문이다.
                //이렇게 데이터를 넣을 수 밖에 없다.
                "select new jpabook.jpashop.repository.order.query.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                        " from Order o"+
                        " join o.member m"+
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    //이렇게 하면 쿼리가 총 2번 나간다. findOrders()에서 한번+em.createQuery에서 한번
    public List<OrderQueryDto> findAllByDto_optimization() {

        //이 부분은 V4와 같다. 왜냐면 Order가져오는 것은 똑같다.
        //이전 findOrderQueryDtos()에 단점은 루프를 돈다는 것이다. 이번에는 한방에 가져올 것이다.
        List<OrderQueryDto> result = findOrders();

     //   List<Long> orderIds = toOrderIds(result);

        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(toOrderIds(result));

        //orderId별로 각각 List가 존재하는데 그 List 자체를 다 넣어주는 것이다.
        result.forEach(o->o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.order.query.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                                " from OrderItem oi" +
                                " join oi.item i" +
                                " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();
        //여기서 key:orderItemQueryDto.getOrderId() value:List<OrderItemQueryDto> 이게 되는 것이다.
        //
        Map<Long, List<OrderItemQueryDto>> orderItemMap = orderItems.stream()
                .collect(Collectors.groupingBy(orderItemQueryDto -> orderItemQueryDto.getOrderId()));
        return orderItemMap;
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        List<Long> orderIds= result.stream()
                .map(o->o.getOrderId())
                .collect(Collectors.toList());
        return orderIds;
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                "select new jpabook.jpashop.repository.order.query.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)"+
                        " from Order o"+
                        " join o.member m"+
                        " join o.delivery d"+
                        " join o.orderItems oi"+
                        " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }
}
