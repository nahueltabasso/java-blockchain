package nrtchain.services;

import nrtchain.Main;
import nrtchain.model.Transaction;
import nrtchain.model.TransactionInput;
import nrtchain.model.TransactionOutput;
import nrtchain.utils.StringUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    public Transaction setNewTransaction(PublicKey sender, PublicKey reciepient, Double value, List<TransactionInput> inputs) {
        Transaction transaction = new Transaction();
        transaction.setSender(sender);
        transaction.setReciepient(reciepient);
        transaction.setValue(value);
        transaction.setInputs(inputs);
        return transaction;
    }

    // This calculates the transaction hash (id)
    public String calculateHash(Transaction transaction) {
        Transaction.sequence++;
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(transaction.getSender()) +
                        StringUtil.getStringFromKey(transaction.getReciepient()) +
                        Double.toString(transaction.getValue()) +
                        Transaction.sequence
        );
    }

    // Signs all the data we dont wish to be tampered with
    public void generateSignature(Transaction transaction, PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(transaction.getSender())
                + StringUtil.getStringFromKey(transaction.getReciepient())
                + Double.toString(transaction.getValue());
        byte[] signature = StringUtil.applyECDSASig(privateKey, data);
        transaction.setSignature(signature);
    }

    // Verifies the data we signed hasn't been tampered with
    public boolean verifySignature(Transaction transaction) {
        String data = StringUtil.getStringFromKey(transaction.getSender())
                + StringUtil.getStringFromKey(transaction.getReciepient())
                + Double.toString(transaction.getValue());
        return StringUtil.verifyECDSASig(transaction.getSender(), data, transaction.getSignature());
    }

    // Returns sum of inputs(UTXOs) values
    public double getInputsValue(Transaction transaction) {
        Double total = 0.0;
        for (TransactionInput i : transaction.getInputs()) {
            if (i.getUTXO() == null) {
                continue;   // If transaction can't be found skip it
            }
            total += i.getUTXO().getValue();
        }
        return total;
    }

    // Return sum of outputs
    public double getOutputsValue(Transaction transaction) {
        Double total = 0.0;
        for (TransactionOutput o : transaction.getOutputs()) {
            total += o.getValue();
        }
        return total;
    }

    // Returns true if new transaction could be created
    public boolean processTransaction(Transaction transaction) {
        if (this.verifySignature(transaction) == false) {
            System.out.println("Transaction Signature failed to verify");
            return false;
        }

        // Gather transactions inputs
        for (TransactionInput i : transaction.getInputs()) {
            i.setUTXO(Main.UTXOs.get(i.getTransactionOutputId()));
        }

        // Check if transaction is valid
        if (this.getInputsValue(transaction) < Main.minimumTransaction) {
            System.out.println("Transaction Inputs are to small: " + getInputsValue(transaction));
            return false;
        }

        // Now generates transactions outputs
        Double leftOver = this.getInputsValue(transaction) - transaction.getValue();     // Get value of inputs then left over change
        transaction.setId(this.calculateHash(transaction));
        List<TransactionOutput> outputs = new ArrayList<>();
        outputs.add(new TransactionOutput(transaction.getReciepient(), transaction.getValue(), transaction.getId()));     // Send value to reciepient
        outputs.add(new TransactionOutput(transaction.getSender(), leftOver, transaction.getId()));      // Send the left cover change back to sender

        // Add outputs to Unspent list
        for (TransactionOutput o : outputs) {
            Main.UTXOs.put(o.getId(), o);
        }

        // Remove transaction inputs from UTXO lists as spent:
        for (TransactionInput i : transaction.getInputs()) {
            if (i.getUTXO() == null) continue;      // If transaction can't be found skip it

            Main.UTXOs.remove(i.getUTXO().getId());
        }

        return true;
    }
}
