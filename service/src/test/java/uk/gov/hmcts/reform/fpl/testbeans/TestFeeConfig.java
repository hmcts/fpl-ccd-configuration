package uk.gov.hmcts.reform.fpl.testbeans;

import com.google.common.collect.ImmutableMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig;
import uk.gov.hmcts.reform.fpl.config.payment.FeesConfig.FeeParameters;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.APPOINTMENT_OF_GUARDIAN;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.C2_WITHOUT_NOTICE;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.C2_WITH_NOTICE;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.CARE_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.CHANGE_SURNAME;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.CHILD_ASSESSMENT;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.INTERIM_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.OTHER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.PARENTAL_RESPONSIBILITY_FATHER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.PARENTAL_RESPONSIBILITY_FEMALE_PARENT;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.PLACEMENT;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.RECOVERY_ORDER;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.SECURE_ACCOMMODATION_ENGLAND;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.SECURE_ACCOMMODATION_WALES;
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

    public static final String CHANGE_SURNAME_KEYWORD = "ZAB";
    public static final String APPOINTMENT_OF_GUARDIAN_KEYWORD = "UVW";
    public static final String CHILD_ASSESSMENT_KEYWORD = "NOP";
    public static final String RECOVERY_ORDER_KEYWORD = "Recovery";
    public static final String SECURE_ACCOMMODATION_ENG_KEYWORD = "SecureAccommEngland";
    public static final String SECURE_ACCOMMODATION_WALES_KEYWORD = "SecureAccommWales";
    public static final String PR_FATHER_KEYWORD = "XYZ";
    public static final String PR_FEMALE_PARENT_KEYWORD = "AABC";

    private static final Map<FeeType, String> feeTypeToKeyword;

    static {
        feeTypeToKeyword = new HashMap<>();
        feeTypeToKeyword.put(C2_WITHOUT_NOTICE, C2_WITHOUT_NOTICE_KEYWORD);
        feeTypeToKeyword.put(C2_WITH_NOTICE, C2_WITH_NOTICE_KEYWORD);
        feeTypeToKeyword.put(CARE_ORDER, CARE_ORDER_KEYWORD);
        feeTypeToKeyword.put(EDUCATION_SUPERVISION_ORDER, EDUCTION_SUPERVISION_ORDER_KEYWORD);
        feeTypeToKeyword.put(EMERGENCY_PROTECTION_ORDER, EMERGENCY_PROTECTION_ORDER_KEYWORD);
        feeTypeToKeyword.put(INTERIM_CARE_ORDER, INTERIM_CARE_ORDER_KEYWORD);
        feeTypeToKeyword.put(INTERIM_SUPERVISION_ORDER, INTERIM_SUPERVISION_ORDER_KEYWORD);
        feeTypeToKeyword.put(OTHER, OTHER_KEYWORD);
        feeTypeToKeyword.put(PLACEMENT, PLACEMENT_KEYWORD);
        feeTypeToKeyword.put(SUPERVISION_ORDER, SUPERVISION_ORDER_KEYWORD);

        feeTypeToKeyword.put(CHANGE_SURNAME, CHANGE_SURNAME_KEYWORD);
        feeTypeToKeyword.put(CHILD_ASSESSMENT, CHILD_ASSESSMENT_KEYWORD);
        feeTypeToKeyword.put(APPOINTMENT_OF_GUARDIAN, APPOINTMENT_OF_GUARDIAN_KEYWORD);
        feeTypeToKeyword.put(RECOVERY_ORDER, RECOVERY_ORDER_KEYWORD);
        feeTypeToKeyword.put(SECURE_ACCOMMODATION_ENGLAND, SECURE_ACCOMMODATION_ENG_KEYWORD);
        feeTypeToKeyword.put(SECURE_ACCOMMODATION_WALES, SECURE_ACCOMMODATION_WALES_KEYWORD);
        feeTypeToKeyword.put(PARENTAL_RESPONSIBILITY_FATHER, PR_FATHER_KEYWORD);
        feeTypeToKeyword.put(PARENTAL_RESPONSIBILITY_FEMALE_PARENT, PR_FEMALE_PARENT_KEYWORD);
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
