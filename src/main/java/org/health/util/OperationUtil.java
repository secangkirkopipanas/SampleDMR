package org.health.util;

import org.jboss.dmr.ModelNode;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class OperationUtil {

    public static ModelNode getOperationNode(String operationJsonFile) throws IOException {
        String content = null;
        try {
            content = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(OperationUtil.class.getResource(operationJsonFile)).toURI())));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        return ModelNode.fromJSONString(content);
    }
}
