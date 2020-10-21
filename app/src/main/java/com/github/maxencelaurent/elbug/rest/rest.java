
package com.github.maxencelaurent.elbug.rest;

import com.github.maxencelaurent.elbug.Item;
import com.github.maxencelaurent.elbug.Container;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author maxence
 */
@Stateless
@Path("R/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class rest {

    @PersistenceContext(unitName = "MyPU")
    private EntityManager em;

    @POST
    @Path("/Container")
    public Container createContainer(Container resource) {
        em.persist(resource);
        return resource;
    }

    @GET
    @Path("/Container/{id: [1-9][0-9]*}")
    public Container getContainer(@PathParam("id") Long id) {
        return em.find(Container.class, id);
    }

    @GET
    @Path("/Item/{id: [1-9][0-9]*}")
    public Item getItem(@PathParam("id") Long id) {
        return em.find(Item.class, id);
    }

    @POST
    @Path("Container/{id: [1-9][0-9]*}/Item/")
    public void createItem(@PathParam("id") Long id) {
        final Item item = new Item();
        Container resource = this.getContainer(id);

        resource.addItem(item);
    }

    @PUT
    @Path("MoveItem/{itemId : [1-9][0-9]*}/{index : [0-9]*}")
    public Container moveItem(@PathParam("itemId") Long itemId, @PathParam("index") Integer index) {
        Item item = this.getItem(itemId);
        Container resource = this.getContainer(item.getContainer().getId());
        resource.moveItem(item, index);
        return resource;
    }

    @DELETE
    @Path("Item/{itemId : [1-9][0-9]*}")
    public Container removeItem(@PathParam("itemId") Long itemId) {
        Item item = this.getItem(itemId);

        Container resource = item.getContainer();

        resource.removeItem(item);

        em.remove(item);

        return resource;
    }
}
