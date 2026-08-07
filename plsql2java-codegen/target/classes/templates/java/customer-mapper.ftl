package ${packageName}.mapper;

import ${packageName}.dto.CustomerDto;
import ${packageName}.entity.Customer;
import org.springframework.stereotype.Component;

/**
 * Maps between Customer entity and CustomerDto.
 */
@Component
public class CustomerMapper {

    public CustomerDto toDto(Customer customer) {
        if (customer == null) return null;
        CustomerDto dto = new CustomerDto();
        dto.setId(customer.getId());
        dto.setCustomerName(customer.getCustomerName());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setStatus(customer.getStatus());
        dto.setAnnualIncome(customer.getAnnualIncome());
        return dto;
    }

    public Customer toEntity(CustomerDto dto) {
        if (dto == null) return null;
        Customer customer = new Customer();
        customer.setCustomerName(dto.getCustomerName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());
        customer.setStatus(dto.getStatus());
        customer.setAnnualIncome(dto.getAnnualIncome());
        return customer;
    }
}
