//package jpabook.jpashop;
//
//import jpabook.jpashop.initial.Member;
//import jpabook.jpashop.initial.MemberRepository;
//import org.assertj.core.api.Assertions;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.annotation.Rollback;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.transaction.annotation.Transactional;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class MemberRepositoryTest{
//    @Autowired
//    private MemberRepository memberRepository;
//
//    //Transactional 어노테이션은 Test에 있으면 테스트 끝내고 Rollback를 해버린다. 그래서 Rollback 어노테이션을 설정해주면
//    //롤백을 안 한다.
//    @Transactional
//    @Rollback(false)
//    @Test
//    public void testMember() throws Exception{
//        Member member=new Member();
//        member.setUsername("memberA");
//        //memberRepository.save(member)까지만 적고
//        //단축키로 ctrl+alt+v를 누른다
//        Long saveId = memberRepository.save(member);
//
//        Member findMember = memberRepository.find(saveId);
//
//        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
//        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
//        //이건 같다 왜냐면 findMember와 member는 영속성 컨텍스트가 똑같기 때문이다.
//        Assertions.assertThat(findMember).isEqualTo(member);
//    }
//}