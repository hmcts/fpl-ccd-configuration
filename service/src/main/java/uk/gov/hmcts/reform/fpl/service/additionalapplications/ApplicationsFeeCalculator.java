package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fnp.model.fee.FeeType;
import uk.gov.hmcts.reform.fpl.enums.AdditionalApplicationType;
import uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
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
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromApplicationType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromC2ApplicationType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromC2OrdersRequestedType;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromParentalResponsibilityTypes;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromSecureAccommodationTypes;
import static uk.gov.hmcts.reform.fnp.model.fee.FeeType.fromSupplementTypes;
import static uk.gov.hmcts.reform.fpl.enums.C2AdditionalOrdersRequested.PARENTAL_RESPONSIBILITY;
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

        if (isAllApplicationsSpecified(caseData.getAdditionalApplicationType())) {
            if (isAllApplicationsUploaded(caseData)) {
                return calculateAdditionalApplicationsFee(caseData);
            }
            return emptyMap();
        }

        return calculateAdditionalApplicationsFee(caseData);
    }

    private Map<String, Object> calculateAdditionalApplicationsFee(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        try {
            final List<FeeType> feeTypes = getFeeTypes(caseData.getTemporaryC2Document(),
                caseData.getTemporaryOtherApplicationsBundle());

            final FeesData feesData = feeService.getFeesDataForAdditionalApplications(feeTypes);

            data.put(AMOUNT_TO_PAY, BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
            data.put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());
        } catch (FeeRegisterException ignore) {
            data.put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        }
        return data;
    }

    public FeesData getFeeDataForAdditionalApplications(AdditionalApplicationsBundle applicationsBundle) {
        final List<FeeType> feeTypes = getFeeTypes(
            applicationsBundle.getC2DocumentBundle(),
            applicationsBundle.getOtherApplicationsBundle());

        return feeService.getFeesDataForAdditionalApplications(feeTypes);
    }

    private List<Element<Supplement>> mergeSupplementsBundles(
        C2DocumentBundle c2DocumentBundle, OtherApplicationsBundle otherApplicationsBundle) {
        List<Element<Supplement>> supplementsBundle = new ArrayList<>();

        if (isNotEmpty(c2DocumentBundle)) {
            ofNullable(c2DocumentBundle.getSupplementsBundle()).ifPresent(supplementsBundle::addAll);
        }

        if (isNotEmpty(otherApplicationsBundle)) {
            ofNullable(otherApplicationsBundle.getSupplementsBundle()).ifPresent(supplementsBundle::addAll);
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

    private boolean isAllApplicationsSpecified(List<AdditionalApplicationType> applicationTypes) {
        return applicationTypes.containsAll(asList(AdditionalApplicationType.values()));
    }

    private boolean isAllApplicationsUploaded(CaseData caseData) {
        return caseData.getTemporaryOtherApplicationsBundle() != null
            && caseData.getTemporaryOtherApplicationsBundle().getDocument() != null;
    }

    private List<FeeType> getFeeTypes(C2DocumentBundle c2Bundle, OtherApplicationsBundle otherBundle) {
        List<FeeType> feeTypes = new ArrayList<>();

        if (!isNull(c2Bundle)) {
            feeTypes.addAll(getC2ApplicationsFeeTypes(c2Bundle));
        }

        if (!isNull(otherBundle)) {
            feeTypes.addAll(getOtherApplicationsFeeTypes(otherBundle));
        }

        final List<Element<Supplement>> supplementsBundle = mergeSupplementsBundles(c2Bundle, otherBundle);
        List<SupplementType> supplementTypes = getSupplementsWithoutSecureAccommodationType(supplementsBundle);
        List<SecureAccommodationType> secureAccommodationTypes = getSecureAccommodationTypes(supplementsBundle);

        feeTypes.addAll(fromSupplementTypes(supplementTypes));
        feeTypes.addAll(fromSecureAccommodationTypes(secureAccommodationTypes));

        return feeTypes;
    }

    private List<FeeType> getC2ApplicationsFeeTypes(C2DocumentBundle c2DocumentBundle) {
        List<FeeType> feeTypes = new ArrayList<>();

        feeTypes.add(fromC2ApplicationType(c2DocumentBundle.getType()));

        List<C2AdditionalOrdersRequested> c2AdditionalOrdersRequested
            = new ArrayList<>(defaultIfNull(c2DocumentBundle.getC2AdditionalOrdersRequested(), emptyList()));

        if (isNotEmpty(c2AdditionalOrdersRequested)) {
            if (c2AdditionalOrdersRequested.contains(PARENTAL_RESPONSIBILITY)) {
                c2AdditionalOrdersRequested.remove(PARENTAL_RESPONSIBILITY);
                feeTypes.add(fromParentalResponsibilityTypes(c2DocumentBundle.getParentalResponsibilityType()));
            }

            feeTypes.addAll(fromC2OrdersRequestedType(c2AdditionalOrdersRequested));
        }
        return feeTypes;
    }

    private List<FeeType> getOtherApplicationsFeeTypes(OtherApplicationsBundle applicationsBundle) {
        List<FeeType> feeTypes = new ArrayList<>();

        if (OtherApplicationType.C1_PARENTAL_RESPONSIBILITY == applicationsBundle.getApplicationType()) {
            feeTypes.add(fromParentalResponsibilityTypes(applicationsBundle.getParentalResponsibilityType()));
        } else {
            fromApplicationType(applicationsBundle.getApplicationType()).ifPresent(feeTypes::add);
        }

        return feeTypes;
    }
}
