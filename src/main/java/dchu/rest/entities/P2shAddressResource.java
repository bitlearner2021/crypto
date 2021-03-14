package dchu.rest.entities;

import dchu.core.entities.P2shAddress;
import org.springframework.hateoas.RepresentationModel;

import java.util.Date;
import java.util.List;

/**
 * Created by Jiri on 7. 7. 2014.
 */
public class P2shAddressResource extends RepresentationModel<P2shAddressResource> {
    String address;
    String redeemScript;
    Integer totalKeys = 3;
    Integer requiredKeys = 2;
    Long invoiceBalance = 0L;
    Date dueDate;

    P2shAddress.INVOICE_STATUS invoiceStatus;

    List<String> keys;

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

    public Integer getTotalKeys() {
        return totalKeys;
    }

    public void setTotalKeys(Integer totalKeys) {
        this.totalKeys = totalKeys;
    }

    public Integer getRequiredKeys() {
        return requiredKeys;
    }

    public void setRequiredKeys(Integer requiredKeys) {
        this.requiredKeys = requiredKeys;
    }

    public List<String> getKeys() {
        return keys;
    }

    public Long getInvoiceBalance() {
        return invoiceBalance;
    }

    public void setInvoiceBalance(Long balance) {
        this.invoiceBalance = balance;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public P2shAddress.INVOICE_STATUS getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(P2shAddress.INVOICE_STATUS invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

}
