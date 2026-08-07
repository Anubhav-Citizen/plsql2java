package ${packageName}.dto;

/**
 * DTO for customer status response — maps to GET_CUSTOMER_STATUS function result.
 */
public class CustomerStatusDto {

    private Long customerId;
    private String status;

    public CustomerStatusDto() {}

    public CustomerStatusDto(Long customerId, String status) {
        this.customerId = customerId;
        this.status = status;
    }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
