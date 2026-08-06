package com.sample.kyc.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for Trgcustomerservice operations.
 */
@Schema(description = "Response payload for Trgcustomerservice")
public class TrgcustomerserviceResponse {

    @Schema(description = "Identifier")
    private Long id;

    @Schema(description = "Name")
    private String name;

    public TrgcustomerserviceResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
