package com.sample.kyc.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for Seqkycservice operations.
 */
@Schema(description = "Response payload for Seqkycservice")
public class SeqkycserviceResponse {

    @Schema(description = "Identifier")
    private Long id;

    @Schema(description = "Name")
    private String name;

    public SeqkycserviceResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
