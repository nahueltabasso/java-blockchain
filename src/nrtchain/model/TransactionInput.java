package nrtchain.model;

/*
    This class will be used to make references to TransactionsOuputs that have not yet been used.
    TransactionOutputId will be used to find the relevant (referenced) TransactionOutput.
    What will allow miners to verify your ownership
 */
public class TransactionInput {

    private String transactionOutputId;
    private TransactionOutput UTXO;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public String getTransactionOutputId() {
        return transactionOutputId;
    }

    public void setTransactionOutputId(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionOutput getUTXO() {
        return UTXO;
    }

    public void setUTXO(TransactionOutput UTXO) {
        this.UTXO = UTXO;
    }
}
