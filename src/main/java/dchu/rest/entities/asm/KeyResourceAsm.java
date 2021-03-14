package dchu.rest.entities.asm;

import dchu.core.entities.Key;
import dchu.rest.controllers.KeyController;
import dchu.rest.entities.KeyResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import org.springframework.hateoas.RepresentationModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Created by Jiri on 7. 7. 2014.
 */
@Component
public class KeyResourceAsm extends RepresentationModelAssemblerSupport<Key, KeyResource> {

    public KeyResourceAsm() {
        super(KeyController.class, KeyResource.class);
    }

    public KeyResource toModel(Key entity) {
        KeyResource resource = new KeyResource();
        resource.setPublicKey(entity.getPublicKey());
        resource.setPrivateKey(entity.getPrivateKey());
        Link link = linkTo(methodOn(KeyController.class).getKey(entity.getPublicKey())).withSelfRel();
        resource.add(link);
        return resource;
    }
}
