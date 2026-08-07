package ${packageName}.service.impl;

<#list imports as imp>
import ${imp};
</#list>
import ${packageName}.service.${className}Service;
import org.springframework.stereotype.Service;

/**
 * Implementation translated from PL/SQL: ${schemaName}.${objectName}
 */
@Service
public class ${className}ServiceImpl implements ${className}Service {

    private static final Logger log = LoggerFactory.getLogger(${className}ServiceImpl.class);

<#list fields as field>
    private final ${field};
</#list>
<#if fields?has_content>

    public ${className}ServiceImpl(<#list fields as field>${field}<#sep>, </#sep></#list>) {
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
    @Override
    public ${method.returnType} ${method.methodName}(<#list method.parameters as p>${p}<#sep>, </#sep></#list>) {
        ${method.body}
    }

</#list>
}
