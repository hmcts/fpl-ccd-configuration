package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.enums.SupplementType;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.Supplement;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsFeeCalculator {

    private static final String DISPLAY_AMOUNT_TO_PAY = "displayAmountToPay";
    private static final String AMOUNT_TO_PAY = "amountToPay";

    private final FeeService feeService;

    public Map<String, Object> calculateFee(CaseData caseData) {
        if (isAllApplicationTypesProvided(caseData.getAdditionalApplicationType())) {

            if (caseData.getTemporaryOtherApplicationsBundle() != null
                && caseData.getTemporaryOtherApplicationsBundle().getDocument() != null) {
                return calculateAdditionalApplicationsFee(caseData);
            }
            return emptyMap();
        }

        return calculateAdditionalApplicationsFee(caseData);
    }

    public FeesData getFeeDataForAdditionalApplications(AdditionalApplicationsBundle applicationsBundle) {
        List<Element<Supplement>> supplementsBundle = mergeSupplementsBundles(
            applicationsBundle.getC2DocumentBundle(), applicationsBundle.getOtherApplicationsBundle());

        return feeService.getFeesDataForAdditionalApplications(
            applicationsBundle.getC2DocumentBundle(),
            applicationsBundle.getOtherApplicationsBundle(),
            getSupplementsWithoutSecureAccommodationType(supplementsBundle),
            getSecureAccommodationTypes(supplementsBundle));
    }

    private Map<String, Object> calculateAdditionalApplicationsFee(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        try {
            List<Element<Supplement>> supplementsBundle = mergeSupplementsBundles(
                caseData.getTemporaryC2Document(),
                caseData.getTemporaryOtherApplicationsBundle());

            FeesData feesData = feeService.getFeesDataForAdditionalApplications(
                caseData.getTemporaryC2Document(),
                caseData.getTemporaryOtherApplicationsBundle(),
                getSupplementsWithoutSecureAccommodationType(supplementsBundle),
                getSecureAccommodationTypes(supplementsBundle));

            data.put(AMOUNT_TO_PAY, BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
            data.put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());

        } catch (FeeRegisterException ignore) {
            data.put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        }
        return data;
    }

    private List<Element<Supplement>> mergeSupplementsBundles(
        C2DocumentBundle c2DocumentBundle, OtherApplicationsBundle otherApplicationsBundle) {
        List<Element<Supplement>> supplementsBundle = new ArrayList<>();

        if (c2DocumentBundle != null
            && isNotEmpty(c2DocumentBundle.getSupplementsBundle())) {
            supplementsBundle.addAll(c2DocumentBundle.getSupplementsBundle());
        }

        if (!isNull(otherApplicationsBundle) && isNotEmpty(otherApplicationsBundle.getSupplementsBundle())) {
            supplementsBundle.addAll(otherApplicationsBundle.getSupplementsBundle());
        }

        return supplementsBundle;
    }

    private List<SupplementType> getSupplementsWithoutSecureAccommodationType(
        List<Element<Supplement>> supplementsBundles) {

        return unwrapElements(supplementsBundles).stream()
            .map(Supplement::getName)
            .filter(name -> !SupplementType.C20_SECURE_ACCOMMODATION.equals(name))
            .collect(Collectors.toList());
    }

    private List<SecureAccommodationType> getSecureAccommodationTypes(
        List<Element<Supplement>> supplementsBundles) {
        return unwrapElements(supplementsBundles).stream()
            .filter(supplement -> SupplementType.C20_SECURE_ACCOMMODATION.equals(supplement.getName()))
            .map(Supplement::getSecureAccommodationType)
            .collect(Collectors.toList());
    }

    private boolean isAllApplicationTypesProvided(List<AdditionalApplicationType> applicationTypes) {
        return applicationTypes.containsAll(asList(AdditionalApplicationType.values()));
    }
}
