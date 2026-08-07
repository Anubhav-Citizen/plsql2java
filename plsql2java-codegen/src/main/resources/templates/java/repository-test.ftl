package ${packageName}.repository;

import ${packageName}.entity.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void save_andFindById_returnsCustomer() {
        Customer customer = new Customer();
        customer.setCustomerName("Test Customer");
        customer.setEmail("test@example.com");
        customer.setStatus("ACTIVE");

        Customer saved = customerRepository.save(customer);
        Optional<Customer> found = customerRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCustomerName()).isEqualTo("Test Customer");
    }

    @Test
    void findAll_returnsAllCustomers() {
        assertThat(customerRepository.findAll()).isNotNull();
    }
}
