package jpabook.jpashop.domain.repository;

import jpabook.jpashop.domain.Order;
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
}
