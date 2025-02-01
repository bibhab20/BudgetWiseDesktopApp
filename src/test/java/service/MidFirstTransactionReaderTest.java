package service;

import model.Transaction;
import model.TransactionSource;
import model.TransactionType;
import org.junit.Before;
import org.junit.Test;
import service.reader.MidFirstTransactionReader;
import util.AppConfig;

import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MidFirstTransactionReaderTest {

    MidFirstTransactionReader processor;

    @Before
    public void setUp() {
        AppConfig config = new AppConfig("test");
        this.processor = new MidFirstTransactionReader(config);
    }

    @Test
    public void testTransactionCreation() throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

        List<Transaction> transactions = processor.readAndProcessRecords();
        assertEquals(56, transactions.size());
        Transaction firstTransaction = transactions.get(0);
        assertEquals(dateFormat.parse("7/28/23"), firstTransaction.getTransactionDate());
        assertEquals(TransactionType.CREDIT, firstTransaction.getType());
        assertEquals("VMware, Inc.-OSV 0000390835 230728", firstTransaction.getDescription());
        assertEquals(TransactionSource.MIDFIRST, firstTransaction.getSource());
        assertEquals(4069.39, firstTransaction.getAmount(), 0.00);
        assertEquals("ACH DEPOSIT", firstTransaction.getAdditionalDetails().get("Channel"));

    }
}
