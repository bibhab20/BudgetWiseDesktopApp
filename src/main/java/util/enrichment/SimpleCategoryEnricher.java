package util.enrichment;

import config.CategoryConfig;
import config.CategoryConfigSupplier;
import lombok.extern.slf4j.Slf4j;
import model.Transaction;
import util.AppConfig;
import util.filter.AdvanceFilter;
import util.filter.VendorMatchOrFilter;
import util.filter.VendorTypeMatchOrFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SimpleCategoryEnricher implements TransactionEnricher{
    CategoryConfigSupplier configSupplier;
    Map<String, AdvanceFilter> filterMap;
    AppConfig config;

    public SimpleCategoryEnricher(CategoryConfigSupplier configSupplier, AppConfig config) {
        this.configSupplier = configSupplier;
        this.config = config;
        try {
            this.filterMap = getFilterMap(configSupplier.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Transaction> enrich(List<Transaction> transactions) {
        int unknownCount =  0, errorCount = 0, successCount=0;
        for (Transaction transaction: transactions) {
            List<String> matches = getMatches(transaction);
            if (matches.size() == 0) {
                log.warn("No match found for transaction with id: {}", transaction.getId());
                transaction.setCategory("UNKNOWN");
                unknownCount++;
            } else if (matches.size() > 1) {
                log.error("Multiple category matches the transaction with id: {} and the matches are {}", transaction.getId(), matches);
                transaction.setCategory("ERROR");
                transaction.addAdditionalDetails("Category matches", toCommaDelimitedString(matches));
                errorCount++;
            }
            else {
                successCount++;
                transaction.setCategory(matches.get(0));
            }
        }
        log.info("{} transactions successfully enriched with Category data",successCount);
        log.info("{} transactions have not matching category",unknownCount);
        log.info("{} transactions have multiple category matches",errorCount);
        return transactions;
    }

    private String toCommaDelimitedString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s: list) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    private Map<String, AdvanceFilter> getFilterMap(List<CategoryConfig> categoryConfigs) throws Exception {
        Map<String, AdvanceFilter> map = new HashMap<>();
        for (CategoryConfig categoryConfig: categoryConfigs) {
            if (map.containsKey(categoryConfig.getName())) {
                log.error("Duplicate category found in category config, name: {}", categoryConfig.getName());
                throw new Exception("Duplicate category names");
            }

            AdvanceFilter filter;
            if (categoryConfig.getVendorTypes() != null && categoryConfig.getVendorTypes().size() > 0) {
                filter = new AdvanceFilter.Builder().addFilter(new VendorTypeMatchOrFilter(categoryConfig.getVendorTypes())).build();
            } else {
                filter = new AdvanceFilter.Builder().addFilter(new VendorMatchOrFilter(categoryConfig.getVendors())).build();
            }
            map.put(categoryConfig.getName(), filter);
        }
        return map;
    }

    private List<String> getMatches(Transaction transaction) {
        List<String> matches = new ArrayList<>();
        for (Map.Entry<String, AdvanceFilter> entry : filterMap.entrySet()) {
            if (entry.getValue().pass(transaction)) {
                matches.add(entry.getKey());
            }
        }
        return matches;
    }
}
