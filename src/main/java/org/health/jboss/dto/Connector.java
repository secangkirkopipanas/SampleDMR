package org.health.jboss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;
import java.util.Map;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class Connector extends ManagementData {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Jacksonized
    private static class Port implements Serializable {
        private List<Map<String, Object>> address;
        private String outcome;
        private Map<String, Object> result;
    }

    private static final String OPERATION_JSON_FILE = "/operations/connector-operation.json";

    private static Connector INSTANCE = null;

    private List<Port> ports;

    public static Connector getInstance(ModelControllerClient client) throws IOException {
        if (INSTANCE == null) {
            String jsonResult = client.execute(OperationUtil.getOperationNode(OPERATION_JSON_FILE)).get("result").toJSONString(true);
            INSTANCE = new Connector(JsonUtil.getObjectMapper().readValue(jsonResult, new TypeReference<List<Port>>() {}));
        }
        return INSTANCE;
    }
}
