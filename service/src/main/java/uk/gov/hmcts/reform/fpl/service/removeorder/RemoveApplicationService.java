package uk.gov.hmcts.reform.fpl.service.removeorder;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.RemovableOrderOrApplicationNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RemoveApplicationService {

    public DynamicList buildDynamicList(CaseData caseData) {
        return buildDynamicList(caseData, null);
    }

    public DynamicList buildDynamicList(CaseData caseData, UUID selected) {
        List<Element<AdditionalApplicationsBundle>> applications = defaultIfNull(
            caseData.getAdditionalApplicationsBundle(), new ArrayList<>());

        applications.sort(Comparator
            .comparing((Element<AdditionalApplicationsBundle> bundle) -> bundle.getValue().getUploadedDateTime()));

        return asDynamicList(applications, selected, AdditionalApplicationsBundle::toLabel);
    }

    public void populateApplicationFields(CaseDetailsMap data, AdditionalApplicationsBundle application) {
        data.put("applicationTypeToBeRemoved", application.toLabel());
        data.put("c2ApplicationToBeRemoved", defaultIfNull(application.getC2DocumentBundle(),
            C2DocumentBundle.builder().build()).getDocument());
        data.put("otherApplicationToBeRemoved", defaultIfNull(application.getOtherApplicationsBundle(),
            OtherApplicationsBundle.builder().build()).getDocument());
        data.put("orderDateToBeRemoved", application.getUploadedDateTime());
    }

    public Element<AdditionalApplicationsBundle> getRemovedApplicationById(CaseData caseData, UUID selectedBundleId) {
        return caseData.getAdditionalApplicationsBundle().stream()
            .filter(orderElement -> selectedBundleId.equals(orderElement.getId()))
            .findAny()
            .orElseThrow(() -> new RemovableOrderOrApplicationNotFoundException(selectedBundleId));
    }

    public void removeApplicationFromCase(CaseData caseData, CaseDetailsMap caseDetailsMap, UUID removedApplicationId) {
        Element<AdditionalApplicationsBundle> bundleElement = getRemovedApplicationById(caseData, removedApplicationId);

        caseData.getAdditionalApplicationsBundle().remove(bundleElement);
        caseDetailsMap.put("additionalApplicationsBundle", caseData.getAdditionalApplicationsBundle());

        List<Element<AdditionalApplicationsBundle>> hiddenApplications = caseData.getHiddenApplicationsBundle();
        hiddenApplications.add(bundleElement);
        caseDetailsMap.put("hiddenApplicationsBundle", hiddenApplications);
    }

}
