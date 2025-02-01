package util.enrichment;

import config.VendorConfig;
import config.VendorConfigSupplier;
import lombok.extern.slf4j.Slf4j;
import model.Transaction;
import util.AppConfig;
import util.filter.AdvanceFilter;
import util.filter.ContainsDescriptionFilter;
import util.filter.NotContainDescriptionFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class VendorEnricher implements TransactionEnricher {
    private static final String VENDOR_MATCHES_HEADER_KEY = "transaction.header.vendor.matches";
    AppConfig config;
    Map<String, AdvanceFilter> filterMap;
    Map<String, VendorConfig> vendorConfigMap;
    VendorConfigSupplier configSupplier;

    public VendorEnricher(AppConfig config, VendorConfigSupplier configSupplier) {
        this.config = config;
        this.configSupplier = configSupplier;
        try {
            filterMap = getVendorFilters(configSupplier.get());
            vendorConfigMap = getVendorMap(configSupplier.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public List<Transaction> enrich(List<Transaction> transactions) {
        int unknownCount =  0, errorCount = 0, successCount=0;
        for (Transaction transaction: transactions) {
            List<String> vendors = getMatches(transaction);
            if (vendors.size() == 0) {
                log.warn("No match found for transaction : {}", transaction);
                transaction.setVendor("UNKNOWN");
                transaction.setVendorType("UNKNOWN");
                transaction.addAdditionalDetails(config.getProperties().getProperty(VENDOR_MATCHES_HEADER_KEY), "NONE");
                unknownCount++;
            } else if (vendors.size() > 1) {
                log.error("Multiple vendor matches the transaction with id: {} and the matches are {}", transaction.getId(), vendors);
                transaction.setVendor("ERROR");
                transaction.setVendorType("ERROR");
                transaction.addAdditionalDetails(config.getProperties().getProperty(VENDOR_MATCHES_HEADER_KEY), toCommaDelimitedString(vendors));
                errorCount++;
            }
            else {
                successCount++;
                transaction.setVendor(vendors.get(0));
                transaction.setVendorType(vendorConfigMap.get(vendors.get(0)).getVendorType());
                transaction.addAdditionalDetails(config.getProperties().getProperty(VENDOR_MATCHES_HEADER_KEY), vendors.get(0));
                transaction.getEnrichmentList().add(this.getClass().getName());
            }
        }
        log.info("{} transactions successfully enriched with vendor data",successCount);
        log.info("{} transactions have not matching vendor",unknownCount);
        log.info("{} transactions have multiple vendor matches",errorCount);
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


    private Map<String, AdvanceFilter> getVendorFilters(List<VendorConfig> vendorConfigs) throws Exception {
        //printVendorConfigs(vendorConfigs);
        Map<String, AdvanceFilter> vendorFilters = new HashMap<>();
        for (VendorConfig vendorConfig: vendorConfigs) {
            AdvanceFilter filter = new AdvanceFilter.Builder().addFilter(new ContainsDescriptionFilter(vendorConfig.getContains()))
                    .addFilter(new NotContainDescriptionFilter(vendorConfig.getNotContain())).build();
            if (vendorFilters.containsKey(vendorConfig.getName())) {
                log.error("Duplicate vendors found in vendor config, name: {}", vendorConfig.getName());
                throw new Exception("Duplicate vendor names");
            }
            vendorFilters.put(vendorConfig.getName(), filter);
        }
        return vendorFilters;
    }

    private Map<String, VendorConfig> getVendorMap(List<VendorConfig> vendorConfigs) {
        Map<String, VendorConfig> map = new HashMap<>();
        for (VendorConfig config: vendorConfigs) {
            map.put(config.getName(), config);
        }
        return map;
    }

    private List<String> getMatches(Transaction transaction) {
        List<String> vendorNames = new ArrayList<>();
        for (Map.Entry<String, AdvanceFilter> entry : filterMap.entrySet()) {
            if (entry.getValue().pass(transaction)) {
                vendorNames.add(entry.getKey());
            }
        }
        return vendorNames;
    }


    private void printVendorConfigs(List<VendorConfig> vendorConfigs) {
        for (VendorConfig config: vendorConfigs) {
            log.info("--------Printing config for vendor: {}-------",config.getName());
            log.info("Vendor contains string: {}",config.getContains());
            log.info("Vendor notContain string: {}",config.getNotContain());
        }
    }
}
