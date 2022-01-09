package nrtchain.services;

import nrtchain.Main;
import nrtchain.model.Block;
import nrtchain.model.Transaction;
import nrtchain.utils.StringUtil;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class BlockService {

    public Block generateNewBlock(String previousHash) {
        Block block = new Block();
        block.setPreviousHash(previousHash);
        LocalDate localDate = LocalDate.now();
        block.setTimeStamp(localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli());
        block.setHash(calculateHash(block));
        return block;
    }

    public String calculateHash(Block block) {
        String calculateHash = StringUtil.applySha256(
                block.getPreviousHash() +
                        Long.toString(block.getTimeStamp()) +
                        Integer.toString(block.getNonce()) +
                        block.getMerkleRoot()
        );
        return calculateHash;
    }

    public void mineBlock(Block block, int difficulty) {
        block.setMerkleRoot(StringUtil.getMerkleRoot(block.getTransactions()));
        String target = StringUtil.getDifficultyString(difficulty);   // Create a string with difficulty * "0"

        while(!block.getHash().substring(0, difficulty).equals(target)) {
            // Increase nonce value until hash target is reached
            block.setNonce(block.getNonce() + 1);
            block.setHash(this.calculateHash(block));
        }
        System.out.println("Block Mined!!! : " + block.getHash());
    }

    /*
    Add transactions to this block
 */
    public boolean addTransaction(Block block, Transaction transaction) {
        // Process transaction and check if valid, unless block is genesis block then ignore
        if(transaction == null) return false;

        if((!"0".equals(block.getPreviousHash()))) {
            if((transaction.processTransaction() != true)) {
                System.out.println("Transaction failed to process. Discarded.");
                return false;
            }
        }

        List<Transaction> transactions = new ArrayList<>();
        transactions = block.getTransactions();
        transactions.add(transaction);
        block.setTransactions(transactions);
        System.out.println("Transaction Successfully added to Block");
        return true;
    }

    public void addBlock(Block newBlock, int difficulty) {
        this.mineBlock(newBlock, difficulty);
        Main.blockchain.add(newBlock);
    }
}
