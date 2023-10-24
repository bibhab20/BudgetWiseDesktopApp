package config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import util.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class CategoryConfigSupplier {
    AppConfig config;
    private List<CategoryConfig> categoryConfigs;
    private static final String CATEGORY_CONFIG_JSON_PATH = "category.config.json.path";

    public CategoryConfigSupplier(AppConfig config) {
        this.config = config;
        categoryConfigs = loadCategoryConfigFromJson();
    }

    public List<CategoryConfig> get() {
        return categoryConfigs;
    }

    private List<CategoryConfig> loadCategoryConfigFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(config.getProperties().getProperty(CATEGORY_CONFIG_JSON_PATH));
        List<CategoryConfig> categoryConfigs = null;
        try {
            categoryConfigs = objectMapper.readValue(jsonFile, new TypeReference<List<CategoryConfig>>() {});
            assert categoryConfigs != null;

            for (CategoryConfig categoryConfig: categoryConfigs) {
                if(!validateCategoryConfig(categoryConfig)) {
                    log.error("CategoryConfig validation failed for category: {}", categoryConfig.getName());
                    throw new Exception("Category Validation Failed");
                }
            }
            log.info("{} vendor configs loaded", categoryConfigs.size());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return categoryConfigs;
    }

    private boolean validateCategoryConfig(CategoryConfig categoryConfig) {
        boolean isVendorsPresent = false, isVendorTypesPresent = false;
        if (categoryConfig.getVendors() != null && categoryConfig.getVendors().size() > 0) {
            isVendorsPresent = true;
        }

        if (categoryConfig.getVendorTypes() != null && categoryConfig.getVendorTypes().size() > 0) {
            isVendorTypesPresent = true;
        }

        if (isVendorsPresent && isVendorTypesPresent) {
            log.error("Both vendor types and vendors are present in the category config, name: {}. Only one is allowed", categoryConfig.getName());
            return false;
        }
        if (!isVendorsPresent && !isVendorTypesPresent) {
            log.error("Neither vendor types nor vendors are present in the category config, name: {}. At least one is required", categoryConfig.getName());
            return false;
        }

        return true;
    }
}
