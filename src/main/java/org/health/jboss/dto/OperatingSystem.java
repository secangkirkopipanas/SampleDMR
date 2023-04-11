package org.health.jboss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.health.base.ManagementData;
import org.health.util.JsonUtil;
import org.health.util.OperationUtil;
import org.jboss.as.controller.client.ModelControllerClient;

import java.io.IOException;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class OperatingSystem extends ManagementData {

    private static final String OPERATION_JSON_FILE = "/operations/os-operation.json";

    private static OperatingSystem INSTANCE = null;

    private String name;
    private String arch;
    private String version;
    @JsonProperty("available-processors")
    private String availableProcessors;
    @JsonProperty("system-load-average")
    private Double systemLoadAverage;
    @JsonProperty("object-name")
    private String objectName;

    public static OperatingSystem getInstance(ModelControllerClient client) throws IOException {
        if (INSTANCE == null) {
            String jsonResult = client.execute(OperationUtil.getOperationNode(OPERATION_JSON_FILE)).get("result").toJSONString(true);
            INSTANCE = JsonUtil.getObjectMapper().readValue(jsonResult, OperatingSystem.class);
        }
        return INSTANCE;
    }
}
