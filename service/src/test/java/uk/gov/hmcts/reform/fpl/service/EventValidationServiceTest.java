package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.interfaces.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import javax.validation.Validation;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class EventValidationServiceTest {
    private EventValidationService eventValidationService;

    @BeforeEach()
    private void setup() {
        eventValidationService = new EventValidationService(Validation
            .buildDefaultValidatorFactory()
            .getValidator());
    }

    @Test
    void shouldReturnAnErrorIfFamilyManCaseNumberIsNotPopulated() {
        CaseData caseData = CaseData.builder().build();
        List<String> errors = eventValidationService.validateEvent(caseData, NoticeOfProceedingsGroup.class);

        assertThat(errors).containsOnlyOnce("Enter Familyman case number");
    }

    @Test
    void shouldNotReturnAnErrorIfFamilyManCaseNumberIsPopulated() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("123")
            .build();
        List<String> errors = eventValidationService.validateEvent(caseData, NoticeOfProceedingsGroup.class);

        assertThat(errors).isEmpty();
    }
}
