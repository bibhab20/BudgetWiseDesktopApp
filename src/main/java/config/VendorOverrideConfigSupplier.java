package config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import util.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class VendorOverrideConfigSupplier {
    AppConfig appConfig;
    List<VendorOverrideConfig> configs;
    private static final String VENDOR_OVERRIDE_CONFIG_JSON_PATH = "vendor.override.config.json.path";

    public VendorOverrideConfigSupplier(AppConfig appConfig) {
        this.appConfig = appConfig;
        loadConfigsFromJson();
    }

    public List<VendorOverrideConfig> get() {
        return this.configs;
    }

    private void loadConfigsFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(appConfig.getProperties().getProperty(VENDOR_OVERRIDE_CONFIG_JSON_PATH));
        List<VendorOverrideConfig> vendorOverrideConfigs = null;
        try {
            vendorOverrideConfigs = objectMapper.readValue(jsonFile, new TypeReference<List<VendorOverrideConfig>>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert vendorOverrideConfigs != null;
        log.info("{} vendor override configs loaded", vendorOverrideConfigs.size());
        this.configs = vendorOverrideConfigs;
    }
}
