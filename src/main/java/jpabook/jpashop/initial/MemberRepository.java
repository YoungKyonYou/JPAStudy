//package jpabook.jpashop.initial;
//
//import lombok.Data;
//import org.springframework.stereotype.Repository;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//
//@Repository
//@Data
//public class MemberRepository {
//    @PersistenceContext
//    private EntityManager em;
//
//    public Long save(Member member){
//        em.persist(member);
//        //Command와 Query를 분리하기 위해서 Member 객체 대신 member의 id를 반환한다.
//        //저장을 하고 나면 가급적이면 이건 side-effect를 일으키는 command 성이 있어서 아이디 정도만 조회하는 걸로 설계한다.
//        return member.getId();
//    }
//
//    public Member find(Long id){
//        return em.find(Member.class, id);
//    }
//
//}
