package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MapperService {

    private final ObjectMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(MapperService.class);

    @Autowired
    public MapperService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public <T> T mapObject(final Map<String, Object> respondentsMaps, Class<T> valueType) throws Exception {
        try {
            String json = mapper.writeValueAsString(respondentsMaps);
            return mapper.readValue(json, valueType);
        } catch (Exception e) {
            logger.error("Exception mapping " + e.toString());
            throw new Exception(e);
        }
    }
}
