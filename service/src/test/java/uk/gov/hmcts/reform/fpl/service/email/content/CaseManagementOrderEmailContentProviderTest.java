package uk.gov.hmcts.reform.fpl.service.email.content;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.NotifyAttachedDocumentLinkHelper.generateAttachedDocumentLink;

@ContextConfiguration(classes = {
    CaseManagementOrderEmailContentProvider.class, FixedTimeConfiguration.class, CaseConverter.class
})
class CaseManagementOrderEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @Autowired
    private CaseConverter caseConverter;

    @Autowired
    private CaseManagementOrderEmailContentProvider caseManagementOrderEmailContentProvider;

    @Test
    void shouldBuildCMOIssuedExpectedParametersWithEmptyCaseUrl() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(TestDataHelper.DOCUMENT_CONTENT);

        final HearingOrder cmo = buildCmo();
        IssuedCMOTemplate expectedTemplate = IssuedCMOTemplate.builder()
            .respondentLastName("lastName")
            .familyManCaseNumber("11")
            .digitalPreference("No")
            .hearing("test hearing, 20th June")
            .caseUrl("")
            .documentLink(generateAttachedDocumentLink(TestDataHelper.DOCUMENT_CONTENT)
                .map(JSONObject::toMap)
                .orElse(null))
            .build();

        assertThat(
            caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(createCase(), cmo, EMAIL))
            .usingRecursiveComparison()
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldBuildCMOIssuedExpectedParametersWithPopulatedCaseUrl() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(TestDataHelper.DOCUMENT_CONTENT);

        final HearingOrder cmo = buildCmo();

        IssuedCMOTemplate expectedTemplate = IssuedCMOTemplate.builder()
            .respondentLastName("lastName")
            .familyManCaseNumber("11")
            .digitalPreference("Yes")
            .hearing("test hearing, 20th June")
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .documentLink(DOC_URL)
            .build();

        assertThat(caseManagementOrderEmailContentProvider.buildCMOIssuedNotificationParameters(
            createCase(), cmo, DIGITAL_SERVICE))
            .usingRecursiveComparison()
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldBuildCMORejectedByJudgeNotificationExpectedParameters() {
        final HearingOrder cmo = buildCmo();
        cmo.setRequestedChanges("change it");

        RejectedCMOTemplate expectedTemplate = RejectedCMOTemplate.builder()
            .requestedChanges("change it")
            .hearing("test hearing, 20th June")
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .respondentLastName("lastName")
            .familyManCaseNumber("11")
            .build();

        assertThat(caseManagementOrderEmailContentProvider
            .buildCMORejectedByJudgeNotificationParameters(createCase(), cmo))
            .usingRecursiveComparison().isEqualTo(expectedTemplate);
    }

    private HearingOrder buildCmo() {
        return HearingOrder.builder()
            .order(testDocument)
            .hearing("Test hearing, 20th June").build();
    }

    private CaseData createCase() {
        final Map<String, Object> data = new HashMap<>();
        data.put("familyManCaseNumber", "11");
        data.put("caseName", "case1");
        data.put("respondents1", wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("lastName").build())
            .build()));
        data.put("allocatedJudge", Judge.builder()
            .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
            .judgeLastName("JudgeLastName")
            .judgeEmailAddress("JudgeEmailAddress")
            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .data(data)
            .id(Long.valueOf(CASE_REFERENCE))
            .build();

        return caseConverter.convert(caseDetails);
    }
}
