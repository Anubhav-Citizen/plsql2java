package ${packageName}.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response DTO for ${baseName} operations.
 */
@Schema(description = "Response payload for ${baseName}")
public class ${baseName}Response {

<#list fields as field>
    @Schema(description = "${field.description}")
    private ${field.type} ${field.name};

</#list>
    public ${baseName}Response() {}

<#list fields as field>
    public ${field.type} get${field.name?cap_first}() { return ${field.name}; }
    public void set${field.name?cap_first}(${field.type} ${field.name}) { this.${field.name} = ${field.name}; }

</#list>
}
