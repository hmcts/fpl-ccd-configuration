package uk.gov.hmcts.reform.fpl.service.removeorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.exceptions.removeorder.RemovableOrderOrApplicationNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C17_EDUCATION_SUPERVISION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_APPOINTMENT_OF_A_GUARDIAN;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C4_WHEREABOUTS_OF_A_MISSING_CHILD;
import static uk.gov.hmcts.reform.fpl.utils.CaseDetailsMap.caseDetailsMap;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

class RemoveApplicationServiceTest {

    private final RemoveApplicationService underTest = new RemoveApplicationService();

    @Test
    void shouldBuildSortedDynamicListOfApplications() {
        List<Element<AdditionalApplicationsBundle>> applications = new ArrayList<>();
        applications.add(element(buildC2Application("12 May 2020")));

        applications.add(element(buildOtherApplication(C4_WHEREABOUTS_OF_A_MISSING_CHILD, "7 August 2020")));

        applications.add(element(buildCombinedApplication(C17_EDUCATION_SUPERVISION_ORDER, "1 January 2020")));

        CaseData caseData = CaseData.builder().additionalApplicationsBundle(applications).build();

        DynamicList listOfApplications = underTest.buildDynamicList(caseData);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(List.of(
                buildListElement(applications.get(0).getId(), "C2, C17, 1 January 2020"),
                buildListElement(applications.get(1).getId(), "C2, 12 May 2020"),
                buildListElement(applications.get(2).getId(), "C4, 7 August 2020")
            ))
            .build();

        assertThat(listOfApplications).isEqualTo(expectedList);
    }

    @Test
    void shouldBuildDynamicListOfApplicationsWithSelectedId() {
        UUID applicationId = UUID.randomUUID();
        List<Element<AdditionalApplicationsBundle>> applications = new ArrayList<>();

        applications.add(element(applicationId, buildC2Application("12 May 2020")));

        CaseData caseData = CaseData.builder().additionalApplicationsBundle(applications).build();

        DynamicList listOfApplications = underTest.buildDynamicList(caseData, applicationId);

        DynamicList expectedList = DynamicList.builder()
            .value(DynamicListElement.builder().code(applicationId).label("C2, 12 May 2020").build())
            .listItems(List.of(
                buildListElement(applications.get(0).getId(), "C2, 12 May 2020")
            ))
            .build();

        assertThat(listOfApplications).isEqualTo(expectedList);
    }

    @Test
    void shouldPopulateApplicationFieldsWithC2Application() {
        CaseDetailsMap caseDetailsMap = caseDetailsMap(Map.of());

        AdditionalApplicationsBundle application = buildC2Application("15 October 2020");
        underTest.populateApplicationFields(caseDetailsMap, application);

        HashMap<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("applicationTypeToBeRemoved", "C2, 15 October 2020");
        expectedMap.put("c2ApplicationToBeRemoved", application.getC2DocumentBundle().getDocument());
        expectedMap.put("otherApplicationToBeRemoved", null);
        expectedMap.put("orderDateToBeRemoved", "15 October 2020");

        assertThat(caseDetailsMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldPopulateApplicationFieldsWithOtherApplication() {
        CaseDetailsMap caseDetailsMap = caseDetailsMap(Map.of());

        AdditionalApplicationsBundle application = buildOtherApplication(C1_APPOINTMENT_OF_A_GUARDIAN,
            "15 October 2020");
        underTest.populateApplicationFields(caseDetailsMap, application);

        HashMap<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("applicationTypeToBeRemoved", "C1, 15 October 2020");
        expectedMap.put("c2ApplicationToBeRemoved", null);
        expectedMap.put("otherApplicationToBeRemoved", application.getOtherApplicationsBundle().getDocument());
        expectedMap.put("orderDateToBeRemoved", "15 October 2020");

        assertThat(caseDetailsMap).isEqualTo(expectedMap);
    }

    @Test
    void shouldGetApplicationElementToRemove() {
        UUID id = UUID.randomUUID();
        List<Element<AdditionalApplicationsBundle>> applications = new ArrayList<>();
        applications.add(element(buildC2Application("3 June 2020")));
        applications.add(element(id, buildC2Application("12 May 2020")));
        applications.add(element(buildC2Application("25 December 2020")));

        CaseData caseData = CaseData.builder().additionalApplicationsBundle(applications).build();
        Element<AdditionalApplicationsBundle> application = underTest.getRemovedApplicationById(caseData, id);

        assertThat(application).isEqualTo(applications.get(1));
    }

    @Test
    void shouldRemoveApplicationFromCaseDetailsAndUpdateHiddenApplications() {
        UUID id = UUID.randomUUID();
        Element<AdditionalApplicationsBundle> bundleToRemove = element(id, buildC2Application("12 May 2020"));
        List<Element<AdditionalApplicationsBundle>> applications = new ArrayList<>();
        applications.add(element(buildC2Application("3 June 2020")));
        applications.add(bundleToRemove);
        applications.add(element(buildC2Application("25 December 2020")));

        CaseDetailsMap caseDetailsMap = caseDetailsMap(CaseDetails.builder()
            .data(Map.of("additionalApplicationsBundle", applications))
            .build());
        CaseData caseData = CaseData.builder().additionalApplicationsBundle(applications).build();
        underTest.removeApplicationFromCase(caseData, caseDetailsMap, id);

        applications.remove(bundleToRemove);
        assertThat(caseDetailsMap.get("additionalApplicationsBundle")).isEqualTo(applications);
        assertThat(caseDetailsMap.get("hiddenApplicationsBundle")).isEqualTo(List.of(bundleToRemove));
    }

    @Test
    void shouldThrowExceptionWhenElementNotFound() {
        List<Element<AdditionalApplicationsBundle>> applications = new ArrayList<>();
        applications.add(element(buildC2Application("3 June 2020")));
        applications.add(element(buildC2Application("12 May 2020")));
        applications.add(element(buildC2Application("25 December 2020")));
        UUID id = UUID.randomUUID();

        CaseData caseData = CaseData.builder().additionalApplicationsBundle(applications).build();

        assertThatThrownBy(() -> underTest.getRemovedApplicationById(caseData, id))
            .isInstanceOf(RemovableOrderOrApplicationNotFoundException.class)
            .hasMessage(String.format("Removable order or application with id %s not found", id));
    }

    private AdditionalApplicationsBundle buildC2Application(String date) {
        return AdditionalApplicationsBundle.builder()
            .uploadedDateTime(date)
            .c2DocumentBundle(C2DocumentBundle.builder()
                .document(testDocumentReference())
                .uploadedDateTime(date)
                .build())
            .build();
    }

    private AdditionalApplicationsBundle buildOtherApplication(OtherApplicationType type, String date) {
        return AdditionalApplicationsBundle.builder()
            .uploadedDateTime(date)
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .applicationType(type)
                .uploadedDateTime(date)
                .build())
            .build();
    }

    private AdditionalApplicationsBundle buildCombinedApplication(OtherApplicationType type, String date) {
        return AdditionalApplicationsBundle.builder()
            .uploadedDateTime(date)
            .c2DocumentBundle(C2DocumentBundle.builder()
                .uploadedDateTime(date)
                .build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .applicationType(type)
                .uploadedDateTime(date)
                .build())
            .build();
    }

    private DynamicListElement buildListElement(UUID id, String label) {
        return DynamicListElement.builder()
            .code(id)
            .label(label)
            .build();
    }
}
