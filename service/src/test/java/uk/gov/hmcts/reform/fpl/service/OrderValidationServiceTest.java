package uk.gov.hmcts.reform.fpl.service;

import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.ChildGender;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingsFromInitialDate;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrderValidationService.class, LocalValidatorFactoryBean.class,
    FixedTimeConfiguration.class})
class OrderValidationServiceTest {

    @Autowired
    private Time time;

    @Autowired
    private OrderValidationService validationService;

    @SpyBean
    private Validator validator;

    @Test
    void shouldNotValidateCaseWhenDraftingOrder() {
        CaseData caseData = buildCaseData(DRAFT);

        final List<String> validationErrors = validationService.validate(caseData);

        assertThat(validationErrors).isEmpty();
        verify(validator, never()).validate(any(), any());
    }

    @Test
    void shouldSuccessfullyValidateCaseWithAllRequiredFieldsWhenSealingOrder() {
        CaseData caseData = buildCaseDataWithMandatoryFields(SEALED);

        final List<String> validationErrors = validationService.validate(caseData);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldReturnValidationErrorsForACaseWithoutRequiredFieldsWhenSealingOrder() {
        CaseData caseData = buildCaseData(SEALED);

        final List<String> validationErrors = validationService.validate(caseData);

        assertThat(validationErrors).containsExactlyInAnyOrder(
            "You need to enter a hearing date.",
            "You need to enter the allocated judge.",
            "Tell us the date of birth of all children in the case",
            "Tell us the gender of all children in the case",
            "Enter the respondent's relationship to child"
        );
    }

    private CaseData buildCaseDataWithMandatoryFields(final OrderStatus orderStatus) {
        return buildCaseData(orderStatus).toBuilder()
            .respondents1(buildRespondent("Uncle"))
            .children1(buildChild(ChildGender.BOY, time.now().toLocalDate().minusYears(1)))
            .hearingDetails(createHearingBookingsFromInitialDate(LocalDateTime.now()))
            .allocatedJudge(Judge.builder().build())
            .build();
    }

    private static CaseData buildCaseData(final OrderStatus orderStatus) {
        return CaseData.builder()
            .standardDirectionOrder(buildOrderWithStatus(orderStatus))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder().build())
            .respondents1(buildRespondent(null))
            .children1(buildChild(null, null))
            .build();
    }

    private static StandardDirectionOrder buildOrderWithStatus(final OrderStatus orderStatus) {
        return StandardDirectionOrder.builder()
            .orderStatus(orderStatus)
            .build();
    }

    private static List<Element<Child>> buildChild(final ChildGender gender, final LocalDate dob) {
        return wrapElements(Child.builder()
            .party(
                ChildParty.builder()
                    .gender(gender)
                    .dateOfBirth(dob)
                    .build())
            .build());
    }

    private static List<Element<Respondent>> buildRespondent(final String relationshipToChild) {
        return wrapElements(Respondent.builder()
            .party(
                RespondentParty.builder()
                    .relationshipToChild(relationshipToChild)
                    .build())
            .build());
    }
}
