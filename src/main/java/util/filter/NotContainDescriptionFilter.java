package util.filter;

import model.Transaction;

import java.util.List;

public class NotContainDescriptionFilter implements TransactionFilter{
    List<String> notContains;

    public NotContainDescriptionFilter(List<String> notContains) {
        this.notContains = notContains;
    }

    @Override
    public boolean pass(Transaction transaction) {
        for (String matchString: notContains) {
            if (transaction.getDescription().contains(matchString)) {
                return false;
            }
        }
        return true;
    }
}
