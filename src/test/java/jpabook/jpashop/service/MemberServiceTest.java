package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;

//아래 두개 어노테이션을 통해서 spring이랑 엮어서 동작하게 함
//JUnit4에서 SpringBootTest 어노테이션과 RunWith를 같이 사용해줘야 SpringBootTest 어노테이션이 ignore이 안된다.
@RunWith(SpringRunner.class)
@SpringBootTest
//rollback할 수 있게 함
@Transactional
public class MemberServiceTest {
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EntityManager em;

    //given: 이렇게 주어졌을때,
    //when: 이렇게 하면
    //then: 이렇게 된다는 의미, 결과가 이게 나와야 된다는 것것
    @Test
//    //rollback안하고 commit를 해버림
//    @Rollback(false)
    public void 회원가입() throws Exception{
        //given
        Member member=new Member();
        member.setName("kim");

        //when
        //memberService.join(member)까지만 적고 Ctrl+Alt+V 단축키 해보기
        Long saveId = memberService.join(member);

        //then
        assertEquals(member, memberRepository.findOne(saveId));
    }
    //이것을 넣어주면 아래에 주석처럼 try catch를 안써도 됨
    @Test(expected = IllegalStateException.class)
    public void 중복_회원_예외() throws Exception {
        //given
        Member member1=new Member();
        member1.setName("kim");

        Member member2=new Member();
        member2.setName("kim");

        //when
        memberService.join(member1);
        memberService.join(member2);//예외가 발생해야 한다.!
//        try{
//            memberService.join(member2);//예외가 발생해야 한다.!
//        }catch(IllegalStateException e){
//            return;
//        }


        //then

        //fail은 assert에서 제공하는 함수인데 여기까지 오면 안된다는 것을 의미미
       fail("예외가 발생해야 한다.");

    }

}