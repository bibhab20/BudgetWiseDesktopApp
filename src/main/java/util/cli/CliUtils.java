package util.cli;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CliUtils {

    /**
     * Parses date string smart.
     * @param dateString String entered for date by the CLI user
     * @return Date object after parsing
     */
    public static Date parseDate(String dateString) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM/yyyy");

        try {
            // Attempt to parse as mm/dd/yyyy
            Date date1 = sdf1.parse(dateString);
            if (date1 != null) {
                return date1;
            }
        } catch (ParseException e1) {
            try {
                // Attempt to parse as mm/yyyy
                Date date2 = sdf2.parse(dateString);
                if (date2 != null) {
                    return date2;
                }
            } catch (ParseException e2) {
                // Unable to parse in either format
                return null;
            }
        }

        return null;
    }

    public static String getCliGrid(List<String> strings, int columns) {
        if (strings == null || strings.isEmpty() || columns <= 0) {
            return "";
        }

        int maxStringLength = 0;

        // Find the length of the longest string
        for (String str : strings) {
            maxStringLength = Math.max(maxStringLength, str.length());
        }

        // Calculate the column width based on the longest string
        int columnWidth = maxStringLength + 2; // Add some spacing between columns

        StringBuilder result = new StringBuilder();

        int rows = (int) Math.ceil((double) strings.size() / columns);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                int index = row + col * rows;
                if (index < strings.size()) {
                    String str = strings.get(index);
                    result.append(String.format("%-" + columnWidth + "s", str));

                    if (col < columns - 1) {
                        // Add some spacing between columns
                        result.append("  ");
                    }
                }
            }
            result.append("\n");
        }

        return result.toString();
    }

    private static Date getLastDayOfMonth(Date inputDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(inputDate);

        // Set the day of the month to the last day of the month
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Reset the time fields to midnight
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static String toCommaDelimitedString(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s: list) {
            if (stringBuilder.length() != 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    public static List<String> getListFromCommaSeparatedString(String input) {
        String[] arr = input.trim().split(",");
        List<String> result = new ArrayList<>();
        for (String s: arr) {
            if (!s.isBlank()) {
                result.add(s);
            }
        }
        return result;
    }


}
