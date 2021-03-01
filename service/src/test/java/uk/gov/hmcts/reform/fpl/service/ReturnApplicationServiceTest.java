package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

class ReturnApplicationServiceTest {

    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final ReturnApplicationService service = new ReturnApplicationService(time);

    @Test
    void shouldUpdateReturnedApplicationPropertiesWithFormattedDates() {
        LocalDate now = time.now().toLocalDate();
        String expectedDate = formatLocalDateToString(now, "d MMMM YYYY");
        DocumentReference applicationDocument = applicationDocument();

        ReturnApplication returnApplication = service.updateReturnApplication(buildReturnedApplication(),
            applicationDocument, now);

        ReturnApplication expectedApplication = ReturnApplication.builder()
            .reason(List.of(INCOMPLETE))
            .note("Missing child details")
            .document(applicationDocument)
            .submittedDate(expectedDate)
            .returnedDate(expectedDate)
            .build();

        assertThat(returnApplication).isEqualTo(expectedApplication);
    }

    @Test
    void shouldAppendReturnedToDocumentFileNameExcludingExtension() {
        DocumentReference file = applicationDocument();
        service.appendReturnedToFileName(file);
        assertThat(file.getFilename()).isEqualTo("mock_document_returned.pdf");
    }

    private ReturnApplication buildReturnedApplication() {
        return ReturnApplication.builder()
            .reason(List.of(INCOMPLETE))
            .note("Missing child details")
            .build();
    }

    private DocumentReference applicationDocument() {
        return DocumentReference.builder()
            .filename("mock_document.pdf")
            .build();
    }
}
