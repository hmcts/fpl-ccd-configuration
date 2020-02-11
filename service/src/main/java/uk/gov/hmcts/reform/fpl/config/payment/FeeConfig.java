package uk.gov.hmcts.reform.fpl.config.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.payment.fees.FeeParameters;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser.parseStringValue;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.SUPERVISION_ORDER;

@Configuration
@Slf4j
public class FeeConfig {

    private Map<OrderType, FeeParameters> orderFeeParameters;
    @Getter
    private final String url;
    @Getter
    private final String api;

    @Autowired
    public FeeConfig(ObjectMapper mapper) {
        this.orderFeeParameters = new HashMap<>();
        String dummy = "channel=>default;"
            + "service=>private law;"
            + "jurisdiction1=>family;"
            + "jurisdiction2=>family court;"
            + "event=>miscellaneous;"
            + "keyword=>TUV";
        addFeeParameters(CARE_ORDER, parseStringValue(dummy), mapper);
        addFeeParameters(EDUCATION_SUPERVISION_ORDER, parseStringValue(dummy), mapper);
        addFeeParameters(EMERGENCY_PROTECTION_ORDER, parseStringValue(dummy), mapper);
        addFeeParameters(INTERIM_CARE_ORDER, parseStringValue(dummy), mapper);
        addFeeParameters(INTERIM_SUPERVISION_ORDER, parseStringValue(dummy), mapper);
        addFeeParameters(OTHER, parseStringValue(dummy), mapper);
        addFeeParameters(SUPERVISION_ORDER, parseStringValue(dummy), mapper);
        url = "http://fees-register-api-aat.service.core-compute-aat.internal";
        api = "/fees-register/fees/lookup";
    }

    private void addFeeParameters(OrderType orderType, Map<String, String> rawFeeParameters, ObjectMapper mapper) {
        FeeParameters feeParameters = mapper.convertValue(rawFeeParameters, FeeParameters.class);
        // log.info("%s => %s", orderType.name(), feeParameters.toString());
        this.orderFeeParameters.put(orderType, feeParameters);
    }

    public FeeParameters getFeeParameters(OrderType orderType) {
        return orderFeeParameters.get(orderType);
    }
}
