package uk.gov.hmcts.reform.fpl.enums;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.NOTICE_OF_HEARING;

class DocmosisTemplatesTest {

    @Test
    void shouldFormatDocumentTitleWithDateWhenProvidedDate() {
        LocalDate now = LocalDate.now();
        assertThat(NOTICE_OF_HEARING.getDocumentTitle(now)).isEqualTo(String.format("%s_%s.%s",
            "Notice_of_hearing", now.format(DateTimeFormatter.ofPattern("ddMMMM")), "pdf"));
    }
}
