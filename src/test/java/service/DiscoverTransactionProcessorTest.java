package service;

import model.Transaction;
import model.TransactionSource;
import model.TransactionType;
import org.junit.Before;
import org.junit.Test;
import util.AppConfig;

import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DiscoverTransactionProcessorTest {

    DiscoverTransactionProcessor processor;

    @Before
    public void setUp() {
        AppConfig config = new AppConfig("test");
        this.processor = new DiscoverTransactionProcessor(config);
    }

    @Test
    public void testEmptyField() throws Exception {
        List<Transaction> transactions = processor.readAndProcessRecords();
        assertEquals("NONE", transactions.get(1).getDescription());
    }

    @Test
    public void testTransactionCreation() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

        List<Transaction> transactions = processor.readAndProcessRecords();
        assertEquals(80, transactions.size());
        Transaction firstTransaction = transactions.get(0);
        assertEquals(dateFormat.parse("06/01/23"), firstTransaction.getTransactionDate());
        assertEquals(TransactionType.DEBIT, firstTransaction.getType());
        assertEquals("TARGET 00023424091 CEDAR PARK TX", firstTransaction.getDescription());
        assertEquals(TransactionSource.DISCOVER, firstTransaction.getSource());
        assertEquals("Supermarkets", firstTransaction.getAdditionalDetails().get("Suggested Category"));
    }
}
