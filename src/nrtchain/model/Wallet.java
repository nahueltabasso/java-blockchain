package nrtchain.model;

import nrtchain.Main;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    public HashMap<String, TransactionOutput> UTXOs = new HashMap<>();

    public Wallet() {
        generateKeyPair();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public HashMap<String, TransactionOutput> getUTXOs() {
        return UTXOs;
    }

    public void setUTXOs(HashMap<String, TransactionOutput> UTXOs) {
        this.UTXOs = UTXOs;
    }

    public void generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key pair generator and generate a keyPair
            keyPairGenerator.initialize(ecSpec, secureRandom);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Double getBalance() {
        double total = 0;
        for (Map.Entry<String, TransactionOutput> item : Main.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();

            if (UTXO.isMine(publicKey)) {       // If output belongs to me (if coins belong to me)

                UTXOs.put(UTXO.getId(), UTXO);     // Add it to our list of unspent transactions
                total += UTXO.getValue();
            }
        }
        return total;
    }

    // Generates and returns a new transaction from this wallet
    public Transaction sendFunds(PublicKey _recipient, Double value) {
        if (getBalance() < value) {
            System.out.println("Not enough funds to send transaction. Transaction Discarded");
            return null;
        }

        // Create an array list of inputs
        List<TransactionInput> inputs = new ArrayList<>();

        double total = 0.0;
        for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input: inputs){
            UTXOs.remove(input.getTransactionOutputId());
        }

        return newTransaction;
    }
}
