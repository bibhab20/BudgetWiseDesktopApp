package util.filter;

import config.AppConstants;
import model.Transaction;

import java.util.List;

public class VendorMatchOrFilter implements TransactionFilter {
    List<String> vendors;

    public VendorMatchOrFilter(List<String> vendors) {
        this.vendors = vendors;
    }

    @Override
    public boolean pass(Transaction transaction) {
        for (String vendor: vendors) {
            if (vendor.equals(AppConstants.ALL_PASS_FILTER) || transaction.getVendor().equals(vendor)) {
                return true;
            }
        }
        return false;
    }
}
