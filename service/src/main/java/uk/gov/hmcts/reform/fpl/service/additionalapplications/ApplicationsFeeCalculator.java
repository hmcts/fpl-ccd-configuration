package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.fnp.exception.FeeRegisterException;
import uk.gov.hmcts.reform.fpl.enums.SecureAccommodationType;
import uk.gov.hmcts.reform.fpl.enums.Supplements;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.FeesData;
import uk.gov.hmcts.reform.fpl.model.SupplementsBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.payment.FeeService;
import uk.gov.hmcts.reform.fpl.utils.BigDecimalHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        if (caseData.getAdditionalApplicationType().size() == 2) {

            if (caseData.getTemporaryOtherApplicationsBundle() != null
                && caseData.getTemporaryOtherApplicationsBundle().getDocument() != null) {
                return getAdditionalApplicationsFee(caseData);
            }
            return emptyMap();
        }

        return getAdditionalApplicationsFee(caseData);
    }

    private Map<String, Object> getAdditionalApplicationsFee(CaseData caseData) {
        Map<String, Object> data = new HashMap<>();

        try {
            List<Element<SupplementsBundle>> supplementsBundles = mergeSupplementsBundles(caseData);

            FeesData feesData = feeService.getFeesDataForAdditionalApplications(
                caseData.getTemporaryC2Document(),
                caseData.getTemporaryOtherApplicationsBundle(),
                getSupplementsWithoutSecureAccommodationType(supplementsBundles),
                getSecureAccommodationTypes(supplementsBundles));

            data.put(AMOUNT_TO_PAY, BigDecimalHelper.toCCDMoneyGBP(feesData.getTotalAmount()));
            data.put(DISPLAY_AMOUNT_TO_PAY, YES.getValue());

        } catch (FeeRegisterException ignore) {
            data.put(DISPLAY_AMOUNT_TO_PAY, NO.getValue());
        }
        return data;
    }

    private List<Element<SupplementsBundle>> mergeSupplementsBundles(CaseData caseData) {
        List<Element<SupplementsBundle>> supplementsBundles = new ArrayList<>();

        if (caseData.getTemporaryC2Document() != null
            && isNotEmpty(caseData.getTemporaryC2Document().getSupplementsBundle())) {
            supplementsBundles.addAll(caseData.getTemporaryC2Document().getSupplementsBundle());
        }

        if (!isNull(caseData.getTemporaryOtherApplicationsBundle())
            && isNotEmpty(caseData.getTemporaryOtherApplicationsBundle().getSupplementsBundle())) {
            supplementsBundles.addAll(caseData.getTemporaryOtherApplicationsBundle().getSupplementsBundle());
        }

        return supplementsBundles;
    }

    private List<Supplements> getSupplementsWithoutSecureAccommodationType(
        List<Element<SupplementsBundle>> supplementsBundles) {

        return unwrapElements(supplementsBundles).stream()
            .map(SupplementsBundle::getName)
            .filter(name -> !Supplements.C20_SECURE_ACCOMMODATION.equals(name))
            .collect(Collectors.toList());
    }

    private List<SecureAccommodationType> getSecureAccommodationTypes(
        List<Element<SupplementsBundle>> supplementsBundles) {
        return unwrapElements(supplementsBundles).stream()
            .filter(supplement -> Supplements.C20_SECURE_ACCOMMODATION.equals(supplement.getName()))
            .map(SupplementsBundle::getSecureAccommodationType)
            .collect(Collectors.toList());
    }
}
