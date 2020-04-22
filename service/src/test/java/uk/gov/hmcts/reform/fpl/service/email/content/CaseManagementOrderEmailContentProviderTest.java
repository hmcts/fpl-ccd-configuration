package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.utils.AssertionHelper;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.apache.commons.lang3.RandomUtils.nextBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest.BASE_URL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseManagementOrderEmailContentProvider.class})
@TestPropertySource(properties = {"ccd.ui.base.url=" + BASE_URL})
class CaseManagementOrderEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;

    @Autowired
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Test
    void shouldBuildCMOIssuedCaseLinkNotificationExpectedParameters() {
        Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", "testName")
            .put("caseUrl", buildCaseUrl())
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMOIssuedCaseLinkNotificationParameters(createCase(), "testName"))
            .isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildCMOIssuedWithoutDocumentLinkNotificationExpectedParameters() {

        Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("cafcassOrRespondentName", "testName")
            .put("caseUrl", buildCaseUrl())
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider.buildCMOIssuedDocumentLinkNotificationParameters(
            createCase(), "testName", new byte[]{}))
            .isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildCMOIssuedWithDocumentLinkNotificationExpectedParameters() {

        byte[] documentContentAsByte = nextBytes(20);
        String documentContent = new String(Base64.encodeBase64(documentContentAsByte), ISO_8859_1);

        JSONObject expectedDocumentLink = new JSONObject().put("file", documentContent);

        Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("cafcassOrRespondentName", "testName")
            .put("caseUrl", buildCaseUrl())
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("link_to_document", expectedDocumentLink)
            .put("reference", "12345")
            .build();

        Map<String, Object> actualParameters =
            caseManagementOrderEmailContentProvider.buildCMOIssuedDocumentLinkNotificationParameters(
                createCase(), "testName", documentContentAsByte);

        AssertionHelper.assertEquals(actualParameters, expectedParameters);

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
            .put("caseUrl", buildCaseUrl())
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMORejectedByJudgeNotificationParameters(createCase())).isEqualTo(expectedParameters);
    }

    @Test
    void shouldBuildCMOReadyForJudgeReviewNotificationExpectedParameters() {
        Map<String, Object> expectedParameters = ImmutableMap.<String, Object>builder()
            .put("caseUrl", buildCaseUrl())
            .put("respondentLastName", "lastName")
            .put("judgeName", "JudgeLastName")
            .put("judgeTitle", "Deputy District Judge")
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMOReadyForJudgeReviewNotificationParameters(createCase())).isEqualTo(expectedParameters);
    }

    String buildCaseUrl() {
        return formatCaseUrl(BASE_URL, CASE_ID);
    }

    private CaseDetails createCase() {
        final Map<String, Object> data = new HashMap<>();
        data.put("familyManCaseNumber", "11");
        data.put("caseName", "case1");
        data.put("cmoToAction", CaseManagementOrder.builder()
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
            .build());

        return CaseDetails.builder()
            .data(data)
            .id(CASE_ID)
            .build();
    }
}
