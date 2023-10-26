package util.filter;

import model.Transaction;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class BeforeDateFilterTest {

    BeforeDateFilter filter;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    @Test
    public void testSuccess() throws ParseException {
        filter = new BeforeDateFilter(dateFormat.parse("7/1/23"));
        Transaction transaction = new Transaction.Builder().setTransactionDate(dateFormat.parse("6/30/23")).build();
        assertTrue(filter.pass(transaction));
    }

    @Test
    public void testSuccessWithSameDate() throws ParseException {
        filter = new BeforeDateFilter(dateFormat.parse("7/1/2023"));
        Transaction transaction = new Transaction.Builder().setTransactionDate(dateFormat.parse("7/1/23")).build();
        assertTrue(filter.pass(transaction));
    }

    @Test
    public void testFailure() throws ParseException {
        filter = new BeforeDateFilter(dateFormat.parse("7/1/2023"));
        Transaction transaction = new Transaction.Builder().setTransactionDate(dateFormat.parse("7/2/23")).build();
        assertFalse(filter.pass(transaction));
    }

}
