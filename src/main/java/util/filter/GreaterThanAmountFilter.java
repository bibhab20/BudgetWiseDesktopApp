package util.filter;

import model.Transaction;

public class GreaterThanAmountFilter implements TransactionFilter{
    double amount;

    public GreaterThanAmountFilter(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean pass(Transaction transaction) {
        return transaction.getAmount() > this.amount;
    }
}
