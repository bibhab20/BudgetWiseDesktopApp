package util.filter;

import model.Transaction;

import java.util.Date;

public class BeforeDateFilter implements TransactionFilter{

    private final Date beforeDate;

    public BeforeDateFilter(Date beforeDate) {
        this.beforeDate = beforeDate;
    }


    @Override
    public boolean pass(Transaction transaction) {
        if (beforeDate == null) {
            return true;
        }
        return transaction.getTransactionDate().before(beforeDate) || transaction.getTransactionDate().equals(beforeDate);
    }
}
