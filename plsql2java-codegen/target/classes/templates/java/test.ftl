package ${packageName}.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ${className}Service.
 * Translated from PL/SQL: ${schemaName}.${objectName}
 */
@ExtendWith(MockitoExtension.class)
class ${className}ServiceTest {

<#list mockFields as field>
    @Mock
    private ${field};
</#list>

    @InjectMocks
    private ${className}Service service;

<#list methods as method>
    @Test
    void ${method.methodName}_shouldExecuteSuccessfully() {
        // TODO: configure mocks and assert expected behaviour
        // Translated from: ${schemaName}.${objectName}.${method.methodName}
        assertThat(service).isNotNull();
    }

</#list>
}
