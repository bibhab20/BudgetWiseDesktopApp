package util.filter;

import model.Transaction;

public interface TransactionFilter {

    public boolean pass(Transaction transaction);
}