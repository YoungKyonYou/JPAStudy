package jpabook.jpashop.domain;

import lombok.Data;
import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {
    private String city;
    private String street;
    private String zipcode;

    //프록시나 리프렉션 같은 기능을 쓰기 위해서 내부적으로 필요
    //이것을 상속할 건 아니라서 protected으로 하고 이건 그냥 JPA SPEC상에 사용하는 것이다 함부로 new로 안 만든다는 것을 명시
    protected Address() {

    }

    //생성자 모든 값을 초기화할 수 있게 한다.
    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
