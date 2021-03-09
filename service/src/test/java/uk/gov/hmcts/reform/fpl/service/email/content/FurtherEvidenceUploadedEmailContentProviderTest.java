package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.furtherevidence.FurtherEvidenceDocumentUploadedData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = FurtherEvidenceUploadedEmailContentProvider.class)
class FurtherEvidenceUploadedEmailContentProviderTest extends AbstractEmailContentProviderTest {
    private static final LocalDateTime HEARING_DATE = LocalDateTime.now().plusMonths(3);
    private static final String RESPONDENT_LAST_NAME = "Smith";
    private static final String CALLOUT = RESPONDENT_LAST_NAME + ", 12345, hearing " + HEARING_DATE
        .toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK));
    private static final Long CASE_ID = 12345L;
    private static final String SENDER = "SENDER";

    @Autowired
    private FurtherEvidenceUploadedEmailContentProvider furtherEvidenceUploadedEmailContentProvider;

    @Test
    void shouldBuildExpectedParametersFromCaseData() {
        FurtherEvidenceDocumentUploadedData expected = FurtherEvidenceDocumentUploadedData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, DOCUMENTS))
            .respondentLastName(RESPONDENT_LAST_NAME)
            .userName(SENDER)
            .callout(CALLOUT)
            .build();

        FurtherEvidenceDocumentUploadedData actual = furtherEvidenceUploadedEmailContentProvider.buildParameters(
            buildCaseData(), SENDER);

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
            .hearingDetails(wrapElements(HearingBooking.builder().startDate((HEARING_DATE)).build()))
            .build();
    }
}
