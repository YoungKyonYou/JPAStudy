package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/members/new")
    public String createForm(Model model){
        model.addAttribute("memberForm",new MemberForm());
        return "members/createMemberForm";
    }

    @PostMapping("/members/new")
    //Valid 어노테이션은 javax를 쓴다고 인지를 해서 MemberForm 클래스에 NotEmpty 어노테이션을 인식한다.
    /*
    여기서 의문이 들 수 있다 MemberForm form를 파라미터로 두지 말고 그냥 Member 엔티티 타입을 두면 되지 않을까? 할 수 있다.
    왜 form를 굳이 넣냐면 Member 엔티티 클래스를 보면 id, name, address 등 필드들이 있다. 즉 city, street, zipcode 등이 있는
    MemberForm과는 잘 안맞는다. 그리고 그렇게 되면 Member 엔티티에 @NotEmpty를 써줘야 하는데 너무 지저분해진다. 차라리 깔끔하게 화면에 feat한
    홈 데이터를 만들고 받는 것이 맞다. 심플하면 엔티티를 받아도 되나 실무에서는 그런 단순한 게 거의 없다. 실무에선 힘들다.
     */
    public String create(@Valid MemberForm form, BindingResult result){

        if(result.hasErrors()){
            //이런 식으로 하면 BindingResult를 아래 url에서 끌고와서 쓸 수 있게 도와준다.
            //그리고 이름을 입력 안하면 에러가 나는데 만약 다른 건 적고 이름만 안적고 에러가 났다면
            //나머지 데이터들이 유지가 된다.

            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member=new Member();
        member.setName(form.getName());
        member.setAddress(address);

        memberService.join(member);
        return "redirect:/";
    }

    @GetMapping("/members")
    public String list(Model model){
        List<Member> members = memberService.findMembers();
        //화면에 뿌리는데 엔티티를 손 댈것이 없고 바로 출력할 수 있으니까 form 대신 member를 반환했다
        //template engine에서 어쩌피 서버 안에서 기능이 돌기 때문에 화면 템플릿에 이렇게 엔티티를 전달해줘도 되나
        //API를 만들때는 이유를 불문하고 절대!!!! 엔티티를 넘기면 안된다.
        //API는 스펙인데 member 엔티티를 반환하게 되면 만약 member 엔티티에 필드 하나를 추가하게 되면
        //두가지 문제가 생긴다. 예를 들어 userpassword가 member 엔티티에 추가된다면 password가 그대로 노출된다는 문제가 있고
        //또 API 스펙이 변해 버린다. 그럼 불완전한 API가 되버린다. 하지만 템플릿 엔진에서(thymeleaf)에는 엔티티를 넘겨줘도 괜찮다.
        //제일 깔끔한 것은 dto로 변환해서 반환하는 것이 가장 깔끔하다.
        model.addAttribute("members",members);
        return "members/memberList";
    }
}
