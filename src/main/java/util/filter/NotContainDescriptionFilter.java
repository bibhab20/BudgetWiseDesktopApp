package util.filter;

import config.AppConstants;
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
            if (matchString.equals(AppConstants.ALL_PASS_FILTER) || transaction.getDescription().contains(matchString)) {
                return false;
            }
        }
        return true;
    }
}
