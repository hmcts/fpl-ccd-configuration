package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = FurtherEvidenceUploadedEmailContentProvider.class)
class FurtherEvidenceUploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {
    private static final LocalDateTime HEARING_DATE = LocalDateTime.of(2021, 12, 2, 0, 0, 0);
    private static final HearingBooking HEARING_BOOKING = HearingBooking.builder().startDate((HEARING_DATE)).build();
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final String CALLOUT_WITH_HEARING = RESPONDENT_LAST_NAME + ", 12345, hearing 2 Dec 2021";
    private static final String CALLOUT_WITHOUT_HEARING = RESPONDENT_LAST_NAME + ", 12345";
    private static final Long CASE_ID = 12345L;
    private static final String SENDER = "SENDER";
    private static final String SOME_NAME = "SOME NAME";
    private static final List<String> DOCUMENT_NAMES = List.of("DOCUMENT");


    @MockBean
    private EmailNotificationHelper helper;
    @Autowired
    private FurtherEvidenceUploadedEmailContentProvider underTest;

    @Test
    void shouldBuildExpectedParametersFromCaseData() {
        FurtherEvidenceDocumentUploadedData expected = FurtherEvidenceDocumentUploadedData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, DOCUMENTS))
            .lastName(SOME_NAME)
            .userName(SENDER)
            .callout(CALLOUT_WITHOUT_HEARING)
            .documents(DOCUMENT_NAMES)
            .build();

        CaseData caseData = buildCaseData();

        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn(SOME_NAME);

        FurtherEvidenceDocumentUploadedData actual = underTest.buildParameters(caseData, SENDER, DOCUMENT_NAMES);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldBuildExpectedParametersWithHearingFromCaseData() {
        FurtherEvidenceDocumentUploadedData expected = FurtherEvidenceDocumentUploadedData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, DOCUMENTS))
            .lastName(SOME_NAME)
            .userName(SENDER)
            .callout(CALLOUT_WITH_HEARING)
            .documents(DOCUMENT_NAMES)
            .build();

        CaseData caseData = buildCaseData();

        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn(SOME_NAME);

        FurtherEvidenceDocumentUploadedData actual = underTest.buildParametersWithHearing(caseData, SENDER,
            DOCUMENT_NAMES, Optional.of(HEARING_BOOKING));

        assertThat(actual).isEqualTo(expected);
    }

    private CaseData buildCaseData() {
        return CaseData.builder()
            .id(CASE_ID)
            .familyManCaseNumber(CASE_REFERENCE)
            .respondents1(wrapElements(Respondent.builder().party(RespondentParty.builder()
                .firstName("John")
                .lastName(RESPONDENT_LAST_NAME)
                .build()).build()))
            .hearingDetails(wrapElements(HEARING_BOOKING))
            .build();
    }
}
