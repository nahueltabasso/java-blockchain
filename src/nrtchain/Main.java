package nrtchain;

import nrtchain.model.Block;
import nrtchain.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static int difficulty = 5;
    public static List<Block> blockchain = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Trying to mine block 1 ...");
        addBlock(new Block("Hi, this is my first block", "0"));

        System.out.println("Trying to mine block 2 ...");
        addBlock(new Block("Hi, this is my second block", blockchain.get(blockchain.size() - 1).getHash()));

        System.out.println("Trying to mine block 3 ...");
        addBlock(new Block("Hi, this is my third block", blockchain.get(blockchain.size() - 1).getHash()));

        System.out.println("\nBlockchain is Valid: " + isChainValid());

        String blockchainJson = StringUtil.getJson(blockchain);
        System.out.println("Now show the blockchain in json format");
        System.out.println(blockchainJson);
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;

        // Loop through blockchain to check hashes
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            String hashTarget = StringUtil.getDifficultyString(difficulty);

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
        }

        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}
