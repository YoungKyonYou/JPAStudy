package jpabook.jpashop.domain.repository;

import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class MemberRepository {
    //RequiredArgsContructor가 작동하려면 final이 필요
    private final EntityManager em;
//    //스프링이 이 entity manager를 만들어서 injection한다.
//    @PersistenceContext
//    private EntityManager em;

//    public MemberRepository(EntityManager em){
//        this.em=em;
//    }

    public void save(Member member){
        em.persist(member);
    }

    public Member findOne(Long id){
        return em.find(Member.class,id);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m", Member.class).getResultList();
    }

    public List<Member> findByName(String name){
        return em.createQuery("select m from Member m where m.name=:name",Member.class)
                .setParameter("name",name)
                .getResultList();
    }
}
