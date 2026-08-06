package com.sample.kyc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for Seqcustomerservice operations.
 */
@Schema(description = "Request payload for Seqcustomerservice")
public class SeqcustomerserviceRequest {

    @Schema(description = "Identifier")
    @NotNull
    private Long id;

    @Schema(description = "Name")
    @NotNull
    @NotBlank
    @Size(max = 255)
    private String name;

    public SeqcustomerserviceRequest() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
