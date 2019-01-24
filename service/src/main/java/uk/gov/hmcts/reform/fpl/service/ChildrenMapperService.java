package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Children;

import java.util.Map;
import java.util.Optional;

@Service
public class ChildrenMapperService {

    private final ObjectMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(ChildrenMapperService.class);

    @Autowired
    public ChildrenMapperService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<Children> mapChildren(final Map<String, Object> childrenMap) {
        try {
            String json = mapper.writeValueAsString(childrenMap);
            return Optional.of(mapper.readValue(json, Children.class));
        } catch (Exception e) {
            logger.error("Exception parsing children data " + e.toString());
            return Optional.empty();
        }
    }

}
