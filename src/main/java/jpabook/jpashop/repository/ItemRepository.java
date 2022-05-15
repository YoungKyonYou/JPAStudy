package jpabook.jpashop.repository;

import jpabook.jpashop.domain.item.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepository {
    private final EntityManager em;

    public void save(Item item){
        if(item.getId()==null){
            em.persist(item);
        }else{
            //merge는 실무에서 쓸 일이 거의 없다.
            //merge는 변경기능감지 메서드와 똑같다. ItemService의 updateItem메서드.
            //item를 찾고 merge(item)의 item를 찾고 다 바꿔치기 하는 것이다. transaction commit될 때 다 반영되는 것이다.
            //그런데 차이가 있다. 문서 확인하기!!
            //병합은 조심해야 한다. 변경 감지 기능을 사용하면 원하는 속성만 선택해서 변경할 수 있지만
            //병합을 사용하면 모든 속성이 변경된다. 이건 실무에서 위험하다 왜냐면 병합시 값이 없으면 null이면 업데이트할 위험이 있다.
            em.merge(item);
        }
    }

    public Item findOne(Long id){
        return em.find(Item.class, id);
    }

    public List<Item> findAll(){
        return em.createQuery("select i from Item i", Item.class).getResultList();
    }
}
