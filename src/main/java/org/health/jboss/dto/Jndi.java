package org.health.jboss.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.health.base.ManagementData;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class Jndi extends ManagementData {

    private static final String OPERATION_JSON_FILE = "/operations/jndi-operation.json";

    private String name;
    private String jndiName;
    private Boolean enabled;
    private Boolean connected;

}
