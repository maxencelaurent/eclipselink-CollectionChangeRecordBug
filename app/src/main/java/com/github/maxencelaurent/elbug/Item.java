
package com.github.maxencelaurent.elbug;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author maxence
 */
@Entity
public class Item implements Serializable {

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Item other = (Item) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @JsonBackReference
    @JsonIgnore
    private Container container;

    public Long getId() {
        return this.id;
    }

    @JsonIgnore
    @JsonBackReference
    public Container getContainer() {
        return container;
    }

    @JsonBackReference
    public void setContainer(Container container) {
        this.container = container;
    }

    @Override
    public String toString() {
        return id != null ? id.toString() : "na";
    }
}
