package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForCMO;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ContextConfiguration(classes = {CaseManagementOrderEmailContentProvider.class, EmailNotificationHelper.class,
    FixedTimeConfiguration.class})
class CaseManagementOrderEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Test
    void shouldBuildCMOIssuedCaseLinkNotificationExpectedParameters() {
        Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", "testName")
            .put("caseUrl", caseUrl(CASE_REFERENCE))
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", CASE_REFERENCE)
            .build();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMOIssuedCaseLinkNotificationParameters(createCase(), "testName"))
            .isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildCMOIssuedExpectedParametersWithEmptyCaseUrl() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(TestDataHelper.DOCUMENT_CONTENT);

        IssuedCMOTemplate expectedTemplate = new IssuedCMOTemplate();
        final uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder cmo = buildCmo();

        expectedTemplate.setRespondentLastName("lastName");
        expectedTemplate.setFamilyManCaseNumber("11");
        expectedTemplate.setDigitalPreference("No");
        expectedTemplate.setHearing("test hearing, 20th June");
        expectedTemplate.setCaseUrl("");
        expectedTemplate.setDocumentLink(generateAttachedDocumentLink(TestDataHelper.DOCUMENT_CONTENT)
            .map(JSONObject::toMap)
            .orElse(null));

        assertThat(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(
            createCase(), cmo, EMAIL))
            .isEqualToComparingFieldByField(expectedTemplate);
    }

    @Test
    void shouldBuildCMOIssuedExpectedParametersWithPopulatedCaseUrl() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(TestDataHelper.DOCUMENT_CONTENT);

        final uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder cmo = buildCmo();

        IssuedCMOTemplate expectedTemplate = new IssuedCMOTemplate();

        expectedTemplate.setRespondentLastName("lastName");
        expectedTemplate.setFamilyManCaseNumber("11");
        expectedTemplate.setDigitalPreference("Yes");
        expectedTemplate.setCaseUrl("http://fake-url/cases/case-details/" + CASE_REFERENCE);
        expectedTemplate.setHearing("test hearing, 20th June");
        expectedTemplate.setDocumentLink(generateAttachedDocumentLink(TestDataHelper.DOCUMENT_CONTENT)
            .map(JSONObject::toMap)
            .orElse(null));

        assertThat(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(
            createCase(), cmo, DIGITAL_SERVICE))
            .isEqualToComparingFieldByField(expectedTemplate);
    }

    @Test
    void shouldBuildCMOPartyReviewExpectedParameters() {
        Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("caseUrl", "")
            .put("digitalPreference", "No")
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("respondentLastName", "lastName")
            .build();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMOPartyReviewParameters(createCase(), new byte[] {}, RepresentativeServingPreferences.POST))
            .isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildCMORejectedByJudgeNotificationExpectedParameters() {
        final uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder cmo = buildCmo();
        cmo.setRequestedChanges("change it");

        RejectedCMOTemplate expectedTemplate = new RejectedCMOTemplate();

        expectedTemplate.setRequestedChanges("change it");
        expectedTemplate.setHearing("test hearing, 20th June");
        expectedTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));
        expectedTemplate.setRespondentLastName("lastName");
        expectedTemplate.setFamilyManCaseNumber("11");

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMORejectedByJudgeNotificationParameters(createCase(), cmo)).isEqualToComparingFieldByField(
            expectedTemplate);
    }

    @Test
    void shouldBuildCMOReadyForJudgeReviewNotificationExpectedParameters() {
        AllocatedJudgeTemplateForCMO expectedParameters = getCMOReadyForJudgeReviewParameters();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(createCase()))
            .isEqualToComparingFieldByField(expectedParameters);
    }

    private uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder buildCmo() {
        return uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder.builder()
            .order(testDocumentReference())
            .hearing("Test hearing, 20th June").build();
    }

    private AllocatedJudgeTemplateForCMO getCMOReadyForJudgeReviewParameters() {
        AllocatedJudgeTemplateForCMO allocatedJudgeTemplate = new AllocatedJudgeTemplateForCMO();
        allocatedJudgeTemplate.setSubjectLineWithHearingDate("lastName, 11");
        allocatedJudgeTemplate.setCaseUrl(caseUrl(CASE_REFERENCE));
        allocatedJudgeTemplate.setReference(CASE_REFERENCE);
        allocatedJudgeTemplate.setRespondentLastName("lastName");
        allocatedJudgeTemplate.setJudgeTitle("Deputy District Judge");
        allocatedJudgeTemplate.setJudgeName("JudgeLastName");

        return allocatedJudgeTemplate;
    }

    private CaseDetails createCase() {
        final Map<String, Object> data = new HashMap<>();
        data.put("familyManCaseNumber", "11");
        data.put("caseName", "case1");
        data.put("cmoToAction", CaseManagementOrder.builder()
            .orderDoc(DocumentReference.builder().binaryUrl("http://test.org").build())
            .hearingDate("Test")
            .action(OrderAction.builder()
                .changeRequestedByJudge("change it")
                .build())
            .status(SEND_TO_JUDGE)
            .build());
        data.put("respondents1", wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("lastName").build())
            .build()));
        data.put("allocatedJudge", Judge.builder()
            .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
            .judgeLastName("JudgeLastName")
            .judgeEmailAddress("JudgeEmailAddress")
            .build());

        return CaseDetails.builder()
            .data(data)
            .id(Long.valueOf(CASE_REFERENCE))
            .build();
    }
}
