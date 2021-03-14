package dchu.rest.entities.asm;

import dchu.core.entities.Key;
import dchu.core.entities.P2shAddress;
import dchu.rest.controllers.KeyController;
import dchu.rest.controllers.P2shAddressController;
import dchu.rest.entities.P2shAddressResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Jiri on 7. 7. 2014.
 */
@Component
public class P2shAddressResourceAsm extends RepresentationModelAssemblerSupport<P2shAddress, P2shAddressResource> {

    public P2shAddressResourceAsm() {
        super(KeyController.class, P2shAddressResource.class);
    }

    public P2shAddressResource toModel(P2shAddress entity) {
        P2shAddressResource resource = new P2shAddressResource();
        resource.setAddress(entity.getAddress());
        resource.setRedeemScript(entity.getRedeemScript());
        resource.setInvoiceBalance(entity.getInvoiceBalance());
        resource.setInvoiceStatus(entity.getInvoiceStatus());
        resource.setDueDate(entity.getDueDate());
        Link link = linkTo(methodOn(P2shAddressController.class).getAddress(entity.getAddress())).withSelfRel();
        resource.add(link);

        List<String> keys = new ArrayList<>();
        for (Key key : entity.getKeys()) {
            keys.add(key.getPublicKey());
            //todo: add key links
        }
        resource.setKeys(keys);
        return resource;
    }
}
