package jpabook.jpashop.api;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {
    private final MemberService memberService;

    @GetMapping("/api/v1/members")
    public List<Member> membersV1(){
        //이런 식으로 하면 쓸데없는 정보도 같이 제공된다. 순수한 회원정보만 API로 뿌리고 싶은데 order 정보까지 다 간다.
        //엔티티를 직접적으로 노출하지 말것!
        return memberService.findMembers();
    }

    @GetMapping("/api/v2/members")
    public Result memberV2(){
        List<Member> findMembers=memberService.findMembers();
        List<MemberDto> collect = findMembers.stream().map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());
        /*이렇게 한번 감싸줘야 한다. 그래야 껍데기 Object가 생기고 그 안에 data 배열이 생기게 된다.
        List를 바로 컬렉션으로 바로 내면 json 배열 타입으로 나가버리기 때문에 유연성이 떨어진다.
        언젠가는 요구사항이 계속 바뀔텐데 배열가지고는 쉽게 되지 않는다 그래서 이렇게 Result라는 껍데기로 한번 감싼다.*/
        return new Result(collect.size(),collect);

    }

    @Data
    @AllArgsConstructor
    static class Result<T>{
        //이런 식으로 필드를 추가할 수 있다.
        private int count;
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto{
        private String name;
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
