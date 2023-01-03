package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ConfirmApplicationReviewedService {
    public static final String ADDITIONAL_APPLICATION_REVIEWED = "ADDITIONAL_APPLICATION_REVIEWED";

    public Map<String, Object> initEventField(CaseData caseData) {
        Map<String, Object> resultMap = new HashMap<>();

        final List<Element<AdditionalApplicationsBundle>> applicationsBundlesToBeReviewed =
            getApplicationsToBeReviewed(caseData);

        if (isEmpty(applicationsBundlesToBeReviewed)) {
            resultMap.put("hasApplicationToBeReviewed", NO);
        } else {
            resultMap.put("confirmApplicationReviewedList",
                asDynamicList(applicationsBundlesToBeReviewed, AdditionalApplicationsBundle::toLabel));
            resultMap.put("hasApplicationToBeReviewed", YES);
        }

        return resultMap;
    }

    private List<Element<AdditionalApplicationsBundle>> getApplicationsToBeReviewed(CaseData caseData) {
        return caseData.getAdditionalApplicationsBundle().stream()
            .filter(bundleElement -> !YES.equals(bundleElement.getValue().getApplicationReviewed()))
            .collect(Collectors.toList());
    }

    public Element<AdditionalApplicationsBundle> getSelectedApplicationsToBeReviewed(CaseData caseData) {
        final List<Element<AdditionalApplicationsBundle>> applicationsBundlesToBeReviewed =
            getApplicationsToBeReviewed(caseData);

        ConfirmApplicationReviewedEventData eventData = caseData.getConfirmApplicationReviewedEventData();

        return findElement(eventData.getConfirmApplicationReviewedList().getValueCodeAsUUID(),
            applicationsBundlesToBeReviewed).orElseThrow();
    }

    public List<Element<AdditionalApplicationsBundle>> markSelectedBundleAsReviewed(CaseData caseData) {
        ConfirmApplicationReviewedEventData eventData = caseData.getConfirmApplicationReviewedEventData();
        List<Element<AdditionalApplicationsBundle>> additionalApplications = caseData.getAdditionalApplicationsBundle();

        Element<AdditionalApplicationsBundle> selectedApplication =
            findElement(eventData.getConfirmApplicationReviewedList().getValueCodeAsUUID(),
                additionalApplications).orElseThrow();

        return additionalApplications.stream().map(existingBundle -> {
                if (selectedApplication.getId().equals(existingBundle.getId())) {
                    return element(selectedApplication.getId(),
                        selectedApplication.getValue().toBuilder().applicationReviewed(YES).build());
                }
                return existingBundle;
            }
        ).collect(Collectors.toList());
    }
}
