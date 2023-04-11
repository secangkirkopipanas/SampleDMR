package org.health.jboss.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class Thread extends ManagementData {

    private static final String OPERATION_JSON_FILE = "/operations/thread-operation.json";

    private static Thread INSTANCE = null;

    @JsonProperty("busy-task-thread-count")
    private Long busyTaskThreadCount;
    @JsonProperty("core-pool-size")
    private Long corePoolSize;
    @JsonProperty("io-thread-count")
    private Long ioThreadCount;
    @JsonProperty("io-threads")
    private Long ioThreads;
    @JsonProperty("max-pool-size")
    private Long maxPoolSize;
    @JsonProperty("queue-size")
    private Long queueSize;
    @JsonProperty("shutdown-requested")
    private Boolean shutdownRequested;
    @JsonProperty("stack-size")
    private Long stackSize;
    @JsonProperty("task-core-threads")
    private Long taskCoreThreads;
    @JsonProperty("task-keepalive")
    private Long taskKeepAlive;
    @JsonProperty("task-max-threads")
    private Long taskMaxThreads;
    @JsonProperty("outbound-bind-address")
    private Long outboundBindAddress;
    @JsonProperty("server")
    private Map<String, Object> server;

    public static Thread getInstance(ModelControllerClient client) throws IOException {
        if (INSTANCE == null) {
            String jsonResult = client.execute(OperationUtil.getOperationNode(OPERATION_JSON_FILE)).get("result").toJSONString(true);
            INSTANCE = JsonUtil.getObjectMapper().readValue(jsonResult, Thread.class);
        }
        return INSTANCE;
    }

}
