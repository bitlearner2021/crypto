package dchu.core.services.impl;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.TransactionOutput;
import dchu.bitcoin.service.BitcoinMagicService;
import dchu.core.entities.Key;
import dchu.core.entities.P2shAddress;
import dchu.core.entities.Transaction;
import dchu.core.services.KeysRepository;
import dchu.core.services.P2shAddressesRepositoryCustom;
import org.bitcoinj.core.Coin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Jiri on 11. 7. 2014.
 */
@Service
public class P2shAddressesRepositoryImpl implements P2shAddressesRepositoryCustom {
    BitcoinMagicService bitcoinMagicService;
    KeysRepository keysRepository;

    @Autowired
    public P2shAddressesRepositoryImpl(BitcoinMagicService bitcoinMagicService, KeysRepository keysRepository) {
        this.bitcoinMagicService = bitcoinMagicService;
        this.keysRepository = keysRepository;
    }

    @Override
    public P2shAddress createNew(List<String> publicKeys, Integer requiredKeys) {
        P2shAddress address = new P2shAddress();
        address.setRedeemScript(bitcoinMagicService.createMultiSignatureRedeemScript(publicKeys, requiredKeys));
        address.setAddress(bitcoinMagicService.getAddressFromRedeemScript(address.getRedeemScript()));
        List<Key> keys = new ArrayList<>(publicKeys.size());
        for (String publicKey : publicKeys) {
            Key byPublicKey = keysRepository.findByPublicKey(publicKey);
            if (byPublicKey != null) {
                keys.add(byPublicKey);
            } else {
                Key key = new Key();
                key.setPublicKey(publicKey);
                keys.add(key);
            }

        }
        address.setKeys(keys);
        bitcoinMagicService.watchAddress(address.getAddress());
        return address;
    }

    @Override
    public Transaction createNewTransaction(P2shAddress address, String targetAddress, Long amount) {
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setRawTransaction(bitcoinMagicService.createTransaction(address.getAddress(), targetAddress, amount));
        transaction.setTargetAddress(targetAddress);
        transaction.setSourceAddress(address);
        return transaction;
    }


}
