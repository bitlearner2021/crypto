package dchu.rest.controllers;

import dchu.core.entities.Key;
import dchu.core.entities.P2shAddress;
import dchu.core.services.KeysRepository;
import dchu.core.services.P2shAddressesRepository;
import dchu.core.services.TransactionsRepository;
import dchu.rest.entities.P2shAddressResource;
import dchu.rest.entities.asm.P2shAddressResourceAsm;
import dchu.rest.exceptions.NoKeysProvidedException;
import dchu.rest.exceptions.NotFoundException;
import dchu.rest.exceptions.ToManyKeysException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.Link;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * Created by Jiri on 7. 7. 2014.
 */
@Controller
@RequestMapping("/rest/addresses")
public class P2shAddressController {
    private static DateTimeFormatter DATEFORMMATER = DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.US)
            .withResolverStyle(ResolverStyle.STRICT);

    public static final Long DEFAULT_DUE_DATE_PERIOD = 5L;  // default to 5 days to expire

    P2shAddressesRepository p2shAddressesRepository;
    KeysRepository keysRepository;
    TransactionsRepository transactionsRepository;

    @Autowired
    public P2shAddressController(P2shAddressesRepository p2shAddressesRepository, KeysRepository keysRepository, TransactionsRepository transactionsRepository) {
        this.p2shAddressesRepository = p2shAddressesRepository;
        this.keysRepository = keysRepository;
        this.transactionsRepository = transactionsRepository;

    }

    @RequestMapping(value = "/{addressId}", method = RequestMethod.GET)
    public
    @ResponseBody
    P2shAddressResource getAddress(@PathVariable String addressId) {
        P2shAddress address = p2shAddressesRepository.findByAddress(addressId);
        if (address != null) {
            return new P2shAddressResourceAsm().toModel(address);
        }
        throw new NotFoundException();
    }

    public boolean isValidDate(String dateStr) throws Exception {
        try {
            DATEFORMMATER.parse(dateStr);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<P2shAddressResource> createNewAddress(@RequestBody P2shAddressResource newAddress) {
        if (newAddress.getKeys() == null) {
            throw new NoKeysProvidedException();
        }
        if (newAddress.getKeys().size() >= newAddress.getTotalKeys()) {
            throw new ToManyKeysException();
        }
        for (int i = newAddress.getKeys().size(); i < newAddress.getTotalKeys(); i++) {
            Key key = keysRepository.generateNewKey();
            key = keysRepository.save(key);
            newAddress.getKeys().add(key.getPublicKey());
        }
        P2shAddress address = p2shAddressesRepository.createNew(newAddress.getKeys(), newAddress.getRequiredKeys());
        address.setInvoiceBalance(newAddress.getInvoiceBalance());
        address.setInvoiceStatus(P2shAddress.INVOICE_STATUS.UNPAID);
        if (newAddress.getDueDate() == null) {
            address.setDueDate(Date.from(LocalDate.now().plusDays(DEFAULT_DUE_DATE_PERIOD).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            address.setDueDate(newAddress.getDueDate());
        }
        address = p2shAddressesRepository.save(address);
        P2shAddressResource p2shAddressResource = new P2shAddressResourceAsm().toModel(address);
        HttpHeaders headers = new HttpHeaders();
        Optional<Link> selfLink = p2shAddressResource.getLink("self");
        if (selfLink.isPresent()) {
            headers.setLocation(selfLink.get().toUri());
        }
        return new ResponseEntity<>(p2shAddressResource, headers, HttpStatus.CREATED);
    }
}

