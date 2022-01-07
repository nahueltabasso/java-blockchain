package nrtchain.model;

import nrtchain.Main;
import nrtchain.utils.StringUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class Transaction {

    private static int sequence = 0;
    private String id;      // This is also the hash of transaction
    private PublicKey sender;   // Sender address/public key
    private PublicKey reciepient;   // Recipient addresss/public key
    private Double value;
    private byte[] signature;
    private List<TransactionInput> inputs;
    private List<TransactionOutput> outputs;

    public Transaction(PublicKey sender, PublicKey reciepient, Double value, List<TransactionInput> inputs) {
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.sender = sender;
        this.reciepient = reciepient;
        this.value = value;
        this.inputs = inputs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PublicKey getSender() {
        return sender;
    }

    public void setSender(PublicKey sender) {
        this.sender = sender;
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

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public List<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public List<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<TransactionOutput> outputs) {
        this.outputs = outputs;
    }

    // This calculates the transaction hash (id)
    private String calculateHash() {
        sequence++;
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                      StringUtil.getStringFromKey(reciepient) +
                      Double.toString(value) +
                      sequence
        );
    }

    // Signs all the data we dont wish to be tampered with
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Double.toString(value);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    // Verifies the data we signed hasn't been tampered with
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(reciepient) + Double.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);
    }

    // Returns true if new transaction could be created
    public boolean processTransaction() {
        if (verifySignature() == false) {
            System.out.println("Transaction Signature failed to verify");
            return false;
        }

        // Gather transactions inputs
        for (TransactionInput i : inputs) {
            i.setUTXO(Main.UTXOs.get(i.getTransactionOutputId()));
        }

        // Check if transaction is valid
        if (getInputsValue() < Main.minimumTransaction) {
            System.out.println("Transaction Inputs are to small: " + getInputsValue());
            return false;
        }

        // Now generates transactions outputs
        Double leftOver = getInputsValue() - value;     // Get value of inputs then left over change
        id = calculateHash();
        outputs.add(new TransactionOutput(this.reciepient, value, id));     // Send value to reciepient
        outputs.add(new TransactionOutput(this.sender, leftOver, id));      // Send the left cover change back to sender

        // Add outputs to Unspent list
        for (TransactionOutput o : outputs) {
            Main.UTXOs.put(o.getId(), o);
        }

        // Remove transaction inputs from UTXO lists as spent:
        for (TransactionInput i : inputs) {
            if (i.getUTXO() == null) continue;      // If transaction can't be found skip it

            Main.UTXOs.remove(i.getUTXO().getId());
        }

        return true;
    }

    // Returns sum of inputs(UTXOs) values
    public double getInputsValue() {
        Double total = 0.0;
        for (TransactionInput i : inputs) {
            if (i.getUTXO() == null) {
                continue;   // If transaction can't be found skip it
            }
            total += i.getUTXO().getValue();
        }
        return total;
    }

    // Return sum of outputs
    public double getOutputsValue() {
        Double total = 0.0;
        for (TransactionOutput o : outputs) {
            total += o.getValue();
        }
        return total;
    }
}
