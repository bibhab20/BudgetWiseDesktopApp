package util.filter;

import model.Transaction;

import java.util.List;

public class ContainsDescriptionFilter implements TransactionFilter{
    private List<String> contains;

    public ContainsDescriptionFilter(List<String> contains) {
        this.contains = contains;
    }

    @Override
    public boolean pass(Transaction transaction) {
        for (String matchString: contains) {
            if (transaction.getDescription().contains(matchString)) {
                return true;
            }
        }
        return false;
    }
}
