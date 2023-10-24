package util;

import model.CsvTable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CsvReaderTest {

    private static final String FILE_PATH = "src/test/resources/testFiles/Discover-Statement-20230602.csv";
    private static final String FIRST_HEADER = "Trans. Date";
    private static final String SECOND_HEADER = "Post Date";
    private static final String THIRD_HEADER = "Description";
    private static final String FOURTH_HEADER = "Amount";
    private static final String FIFTH_HEADER = "Category";

    CsvTable table;

    @Before
    public void setUp() throws Exception {
        table = CsvReader.readCSVFile(FILE_PATH);
    }

    @Test
    public void testReadCSVFile() {
        //verify headers
        assertEquals(FIRST_HEADER, table.getHeaders().get(0));
        assertEquals(SECOND_HEADER, table.getHeaders().get(1));
        assertEquals(THIRD_HEADER, table.getHeaders().get(2));
        assertEquals(FOURTH_HEADER, table.getHeaders().get(3));
        assertEquals(FIFTH_HEADER, table.getHeaders().get(4));

        //verify first row
        List<String> firstRow = new ArrayList<>(Arrays.asList("6/1/23","6/1/23", "TARGET 00023424091 CEDAR PARK TX", "50", "Supermarkets"));
        assertEquals(firstRow, table.getRows().get(0));
        assertEquals(81, table.getRows().size());
        assertEquals("\"\"", table.getRows().get(1).get(2));

    }

    @Test
    public void testEqualsInList() {
        List<List<String>> lines = new ArrayList<>();
        lines.add(List.of("apple", "orange", "banana"));
        lines.add(List.of("apple", "orange", "banana"));
        lines.add(List.of("kiwi", "grape", "melon"));
        lines.add(List.of("apple", "orange", "banana"));
        assertEquals(lines.get(0), lines.get(1));
        System.out.println("test");
        System.out.println(lines.get(0).equals(lines.get(1)));
    }

    @Test
    public void testCSVEmptyValue() {

    }
}
