package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Member {
    @Id @GeneratedValue
    @Column(name="member_id")
    private Long id;

    /*javax NotEmpty를 넣으면 무조건 값이 있어야 된다. api/MemberApiController에서 @Valid를 함으로써 사용됨
    하지만 이 NotEmpty를 빼는 것이 좋다. 어떤 곳에선 필요하고 안 필요할 수가 있기 때문이다.
    만약 name에서 username으로 바뀌게 되면 API 스펙이 username으로 바뀌게 된다. 예를 들어
    어느 팀에서 name를 username으로 바꾸게 된다면 클라이언트 입장에서 api를 호출을 햇는데 어느 날 작동이 안되는 것이다.
    왜냐면 member api를 만들어준 팀에서 name에서 username으로 수정해서 안 동작하게 되는 것이다. 즉 엔티티를 손대서
    발생하는 문제인 것이다. 즉 별도로 만드는 것이 좋다.*/
   // @NotEmpty
    private String name;

//    내장 타입임을 선언
    @Embedded
    private Address address;

    /*api를 만들 때 이 Member를 그대로 넘겨줄때 이 orders가 빠진다. 하지만 과연 이게 맞나?
    엔티티를 직접적으로 넘겨주지 말아야 한다! 이런 어노테이션은 나중에 실무에서 답이 안나온다.*/
    //@JsonIgnore
    @OneToMany(mappedBy="member")
    private List<Order> orders=new ArrayList<>();


}
