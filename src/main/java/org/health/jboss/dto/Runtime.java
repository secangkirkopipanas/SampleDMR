package org.health.jboss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import org.health.base.ManagementData;
import org.health.util.JsonUtil;
import org.health.util.OperationUtil;
import org.jboss.as.controller.client.ModelControllerClient;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class Runtime extends ManagementData {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    private static class SystemProperties implements Serializable {
        private Long init;
        private Long used;
        private Long committed;
        private Long max;
    }

    private static final String OPERATION_JSON_FILE = "/operations/runtime-operation.json";

    private static Runtime INSTANCE = null;

    private String name;
    @JsonProperty("vm-name")
    private String vmName;
    @JsonProperty("vm-vendor")
    private String vmVendor;
    @JsonProperty("vm-version")
    private String vmVersion;
    @JsonProperty("spec-name")
    private String specName;
    @JsonProperty("spec-vendor")
    private String specVendor;
    @JsonProperty("spec-version")
    private String specVersion;
    @JsonProperty("input-arguments")
    private String[] inputArgs;
    @JsonProperty("start-time")
    private Long startTime;
    private Long uptime;
    @JsonProperty("system-properties")
    private Map<String, Object> systemProperties;
    @JsonProperty("object-name")
    private String objectName;

    public static Runtime getInstance(ModelControllerClient client) throws IOException {
        if (INSTANCE == null) {
            String jsonResult = client.execute(OperationUtil.getOperationNode(OPERATION_JSON_FILE)).get("result").toJSONString(true);
            INSTANCE = JsonUtil.getObjectMapper().readValue(jsonResult, Runtime.class);
        }
        return INSTANCE;
    }
}
