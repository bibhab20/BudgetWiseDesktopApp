package util.filter;

import model.Transaction;
import model.TransactionSource;

public class SourceFilter implements TransactionFilter{
    TransactionSource source;

    public SourceFilter(TransactionSource source) {
        this.source = source;
    }

    @Override
    public boolean pass(Transaction transaction) {
        return this.source.equals(TransactionSource.ALL) || transaction.getSource().equals(this.source);
    }
}
