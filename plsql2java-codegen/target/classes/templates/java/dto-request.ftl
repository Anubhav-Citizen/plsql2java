package ${packageName}.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for ${baseName} operations.
 */
@Schema(description = "Request payload for ${baseName}")
public class ${baseName}Request {

<#list fields as field>
    @Schema(description = "${field.description}")
    <#if field.required>
    @NotNull
    </#if>
    <#if field.type == "String">
    @NotBlank
    @Size(max = ${field.maxLength!255})
    </#if>
    private ${field.type} ${field.name};

</#list>
    public ${baseName}Request() {}

<#list fields as field>
    public ${field.type} get${field.name?cap_first}() { return ${field.name}; }
    public void set${field.name?cap_first}(${field.type} ${field.name}) { this.${field.name} = ${field.name}; }

</#list>
}
