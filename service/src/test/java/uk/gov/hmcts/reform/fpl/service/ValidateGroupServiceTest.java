package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.validation.groups.NoticeOfProceedingsGroup;
import uk.gov.hmcts.reform.fpl.validation.groups.ValidateFamilyManCaseNumberGroup;

import java.util.List;
import java.util.UUID;
import javax.validation.Validation;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class ValidateGroupServiceTest {
    private ValidateGroupService validateGroupService;

    @BeforeEach()
    private void setup() {
        validateGroupService = new ValidateGroupService(Validation
            .buildDefaultValidatorFactory()
            .getValidator());
    }

    @Test
    void shouldReturnAnErrorWhenFamilyManCaseNumberIsNotPopulated() {
        List<String> errors = validateGroupService.validateGroup(CaseData.builder().build(),
            ValidateFamilyManCaseNumberGroup.class);

        assertThat(errors).containsExactly("Enter Familyman case number");
    }

    @Test
    void shouldReturnAnErrorWhenFamilyManCaseNumberAndHearingBookingDetailsIsNotPopulated() {
        CaseData caseData = CaseData.builder().build();
        List<String> errors = validateGroupService.validateGroup(caseData, NoticeOfProceedingsGroup.class);

        assertThat(errors).containsOnlyOnce(
            "Enter Familyman case number",
            "Enter hearing details"
        );
    }

    @Test
    void shouldNotReturnAnErrorWhenFamilyManCaseNumberAndHearingBookingDetailsIsPopulated() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(ImmutableList.of(
                Element.<HearingBooking>builder()
                    .id(UUID.randomUUID())
                    .value(HearingBooking.builder().build())
                    .build()))
            .familyManCaseNumber("123")
            .build();
        List<String> errors = validateGroupService
            .validateGroup(caseData, NoticeOfProceedingsGroup.class);

        assertThat(errors).isEmpty();
    }
}
