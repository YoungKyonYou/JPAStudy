package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        //이런 식으로 하면 쓸데없는 정보도 같이 제공된다. 순수한 회원정보만 API로 뿌리고 싶은데 order 정보까지 다 간다.

        return memberService.findMembers();
    }

    @PostMapping("/api/v1/members")
    //@Valid는 javax 관련된 validation 기능을 쓸 수 있게 해준다.
    //API를 만들때는 엔티티를 이렇게 파라미터로 받으면 안된다!!! 이유 Member class 주석 참고
    //실무에서는 절대로 엔티티를 외부로 노출시키거나 엔티티를 파라미터로 그대로 받아선 안 된다.
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member){
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request){
        Member member=new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //수정도 별도의 class를 만들어서 넣는 게 좋다.
    @PutMapping("/api/v2/members/{id}")
    public UpdateMemberResponse updateMemberV2(@PathVariable("id") Long id,@RequestBody @Valid UpdateMemberRequest request ){
        //command와 query의 분리가 유지보수에 좋다
        memberService.update(id, request.getName());//command - 이건 update하라는 커맨드
        Member findMember = memberService.findOne(id);//쿼리 - 정상적으로 잘 됐는지 response를 하기 위한 쿼리
        return new UpdateMemberResponse(findMember.getId(),findMember.getName());
    }

    @Data
    static class UpdateMemberRequest{
        private String name;
    }
    @Data
    @AllArgsConstructor
    static class UpdateMemberResponse{
        private Long id;
        private String name;
    }


    @Data
    static class CreateMemberRequest{
        private String name;
    }

    @Data
    static class CreateMemberResponse{
        private Long id;
        public CreateMemberResponse(Long id){
            this.id=id;
        }
    }
}
