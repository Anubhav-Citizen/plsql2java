package com.example.migrated.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for Emppkgservice operations.
 */
@Schema(description = "Response payload for Emppkgservice")
public class EmppkgserviceResponse {

    @Schema(description = "Identifier")
    private Long id;

    @Schema(description = "Name")
    private String name;

    public EmppkgserviceResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

}
