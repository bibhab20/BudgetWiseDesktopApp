package service;

import config.VendorConfigSupplier;
import model.Transaction;
import org.junit.Before;
import org.junit.Test;
import util.AppConfig;
import util.enrichment.VendorEnricher;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class VendorEnricherTest {

    private static final String ATT_MARKET_PLACE_DESCRIPTION = "MERCHANT PURCHASE TERMINAL 469216 ATT BILL PAYMENT 800 288 2 TX                      06-14- 23                                SEQ # 316521109429";
    private static final String AMAZON_MARKET_PLACE_DESCRIPTION_1 = "WALMART SC 02991 CEDAR PARK TX";
    private static final String AMAZON_MARKET_PLACE_DESCRIPTION_2 = "POS PURCHASE TERMINAL 24416301 WAL-MART #4163 CEDAR PAR TX                      02-08- 23   2:27 PM                      SEQ # 08283500";
    private static final String WALMART_VENDOR_NAME = "Walmart";
    VendorEnricher enricher;
    VendorConfigSupplier configSupplier;


    @Before
    public void setUp() {
        AppConfig config = new AppConfig("test");
        configSupplier = new VendorConfigSupplier(config);
        this.enricher = new VendorEnricher(config, configSupplier);
    }

    @Test
    public void testSuccessfulMultipleContainStringEnrichment() {
        List<Transaction> enrichedTransactions = enricher.enrich(getWalmartTransactions());
        assertEquals(WALMART_VENDOR_NAME, enrichedTransactions.get(0).getVendor());
        assertEquals(WALMART_VENDOR_NAME, enrichedTransactions.get(1).getVendor());
    }

    @Test
    public void testMultipleVendorMatch() {
        Transaction transaction1 = new Transaction.Builder().setDescription("WALMART Chowrasta").build();
        List<Transaction> enrichedTransactions = enricher.enrich(List.of(transaction1));
        assertEquals( "ERROR", enrichedTransactions.get(0).getVendor());

    }

    private List<Transaction> getWalmartTransactions() {
        Transaction walmartTransaction1 = new Transaction.Builder().setDescription(AMAZON_MARKET_PLACE_DESCRIPTION_1).build();
        Transaction walmartTransaction2 = new Transaction.Builder().setDescription(AMAZON_MARKET_PLACE_DESCRIPTION_2).build();
        List<Transaction> transactions = new ArrayList<>(List.of(walmartTransaction1, walmartTransaction2));
        return transactions;
    }

}
