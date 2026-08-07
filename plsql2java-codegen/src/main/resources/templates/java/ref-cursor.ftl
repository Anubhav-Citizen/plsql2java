package ${packageName}.cursor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Handles Oracle SYS_REFCURSOR patterns translated to Spring JDBC queries.
 * Translated from PL/SQL REF CURSOR / GET_CUSTOMERS procedure.
 */
@Service
public class RefCursorService {

    private final JdbcTemplate jdbcTemplate;

    public RefCursorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Replaces SYS_REFCURSOR: OPEN P_CURSOR FOR SELECT * FROM CUSTOMER.
     * Returns all rows as a list of maps; replace with a typed RowMapper as needed.
     */
    public List<Map<String, Object>> getCustomers() {
        return jdbcTemplate.queryForList("SELECT * FROM CUSTOMER");
    }

    /**
     * Generic ref-cursor executor — supply any SELECT query and RowMapper.
     */
    public <T> List<T> executeRefCursor(String sql, RowMapper<T> rowMapper, Object... args) {
        return jdbcTemplate.query(sql, rowMapper, args);
    }
}
