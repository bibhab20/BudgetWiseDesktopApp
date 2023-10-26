package util.cli;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CliUtils {
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

}
