package ${packageName}.batch;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles Oracle BULK COLLECT / FORALL patterns translated to Spring JDBC batch operations.
 * Translated from PL/SQL BULK COLLECT INTO and FORALL UPDATE constructs.
 */
@Service
public class BulkUpdateService {

    private final JdbcTemplate jdbcTemplate;

    public BulkUpdateService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Replaces BULK COLLECT: SELECT CUSTOMER_NAME BULK COLLECT INTO V_NAMES FROM CUSTOMER.
     */
    public List<String> bulkFetchCustomerNames() {
        return jdbcTemplate.queryForList("SELECT CUSTOMER_NAME FROM CUSTOMER", String.class);
    }

    /**
     * Replaces FORALL: UPDATE CUSTOMER SET STATUS='ACTIVE' WHERE CUSTOMER_ID = ids(i).
     * Uses Spring JDBC batch update for efficiency.
     */
    @Transactional
    public int[] bulkUpdateStatus(List<Long> ids, String status) {
        return jdbcTemplate.batchUpdate(
                "UPDATE CUSTOMER SET STATUS = ? WHERE CUSTOMER_ID = ?",
                ids,
                ids.size(),
                (ps, id) -> {
                    ps.setString(1, status);
                    ps.setLong(2, id);
                }
        );
    }
}
