package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_NEEDED;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EventChecker.class, LocalValidatorFactoryBean.class})
@TestInstance(PER_CLASS)
class EventCheckerTest {

    @MockBean
    private CaseNameValidator caseNameValidator;
    @MockBean
    private AllocationProposalValidator allocationProposalValidator;
    @MockBean
    private ChildrenValidator childrenValidator;
    @MockBean
    private RespondentsValidator respondentsValidator;
    @MockBean
    private HearingNeededValidator hearingNeededValidator;
    @MockBean
    private OrdersNeededValidator ordersNeededValidator;
    @MockBean
    private GroundsValidator groundsValidator;
    @MockBean
    private ApplicantValidator applicantValidator;
    @MockBean
    private DocumentsValidator documentsValidator;
    @MockBean
    private CaseSubmissionGuard submissionValidator;
    @MockBean
    private RiskAndHarmValidator riskAndHarmValidator;
    @MockBean
    private FactorsAffectingParentingValidator factorsAffectingParentingValidator;

    @Autowired
    private EventChecker eventChecker;

    private final CaseData caseData = CaseData.builder().build();

    @ParameterizedTest
    @MethodSource("getUnguardedEvents")
    void shouldVerifyUnguardedEventWithError(Event event, Validator validator) {
        List<String> expectedErrors = List.of("Case name error");
        when(validator.validate(caseData)).thenReturn(expectedErrors);

        assertThat(eventChecker.validate(event, caseData)).isEqualTo(expectedErrors);
        assertThat(eventChecker.isCompleted(event, caseData)).isFalse();
        assertThat(eventChecker.isAvailable(event, caseData)).isTrue();

        verify(validator, times(2)).validate(caseData);
    }

    @ParameterizedTest
    @MethodSource("getUnguardedEvents")
    void shouldVerifyUnguardedEventWithoutError(Event event, Validator validator) {
        List<String> expectedErrors = List.of();
        when(validator.validate(caseData)).thenReturn(expectedErrors);

        assertThat(eventChecker.validate(event, caseData)).isEqualTo(expectedErrors);
        assertThat(eventChecker.isCompleted(event, caseData)).isTrue();
        assertThat(eventChecker.isAvailable(event, caseData)).isTrue();

        verify(validator, times(2)).validate(caseData);
    }

    @ParameterizedTest
    @MethodSource("getGuardedEvents")
    void shouldRejectEventWhenGuardReturnsErrors(Event event, Validator guards) {
        List<String> expectedErrors = List.of("Case name error");
        when(guards.validate(caseData)).thenReturn(expectedErrors);

        assertThat(eventChecker.validate(event, caseData)).isEmpty();
        assertThat(eventChecker.isCompleted(event, caseData)).isFalse();
        assertThat(eventChecker.isAvailable(event, caseData)).isFalse();

        verify(guards).validate(caseData);
    }

    @ParameterizedTest
    @MethodSource("getGuardedEvents")
    void shouldAllowEventWhenGuardReturnsEmptyErrors(Event event, Validator guard) {
        when(guard.validate(caseData)).thenReturn(Collections.emptyList());

        assertThat(eventChecker.validate(event, caseData)).isEmpty();
        assertThat(eventChecker.isCompleted(event, caseData)).isFalse();
        assertThat(eventChecker.isAvailable(event, caseData)).isTrue();

        verify(guard).validate(caseData);
    }

    @AfterEach
    void verifyNoMoreInteractionsWithValidators() {
        verifyNoMoreInteractions(
                allocationProposalValidator,
                caseNameValidator,
                childrenValidator,
                respondentsValidator,
                hearingNeededValidator,
                ordersNeededValidator,
                groundsValidator,
                applicantValidator,
                documentsValidator,
                submissionValidator,
                riskAndHarmValidator,
                factorsAffectingParentingValidator
        );
    }

    private Stream<Arguments> getUnguardedEvents() {
        return Stream.of(
                Arguments.of(CASE_NAME, caseNameValidator),
                Arguments.of(ALLOCATION_PROPOSAL, allocationProposalValidator),
                Arguments.of(CHILDREN, childrenValidator),
                Arguments.of(RESPONDENTS, respondentsValidator),
                Arguments.of(HEARING_NEEDED, hearingNeededValidator),
                Arguments.of(ORDERS_NEEDED, ordersNeededValidator),
                Arguments.of(GROUNDS, groundsValidator),
                Arguments.of(APPLICANT, applicantValidator),
                Arguments.of(DOCUMENTS, documentsValidator),
                Arguments.of(RISK_AND_HARM, riskAndHarmValidator),
                Arguments.of(FACTORS_AFFECTING_PARENTING, factorsAffectingParentingValidator)
        );
    }

    private Stream<Arguments> getGuardedEvents() {
        return Stream.of(
                Arguments.of(SUBMIT_APPLICATION, submissionValidator)
        );
    }
}
