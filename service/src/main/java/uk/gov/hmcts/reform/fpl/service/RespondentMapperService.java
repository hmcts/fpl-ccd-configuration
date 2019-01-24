package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.Respondents;

import java.util.Map;
import java.util.Optional;

@Service
public class RespondentMapperService {

    private final ObjectMapper mapper;
    private final Logger logger = LoggerFactory.getLogger(RespondentMapperService.class);

    @Autowired
    public RespondentMapperService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<Respondents> mapRespondents(final Map<String, Object> respondentsMaps) {
        try {
            String json = mapper.writeValueAsString(respondentsMaps);
            return Optional.of(mapper.readValue(json, Respondents.class));
        } catch (Exception e) {
            logger.error("Exception parsing respondents data " + e.toString());
            return Optional.empty();
        }
    }

}
