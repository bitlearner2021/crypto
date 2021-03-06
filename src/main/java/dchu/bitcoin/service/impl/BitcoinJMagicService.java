package dchu.bitcoin.service.impl;

import com.google.bitcoin.core.*;
import com.google.bitcoin.crypto.TransactionSignature;
import com.google.bitcoin.script.Script;
import com.google.bitcoin.script.ScriptBuilder;
import com.google.bitcoin.script.ScriptChunk;
import dchu.core.entities.P2shAddress;
import dchu.core.services.P2shAddressesRepository;
import dchu.rest.entities.InvWallet;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Utils;
import dchu.bitcoin.exceptions.NotEnoughMoneyException;
import dchu.bitcoin.service.BitcoinMagicService;
import org.h2.expression.ConditionIn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.swing.plaf.ComponentInputMapUIResource;
import java.math.BigInteger;
import java.util.*;

/**
 * Created by Jiri on 11. 7. 2014.
 */
@Service
public class BitcoinJMagicService implements BitcoinMagicService {


    Wallet wallet;
    private NetworkParameters networkParams;

    @Autowired
    public BitcoinJMagicService(InvWallet wallet) {
        this.wallet = (Wallet) wallet;
        //this.networkParams = networkParams;
        this.networkParams = NetworkParameters.testNet();
    }

    @Override
    public String createMultiSignatureRedeemScript(List<String> publicKeys, int requiredKeys) {

        List<ECKey> keys = new ArrayList<>(publicKeys.size());
        for (String publicKey : publicKeys) {
            keys.add(new ECKey(null, Utils.HEX.decode(publicKey)));
        }
        Script script = ScriptBuilder.createMultiSigOutputScript(requiredKeys, keys);
        return Utils.HEX.encode(script.getProgram());
    }

    @Override
    public Map<String, String> generateKeyPair() {
        ECKey key = new ECKey();
        Map<String, String> map = new HashMap<>();
        map.put(PUBLIC_KEY, Utils.HEX.encode(key.getPubKey()));
        map.put(PRIVATE_KEY, Utils.HEX.encode(key.getPrivKeyBytes()));
        return map;
    }

    @Override
    public String getAddressFromRedeemScript(String multiSignatureRedeemScript) {
        Script script = new Script(Utils.HEX.decode(multiSignatureRedeemScript));
        byte[] sha256hash160 = Utils.sha256hash160(script.getProgram());
        byte[] bytes = new byte[sha256hash160.length + 1];
        bytes[0] = -60;
        System.arraycopy(sha256hash160, 0, bytes, 1, sha256hash160.length);
        byte[] checkSum = com.google.bitcoin.core.Utils.doubleDigest(bytes);
        byte[] address = new byte[bytes.length + 4];
        System.arraycopy(bytes, 0, address, 0, bytes.length);
        System.arraycopy(checkSum, 0, address, bytes.length, 4);
        return Base58.encode(address);
    }

    @Override
    public String createTransaction(String address, String targetAddress, Long amount) {
        Transaction transaction = new Transaction(networkParams);
        try {
            Address sourceAddress = new Address(networkParams,address);
            Coin coinAmount = Coin.valueOf(amount);
            Coin total = addInputs(transaction, sourceAddress, coinAmount);

//            Coin total = addInputs(transaction, sourceAddress, coinAmount.add(Coin.valueOf(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.longValue())));
//            if (total.isLessThan(coinAmount)){
//                throw new NotEnoughMoneyException();
//            }

            Address toAddress = new Address(networkParams,targetAddress);
            TransactionOutput output = new TransactionOutput(networkParams,transaction, BigInteger.valueOf(amount),toAddress);
            transaction.addOutput(output);
            if (total.isGreaterThan(coinAmount)) {
                // Coin returnamt = total.subtract(Coin.valueOf(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.longValue())).subtract(coinAmount);
                Coin returnamt = total.subtract(coinAmount);
                TransactionOutput returnRest = new TransactionOutput(networkParams,transaction,BigInteger.valueOf(returnamt.getValue()),sourceAddress);
                transaction.addOutput(returnRest);

           }

        } catch (AddressFormatException e) {
            throw new RuntimeException(e);
        }
        return Utils.HEX.encode(transaction.bitcoinSerialize());
    }

    private Coin addInputs(Transaction transaction, Address sourceAddress, Coin amount) {
        List<TransactionOutput> watchedOutputs = wallet.getWatchedOutputs(true);
        Coin total = Coin.ZERO;
        for (TransactionOutput watchedOutput : watchedOutputs) {
            if (watchedOutput.getScriptPubKey().getToAddress(networkParams).equals(sourceAddress)) {
                transaction.addInput(watchedOutput);
                total = total.add(Coin.valueOf(watchedOutput.getValue().longValue()));
                if (total.isGreaterThan(amount)){
                    return total;
                }
            }
        }
        return total;
    }

    @Override
    public void watchAddress(String addressString) {
        try {
            Address address = new Address(networkParams, addressString);
            wallet.addWatchedAddress(address);
        } catch (AddressFormatException e) {
            //todo: maybe create some exception for this?
            throw new RuntimeException(e);
        }
    }

    @Override
    public Long getBalance(String address) {
        return null;
    }

    @Override
    public String addSignature(String rawTransaction, String privateKey) {
        Transaction transaction = new Transaction(networkParams,Utils.HEX.decode(rawTransaction));
        ECKey ecKey = ECKey.fromASN1(Utils.HEX.decode(privateKey));

        for (TransactionInput transactionInput : transaction.getInputs()) {
            Script scriptSig = transactionInput.getScriptSig();
            Script redeemScript = new Script(scriptSig.getChunks().get(scriptSig.getChunks().size()-1).data);
            TransactionSignature transactionSignature = transaction.calculateSignature(transaction.getInputs().indexOf(transactionInput), ecKey, redeemScript, Transaction.SigHash.ALL, true);
            ScriptBuilder builder = new ScriptBuilder();
            for (ScriptChunk scriptChunk : scriptSig.getChunks()) {
                if (scriptSig.getChunks().indexOf(scriptChunk) == 2){
                    builder.data(transactionSignature.encodeToBitcoin());
                }
                builder.data(scriptChunk.data);
            }
            transactionInput.setScriptSig(builder.build());
        }

        return Utils.HEX.encode(transaction.bitcoinSerialize());
    }

}
