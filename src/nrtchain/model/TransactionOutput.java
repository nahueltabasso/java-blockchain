package nrtchain.model;

import nrtchain.utils.StringUtil;

import java.security.PublicKey;

public class TransactionOutput {

    private String id;
    private PublicKey reciepient;    // Also known as the new owner of these coins
    private Double value;           // The amount of coins they own
    private String parentTransactionId;     // The id of the transaction this output was created in

    public TransactionOutput(PublicKey reciepient, Double value, String parentTransactionId) {
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient) + Double.toString(value) + parentTransactionId);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PublicKey getReciepient() {
        return reciepient;
    }

    public void setReciepient(PublicKey reciepient) {
        this.reciepient = reciepient;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }

    public void setParentTransactionId(String parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }

    // Check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == reciepient);
    }
}
