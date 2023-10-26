package util.cli;

import model.Transaction;
import org.junit.Assert;
import org.junit.Test;
import util.filter.AfterDateFilter;
import util.filter.TransactionFilter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CliUtilsTest {

    @Test
    public void testParseDate() throws ParseException {
        String fullFormatDate = "1/2/2023";
        String monthYearFormatDate = "2/2023";
        Date parsedFullFormatDate = CliUtils.parseDate(fullFormatDate);
        Date parsedMonthYearFormatDate = CliUtils.parseDate(monthYearFormatDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Assert.assertEquals(dateFormat.parse(fullFormatDate), parsedFullFormatDate);
        TransactionFilter filter = new AfterDateFilter(dateFormat.parse("1/1/2022"));
        Assert.assertFalse(filter.pass(new Transaction.Builder().setTransactionDate(dateFormat.parse("1/1/2022")).build()));
        Assert.assertTrue(parsedMonthYearFormatDate.equals(dateFormat.parse("2/1/2023")));
    }
}
