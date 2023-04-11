package org.health.jboss.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.*;
import org.health.base.ManagementData;
import org.health.util.JsonUtil;
import org.health.util.OperationUtil;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class DataSource extends ManagementData {

    private static final String DS_OPERATION_JSON_FILE = "/operations/ds-operation.json";
    private static final String XA_DS_OPERATION_JSON_FILE = "/operations/xa-ds-operation.json";

    private static final String JNDI_OPERATION_JSON_FILE = "/operations/jndi-operation.json";
    private static final String JNDI_STATUS_OPERATION_JSON_FILE = "/operations/jndi-status-operation.json";
    private static final String JNDI_CONNECTED_OPERATION_JSON_FILE = "/operations/jndi-connected-operation.json";

    private static DataSource INSTANCE = null;

    @JsonIgnore
    private List<String> dsNames = new ArrayList<>();
    @JsonIgnore
    private List<String> xaDsNames = new ArrayList<>();

    private List<Jndi> dataSources = null;
    private List<Jndi> xaDataSources = null;

    private DataSource(List<String> dsNames, List<String> xaDsNames) {
        this.dsNames = dsNames;
        this.xaDsNames = xaDsNames;
    }

    public static DataSource getInstance(ModelControllerClient client) throws IOException {
        if (INSTANCE == null) {
            String dsJsonResult =
                    client.execute(OperationUtil.getOperationNode(DS_OPERATION_JSON_FILE)).get("result").toJSONString(true);
            String xaDsJsonResult =
                    client.execute(OperationUtil.getOperationNode(XA_DS_OPERATION_JSON_FILE)).get("result").toJSONString(true);

            List<String> dsNames = JsonUtil.getObjectMapper().readValue(dsJsonResult, new TypeReference<List<String>>() {});
            List<String> xaDsNames = JsonUtil.getObjectMapper().readValue(xaDsJsonResult, new TypeReference<List<String>>() {});

            INSTANCE = new DataSource(dsNames, xaDsNames);
        }
        return INSTANCE;
    }

    public void getJndiDetails(ModelControllerClient client) throws IOException {
        dsNames.forEach(
                dsName -> {
                    dataSources = new ArrayList<>();
                    String dsType = "data-source";
                    String result = "result";
                    try {
                        // To get JNDI name of datasource
                        ModelNode jndiOp = OperationUtil.getOperationNode(JNDI_OPERATION_JSON_FILE);
                        jndiOp.get("address").add(dsType, dsName);
                        String dsJndiJsonResult = client.execute(jndiOp).get(result).toJSONString(true).replaceAll("\"","");

                        // To get status of datasource
                        ModelNode jndiStatusOp = OperationUtil.getOperationNode(JNDI_STATUS_OPERATION_JSON_FILE);
                        jndiStatusOp.get("address").add(dsType, dsName);
                        Boolean dsJndiStats =
                                client.execute(jndiStatusOp).get(result).asBoolean(false);

                        // To get connection status of datasource
                        ModelNode jndiConnectedOp = OperationUtil.getOperationNode(JNDI_CONNECTED_OPERATION_JSON_FILE);
                        jndiConnectedOp.get("address").add(dsType, dsName);
                        Boolean dsJndiConnected =
                                client.execute(jndiConnectedOp).get(result).asBoolean(false);

                        dataSources.add(new Jndi(dsName, dsJndiJsonResult, dsJndiStats, dsJndiConnected));

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );

        xaDsNames.forEach(
                dsName -> {
                    xaDataSources = new ArrayList<>();
                    String dsType = "xa-data-source";
                    String result = "result";
                    try {
                        // To get JNDI name of datasource
                        ModelNode jndiOp = OperationUtil.getOperationNode(JNDI_OPERATION_JSON_FILE);
                        jndiOp.get("address").add(dsType, dsName);
                        String dsJndiJsonResult = client.execute(jndiOp).get(result).toJSONString(true).replaceAll("\"","");

                        // To get status of datasource
                        ModelNode jndiStatusOp = OperationUtil.getOperationNode(JNDI_STATUS_OPERATION_JSON_FILE);
                        jndiStatusOp.get("address").add(dsType, dsName);
                        Boolean dsJndiStats =
                                client.execute(jndiStatusOp).get(result).asBoolean(false);

                        // To get connection status of datasource
                        ModelNode jndiConnectedOp = OperationUtil.getOperationNode(JNDI_CONNECTED_OPERATION_JSON_FILE);
                        jndiConnectedOp.get("address").add(dsType, dsName);
                        Boolean dsJndiConnected =
                                client.execute(jndiConnectedOp).get(result).asBoolean(false);

                        xaDataSources.add(new Jndi(dsName, dsJndiJsonResult, dsJndiStats, dsJndiConnected));

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
