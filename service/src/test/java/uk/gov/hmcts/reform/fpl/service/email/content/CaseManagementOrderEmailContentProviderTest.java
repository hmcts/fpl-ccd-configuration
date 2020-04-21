package uk.gov.hmcts.reform.fpl.service.email.content;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.OrderAction;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.apache.commons.lang3.RandomUtils.nextBytes;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseManagementOrderEmailContentProvider.class})
class CaseManagementOrderEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_ID = 12345L;

    @Autowired
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    void setField() {
        ReflectionTestUtils.setField(caseManagementOrderEmailContentProvider, "uiBaseUrl", BASE_URL);
    }

    @Test
    void shouldBuildCMOIssuedCaseLinkNotificationParameters() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("localAuthorityNameOrRepresentativeFullName", "testName")
            .put("caseUrl", buildCaseUrl("12345"))
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider.buildCMOIssuedCaseLinkNotificationParameters(createCase(), "testName")).isEqualTo(expectedMap);
    }

    @Test
    void shouldBuildCMOIssuedWithoutDocumentLinkNotificationParameters() {

        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("cafcassOrRespondentName", "testName")
            .put("caseUrl", buildCaseUrl("12345"))
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider.buildCMOIssuedDocumentLinkNotificationParameters(createCase(), "testName", new byte[]{})).isEqualTo(expectedMap);
    }

    @Test
    void shouldBuildCMOIssuedWithDocumentLinkNotificationParameters() throws JsonProcessingException {

        byte[] documentContentAsByte = nextBytes(20);
        String documentContent = new String(Base64.encodeBase64(documentContentAsByte), ISO_8859_1);

        JSONObject expectedDocumentLink = new JSONObject().put("file", documentContent);

        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("cafcassOrRespondentName", "testName")
            .put("caseUrl", buildCaseUrl("12345"))
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("link_to_document", expectedDocumentLink)
            .put("reference", "12345")
            .build();

        String expected = objectMapper.writeValueAsString(expectedMap);
        String actual = objectMapper.writeValueAsString(caseManagementOrderEmailContentProvider.buildCMOIssuedDocumentLinkNotificationParameters(createCase(), "testName", documentContentAsByte));

        JSONAssert.assertEquals(expected, actual, true);
    }

    @Test
    void shouldBuildCMOPartyReviewParameters() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("caseUrl", "")
            .put("digitalPreference", "No")
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("respondentLastName", "lastName")
            .build();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMOPartyReviewParameters(createCase(), new byte[]{}, RepresentativeServingPreferences.POST))
            .isEqualTo(expectedMap);
    }

    @Test
    void shouldBuildCMORejectedByJudgeNotificationParameters() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("requestedChanges", "change it")
            .put("caseUrl", buildCaseUrl("12345"))
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider.buildCMORejectedByJudgeNotificationParameters(createCase())).isEqualTo(expectedMap);
    }

    @Test
    void shouldBuildCMOReadyForJudgeReviewNotificationParameters() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
            .put("caseUrl", buildCaseUrl("12345"))
            .put("respondentLastName", "lastName")
            .put("judgeName", "JudgeLastName")
            .put("judgeTitle", "Deputy District Judge")
            .put("subjectLineWithHearingDate", "lastName, 11")
            .put("reference", "12345")
            .build();

        assertThat(caseManagementOrderEmailContentProvider.buildCMOReadyForJudgeReviewNotificationParameters(createCase())).isEqualTo(expectedMap);
    }

    String buildCaseUrl(String caseId) {
        return formatCaseUrl(BASE_URL, Long.parseLong(caseId));
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
