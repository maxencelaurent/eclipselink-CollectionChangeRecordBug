package com.github.maxencelaurent.elbug;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;

/**
 *
 * @author maxence
 */
@Entity
@Access(AccessType.FIELD)
public class Container implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "container", cascade = {CascadeType.ALL})
    @JsonManagedReference
    @OrderColumn
    private List<Item> items = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        for (Item assignment : items) {
            assignment.setContainer(this);
        }
        this.items = items;
    }

    public void moveItem(Item item, final int index) {
        this.removeItem(item);
        this.addItem(item, index);

        List<Item> newItems = new ArrayList<>();

        for (Item a : this.getItems()) {
            newItems.add(a);
        }

        this.setItems(newItems);
    }

    public void addItem(Item assignment) {
        items.add(assignment);
        assignment.setContainer(this);
    }

    public void addItem(Item item, final int index) {
        items.add(index, item);
        item.setContainer(this);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }
}
