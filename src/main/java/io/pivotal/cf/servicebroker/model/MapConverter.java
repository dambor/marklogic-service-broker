package io.pivotal.cf.servicebroker.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MapConverter implements AttributeConverter<Map<String, Object>, String> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TypeReference mapOfStringObject = new TypeReference<Map<String, Object>>() {
    };

    @Override
    public String convertToDatabaseColumn(Map<String, Object> map) {
        return mapToJson(map);
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String s) {
        return stringToMap(s);
    }

    private String mapToJson(Map<String, Object> map) {
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.error("unable to convert map to json.", e);
            return "";
        }
    }

    private Map<String, Object> stringToMap(String s) {
        try {
            return mapper.readValue(s, mapOfStringObject);
        } catch (IOException e) {
            log.error("unable to convert json to map.", e);
            return new HashMap<>();
        }
    }
}
