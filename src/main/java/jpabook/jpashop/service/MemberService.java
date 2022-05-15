package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
//spring이 제공하는 transactional 어노테이션을 사용하는 것이 좋다.
//이렇게 설정하면 jpa가 조회하는 것에서는 최적화한다.
//db에 따라서는 읽기 전용 트랜잭션이라고 알려주고 리소스를 적게 쓰고 읽기만해서 최적화가 좋다.
//읽기에는 가급적 이 옵션을 넣어주면 좋다. 수정 작업에 넣으면 수정작업이 제대로 안 된다.
//함수 위에다가 쓸수도 있고 이렇게 전체 적용하게 쓸수도 있다.
@Transactional(readOnly=true)
////생성자로 주입하는 것보다 이걸 쓸스도 있다.
//@AllArgsConstructor
//@AllArgsConstructor보다 좋은건 @RequiredArgsConstructor이다.
//이건 final인 것만 생성자를 만들어 준다.
//이 스타일의 injection를 선호한다.
@RequiredArgsConstructor
//바로 아래 MemberService에커서를 대고 Ctrl+Shift+T 를 누르면 TEST를 바로 만들 수 있음
public class MemberService {
    //변경할 일이 없기 때문에 final을 권장함.
    //final를 해놓으면 생성자에서의 실수를 방지 가능, 즉 컴파일 시점에서 체크를 해줄 수 있다.
    private final MemberRepository memberRepository;

    //@Autowired는 단점이 있다. 왜냐면 못 바꾼다. 즉 엑세스할 수 있는 방법이 없다
    //방법은 setter injection를 쓸수도 있다. 하지만 setter injection의 장점은 매개변수로 주입이 가능하나
    //단점은 치명적이다. 이게 한번 뭔가 runtime application 중에 누군가가 바꿀수가 있게 된다.
    //실제로 애플리케이션이 잘 작동하고 있는데 바꿀 일이 거의 없다. 그래서 이건 권장하지 않고 오히려
    //이렇게 생성자 injection를 선호하게 된다. 테스트할 때 그리고 명확하다는 장점이 있음 매개변수를 넘겨줘야하니 어디에 의존하고 있는지 보임
    /*public MemberService(MemberRepository memberRepository){
        this.memberRepository=memberRepository;
    }*/

    //spring이 제공하는 transactional 어노테이션을 사용하는 것이 좋다.
    @Transactional
    //회원 가입
    public Long join(Member member){
        validateDuplicateMember(member); //중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

//    //spring이 제공하는 transactional 어노테이션을 사용하는 것이 좋다.
//    //이렇게 설정하면 jpa가 조회하는 것에서는 최적화한다.
//    //db에 따라서는 읽기 전용 트랜잭션이라고 알려주고 리소스를 적게 쓰고 읽기만해서 최적화가 좋다.
//    @Transactional(readOnly=true)
    //회원 전체 조회
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }
//    //읽기에는 가급적 이 옵션을 넣어주면 좋다. 수정 작업에 넣으면 수정작업이 제대로 안 된다.
//    @Transactional(readOnly=true)
    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id);
        member.setName(name);

    }
}
