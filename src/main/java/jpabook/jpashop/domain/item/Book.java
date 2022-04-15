package jpabook.jpashop.domain.item;

import lombok.Data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
@Data
@DiscriminatorValue("B")
@Entity
public class Book extends Item{
    private String author;
    private String isbn;

}
