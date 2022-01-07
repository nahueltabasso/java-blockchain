package nrtchain;

import nrtchain.model.*;
import nrtchain.utils.StringUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static int difficulty = 5;
    public static Double minimumTransaction = 0.1;
    public static List<Block> blockchain = new ArrayList<>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    public static Wallet walletA;
    public static Wallet walletB;
    public static Transaction genesisTransaction;

    public static void main(String[] args) {
        // Setup Bouncey Castle as a Security Provider
        Security.addProvider(new BouncyCastleProvider());

        // Create the new wallets
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinBase = new Wallet();

        // Create genesis transaction, which sends 100 Coins to walletA
        genesisTransaction = new Transaction(coinBase.getPublicKey(), walletA.getPublicKey(), 100.0, null);
        genesisTransaction.generateSignature(coinBase.getPrivateKey());
        genesisTransaction.setId("0");      // Manually set the transaction id
        List<TransactionOutput> transactionOutputs = new ArrayList<>();
        transactionOutputs.add(new TransactionOutput(genesisTransaction.getReciepient(), genesisTransaction.getValue(), genesisTransaction.getId()));
        genesisTransaction.setOutputs(transactionOutputs);
        // Its important to store our first transaction in the UTXOs
        UTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));

        System.out.println("Creating and Mining Genesis block...");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        // Testing our app
        Block block1 = new Block(genesis.getHash());
        System.out.println("WalletA balance is: " + walletA.getBalance());
        System.out.println("WalletA is Attempting to send funds (40) to walletB...");
        block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40.0));
        addBlock(block1);
        System.out.println("WalletA balance is: " + walletA.getBalance());
        System.out.println("WalletB balance is: " + walletB.getBalance());

        Block block2 = new Block(block1.getHash());
        System.out.println("WalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000.0));
        addBlock(block2);
        System.out.println("WalletA balance is: " + walletA.getBalance());
        System.out.println("WalletB balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.getHash());
        System.out.println("WalletB is Attempting to send funds (20) to walletA...");
        block3.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 20.0));
        System.out.println("WalletA balance is: " + walletA.getBalance());
        System.out.println("WalletB balance is: " + walletB.getBalance());

        boolean isChainValid = isChainValid();

        // Test public and private keys
//        System.out.println("Private and public keys: ");
//        System.out.println(StringUtil.getStringFromKey(walletA.getPrivateKey()));
//        System.out.println(StringUtil.getStringFromKey(walletA.getPublicKey()));
//
//        // Create a test transaction from walletA to walletB
//        Transaction transaction = new Transaction(walletA.getPublicKey(), walletB.getPublicKey(), 5.0, null);
//        transaction.generateSignature(walletA.getPrivateKey());
//
//        // Verify the signature works and verify it from the public key
//        System.out.println("Is signature verified");
//        System.out.println(transaction.verifySignature());
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;

        String hashTarget = StringUtil.getDifficultyString(difficulty);
        // A temporary working list of unspent transactions at a given block state
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>();
        tempUTXOs.put(genesisTransaction.getOutputs().get(0).getId(), genesisTransaction.getOutputs().get(0));

        // Loop through blockchain to check hashes
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            // Compare registered hash and calculate hash
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Error: Current hashes are not equals");
                return false;
            }

            // Now compare the previous hash and registered previous hash
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("Error: Previoush hash is not equal");
                return false;
            }

            // Check if hash is solved
            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined");
                return false;
            }

            // Loop through blockchains transactions
            TransactionOutput tempOutput;
            for (int k = 0; k < currentBlock.getTransactions().size(); k++) {

                Transaction currentTransaction = currentBlock.getTransactions().get(k);
                if (!currentTransaction.verifySignature()) {
                    System.out.println("Signature on Transaction(" + k + ") is invalid");
                    return false;
                }

                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("Inputs are not equal to outputs on Transaction(" + k + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.getInputs()) {
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    if (tempOutput == null) {
                        System.out.println("Referenced input on Transaction(" + k + ") is missing");
                        return false;
                    }

                    if (input.getUTXO().getValue() != tempOutput.getValue()) {
                        System.out.println("Referenced input Transaction(" + k + ") value is invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                for (TransactionOutput output : currentTransaction.getOutputs()) {
                    tempUTXOs.put(output.getId(), output);
                }

                if (currentTransaction.getOutputs().get(0).getReciepient() != currentTransaction.getReciepient()) {
                    System.out.println("Transaction(" + k + ") output reciepient is not who it should be");
                    return false;
                }

                if (currentTransaction.getOutputs().get(1).getReciepient() != currentTransaction.getSender()) {
                    System.out.println("Transaction(" + k + ") output 'change' is not sender");
                    return false;
                }
            }
        }

        System.out.println("Blockchain is valid!!!!");

        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}
