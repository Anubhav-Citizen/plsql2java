package ${packageName}.batch;

import ${packageName}.entity.Customer;
import ${packageName}.repository.CustomerRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Translated from Oracle BULK COLLECT / FORALL pattern.
 */
@Component
public class BulkCustomerUpdater {

    private final JdbcTemplate jdbcTemplate;
    private final CustomerRepository customerRepository;

    public BulkCustomerUpdater(JdbcTemplate jdbcTemplate, CustomerRepository customerRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.customerRepository = customerRepository;
    }

    /**
     * Translated from: SELECT ... BULK COLLECT INTO collection FROM CUSTOMER
     */
    public List<Customer> bulkCollect() {
        return customerRepository.findAll();
    }

    /**
     * Translated from: FORALL i IN collection.FIRST..collection.LAST UPDATE CUSTOMER ...
     */
    @Transactional
    public void forallUpdate(List<Customer> customers) {
        customerRepository.saveAll(customers);
    }

    /**
     * Translated from: FORALL with batchUpdate pattern
     */
    @Transactional
    public void batchUpdate(List<Object[]> params) {
        jdbcTemplate.batchUpdate("UPDATE CUSTOMER SET STATUS=? WHERE CUSTOMER_ID=?", params);
    }
}
