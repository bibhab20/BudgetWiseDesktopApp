package util.enrichment;

import config.VendorOverrideConfig;
import config.VendorOverrideConfigSupplier;
import lombok.extern.slf4j.Slf4j;
import model.Transaction;
import util.filter.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class VendorOverrideEnricher implements TransactionEnricher{
    Map<VendorOverrideConfig, AdvanceFilter> filterMap;
    VendorOverrideConfigSupplier configSupplier;

    public VendorOverrideEnricher(VendorOverrideConfigSupplier configSupplier) {
        this.configSupplier = configSupplier;
        loadFilterMap();
    }

    @Override
    public List<Transaction> enrich(List<Transaction> transactions) {
        int singleMatchCount = 0, multipleMatchCount = 0, noMatchCount = 0;
        for (Transaction transaction: transactions) {
            List<VendorOverrideConfig> matches = getMatches(transaction);
            if (matches.size() == 0) {
                noMatchCount++;
            } else if (matches.size() > 1) {
                log.error("Multiple overriding vendors matched with transaction with id: {}", transaction.getId());
                multipleMatchCount++;
            } else {
                singleMatchCount++;
                transaction.setVendor(matches.get(0).getName());
                transaction.setVendorType(matches.get(0).getVendorType());
                transaction.getEnrichmentList().add(this.getClass().getName());
            }
        }
        log.info("{} out of {} transactions have multiple vendor override matches(id duplication in overrides)", multipleMatchCount, transactions.size());
        log.info("{} out of {} transactions are successfully overriden with new vendors", singleMatchCount, transactions.size());
        return transactions;
    }

    private List<VendorOverrideConfig> getMatches(Transaction transaction){
        List<VendorOverrideConfig> matches = new ArrayList<>();
        for (VendorOverrideConfig vendor: this.filterMap.keySet()) {
            if (filterMap.get(vendor).pass(transaction)) {
                matches.add(vendor);
            }
        }
        return matches;
    }

    private void loadFilterMap() {
        this.filterMap = new HashMap<>();
        for (VendorOverrideConfig config: this.configSupplier.get()) {
            AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new IDFilter(config.getIds())).build();
            filterMap.put(config, filter);
        }
    }


}
