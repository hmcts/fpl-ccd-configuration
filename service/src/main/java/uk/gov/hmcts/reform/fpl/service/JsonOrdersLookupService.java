package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.exceptions.StandardDirectionNotFoundException;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.UncheckedIOException;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@Slf4j
@Service
public class JsonOrdersLookupService implements OrdersLookupService {
    private static final String ORDERS_CONFIG_FILENAME = "ordersConfig.json";
    private final ObjectMapper objectMapper;
    private OrderDefinition orderDefinition;

    @Autowired
    public JsonOrdersLookupService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void init() {
        try {
            String content = readString(ORDERS_CONFIG_FILENAME);
            orderDefinition = this.objectMapper.readValue(content, OrderDefinition.class);
        } catch (JsonProcessingException e) {
            log.error("Could not read file " + ORDERS_CONFIG_FILENAME);
            throw new UncheckedIOException(e);
        }
    }

    public OrderDefinition getStandardDirectionOrder() {
        return orderDefinition;
    }

    public DirectionConfiguration getDirectionConfiguration(DirectionType directionType) {
        return getStandardDirectionOrder().getDirections().stream()
            .filter(directionConfig -> directionConfig.getId().equals(directionType))
            .findFirst()
            .orElseThrow(() -> new StandardDirectionNotFoundException(directionType));
    }
}
