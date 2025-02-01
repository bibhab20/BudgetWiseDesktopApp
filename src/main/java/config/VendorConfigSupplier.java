package config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import util.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class VendorConfigSupplier {
    AppConfig config;
    private List<VendorConfig> vendorConfigs;
    private static final String VENDOR_CONFIG_JSON_PATH = "vendor.config.json.path";

    public VendorConfigSupplier(AppConfig config) {
        this.config = config;
        vendorConfigs = loadVendorConfigFromJson();
    }

    public List<VendorConfig> get() {
        return vendorConfigs;
    }
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (VendorConfig config: this.vendorConfigs) {
            names.add(config.getName());
        }
        return names;
    }

    private List<VendorConfig> loadVendorConfigFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(config.getProperties().getProperty(VENDOR_CONFIG_JSON_PATH));
        List<VendorConfig> vendorConfigs = null;
        try {
            vendorConfigs = objectMapper.readValue(jsonFile, new TypeReference<>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert vendorConfigs != null;
        log.info("{} vendor configs loaded", vendorConfigs.size());
        return vendorConfigs;
    }
}
