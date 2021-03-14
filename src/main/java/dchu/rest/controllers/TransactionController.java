package dchu.rest.controllers;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.TransactionOutput;
import dchu.bitcoin.service.impl.BitcoinJMagicService;
import dchu.core.entities.Key;
import dchu.core.entities.P2shAddress;
import dchu.core.entities.Transaction;
import dchu.core.services.P2shAddressesRepository;
import dchu.core.services.TransactionsRepository;
import dchu.rest.entities.TransactionResource;
import dchu.rest.entities.asm.TransactionResourceAsm;
import dchu.rest.exceptions.AddressNotFoundException;
import dchu.rest.exceptions.NotFoundException;
import dchu.rest.exceptions.SomehtingGotWrongException;
import org.bitcoinj.core.Coin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Jiri on 9. 7. 2014.
 */
@Controller
@RequestMapping("/rest/transactions")
public class TransactionController {
    TransactionsRepository transactionsRepository;
    BitcoinJMagicService bitcoinJMagicService;
    P2shAddressesRepository p2shAddressesRepository;

    @Autowired
    public TransactionController(TransactionsRepository transactionsRepository, BitcoinJMagicService bitcoinJMagicService, P2shAddressesRepository p2shAddressesRepository) {
        this.transactionsRepository = transactionsRepository;
        this.bitcoinJMagicService = bitcoinJMagicService;
        this.p2shAddressesRepository = p2shAddressesRepository;
    }

    @RequestMapping(value = "/{transactionId}", method = RequestMethod.GET)
    public
    @ResponseBody
    TransactionResource getTransaction(@PathVariable Long transactionId) {
        Transaction transaction = transactionsRepository.getOne(transactionId);
        if (transaction == null) {
            throw new NotFoundException();
        }
        return new TransactionResourceAsm().toModel(transaction);
    }

    @RequestMapping(value = "", method = RequestMethod.POST)
    public
    @ResponseBody
    ResponseEntity<TransactionResource> createTransaction(@RequestBody TransactionResource transactionResource) {
        P2shAddress address = p2shAddressesRepository.findByAddress(transactionResource.getSourceAddress());
        if (address == null) {
            throw new AddressNotFoundException();
        }
        Transaction transaction = p2shAddressesRepository.createNewTransaction(address, transactionResource.getTargetAddress(), transactionResource.getAmount());
        transaction = transactionsRepository.save(transaction);
        TransactionResource resource = new TransactionResourceAsm().toModel(transaction);
        updateAddress(transaction, transactionResource.getTargetAddress());
        HttpHeaders headers = new HttpHeaders();
        Optional<Link> selfLink = resource.getLink("self");
        if (selfLink.isPresent()) {
            headers.setLocation(selfLink.get().toUri());
        }
        return new ResponseEntity<>(resource, headers, HttpStatus.CREATED);
    }

    private void updateAddress(Transaction transaction, String toAddress) {

        P2shAddress byAddress = p2shAddressesRepository.findByAddress(toAddress);
        Long invBalance = byAddress.getInvoiceBalance();
        byAddress.setInvoiceBalance(invBalance - transaction.getAmount().longValue());
        if (byAddress.getInvoiceBalance() <= 0L) {
            byAddress.setInvoiceStatus(P2shAddress.INVOICE_STATUS.PAID);
        } else {
            byAddress.setInvoiceStatus(P2shAddress.INVOICE_STATUS.PARTIALLY_PAID);
        }
        Long now = System.currentTimeMillis();
        // check for past due
        if (now > byAddress.getDueDate().getTime()) {
            byAddress.setInvoiceStatus(P2shAddress.INVOICE_STATUS.EXPIRED);
        }
        p2shAddressesRepository.save(byAddress);
    }

    @RequestMapping(value = "/{transactionId}", method = RequestMethod.POST)
    public
    @ResponseBody
    TransactionResource signTransaction(@RequestBody TransactionResource transactionResource, @PathVariable Long transactionId) {
        Transaction transaction = transactionsRepository.getOne(transactionId);
        if (transaction == null) {
            throw new NotFoundException();
        }
        //todo: verify transaction
        List<Key> keys = transaction.getSourceAddress().getKeys();
        for (Key key : keys) {
            if (key.getPrivateKey() != null) {
                transaction.setRawTransaction(bitcoinJMagicService.addSignature(transactionResource.getRawTransaction(), key.getPrivateKey()));
                return new TransactionResourceAsm().toModel(transaction);
            }
        }
        throw new SomehtingGotWrongException();
    }
}
