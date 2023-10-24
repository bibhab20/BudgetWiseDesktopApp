package util.filter;

import model.Transaction;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AdvanceFilterTest {

    AdvanceFilter advanceFilter;
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    @Test
    public void tesAfterFilterSuccess() throws ParseException {
        advanceFilter = new AdvanceFilter.Builder().addFilter(new AfterDateFilter(dateFormat.parse("7/1/23"))).build();
        List<Transaction> transactions = new ArrayList<>();
        Date passDate = dateFormat.parse("7/20/2023");
        Date failDate = dateFormat.parse("6/20/2023");
        transactions.add(new Transaction.Builder().setTransactionDate(passDate).build());
        transactions.add(new Transaction.Builder().setTransactionDate(failDate).build());
        List<Transaction> result = advanceFilter.filter(transactions);
        assertEquals(1, result.size());
        assertEquals(passDate, result.get(0).getTransactionDate());
    }
}
