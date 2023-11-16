package service;

import config.VendorConfig;
import config.VendorConfigSupplier;
import lombok.extern.slf4j.Slf4j;
import model.Transaction;
import model.TransactionRepository;
import util.filter.*;

import java.util.*;

@Slf4j
public class VendorProcessorService {
    private static final String UNKNOWN_VENDOR = "UNKNOWN";
    private static final String MULTIPLE_VENDOR_MATCH = "ERROR";
    TransactionRepository repository;
    VendorConfigSupplier vendorConfigSupplier;
    Map<VendorConfig, AdvanceFilter> vendorFilters;

    public VendorProcessorService(TransactionRepository repository, VendorConfigSupplier vendorConfigSupplier) {
        this.repository = repository;
        this.vendorConfigSupplier = vendorConfigSupplier;
        loadVendorFilters();
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
        log.info("Staring filtering missing vendor transactions");
        VendorMatchOrFilter filter = new VendorMatchOrFilter(List.of(UNKNOWN_VENDOR));
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction: repository.getAll()) {
            if (filter.pass(transaction)) {
                result.add(transaction);
            }
        }
        result.sort(Comparator.comparing(Transaction::getDescription));
        log.info("{} transactions found with missing vendors", result.size());
        return result;
    }

    public List<Transaction> filterByMultipleVendorMatches() {
        VendorMatchOrFilter filter = new VendorMatchOrFilter(List.of(MULTIPLE_VENDOR_MATCH));
        List<Transaction> result = new ArrayList<>();
        for (Transaction transaction: repository.getAll()) {
            if (filter.pass(transaction)) {
                result.add(transaction);
            }
        }
        return result;
    }

    public List<Transaction> getTransactionMatches(VendorConfig vendorConfig) {
        List<Transaction> result = new ArrayList<>();
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new ContainsDescriptionFilter(vendorConfig.getContains()))
                .addFilter(new NotContainDescriptionFilter(vendorConfig.getNotContain())).build();

        for (Transaction transaction: repository.getAll()) {
            if (filter.pass(transaction)) {
                result.add(transaction);
            }
        }
        return result;
    }

    public List<Transaction> getNewTransactionMatches(VendorConfig vendorConfig) {
        List<Transaction> result = new ArrayList<>();
        AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new ContainsDescriptionFilter(vendorConfig.getContains()))
                .addFilter(new NotContainDescriptionFilter(vendorConfig.getNotContain())).build();

        for (Transaction transaction: repository.getAll()) {
            if (transaction.getVendor().equals("UNKNOWN") && filter.pass(transaction)) {
                result.add(transaction);
            }
        }
        return result;
    }

    public Map<VendorConfig, List<Transaction>> getVendorConfigCollision(VendorConfig newConfig) {
        Map<VendorConfig, List<Transaction>> collisionMap = new HashMap<>();
        AdvanceFilter newFilter = new AdvanceFilter.Builder().addFilter(new ContainsDescriptionFilter(newConfig.getContains()))
                .addFilter(new NotContainDescriptionFilter(newConfig.getNotContain())).build();
        for (VendorConfig vendorConfig: this.vendorFilters.keySet()) {
            List<Transaction> matches = new ArrayList<>();
            for (Transaction transaction: repository.getAll()) {
                if (newFilter.pass(transaction) && vendorFilters.get(vendorConfig).pass(transaction)) {
                    matches.add(transaction);
                }
            }
            if (matches.size() > 0) {
                collisionMap.put(vendorConfig, matches);
            }
        }

        return collisionMap;
    }

    private void loadVendorFilters(){
        this.vendorFilters = new HashMap<>();
        for (VendorConfig config: this.vendorConfigSupplier.get()) {
            AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new NotContainDescriptionFilter(config.getNotContain()))
                    .addFilter(new ContainsDescriptionFilter(config.getContains())).build();
            this.vendorFilters.put(config, filter);
        }
    }



}
