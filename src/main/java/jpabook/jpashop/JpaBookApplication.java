package jpabook.jpashop;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpaBookApplication {
    public static void main(String[] args) {
        SpringApplication.run(JpaBookApplication.class, args);
    }
    @Bean
    Hibernate5Module hibernate5Module(){
        /*
        이렇게 하면 안 된다. 이것은 강제로 LAZY LOADING를 하게 되는데 이건 API스펙이 조금 바뀌면 큰일 나기 때문에
        쓰면 안된다. 엔티티를 그대로 노출하면 안된다! 그리고 성능상에 문제가 된다. 사용하지 않는 데이터까지 같이 노출되어 버린다.
       우리가 애초에 ordersV1 API에서 얻고자 하는 정보는 order 정보, Delivery 정보, Member 정보인데 이걸 쓰면
       Category, OrderItem 정보까지 다 긁어오게 된다. FORCE_LAZY_LOADING이기 때문에 LAZY로 설정된 엔티티들의 다 긁어서 가져오는
       것이다. 쿼리가 불필요하게 많이 나가게 되서 성능에 문제가 생긴다. 그러니 이런 FORCE_LAZY_LOADING은 쓰면 안된다.
         */
//        Hibernate5Module hibernate5Module=new Hibernate5Module();
//        hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING,true);
//        return hibernate5Module;
        return new Hibernate5Module();
    }
}
