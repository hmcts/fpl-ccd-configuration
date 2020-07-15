package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import com.launchdarkly.shaded.org.apache.commons.codec.binary.Base64;
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
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForCMO;
import uk.gov.hmcts.reform.fpl.model.notify.draftCMO.ApprovedCMOTemplate;
import uk.gov.hmcts.reform.fpl.service.HearingBookingService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomUtils.nextBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;

@ContextConfiguration(classes = {CaseManagementOrderEmailContentProvider.class, EmailNotificationHelper.class,
    HearingBookingService.class, FixedTimeConfiguration.class})
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
    void shouldBuildCMOIssuedExpectedParametersWithoutCaseUrl() {
        byte[] documentContentAsByte = nextBytes(20);
        String documentContent = new String(Base64.encodeBase64(documentContentAsByte), ISO_8859_1);

        Map<String, Object> expectedDocumentLink = Map.of(
            "link_to_document", Map.of(
                "file", documentContent));

        ApprovedCMOTemplate expectedTemplate = new ApprovedCMOTemplate();

        expectedTemplate.setRespondentLastName("lastName");
        expectedTemplate.setFamilyManCaseNumber("11");
        expectedTemplate.setDigitalPreference("No");
        expectedTemplate.setHearingDate("Test");
        expectedTemplate.setCaseUrl("");
        expectedTemplate.setDocumentLink(expectedDocumentLink);

        assertThat(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(
            createCase(), EMAIL, documentContentAsByte))
            .isEqualToComparingFieldByField(expectedTemplate);
    }

    @Test
    void shouldBuildCMOIssuedExpectedParametersWithCaseUrl() {
        byte[] documentContentAsByte = nextBytes(20);
        String documentContent = new String(Base64.encodeBase64(documentContentAsByte), ISO_8859_1);

        Map<String, Object> expectedDocumentLink = Map.of(
            "link_to_document", Map.of(
                "file", documentContent));

        ApprovedCMOTemplate expectedTemplate = new ApprovedCMOTemplate();

        expectedTemplate.setRespondentLastName("lastName");
        expectedTemplate.setFamilyManCaseNumber("11");
        expectedTemplate.setDigitalPreference("Yes");
        expectedTemplate.setCaseUrl("http://fake-url/cases/case-details/" + CASE_REFERENCE);
        expectedTemplate.setHearingDate("Test");
        expectedTemplate.setDocumentLink(expectedDocumentLink);

        assertThat(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(
            createCase(), DIGITAL_SERVICE, documentContentAsByte))
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
            .buildCMOPartyReviewParameters(createCase(), new byte[]{}, RepresentativeServingPreferences.POST))
            .isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildCMORejectedByJudgeNotificationExpectedParameters() {
        Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("requestedChanges", "change it")
            .put("caseUrl", caseUrl(CASE_REFERENCE))
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", CASE_REFERENCE)
            .build();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMORejectedByJudgeNotificationParameters(createCase())).isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildCMOReadyForJudgeReviewNotificationExpectedParameters() {
        AllocatedJudgeTemplateForCMO expectedParameters = getCMOReadyForJudgeReviewParameters();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(createCase()))
            .isEqualToComparingFieldByField(expectedParameters);
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
