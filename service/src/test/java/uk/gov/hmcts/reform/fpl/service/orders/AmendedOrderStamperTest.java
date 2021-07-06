package uk.gov.hmcts.reform.fpl.service.orders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.service.DocumentDownloadService;
import uk.gov.hmcts.reform.fpl.service.orders.amendment.AmendedOrderStamper;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTime;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ResourceReader.readBytes;

@ExtendWith({TestLogsExtension.class})
class AmendedOrderStamperTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(420, 6, 9);
    private static final String BINARY_URL = "binary url";
    private static final String FILE_NAME = "order.pdf";

    @TestLogs
    private final TestLogger logs = new TestLogger(AmendedOrderStamper.class);

    private DocumentDownloadService downloadService;
    private AmendedOrderStamper underTest;

    @BeforeEach
    void setUp() {
        downloadService = mock(DocumentDownloadService.class);
        Time time = new FixedTime(LocalDateTime.of(FIXED_DATE, LocalTime.MIDNIGHT));
        underTest = new AmendedOrderStamper(downloadService, time);
    }

    @Test
    void amendedPDF() {
        final byte[] inputBinaries = readBytes("documents/document.pdf");
        final byte[] outputBinaries = readBytes("documents/document-amended.pdf");

        DocumentReference inputPDF = mock(DocumentReference.class);

        when(inputPDF.getFilename()).thenReturn(FILE_NAME);
        when(inputPDF.getBinaryUrl()).thenReturn(BINARY_URL);
        when(downloadService.downloadDocument(BINARY_URL)).thenReturn(inputBinaries);

        byte[] amendedPDF = underTest.amendDocument(inputPDF);

        assertThat(amendedPDF).isEqualTo(outputBinaries);
    }

    @Test
    void amendedPDFShouldThrowErrorWhenInputFileIsNotPDF() {
        DocumentReference inputPDF = mock(DocumentReference.class);
        when(inputPDF.getFilename()).thenReturn("some_file.xyz");

        assertThatThrownBy(() -> underTest.amendDocument(inputPDF))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Can only amend documents that are pdf, requested document was of type: xyz");
    }

    @Test
    void amendedPDFShouldLogAndThrowErrorWhenInputFileIsCorruptedPDF() {
        DocumentReference inputPDF = mock(DocumentReference.class);

        when(inputPDF.getFilename()).thenReturn(FILE_NAME);
        when(inputPDF.getBinaryUrl()).thenReturn(BINARY_URL);
        when(downloadService.downloadDocument(BINARY_URL)).thenReturn(new byte[] {0});

        assertThatThrownBy(() -> underTest.amendDocument(inputPDF)).isInstanceOf(UncheckedIOException.class);

        assertThat(logs.getErrors()).isEqualTo(List.of("Could not add amendment text to " + inputPDF));
    }
}
