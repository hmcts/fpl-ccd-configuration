package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.ReturnApplication;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.ReturnedApplicationReasons.INCOMPLETE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FixedTimeConfiguration.class, ReturnApplicationService.class})
class ReturnApplicationServiceTest {

    @Autowired
    private Time time;

    @Autowired
    private ReturnApplicationService service;

    @Test
    void shouldUpdateReturnedApplicationPropertiesWithFormattedDates() {
        LocalDate now = time.now().toLocalDate();
        String expectedDate = formatLocalDateToString(now, "d MMMM YYYY");

        ReturnApplication returnApplication = service.updateReturnApplication(buildReturnedApplication(),
            buildDocumentReference(), now);

        DocumentReference documentReference = returnApplication.getDocument();

        assertThat(returnApplication.getReason()).isEqualTo(List.of(INCOMPLETE));
        assertThat(returnApplication.getNote()).isEqualTo("Missing child details");
        assertThat(returnApplication.getSubmittedDate()).isEqualTo(expectedDate);
        assertThat(returnApplication.getReturnedDate()).isEqualTo(expectedDate);
        assertThat(documentReference.getFilename()).isEqualTo("mock_document.pdf");
    }

    @Test
    void shouldAppendReturnedToDocumentFileNameExcludingExtension() {
        String updatedFileName = service.appendReturnedToFileName("mock_document.pdf");
        assertThat(updatedFileName).isEqualTo("mock_document_returned.pdf");
    }

    private ReturnApplication buildReturnedApplication() {
        return ReturnApplication.builder()
            .reason(List.of(INCOMPLETE))
            .note("Missing child details")
            .build();
    }

    private DocumentReference buildDocumentReference() {
        return DocumentReference.builder()
            .filename("mock_document.pdf")
            .build();
    }
}
