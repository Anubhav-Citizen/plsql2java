package ${packageName}.controller;

import ${packageName}.service.${serviceClassName};
import ${packageName}.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for ${resourceName}.
 * Translated from PL/SQL: ${schemaName}.${objectName}
 */
@RestController
@RequestMapping("/${resourcePath}")
@Tag(name = "${resourceName}", description = "Operations for ${resourceName}")
public class ${serviceClassName}Controller {

    private final ${serviceClassName} service;

    public ${serviceClassName}Controller(${serviceClassName} service) {
        this.service = service;
    }

<#list methods as method>
    @Operation(summary = "${method.summary}")
    <#if method.readOnly>
    @GetMapping("/${method.path}")
    public ResponseEntity<${method.responseType}Response> ${method.methodName}(<#list method.parameters as p>@RequestParam ${p}<#sep>, </#sep></#list>) {
        return ResponseEntity.ok(service.${method.methodName}(<#list method.paramNames as n>${n}<#sep>, </#sep></#list>));
    }
    <#else>
    @PostMapping("/${method.path}")
    public ResponseEntity<${method.responseType}Response> ${method.methodName}(@Valid @RequestBody ${method.responseType}Request request) {
        return ResponseEntity.ok(service.${method.methodName}(request));
    }
    </#if>

</#list>
}
