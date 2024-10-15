package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.IssuedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedCMOTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.Base64;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ContextConfiguration(classes = {CaseManagementOrderEmailContentProvider.class})
class CaseManagementOrderEmailContentProviderTest extends AbstractEmailContentProviderTest {
    private static final String ENCODED_DOC = Base64.getEncoder().encodeToString(DOCUMENT_CONTENT);
    private static final CaseData CASE_DATA = CaseData.builder()
        .id(Long.valueOf(CASE_REFERENCE))
        .familyManCaseNumber("11")
        .caseName("case1")
        .respondents1(wrapElements(Respondent.builder()
            .party(RespondentParty.builder().lastName("lastName").build())
            .build()))
        .children1(wrapElements(mock(Child.class)))
        .allocatedJudge(Judge.builder()
            .judgeTitle(JudgeOrMagistrateTitle.DEPUTY_DISTRICT_JUDGE)
            .judgeLastName("JudgeLastName")
            .judgeEmailAddress("JudgeEmailAddress")
            .build())
        .build();

    @MockBean
    private EmailNotificationHelper helper;
    @Autowired
    private CaseManagementOrderEmailContentProvider underTest;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(CASE_DATA.getAllChildren())).thenReturn("Some last name");
    }

    @Test
    void shouldBuildCMOIssuedExpectedParametersWithEmptyCaseUrl() {
        HearingOrder cmo = buildCmo();
        IssuedCMOTemplate expectedTemplate = IssuedCMOTemplate.builder()
            .respondentLastName("lastName")
            .familyManCaseNumber("11")
            .digitalPreference("No")
            .hearing("test hearing, 20th June")
            .caseUrl("")
            .documentLink(new HashMap<>() {{
                              put("retention_period", null);
                              put("filename", null);
                              put("confirm_email_before_download", null);
                              put("file", ENCODED_DOC);
                          }}
            )
            .childLastName("Some last name")
            .build();

        when(documentDownloadService.downloadDocument(cmo.getOrder().getBinaryUrl()))
            .thenReturn(DOCUMENT_CONTENT);

        assertThat(underTest.buildCMOIssuedNotificationParameters(CASE_DATA, cmo, EMAIL))
            .isEqualTo(expectedTemplate);
    }

    @Test
    void shouldBuildCMOIssuedExpectedParametersWithPopulatedCaseUrl() {
        final HearingOrder cmo = buildCmo();

        IssuedCMOTemplate expectedTemplate = IssuedCMOTemplate.builder()
            .respondentLastName("lastName")
            .familyManCaseNumber("11")
            .digitalPreference("Yes")
            .hearing("test hearing, 20th June")
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .documentLink(DOC_URL)
            .childLastName("Some last name")
            .build();

        assertThat(underTest.buildCMOIssuedNotificationParameters(CASE_DATA, cmo, DIGITAL_SERVICE))
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
            .childLastName("Some last name")
            .build();

        assertThat(underTest.buildCMORejectedByJudgeNotificationParameters(CASE_DATA, cmo))
            .isEqualTo(expectedTemplate);
    }

    private HearingOrder buildCmo() {
        return HearingOrder.builder()
            .order(testDocument)
            .hearing("Test hearing, 20th June")
            .build();
    }
}
