package nrtchain.model;

import nrtchain.utils.StringUtil;

import java.time.LocalDate;
import java.time.ZoneOffset;

public class Block {

    private String hash;
    private String previousHash;
    private String data;
    private Long timeStamp;
    private int nonce;

    public Block(String data, String previousHash) {
        this.data = data;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String calculateHash() {
        String calculateHash = StringUtil.applySha256(
                previousHash +
                      Long.toString(timeStamp) +
                      Integer.toString(nonce) +
                      data
        );
        return calculateHash;
    }

    public void mineBlock(int difficulty) {
        String target = StringUtil.getDifficultyString(difficulty);   // Create a string with difficulty * "0"

        while(!hash.substring(0, difficulty).equals(target)) {
            nonce++;
            hash = calculateHash();
        }
        System.out.println("Block Mined!!! : " + hash);
    }
}
