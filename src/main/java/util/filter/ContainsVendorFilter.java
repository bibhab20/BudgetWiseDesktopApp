package util.filter;

import model.Transaction;

import java.util.List;

public class ContainsVendorFilter implements TransactionFilter{
    List<String> matchStrings;

    public ContainsVendorFilter(List<String> matchStrings) {
        this.matchStrings = matchStrings;
    }

    @Override
    public boolean pass(Transaction transaction) {
        for (String token: matchStrings) {
            if (transaction.getVendor().contains(token)) {
                return true;
            }
        }
        return false;
    }
}
