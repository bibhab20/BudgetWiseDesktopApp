package service.supplier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ReaderConfig;
import config.VendorConfig;
import lombok.extern.slf4j.Slf4j;
import util.AppConfig;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
public class JsonReaderConfigSupplier implements ReaderConfigSupplier{
    AppConfig appConfig;
    Optional<ReaderConfig> readerConfig;

    private static final String READER_CONFIG_JSON_PATH = "reader.config.json.path";

    public JsonReaderConfigSupplier(AppConfig config) {
        this.appConfig = config;
        this.readerConfig = loadConfigFromJson();
    }
    @Override
    public Optional<ReaderConfig> get() {

        return this.readerConfig;
    }

    private Optional<ReaderConfig> loadConfigFromJson() {
        ObjectMapper objectMapper = new ObjectMapper();
        File jsonFile = new File(appConfig.getProperties().getProperty(READER_CONFIG_JSON_PATH));
        ReaderConfig readerConfig = null;

        try {
            readerConfig = objectMapper.readValue(jsonFile, new TypeReference<ReaderConfig>() {});
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (readerConfig != null) {
            log.info("Readers config loaded");
        }
        return Optional.ofNullable(readerConfig);
    }
}
