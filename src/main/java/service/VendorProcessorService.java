package service;

import model.Transaction;
import model.TransactionRepository;
import util.filter.AdvanceFilter;
import util.filter.AfterDateFilter;
import util.filter.BeforeDateFilter;
import util.filter.VendorMatchOrFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class VendorProcessorService {
    private static final String UNKNOWN_VENDOR = "UNKNOWN";
    private static final String MULTIPLE_VENDOR_MATCH = "ERROR";
    TransactionRepository repository;

    public VendorProcessorService(TransactionRepository repository) {
        this.repository = repository;
    }

    public List<Transaction> filterByVendorName(String vendorName) {
        VendorMatchOrFilter filter = new VendorMatchOrFilter(List.of(vendorName));
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction: repository.getAll()) {
            if (filter.pass(transaction)) {
                result.add(transaction);
            }
        }
        return result;
    }

    public List<Transaction> filterByVendorNameAndDate(Date startDate, Date endDate, String vendorName) {
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new BeforeDateFilter(endDate))
                .addFilter(new AfterDateFilter(startDate))
                .addFilter(new VendorMatchOrFilter(List.of(vendorName))).build();
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction: repository.getAll()) {
            if (filter.pass(transaction)) {
                result.add(transaction);
            }
        }
        return result;
    }

    public List<Transaction> filterByMissingVendors() {
        VendorMatchOrFilter filter = new VendorMatchOrFilter(List.of(UNKNOWN_VENDOR, MULTIPLE_VENDOR_MATCH));
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction: repository.getAll()) {
            if (filter.pass(transaction)) {
                result.add(transaction);
            }
        }
        return result;
    }

}
