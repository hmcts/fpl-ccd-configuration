package uk.gov.hmcts.reform.fpl.service.additionalapplications;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ConfirmApplicationReviewedEventData;

import java.util.ArrayList;
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
public class ReviewAdditionalApplicationService {
    public static final String ONLY_ONE_APPLICATION = "onlyOneApplicationToBeReviewed";

    public Map<String, Object> initEventField(CaseData caseData) {
        Map<String, Object> resultMap = new HashMap<>();

        final List<Element<AdditionalApplicationsBundle>> applicationsBundlesToBeReviewed =
            getApplicationsToBeReviewed(caseData);

        if (isEmpty(applicationsBundlesToBeReviewed)) {
            resultMap.put("hasApplicationToBeReviewed", NO);
            resultMap.put(ONLY_ONE_APPLICATION, NO);
        } else {
            resultMap.put("hasApplicationToBeReviewed", YES);

            if (applicationsBundlesToBeReviewed.size() > 1) {
                resultMap.put("additionalApplicationToBeReviewedList", asDynamicList(applicationsBundlesToBeReviewed,
                    AdditionalApplicationsBundle::toLabel));
                resultMap.put(ONLY_ONE_APPLICATION, NO);
            } else if (applicationsBundlesToBeReviewed.size() == 1) {
                resultMap.put(ONLY_ONE_APPLICATION, YES);
                resultMap.put("additionalApplicationsBundleToBeReviewed",
                    applicationsBundlesToBeReviewed.get(0).getValue());
            }
        }

        return resultMap;
    }

    private List<Element<AdditionalApplicationsBundle>> getApplicationsToBeReviewed(CaseData caseData) {
        if (caseData.getAdditionalApplicationsBundle() != null) {
            return caseData.getAdditionalApplicationsBundle().stream()
                .filter(bundleElement -> !YES.equals(bundleElement.getValue().getApplicationReviewed()))
                .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }
    }

    public Element<AdditionalApplicationsBundle> getSelectedApplicationsToBeReviewed(CaseData caseData) {
        final List<Element<AdditionalApplicationsBundle>> applicationsBundlesToBeReviewed =
            getApplicationsToBeReviewed(caseData);

        if (applicationsBundlesToBeReviewed.size() == 1) {
            return applicationsBundlesToBeReviewed.get(0);
        } else {
            ConfirmApplicationReviewedEventData eventData = caseData.getConfirmApplicationReviewedEventData();

            return findElement(eventData.getAdditionalApplicationToBeReviewedList().getValueCodeAsUUID(),
                applicationsBundlesToBeReviewed).orElseThrow();
        }
    }

    public List<Element<AdditionalApplicationsBundle>> markSelectedBundleAsReviewed(CaseData caseData) {
        List<Element<AdditionalApplicationsBundle>> additionalApplications = caseData.getAdditionalApplicationsBundle();

        Element<AdditionalApplicationsBundle> selectedApplication = getSelectedApplicationsToBeReviewed(caseData);

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
