package ${packageName}.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Utility methods replacing Oracle date functions (SYSDATE, date arithmetic).
 */
public final class DateUtils {

    private DateUtils() {}

    /** Equivalent of Oracle SYSDATE. */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /** Converts java.util.Date (from JDBC) to LocalDate. */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Converts LocalDate to java.util.Date for legacy JDBC use. */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
