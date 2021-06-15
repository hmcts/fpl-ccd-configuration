package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.DirectionType;
import uk.gov.hmcts.reform.fpl.exceptions.OrderDefinitionNotFoundException;
import uk.gov.hmcts.reform.fpl.model.configuration.DirectionConfiguration;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import java.io.UncheckedIOException;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@Slf4j
@Service
public class JsonOrdersLookupService implements OrdersLookupService {
    private static final String ORDERS_CONFIG_FILENAME = "ordersConfig.json";
    private OrderDefinition cachedOrderDefinition;

    @Autowired
    public JsonOrdersLookupService(ObjectMapper objectMapper) {
        try {
            String content = readString(ORDERS_CONFIG_FILENAME);
            cachedOrderDefinition = objectMapper.readValue(content, OrderDefinition.class);
        } catch (JsonProcessingException e) {
            log.error("Could not read file " + ORDERS_CONFIG_FILENAME);
            throw new UncheckedIOException(e);
        }
    }

    public OrderDefinition getStandardDirectionOrder() {
        return cachedOrderDefinition;
    }

    public DirectionConfiguration getDirectionConfiguration(DirectionType directionType) {
        return getConfiguration(directionType)
            .orElseThrow(() -> new OrderDefinitionNotFoundException(directionType));
    }


    private Optional<DirectionConfiguration> getConfiguration(DirectionType directionType) {
        if (directionType.isStandard()) {
            return getStandardDirectionOrder().getStandardDirections().stream()
                .filter(directionConfig -> directionConfig.getType().equals(directionType))
                .findFirst();
        }

        return ofNullable(getStandardDirectionOrder().getCustomDirection());
    }
}
