package controller;

import config.VendorTypeConfigSupplier;
import lombok.extern.slf4j.Slf4j;
import model.CsvTable;
import util.AppConfig;
import util.CsvWriter;

import java.util.List;

@Slf4j
public class VendorController {
    private static final String OUTPUT_FILE_PATH = "vendor-types.output.path";

    VendorTypeConfigSupplier vendorTypeConfigSupplier;
    AppConfig config;
    CsvWriter writer;

    public VendorController(VendorTypeConfigSupplier vendorTypeConfigSupplier, AppConfig config, CsvWriter writer) {
        this.vendorTypeConfigSupplier = vendorTypeConfigSupplier;
        this.config = config;
        this.writer = writer;
    }

    public void writeAllVendorTypesToCsv() throws Exception {
        List<String> vendorTypes = vendorTypeConfigSupplier.getVendorTypes();
        CsvTable table = new CsvTable();
        table.setHeaders(List.of("Vendor Types"));
        for (String vendorType: vendorTypes) {
            table.addRow(List.of(vendorType));
        }
        writer.writeToFile(table, config.getProperties().getProperty(OUTPUT_FILE_PATH));
    }

}
