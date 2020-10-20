package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.OrderLookupException;
import uk.gov.hmcts.reform.fpl.model.configuration.OrderDefinition;

import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readString;

@Service
public class JsonOrdersLookupService implements OrdersLookupService {
    private static final String ORDERS_CONFIG_FILENAME = "ordersConfig.json";
    private final ObjectMapper objectMapper;

    @Autowired
    public JsonOrdersLookupService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OrderDefinition getStandardDirectionOrder() {
        String content = readString(ORDERS_CONFIG_FILENAME);

        try {
            return this.objectMapper.readValue(content, OrderDefinition.class);
        } catch (JsonProcessingException e) {
            throw new OrderLookupException("Could not read file " + ORDERS_CONFIG_FILENAME);
        }
    }
}
