package config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import util.AppConfig;

import java.io.File;
import java.util.*;

@Slf4j
public class CategoryConfigSupplier {
    AppConfig config;
    VendorTypeConfigSupplier vendorTypeConfigSupplier;
    private final List<CategoryConfig> categoryConfigs;
    private static final String CATEGORY_CONFIG_JSON_PATH = "category.config.json.path";

    public CategoryConfigSupplier(AppConfig config, VendorTypeConfigSupplier vendorTypeConfigSupplier) {
        this.config = config;
        this.vendorTypeConfigSupplier = vendorTypeConfigSupplier;
        categoryConfigs = loadCategoryConfigFromJson();
        categoryConfigs.add(getCategoryConfigsOfUncategorizedVendorTypes());
        validateCategoryConfigs();

    }

    public List<CategoryConfig> get() {
        return categoryConfigs;
    }
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (CategoryConfig config: this.categoryConfigs) {
            names.add(config.getName());
        }
        return names;
    }

    public List<String> getSortedNames() {
        List<String> names = this.getNames();
        names.sort(String::compareTo);
        return names;
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

    private boolean validateCategoryConfigs() {
        boolean pass = true;
        Set<String> categoryConfigSet = new HashSet<>();
        for (CategoryConfig config: categoryConfigs) {
            if (categoryConfigSet.contains(config.getName())) {
                log.error("Duplicate category name found. Category name: {}", config.getName());
                pass = false;
            } else {
                categoryConfigSet.add(config.getName());
            }
        }
        Map<String, List<String>> duplicates = getDuplicateVendorTypes(this.categoryConfigs);
        for (String vendorType: duplicates.keySet()) {
            if (duplicates.get(vendorType).size() > 1) {
                log.error("Vendor Type is in multiple categories. Vendor Type: {} | Categories: {}", vendorType,
                        toCommaDelimitedString(duplicates.get(vendorType)));
                pass = false;
            }
        }
        return pass;
    }

    private Map<String, List<String>> getDuplicateVendorTypes(List<CategoryConfig> categoryConfigs) {
        Map<String, List<String>> duplicates = new HashMap<>();
        for (CategoryConfig config: categoryConfigs) {
            for (String vendorType: config.getVendorTypes()) {
                duplicates.put(vendorType, duplicates.getOrDefault(vendorType, new ArrayList<>()));
                duplicates.get(vendorType).add(config.getName());
            }
        }
        return duplicates;
    }

    private List<String> getUnCategorizedVendorTypes(List<CategoryConfig> categoryConfigs){
        List<String> result = new ArrayList<>();
        Set<String> vendorTypeSet = new HashSet<>();
        for (CategoryConfig config: categoryConfigs) {
            vendorTypeSet.addAll(config.getVendorTypes());
        }
        for (String vendorType: vendorTypeConfigSupplier.getVendorTypes()) {
            if (!vendorTypeSet.contains(vendorType)) {
                result.add(vendorType);
            }
        }

        return result;
    }

    private CategoryConfig getCategoryConfigsOfUncategorizedVendorTypes() {
        List<String> vendorTypes = getUnCategorizedVendorTypes(this.categoryConfigs);
        log.info("Adding the following vendor types to UNCATEGORIZED category {}", toCommaDelimitedString(vendorTypes));
        CategoryConfig config = new CategoryConfig();
        config.setVendorTypes(vendorTypes);
        config.setName("UNCATEGORIZED");
        return config;
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
}

