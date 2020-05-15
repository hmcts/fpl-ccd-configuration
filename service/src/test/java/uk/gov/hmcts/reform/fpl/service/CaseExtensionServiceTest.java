package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CaseExtensionService.class})
class CaseExtensionServiceTest {

    @Autowired
    private CaseExtensionService service;

    @Autowired
    private ObjectMapper mapper;

    @BeforeEach
    void before() {
        service = new CaseExtensionService(mapper);
    }

    @Test
    void shouldGetCaseCompletionDateWhenSubmittingWithOtherExtensionDate() {
        LocalDate extensionDateOther = LocalDate.of(2030, 11, 12);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("extensionDateOther", extensionDateOther,
                "caseExtensionTimeList", "otherExtension"))
            .build();

        LocalDate caseCompletionDate = service.getCaseCompletionDate(caseDetails);

        assertThat(caseCompletionDate.isEqual(extensionDateOther));
    }

    @Test
    void shouldGetCaseCompletionDateWhenSubmittingWith8WeekExtensionOther() {
        LocalDate eightWeeksExtensionDateOther = LocalDate.of(2030, 11, 12);

        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("caseExtensionTimeList", "EightWeekExtension",
                "caseExtensionTimeConfirmationList", "EightWeekExtensionDateOther",
                "eightWeeksExtensionDateOther", eightWeeksExtensionDateOther))
            .build();

        LocalDate caseCompletionDate = service.getCaseCompletionDate(caseDetails);

        assertThat(caseCompletionDate.isEqual(eightWeeksExtensionDateOther));
    }
}
