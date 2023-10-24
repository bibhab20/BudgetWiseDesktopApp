package util.filter;

import model.Transaction;

import java.util.Date;

public class AfterDateFilter implements TransactionFilter{
    private final Date afterDate;

    public AfterDateFilter(Date afterDate) {
        this.afterDate = afterDate;
    }


    @Override
    public boolean pass(Transaction transaction) {
        if (afterDate == null) {
            return true;
        }
        return transaction.getTransactionDate().after(afterDate);
    }
}
