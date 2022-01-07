package nrtchain.model;

import nrtchain.utils.StringUtil;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class Block {

    private String hash;
    private String previousHash;
    private String merkleRoot;
    private Long timeStamp;
    private int nonce;
    private List<Transaction> transactions = new ArrayList<>();     // Our data will be a simple message

    public Block(String previousHash) {
        this.previousHash = previousHash;
        LocalDate localDate = LocalDate.now();
        this.timeStamp = localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        this.hash = calculateHash();
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public String calculateHash() {
        String calculateHash = StringUtil.applySha256(
                previousHash +
                      Long.toString(timeStamp) +
                      Integer.toString(nonce) +
                      merkleRoot
        );
        return calculateHash;
    }

    public void mineBlock(int difficulty) {
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty);   // Create a string with difficulty * "0"

        while(!hash.substring(0, difficulty).equals(target)) {
            // Increase nonce value until hash target is reached
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }

    /*
        Add transactions to this block
     */
    public boolean addTransaction(Transaction transaction) {
        // Process transaction and check if valid, unless block is genesis block then ignore
        if(transaction == null) return false;

        if((!"0".equals(previousHash))) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }

        transactions.add(transaction);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }


}
