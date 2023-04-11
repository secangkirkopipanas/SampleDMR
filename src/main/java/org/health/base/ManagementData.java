package org.health.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.health.util.JsonUtil;

import java.io.Serializable;

public class ManagementData implements Serializable {

    public String toJsonString(boolean minified) {
        try {
            if (minified) {
                return JsonUtil.getObjectMapper().writeValueAsString(this);
            } else {
                return JsonUtil.getObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
