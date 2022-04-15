package jpabook.jpashop.domain.item;

import lombok.Data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Data
@DiscriminatorValue("M")
@Entity
public class Movie extends Item{
    private String director;
    private String actor;
}
