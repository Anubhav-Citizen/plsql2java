package ${packageName}.dynamic;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles Oracle EXECUTE IMMEDIATE (dynamic SQL) patterns translated to Spring JDBC.
 * Translated from PL/SQL EXECUTE IMMEDIATE constructs.
 */
@Service
public class DynamicSqlService {

    private final JdbcTemplate jdbcTemplate;

    public DynamicSqlService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Replaces: EXECUTE IMMEDIATE 'UPDATE CUSTOMER SET STATUS=''ACTIVE'' WHERE STATUS=''INACTIVE'''
     * Parameterised to avoid SQL injection.
     */
    @Transactional
    public int activateInactiveCustomers() {
        return jdbcTemplate.update(
                "UPDATE CUSTOMER SET STATUS = ? WHERE STATUS = ?",
                "ACTIVE", "INACTIVE"
        );
    }

    /**
     * Generic dynamic update — use with caution; prefer typed methods above.
     * Only call with trusted, validated SQL strings.
     */
    @Transactional
    public int executeDynamic(String sql, Object... params) {
        return jdbcTemplate.update(sql, params);
    }
}
