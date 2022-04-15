package jpabook.jpashop.domain.item;

import lombok.Data;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
@Data
@DiscriminatorValue("A")
@Entity
public class Album extends Item{
    private String artist;
    private String etc;
}
