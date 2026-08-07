package ${packageName}.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JDBC type conversion utilities.
 * Replaces Oracle SYSDATE / TO_DATE / TO_CHAR patterns.
 */
public final class JdbcUtil {

    private JdbcUtil() {}

    public static LocalDate toLocalDate(Date sqlDate) {
        return sqlDate != null ? sqlDate.toLocalDate() : null;
    }

    public static LocalDateTime toLocalDateTime(Timestamp ts) {
        return ts != null ? ts.toLocalDateTime() : null;
    }

    public static Date toSqlDate(LocalDate date) {
        return date != null ? Date.valueOf(date) : null;
    }

    public static Timestamp toTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? Timestamp.valueOf(dateTime) : null;
    }

    /** Equivalent of Oracle SYSDATE */
    public static LocalDateTime sysdate() {
        return LocalDateTime.now();
    }
}
