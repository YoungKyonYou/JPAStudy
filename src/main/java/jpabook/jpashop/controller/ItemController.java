package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/items/new")
    public String createForm(Model model){
        model.addAttribute("form",new BookForm());
        return "items/createItemForm";
    }
    @PostMapping("/items/new")
    public String create(BookForm form){
        //여기서는 setter를 했지만 사실 생성자로 초기화하는 것이 가장 좋다.
        //BookForm 클래스에서 createBook static 메서드를 만들어서 하는 것도 좋다. 그렇게 setter를 날려주는 것이 좋다.
        //실무에서 김영한님께서는 setter 다 날린다.
        Book book=new Book();
        book.setName(form.getName());
        book.setPrice(form.getPrice());
        book.setStockQuantity(form.getStockQuantity());
        book.setAuthor(form.getAuthor());
        book.setIsbn(form.getIsbn());

        itemService.saveItem(book);
        return "redirect:/";


    }

    @GetMapping("/items")
    public String list(Model model){
        List<Item> items=itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    @GetMapping("/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model){
        Book item =(Book)itemService.findOne(itemId);

        BookForm form=new BookForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setAuthor(item.getAuthor());
        form.setIsbn(item.getIsbn());

        model.addAttribute("form",form);
        return "items/updateItemForm";
    }

    @PostMapping("/items/{itemId}/edit")
    //@ModelAttribute는 화면 단에 form 값들이 BookForm form에 넘어온다.
    public String updateItem(@PathVariable("itemId") Long itemId, @ModelAttribute("form") BookForm form) {
        //이건 준영속성 엔티티 이기 때문에 아무리 set으로 값을 바꿔도 dirty checking이 일어나지 않는다.
        //준영속성 엔티티는 JPA가 관리하지 않기 때문이다.
        //준영속 엔티티를 수정하는 2가지 방법은 변경 감지 기능(dirty checking)을 사용하거나 병합(merge)를 사용하는 것이다.
        //ItemService의 updateItem 메서드가 변경감지기능으로 하는 법이다. 보통 이렇게 사용한다.이렇게 해도 되고
        // 한가지 또다른 방법은 병합이다.

//        Book book=new Book();
//        book.setId(form.getId());
//        book.setName(form.getName());
//        book.setPrice(form.getPrice());
//        book.setStockQuantity(form.getStockQuantity());
//        book.setAuthor(form.getAuthor());
//        book.setIsbn(form.getIsbn());

        //위 방법보다는 아래 방법이 낫다
        itemService.updateItem(itemId,form.getName(),form.getPrice(),form.getStockQuantity());
        return "redirect:/items";
    }


}
