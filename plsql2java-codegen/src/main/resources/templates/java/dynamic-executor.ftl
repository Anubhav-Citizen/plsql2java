package ${packageName}.dynamic;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Translated from Oracle EXECUTE IMMEDIATE dynamic SQL pattern.
 */
@Component
public class DynamicSqlExecutor {

    private final JdbcTemplate jdbcTemplate;

    public DynamicSqlExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Translated from: EXECUTE IMMEDIATE sql_stmt USING bind_var
     * Uses parameterized query to prevent SQL injection.
     */
    @Transactional
    public void execute(String sql, Object... params) {
        jdbcTemplate.update(sql, params);
    }

    /**
     * Translated from: EXECUTE IMMEDIATE 'SELECT ...' INTO var USING bind_var
     */
    public <T> T executeQuery(String sql, Class<T> type, Object... params) {
        return jdbcTemplate.queryForObject(sql, type, params);
    }
}
