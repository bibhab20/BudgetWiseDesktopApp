package util.filter;

import config.AppConstants;
import model.Transaction;

import java.util.List;

public class VendorTypeMatchOrFilter implements TransactionFilter{
    List<String> vendorTypes;

    public VendorTypeMatchOrFilter(List<String> vendors) {
        this.vendorTypes = vendors;
    }

    @Override
    public boolean pass(Transaction transaction) {
        for (String vendorType: vendorTypes) {
            if (vendorType.equals(AppConstants.ALL_PASS_FILTER) || transaction.getVendorType().equals(vendorType)) {
                return true;
            }
        }
        return false;
    }
}
