package dchu.core.entities;

import org.bitcoinj.protocols.payments.PaymentProtocolException;
import org.bouncycastle.jcajce.provider.symmetric.IDEA;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Jiri on 7. 7. 2014.
 */
@Entity
public class P2shAddress {

    public enum INVOICE_STATUS {
        UNPAID,
        PARTIALLY_PAID,
        PAID,
        EXPIRED
    }
    private Long id;
    private String address;
    private String redeemScript;
    private List<Key> keys;
    private Long invoiceBalance = 0L;
    private Date dueDate;
    private INVOICE_STATUS invoiceStatus;
    private List<Transaction> transactions;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRedeemScript() {
        return redeemScript;
    }

    public void setRedeemScript(String redeemScript) {
        this.redeemScript = redeemScript;
    }

    @ManyToMany(cascade = CascadeType.ALL) //TODO: I am pretty sure this is wrong... Am I not overriding all keys sometimes?
    @JoinTable(name="ADDRESS_KEYS")
    public List<Key> getKeys() {
        return keys;
    }

    public void setKeys(List<Key> keys) {
        this.keys = keys;
    }

    public Long getInvoiceBalance() {
        return invoiceBalance;
    }

    public void setInvoiceBalance(Long invoiceBalance) {
        this.invoiceBalance = invoiceBalance;
    }

    @OneToMany(cascade=CascadeType.ALL, mappedBy="sourceAddress")
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public INVOICE_STATUS getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(INVOICE_STATUS invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }
}
