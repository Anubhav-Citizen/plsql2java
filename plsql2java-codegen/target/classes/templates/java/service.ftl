package ${packageName}.service;

<#list imports as imp>
import ${imp};
</#list>

/**
 * Translated from PL/SQL: ${schemaName}.${objectName}
 */
@Service
public class ${className}Service {

    private static final Logger log = LoggerFactory.getLogger(${className}Service.class);

<#list fields as field>
    private final ${field};
</#list>
<#if fields?has_content>

    public ${className}Service(<#list fields as field>${field}<#sep>, </#sep></#list>) {
<#list fieldNames as name>
        this.${name} = ${name};
</#list>
    }
</#if>

<#list methods as method>
    /**
     * Translated from PL/SQL: ${schemaName}.${objectName}.${method.methodName}
     */
<#list method.annotations as ann>
    ${ann}
</#list>
<#if method.belowThreshold>
    // @ConfidenceScore(${method.confidenceScore}%) — manual review recommended
</#if>
    public ${method.returnType} ${method.methodName}(<#list method.parameters as p>${p}<#sep>, </#sep></#list>) {
        ${method.body}
    }

</#list>
}
