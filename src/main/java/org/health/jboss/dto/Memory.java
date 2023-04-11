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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class Memory extends ManagementData {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    private static class MemoryUsage implements Serializable {
        private Long init;
        private Long used;
        private Long committed;
        private Long max;
    }

    private static final String OPERATION_JSON_FILE = "/operations/memory-operation.json";

    private static Memory INSTANCE = null;

    @JsonProperty("heap-memory-usage")
    private MemoryUsage heap;
    @JsonProperty("non-heap-memory-usage")
    private MemoryUsage nonHeap;
    @JsonProperty("object-name")
    private String objectName;

    public static Memory getInstance(ModelControllerClient client) throws IOException {
        if (INSTANCE == null) {
            String jsonResult = client.execute(OperationUtil.getOperationNode(OPERATION_JSON_FILE)).get("result").toJSONString(true);
            INSTANCE = JsonUtil.getObjectMapper().readValue(jsonResult, Memory.class);
        }
        return INSTANCE;
    }
}
