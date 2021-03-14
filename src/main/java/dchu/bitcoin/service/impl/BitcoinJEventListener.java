package dchu.bitcoin.service.impl;

import com.google.bitcoin.core.*;
import com.google.bitcoin.script.Script;
import org.bitcoinj.core.Coin;
import dchu.core.entities.P2shAddress;
import dchu.core.services.P2shAddressesRepository;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.*;
import org.slf4j.*;

/**
 * Created by Jiri on 21. 7. 2014.
 */
@Service
public class BitcoinJEventListener implements WalletEventListener {

    private static final Logger log = LoggerFactory.getLogger(dchu.bitcoin.service.impl.BitcoinJEventListener.class);

    P2shAddressesRepository addressesRepository;
    private NetworkParameters params;

    @Autowired
    public BitcoinJEventListener(P2shAddressesRepository addressesRepository) {
        this.addressesRepository = addressesRepository;
        this.params = NetworkParameters.testNet();
    }


    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        List<Address> addressesToUpdate = new ArrayList<>(tx.getOutputs().size());
        for (TransactionOutput output : tx.getOutputs()) {
            Address toAddress = output.getScriptPubKey().getToAddress(params);
            if (wallet.isAddressWatched(toAddress)) {
                addressesToUpdate.add(toAddress);
            }
        }
        updateAddresses(wallet, addressesToUpdate);
    }

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {
        onCoinsReceived(wallet, tx, Coin.valueOf(prevBalance.longValue()), Coin.valueOf(newBalance.longValue()));
    }

    private void updateAddresses(Wallet wallet, List<Address> addressesToUpdate) {
        log.error("updateing address");
        List<TransactionOutput> watchedOutputs = wallet.getWatchedOutputs(true);
        Map<Address, Coin> balances = new HashMap<>();
        for (TransactionOutput watchedOutput : watchedOutputs) {
            Address toAddress = watchedOutput.getScriptPubKey().getToAddress(params);
            if (addressesToUpdate.contains(toAddress)) {
                Coin coin = balances.get(toAddress);
                if (coin == null) {
                    coin = Coin.ZERO;
                }

                balances.put(toAddress,coin.add(Coin.valueOf(watchedOutput.getValue().longValue())));
            }
        }
        for (Address address : balances.keySet()) {
            P2shAddress byAddress = addressesRepository.findByAddress(address.toString());
            log.error("byAddress is " + byAddress.getAddress());
            byAddress.setInvoiceBalance(byAddress.getInvoiceBalance() - balances.get(address).longValue());
            if (byAddress.getInvoiceBalance() <= 0L) {
                byAddress.setInvoiceStatus(P2shAddress.INVOICE_STATUS.PAID);
            } else {
                byAddress.setInvoiceStatus(P2shAddress.INVOICE_STATUS.PARTIALLY_PAID);
            }
            Long now = System.currentTimeMillis();
            // check for past due
            if (now < byAddress.getDueDate().getTime()) {
                byAddress.setInvoiceStatus(P2shAddress.INVOICE_STATUS.EXPIRED);
            }
            addressesRepository.save(byAddress);
            log.error("byAddress invoice balance is: " + byAddress.getInvoiceBalance());
        }
    }


    public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {

    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, BigInteger prevBalance, BigInteger newBalance) {

    }

    @Override
    public void onReorganize(Wallet wallet) {

    }

    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {

    }

    @Override
    public void onWalletChanged(Wallet wallet) {

    }

    @Override
    public void onScriptsAdded(Wallet wallet, List<Script> scripts) {

    }

    public void onKeysAdded(List<ECKey> keys) {

    }

    @Override
    public void onKeysAdded(Wallet wallet, List<ECKey> keys) {

    }
}
