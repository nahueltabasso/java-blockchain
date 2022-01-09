package nrtchain.services;

import nrtchain.Main;
import nrtchain.model.Transaction;
import nrtchain.model.TransactionInput;
import nrtchain.model.TransactionOutput;
import nrtchain.model.Wallet;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletService {

    // Generates and returns a new transaction from this wallet
    public Transaction sendFunds(Wallet walletSender, PublicKey _recipient, Double value) {
        if (this.getBalance(walletSender) < value) {
            System.out.println("Not enough funds to send transaction. Transaction Discarded");
            return null;
        }

        // Create an array list of inputs
        List<TransactionInput> inputs = new ArrayList<>();

        double total = 0.0;
        for (Map.Entry<String, TransactionOutput> item: walletSender.getUTXOs().entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(walletSender.getPublicKey(), _recipient, value, inputs);
        newTransaction.generateSignature(walletSender.getPrivateKey());

        HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
        UTXOs = walletSender.getUTXOs();
        for (TransactionInput input: inputs){
            UTXOs.remove(input.getTransactionOutputId());
        }
        walletSender.setUTXOs(UTXOs);
        return newTransaction;
    }

    public Double getBalance(Wallet wallet) {
        double total = 0;
        HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
        for (Map.Entry<String, TransactionOutput> item : Main.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();

            if (UTXO.isMine(wallet.getPublicKey())) {       // If output belongs to me (if coins belong to me)

                UTXOs.put(UTXO.getId(), UTXO);     // Add it to our list of unspent transactions
                total += UTXO.getValue();
            }
        }
        wallet.setUTXOs(UTXOs);
        return total;
    }
}
