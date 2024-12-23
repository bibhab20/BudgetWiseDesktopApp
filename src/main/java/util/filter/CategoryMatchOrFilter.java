package util.filter;

import config.AppConstants;
import model.Transaction;

import java.util.List;

public class CategoryMatchOrFilter implements TransactionFilter{
    List<String> categories;

    public CategoryMatchOrFilter(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public boolean pass(Transaction transaction) {
        for (String category: categories) {
            if (category.equals(AppConstants.ALL_PASS_FILTER) || transaction.getCategory().equals(category)) {
                return true;
            }
        }
        return false;
    }
}
