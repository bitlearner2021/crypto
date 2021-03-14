package dchu.rest.entities;

import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.crypto.KeyCrypter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

/**
 * Invoice Wallet
 * Created by Jiri on 5. 7. 2014.
 */
@Component
public class InvWallet extends Wallet  {

    public InvWallet() { super(NetworkParameters.testNet());}

    public InvWallet(NetworkParameters params) {
        super(params);
    }

    public InvWallet(NetworkParameters params, KeyCrypter keyCrypter) {
        super(params,keyCrypter);
    }
}
