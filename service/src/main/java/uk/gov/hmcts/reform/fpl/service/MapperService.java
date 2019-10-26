package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.MappingException;

import java.util.Map;

@Service
public class MapperService {

    private final ObjectMapper mapper;

    @Autowired
    public MapperService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T mapObject(Map<String, Object> map, Class<T> valueType) {
        try {
            String json = mapper.writeValueAsString(map);
            return mapper.readValue(json, valueType);
        } catch (Exception ex) {
            throw new MappingException(ex);
        }
    }
}
