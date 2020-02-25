package uk.gov.hmcts.reform.fpl.testbeans;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeParameters;

import java.util.Arrays;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.CARE_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.OTHER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.PLACEMENT;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.SUPERVISION_ORDER;

@Configuration
public class TestFeeConfig {
    public static final String CHANNEL = "default";
    public static final String EVENT = "miscellaneous";
    public static final String JURISDICTION_1 = "family";
    public static final String JURISDICTION_2 = "family court";
    public static final String SERVICE = "private law";

    public static final String C2_WITHOUT_NOTICE_KEYWORD = "CWON";
    public static final String C2_WITH_NOTICE_KEYWORD = "CWN";
    public static final String CARE_ORDER_KEYWORD = "CO";
    public static final String EDUCTION_SUPERVISION_ORDER_KEYWORD = "ESO";
    public static final String EMERGENCY_PROTECTION_ORDER_KEYWORD = "EPO";
    public static final String INTERIM_CARE_ORDER_KEYWORD = "ICO";
    public static final String INTERIM_SUPERVISION_ORDER_KEYWORD = "ISO";
    public static final String OTHER_KEYWORD = "O";
    public static final String PLACEMENT_KEYWORD = "P";
    public static final String SUPERVISION_ORDER_KEYWORD = "SO";

    private static final Map<FeeType, String> feeTypeToKeyword;

    static {
        feeTypeToKeyword = Map.of(
            C2_WITHOUT_NOTICE, C2_WITHOUT_NOTICE_KEYWORD,
            C2_WITH_NOTICE, C2_WITH_NOTICE_KEYWORD,
            CARE_ORDER, CARE_ORDER_KEYWORD,
            EDUCATION_SUPERVISION_ORDER, EDUCTION_SUPERVISION_ORDER_KEYWORD,
            EMERGENCY_PROTECTION_ORDER, EMERGENCY_PROTECTION_ORDER_KEYWORD,
            INTERIM_CARE_ORDER, INTERIM_CARE_ORDER_KEYWORD,
            INTERIM_SUPERVISION_ORDER, INTERIM_SUPERVISION_ORDER_KEYWORD,
            OTHER, OTHER_KEYWORD,
            PLACEMENT, PLACEMENT_KEYWORD,
            SUPERVISION_ORDER, SUPERVISION_ORDER_KEYWORD
        );
    }

    @ConditionalOnMissingBean
    @Bean
    public FeesConfig feesConfig() {
        FeesConfig config = new FeesConfig();

        config.setParameters(Arrays.stream(FeeType.values())
            .collect(collectingAndThen(toMap(feeType -> feeType, this::getFeeParameters), ImmutableMap::copyOf)));

        return config;
    }

    private FeeParameters getFeeParameters(FeeType feeType) {
        return new FeeParameters(
            CHANNEL,
            EVENT,
            JURISDICTION_1,
            JURISDICTION_2,
            feeTypeToKeyword.get(feeType),
            SERVICE
        );
    }
}
