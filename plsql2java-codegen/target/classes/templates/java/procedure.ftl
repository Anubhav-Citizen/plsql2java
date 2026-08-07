package ${packageName}.procedure;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Wrapper for PL/SQL package ${className}.
 * Translated from Oracle PKG_${className?upper_case}.
 */
@Component
public class ${className}Package {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public ${className}Package(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * Translated from PKG_${className?upper_case}.ADD_${className?upper_case}.
     * INSERT INTO ${className?upper_case} (CUSTOMER_NAME, EMAIL, PHONE, ANNUAL_INCOME) VALUES (...)
     */
    @Transactional
    public void add${className}(String name, String email, String phone, BigDecimal income) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_${className?upper_case}")
                .withProcedureName("ADD_${className?upper_case}");
        call.execute(Map.of(
                "P_NAME", name,
                "P_EMAIL", email,
                "P_PHONE", phone,
                "P_INCOME", income
        ));
    }

    /**
     * Translated from PKG_${className?upper_case}.UPDATE_STATUS.
     * UPDATE ${className?upper_case} SET STATUS=P_STATUS WHERE ${className?upper_case}_ID=P_${className?upper_case}_ID
     */
    @Transactional
    public void updateStatus(Long id, String status) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_${className?upper_case}")
                .withProcedureName("UPDATE_STATUS");
        call.execute(Map.of("P_${className?upper_case}_ID", id, "P_STATUS", status));
    }

    /**
     * Translated from PKG_${className?upper_case}.GET_TOTAL_${className?upper_case}S.
     * SELECT COUNT(*) INTO V_COUNT FROM ${className?upper_case}
     */
    public Long getTotalCount() {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_${className?upper_case}")
                .withFunctionName("GET_TOTAL_${className?upper_case}S");
        return call.executeFunction(Long.class, Map.of());
    }

    /**
     * Translated from PKG_${className?upper_case}.GET_${className?upper_case}_STATUS.
     * SELECT STATUS INTO V_STATUS FROM ${className?upper_case} WHERE ${className?upper_case}_ID=P_${className?upper_case}_ID
     * EXCEPTION WHEN NO_DATA_FOUND THEN RETURN 'NOT FOUND'
     */
    public String get${className}Status(Long id) {
        SimpleJdbcCall call = new SimpleJdbcCall(dataSource)
                .withCatalogName("PKG_${className?upper_case}")
                .withFunctionName("GET_${className?upper_case}_STATUS");
        return call.executeFunction(String.class, Map.of("P_${className?upper_case}_ID", id));
    }
}
