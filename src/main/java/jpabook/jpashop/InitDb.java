package jpabook.jpashop;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;


/**
 * 총 주문 2개
 * userA
 *  * JPA1 BOOK
 *  * JPA2 BOOK
 * userB
 *  * SPRING1 BOOK
 *  * SPRING2 BOOK
 */

//스프링의 component 스캔 대상이 된다.
@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;

    //의존성 주입 후 자동 실행되는 메서드
    //그냥 이 메서드 init 안에 dbInit1 내용을 다 넣으면 될 것 같지만
    //그것이 잘 안 된다. Spring lifecycle이 있어서 PostConsturct에 @Transacional 하는 것이 잘 안된다.
    //그래서 아래와 같이 별도의 빈으로 등록해야 한다.
    @PostConstruct
    public void init(){
        initService.dbInit1();
        initService.dbInit2();
    }
    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService{
        private final EntityManager em;
        public void dbInit1(){
            Member member = createMember("userA","서울","1","1111");
            em.persist(member);

            Book book1 = createBook("JPA1 BOOK",10000,100);
            em.persist(book1);

            Book book2 = createBook("JPA2 BOOK",20000,100);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private Delivery createDelivery(Member member) {
            Delivery delivery=new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private Book createBook(String bookTitle, int price, int stockQuantity) {
            Book book1=new Book();
            book1.setName(bookTitle);
            book1.setPrice(price);
            book1.setStockQuantity(stockQuantity);
            return book1;
        }

        private Member createMember(String user, String city, String street, String zipcode) {
            Member member=new Member();
            member.setName(user);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }

        public void dbInit2(){
            Member member=createMember("userB", "진주", "2", "2222");
            em.persist(member);

            Book book1 = createBook("SPRING1 BOOK",20000,200);
            em.persist(book1);

            Book book2 = createBook("SPRING2 BOOK",40000,300);
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

            Delivery delivery = createDelivery(member);
            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }
    }
}

