package config;

import lombok.extern.slf4j.Slf4j;
import util.AppConfig;

import java.util.*;

@Slf4j
public class VendorTypeConfigSupplier {
    private AppConfig config;
    private VendorConfigSupplier vendorConfigSupplier;

    public VendorTypeConfigSupplier(AppConfig config, VendorConfigSupplier vendorConfigSupplier) {
        this.config = config;
        this.vendorConfigSupplier = vendorConfigSupplier;
    }

    public List<String> getVendorTypes() {
        List<VendorConfig> vendorConfigs = vendorConfigSupplier.get();
        Set<String> vendorTypes = new HashSet<>();
        for (VendorConfig config: vendorConfigs) {
            if (config.getVendorType() == null || config.getVendorType().isEmpty()) {
                log.error("Vendor Type found empty or null : {}", config.getName());
            }
            vendorTypes.add(config.getVendorType());
        }
        List<String> result = new ArrayList<>(vendorTypes);
        Collections.sort(result);

        return result;
    }

    public Map<String, List<VendorConfig>> getVendorTypeConfigs() {
        List<VendorConfig> vendorConfigs = vendorConfigSupplier.get();
        Map<String, List<VendorConfig>> map = new HashMap<>();
        for (VendorConfig config: vendorConfigs) {
            if (config.getVendorType() == null || config.getVendorType().isEmpty()) {
                log.error("Vendor Type found empty or null : {}", config.getName());
            }
            List<VendorConfig> list = map.getOrDefault(config.getVendorType(), new ArrayList<>());
            list.add(config);
            map.put(config.getVendorType(), list);

        }
        return map;
    }


}
