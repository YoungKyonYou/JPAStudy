package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class, id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch){
        //파라미터가 이렇게 name과 status가 무조건 들어가면 괜찮다. 그런데 동적 쿼리를 작성하려고 하는 순간 지옥이 시작됨
        //status나 name이 안들어가면 모든 것이 다 조회가 되게 만들거나 둘 중 하나만 있다면 그것에 해당하는 것만
        //조회하게 하는 것을 동적쿼리라 한다. 이게 만만치 않다.
        /*return em.createQuery("select o from Order o join o.member m"+
                " where o.status =: status "+
                " and m.name like :name", Order.class)
                .setParameter("status",orderSearch.getOrderStatus())
                .setParameter("name",orderSearch.getMemberName())
                .setMaxResults(1000) //최대 1000건
                .getResultList();*/

        //동적 쿼리 해결 방안 1. 무식한 방법 - 이런 방식 잘 안씀
        String jpql="select o from Order o join o.member m";
        boolean isFirstCondition=true;

        //주문 상태 검색
        if(orderSearch.getOrderStatus()!=null){
            if(isFirstCondition){
                jpql+=" where";
                isFirstCondition=false;
            }else{
                jpql+=" and";
            }
            jpql+=" o.status =: status";
        }

        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            if(isFirstCondition){
                jpql+=" where";
                isFirstCondition=false;
            }else{
                jpql+=" and";
            }
            jpql+=" m.name like :name";
        }

        TypedQuery<Order> query=em.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if(orderSearch.getOrderStatus()!=null){
            query=query.setParameter("status", orderSearch.getOrderStatus());
        }
        if(StringUtils.hasText(orderSearch.getMemberName())){
            query=query.setParameter("name",orderSearch.getMemberName());
        }

        return query.getResultList();
    }

    /**
     * JPA Criteria
     */
    //동적 쿼리 2번째 방법 : 이것 또한 권장하는 방법이 아님.
    //이렇게 하면 치명적인 단점이 있다
    //이건 실무에서 쓸 수 없다..
    //이건 유지보수성이 0에 가깝다. 이렇게 하면 무슨 query가 생성될 지 상상이 되는 가? 안 떠오른다
    //JPA 표준 스펙에 JPA Criteria가 있지만 안 쓴다. 이거 썼다가 유지보수가 안 되서 안 쓴다.
    //그냥 이런 식으로 할 수 있다는 것만 보여준 것이다.
    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Object, Object> m=o.join("member", JoinType.INNER);

        List<Predicate> criteria=new ArrayList<>();

        //주문 상태 검색
        if(orderSearch.getOrderStatus()!=null){
            Predicate status = cb.equal(o.get("status"), orderSearch.getOrderStatus());
            criteria.add(status);
        }

        //회원 이름 검색
        if(StringUtils.hasText(orderSearch.getMemberName())){
            Predicate name=
                    cb.like(m.<String>get("name"), "%"+orderSearch.getMemberName()+"%");
            criteria.add(name);
        }

        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000);
        return query.getResultList();
    }

    //이러한 기법을 fetch join이라고 한다. JPA에만 있는 문법임
    //이건 매우 중요하기 실무에서 JPA를 쓰려면 100% 이해해야 한다!!
    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o"+
                        " join fetch o.member m"+
                        " join fetch o.delivery d", Order.class)
                .getResultList();
    }

    //페이징 *ToOne 관계는 fetch join해도 페이징이 잘된다! 참고: setFirstResult()는 0부터 시작함으로 1로 설정하면 두 번째 데이터부터 표시됨
   public List<Order> findAllWithMemberDelivery(int offset,int limit ) {
        return em.createQuery(
                        "select o from Order o"+
                                " join fetch o.member m"+
                                " join fetch o.delivery d", Order.class)
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();

    }

    //fetch join 하기
    public List<Order> findAllWithItem() {
        /**
         * 여기보면 Order와 orderItems를 조인하고 있다 그럼 지금 데이터량이
         * 예를 들어 Order가 2개라고 하고 OrderItems가 4개라고 했을 떼
         * 이게 다 조인이 되는 것이다. 그렇게 되면 Order가 4개가 되는 것이다.(조인이 되니까, 강의 "주문 조회 V3: 엔티티를 DTO로 변환-페치 조인 최적화" 편에 3:53분 참고)
         * 결국 조인한다고 하면 Order_Id로 조인을 하게 되는데 Order가 2개고 OrderItems가 4개인데 관계형 데이터베이스에서 조인을 하면 결과는 많은 쪽으로 맞춰져서 나온다.
         * 이러면 문제가 JPA에서 데이터를 가져올 때 데이터가 두배가 되버리는 것이다. 그래서 distinct로 중복을 제거해주는 것이다. 이 distinct는 실제 query에서도 distinct를
         * 넣어주는 것을 실행결과로 알 수 있다 하지만 이 distinct는 문제가 있다. 앱을 실행하고 나오는 distinct를 포함하는 select 절을 h2에서 조회를 해보면
         * 그대로 데이터가 뻥튀기 되는 것을 볼 수 있는데 이 이유는 distinct는 한 행이 완전히 다른 행이랑 같아야 적용이 된다. 그런데 h2에서 복사해서 결과를 보면
         * 컬럼 중에 데이터가 다른 것들이 있다.
         * 하지만~! 여기 em.createQuery 안에 들어가는 distinct는 일반적인 query에 들어가는 distinct와는 좀 다르다 여기서는 Order를 가지고 올때 Order가 같은 ID값이면  중복된
         * 데이터를 버린다. 그래서 데이터가 뻥튀기가 안 되는 것이다. 즉, 애플리케이션 단에 다 가져와서 한번 더 걸러주는 것이다.그래서 원하는데로 데이터가 나오는 것이다.
         * 즉 distinct에는 2가지 기능이 있다.
         * 1. Query 단에서 distinct를 추가한다
         * 2. 엔티티가 중복이면 그 중복을 걸러서 담아준다.
         * 하지만 치명적인 단점이 있다. 페이징이 불가능하다!! 일대다를 페치조인하는 순간 페이징 쿼리가 아예 나가지 않는다.
         * 단, 일대다가 아니면 상관이 없다. 다대일은 괜찮다. 여기서 member와 delivery는 괜찮다는 것이다. item도 일대다라 괜찮지만 중간에 orderItem이 걸려들어간다.
         * Order와 OrderItem은 일대다 이다. 그래서 결국 item도 안 된다.
         * 또한 컬렉선 페치 조인은 1개만 사용할 수 있다. 즉, 컬렉션 둘 이상에 페치 조인을 사용하면 안된다. 데이터가 부정합하게 조회될 수 있다.
         * 쉽게 말해서 일대다에 대한 페치조인은 한 개만 사용해야 한다는 것이다.
         * 꼭 기억하기!*/
        return em.createQuery(
                "select distinct o from Order o"+
                        " join fetch o.member m"+
                       " join fetch o.delivery d"+
                       " join fetch o.orderItems oi"+
                       " join fetch oi.item i", Order.class).getResultList();

        /*
        아래가 페이징 조건을 추가한 것이다. 몇번째부터 몇개를 가져오라는 것이다. 하지만 fetch join에서는 이것이 불가하다!!!!!
        참고: setFirstResult는 0부터 시작 즉 여기서 1이라는 것은 0를 버리고 1부터 시작하라는 것이다.
        return em.createQuery(
                "select distinct o from Order o"+
                        " join fetch o.member m"+
                       " join fetch o.delivery d"+
                       " join fetch o.orderItems oi"+
                       " join fetch oi.item i", Order.class)
                       .setFirstResult(1)
                       .setMaxResults(100)
                       .getResultList();
         */
    }
/*
    public List<OrderSimpleQueryDto> findOrderDtos() {
        //select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto()에서 주의할 점은 엔티티를 인자로 넘기면 안된다.
        //그렇게 되면 엔티티가 식별자로 들어가기 때문이다 그래서 OrderSimpleQueryDto에서 생성자 파라미터를 일일이 다시 만들어준 것이다.
        //하지만 d.address처럼 Address는 가능하다. Address 클래스에 들어가보면 엔티티가 아니라 value타입이기 때문이다. 이런 건 값으로 동작하기 때문이다.
        return em.createQuery(
                "select new jpabook.jpashop.repository.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"+
                        " from Order o"+
                        " join o.member m"+
                        " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }*/


}
