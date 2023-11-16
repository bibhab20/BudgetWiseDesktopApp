package util.filter;

import model.Transaction;
import model.TransactionType;

public class TransactionTypeFilter implements TransactionFilter{
    TransactionType type;

    public TransactionTypeFilter(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean pass(Transaction transaction) {
        return type.equals(TransactionType.ALL) || transaction.getType().equals(type);
    }
}
