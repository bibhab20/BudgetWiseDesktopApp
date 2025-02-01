package service.supplier;

import config.ReaderConfig;
import util.AppConfig;

import java.util.Optional;

public interface ReaderConfigSupplier {
    Optional<ReaderConfig> get();
}
