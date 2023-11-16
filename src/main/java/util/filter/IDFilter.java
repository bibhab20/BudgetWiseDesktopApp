package util.filter;

import model.Transaction;

import java.util.List;

public class IDFilter implements TransactionFilter{
    private List<String> ids;

    public IDFilter(List<String> ids) {
        this.ids = ids;
    }

    @Override
    public boolean pass(Transaction transaction) {
        for (String id: ids) {
            if (transaction.getId().equals(id))
                return true;
        }
        return false;
    }
}
