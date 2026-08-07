package ${packageName}.service;

<#list imports as imp>
import ${imp};
</#list>

/**
 * Service interface translated from PL/SQL: ${schemaName}.${objectName}
 */
public interface ${className}Service {

<#list methods as method>
    /**
     * Translated from PL/SQL: ${schemaName}.${objectName}.${method.methodName}
     */
    ${method.returnType} ${method.methodName}(<#list method.parameters as p>${p}<#sep>, </#sep></#list>);

</#list>
}
