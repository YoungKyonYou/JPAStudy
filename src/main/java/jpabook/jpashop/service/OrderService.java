package jpabook.jpashop.service;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.repository.ItemRepository;
import jpabook.jpashop.domain.repository.MemberRepository;
import jpabook.jpashop.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly=true)
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    /**
     * 주문
     */
    @Transactional
    public Long order(Long memberId, Long itemId, int count){
        Member member = memberRepository.findOne(memberId);
        Item item = itemRepository.findOne(itemId);

        //배송정보 생성
        Delivery delivery=new Delivery();
        delivery.setAddress(member.getAddress());

        //주문상품 생성
        /*
        Service 단에서 이렇게 하는 게 아니라 OrderItem orderItem=new ~
        orderItem.setCount, orderItem.setPrice해서 만드는 경우가 있는데
        나중에 유지보수하기도 어렵고 일관성이 없다. 예를 들어 생성로직에 필드를 추가한다거나 변경한다거나
        로직을 더 넣는 다거나 하면 복잡해지기 때문에 이런 식의 생성은 막아야 한다. 이거 외의 다른 스타일의 생성은 막는다.
        어떻게 막냐면 OrderItem에 Constructor를 Protected로 설정한다.
         */
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        //주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        //주문 저장
        //지금 보면 orderRepository.save를 이거 하나만 했다.
        //원래 위에 Delivery 따로 생성하고(persist) OrderItem도 따로 생성하고(persist) 즉,
        //원래 DeliveryRepository가 따로 있게 해서 deliveryRepository.save(~) 하고 넣어주고
        // OrderRepository.save(~) 해서 따로 넣어주고 난 다음에 orderRepository.save를 해야 한다.
        //근데 지금은 이렇게 orderRepository만 했는데 이게 왜 그러냐면 Order 클래스에서 cascade=CascadeType.ALL 세팅을 해줬었다.
        //Order 클래스 가보면 @OneToMany로 orderItems 같은 경우 CascadeType.ALL로 되어 있고
        //Delivery 같은 경우 @OneToOne로 CascacadeType.ALL로 해줬다.
        //이 뜻이 order를 persist하면 orderItems와 delivery를 다 persist해주는 것이다.
        //그래서 이렇게 하나만 저장을 해줘도 orderItems와 delivery가 자동으로 persist를 해주는 것이다.
        /*
        주의사항: 어디까지 Cascade를 설정해줘야 하는가?
        쓰면 도움을 받을 수 있으나, 예를 들어 Delivery가 매우 중요하다고 해보자. 다른 곳에서도 Delivery를 참조하고 가져다 쓴다면
        이렇게 Cascade를 막 쓰면 안 된다. 왜냐면 잘못하면 지울 때 막 다 지워지고 persist도 막 다른 것이 걸려 있으면 복잡하게 돌아간다.
        만약 orderItem이 매우 중요하다 다른 곳에서 쓴다? 그럼 cascade를 설정 안 하는 게 좋다 하지만 이번 케이스에서는 딱 Order만
        Delivery와 OrderItem를 사용하고 Persist해야 하는 lifecycle이 똑같기 때문에 쓴 것이다.
         */
        orderRepository.save(order);
        return order.getId();
    }
    /**
     * 주문취소
     */
    @Transactional
    public void cancelOrder(Long orderId){
        //주문 엔티티 조회
        Order order = orderRepository.findOne(orderId);
        //주문 취소
        /*
        JPA를 활용하면 이렇게 Order.java에 cancel()메서드에 있는 것처럼 데이터만 바꾸면, Entity 안에 있는 데이터만 바꿔주면
        JPA가 알아서 변경된 포인트들을 dirty checking으로 인해 변경 내역 감지를 통해 데이터베이스에 업데이트 쿼리가 날라간다.
        지금 바꾼 데이터가 Order.java에 setStatus(OrderStatus.CANCEL)로 바꿨으니 Order에 Update가 일어날 것이고
        그리고 OrderItem.java에 바꾼 건 없지만 getItem().addStock(count)를 보니 addStock() 메서드 안에서
        this.stockQuantity+=quantity에서 stockQuantity가 바뀌었으니 Item도 쿼리가 날라가서 stockQuantity가 Update될 것이다.
        이게 JPA를 사용하는 엄청 큰 장점이다.
          */
        order.cancel();
    }

    //검색
/*    public List<Order> findOrders(OrderSearch orderSearch){
        return orderRepository.findAll(orderSearch);
    }*/
}
