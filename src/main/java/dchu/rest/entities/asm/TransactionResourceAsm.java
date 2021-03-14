package dchu.rest.entities.asm;

import dchu.core.entities.Transaction;
import dchu.rest.controllers.TransactionController;
import dchu.rest.entities.TransactionResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Jiri on 9. 7. 2014.
 */
public class TransactionResourceAsm extends RepresentationModelAssemblerSupport<Transaction,TransactionResource> {
    public TransactionResourceAsm() {
        super(TransactionController.class, TransactionResource.class);
    }

    public TransactionResource toModel(Transaction entity) {
        TransactionResource resource = new TransactionResource();
        resource.setAmount(entity.getAmount());
        resource.setTargetAddress(entity.getTargetAddress());
        resource.setSourceAddress(entity.getSourceAddress().getAddress());
        resource.setRawTransaction(entity.getRawTransaction());
        Link link = linkTo(methodOn(TransactionController.class).getTransaction(entity.getId())).withSelfRel();
        //todo: add links to other entities in transaction
        resource.add(link);
        return resource;
    }
}
