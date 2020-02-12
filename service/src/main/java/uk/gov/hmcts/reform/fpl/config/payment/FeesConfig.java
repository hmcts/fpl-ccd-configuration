package uk.gov.hmcts.reform.fpl.config.payment;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.payment.fee.FeeParameters;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.OTHER;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.PLACEMENT;
import static uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeType.SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.config.utils.LookupConfigParser.parseStringValue;

@Configuration
@Slf4j
public class FeesConfig {

    private final Map<FeeType, FeeParameters> orderFeeParameters;
    @Getter
    private final String url;
    @Getter
    private final String api;

    public FeesConfig(@Value("${fee.url}") String url,
                      @Value("${fee.api}") String api,
                      @Value("${fee.types.c2_with_notice}") String c2WithNotice,
                      @Value("${fee.types.c2_without_notice}") String c2WithoutNotice,
                      @Value("${fee.types.care_order}") String careOrder,
                      @Value("${fee.types.education_supervision_order}") String educationSupervisionOrder,
                      @Value("${fee.types.emergency_protection_order}") String emergencyProtectionOrder,
                      @Value("${fee.types.interim_care_order}") String interimCareOrder,
                      @Value("${fee.types.interim_supervision_order}") String interimSupervisionOrder,
                      @Value("${fee.types.other}") String other,
                      @Value("${fee.types.placement}") String placement,
                      @Value("${fee.types.supervision_order}") String superVisionOrder) {

        this.orderFeeParameters = new HashMap<>();
        this.url = url;
        this.api = api;

        addFeeParameters(C2_WITH_NOTICE, parseStringValue(c2WithNotice));
        addFeeParameters(C2_WITHOUT_NOTICE, parseStringValue(c2WithoutNotice));
        addFeeParameters(CARE_ORDER, parseStringValue(careOrder));
        addFeeParameters(EDUCATION_SUPERVISION_ORDER, parseStringValue(educationSupervisionOrder));
        addFeeParameters(EMERGENCY_PROTECTION_ORDER, parseStringValue(emergencyProtectionOrder));
        addFeeParameters(INTERIM_CARE_ORDER, parseStringValue(interimCareOrder));
        addFeeParameters(INTERIM_SUPERVISION_ORDER, parseStringValue(interimSupervisionOrder));
        addFeeParameters(OTHER, parseStringValue(other));
        addFeeParameters(PLACEMENT, parseStringValue(placement));
        addFeeParameters(SUPERVISION_ORDER, parseStringValue(superVisionOrder));
    }

    private void addFeeParameters(FeeType feeType, Map<String, String> map) {
        FeeParameters feeParameters = FeeParameters.builder()
            .channel(map.getOrDefault("channel", ""))
            .event(map.getOrDefault("event", ""))
            .jurisdiction1(map.getOrDefault("jurisdiction1", ""))
            .jurisdiction2(map.getOrDefault("jurisdiction2", ""))
            .keyword(map.getOrDefault("keyword", ""))
            .service(map.getOrDefault("service", ""))
            .build();

        log.info("{} => {}", feeType, feeParameters);

        this.orderFeeParameters.put(feeType, feeParameters);
    }

    public FeeParameters getFeeParameters(FeeType feeType) {
        return orderFeeParameters.get(feeType);
    }

    public enum FeeType {
        C2_WITHOUT_NOTICE,
        C2_WITH_NOTICE,
        CARE_ORDER,
        EDUCATION_SUPERVISION_ORDER,
        EMERGENCY_PROTECTION_ORDER,
        INTERIM_CARE_ORDER,
        INTERIM_SUPERVISION_ORDER,
        OTHER,
        PLACEMENT,
        SUPERVISION_ORDER;

        public static FeeType fromOrderType(OrderType orderType) {
            return FeeType.valueOf(orderType.name());
        }
    }
}
