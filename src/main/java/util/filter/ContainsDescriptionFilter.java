package util.filter;

import config.AppConstants;
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
            if (matchString.equals(AppConstants.ALL_PASS_FILTER) || transaction.getDescription().contains(matchString)) {
                return true;
            }
        }
        return false;
    }
}
