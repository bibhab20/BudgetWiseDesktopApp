package config;

import lombok.Getter;
import util.TransactionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Getter
public class ReaderConfig {
    private String version = null;
    private List<String> headers = null;
    private Map<String, TransactionUtil.TransactionProperty> columnMapping = null;
    private Map<TransactionUtil.TransactionProperty, String> hardMapping = null;
    public ReaderConfig(String version, List<String> headers, Map<String, TransactionUtil.TransactionProperty> columnMapping) {
        this.version = version;
        this.headers = headers;
        this.columnMapping = columnMapping;
        this.hardMapping = new HashMap<>();
    }

    public ReaderConfig() {
    }

    public ReaderConfig withVersion(String version) {
        return this;
    }

    public ReaderConfig withHeaders(List<String> headers) {
        return this;
    }

    public ReaderConfig withColumnMapping(Map<String, TransactionUtil.TransactionProperty> columnMapping) {
        return this;
    }

    public ReaderConfig withHardMapping(TransactionUtil.TransactionProperty hardMapping) {
        return this;
    }
}
