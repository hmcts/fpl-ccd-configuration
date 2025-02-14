package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.submission.EventValidationErrors;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT_DETAILS_LA;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT_DETAILS_SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.SELECT_COURT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

@ExtendWith(SpringExtension.class)
class CaseSubmissionCheckerTest {

    @Mock
    private CaseData caseData;

    @Mock
    private EventsChecker eventsChecker;

    @InjectMocks
    private CaseSubmissionChecker caseSubmissionValidator;

    private final List<String> caseNameErrors = List.of("Case name error");
    private final List<String> ordersNeededErrors = List.of("Orders needed error 1", "Orders needed error 2");
    private final List<String> hearingNeededErrors = List.of("Hearing needed error");
    private final List<String> groundsErrors = List.of("Grounds for application error");
    private final List<String> applicantLaErrors = List.of("Applicant error LA 1", "Applicant error LA 2");
    private final List<String> applicantSolicitorErrors = List.of("Applicant error Sol 1", "Applicant error Sol 2");
    private final List<String> childrenErrors = List.of("Children error");
    private final List<String> respondentsErrors = List.of("Respondent error 1", "Respondent error 2");
    private final List<String> allocationProposalErrors = List.of("Allocation proposal error");
    private final List<String> courtSelectionErrors = List.of("Court selection error");

    @Nested
    class Validation {

        @Test
        void shouldOnlyShowValidEventsIfC1Application() {
            when(caseData.isC1Application()).thenReturn(true);

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);
            final List<String> errors = caseSubmissionValidator.validate(caseData);

            assertThat(errors).containsExactly(
                "In the change case name section:",
                "• Case name error",
                "In the orders and directions sought section:",
                "• Orders needed error 1",
                "• Orders needed error 2",
                "In the grounds for the application section:",
                "• Grounds for application error",
                "In the applicant's details section:",
                "• Applicant error Sol 1",
                "• Applicant error Sol 2",
                "In the child's details section:",
                "• Children error",
                "In the respondents' details section:",
                "• Respondent error 1",
                "• Respondent error 2",
                "In the allocation proposal section:",
                "• Allocation proposal error"
            );

        }

        @Test
        void shouldOnlyShowValidEventsIfRefuseContactApplication() {
            when(caseData.isC1Application()).thenReturn(true);
            when(caseData.isRefuseContactWithChildApplication()).thenReturn(true);

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);
            final List<String> errors = caseSubmissionValidator.validate(caseData);

            assertThat(errors).containsExactly(
                "In the change case name section:",
                "• Case name error",
                "In the orders and directions sought section:",
                "• Orders needed error 1",
                "• Orders needed error 2",
                "In the hearing urgency section:",
                "• Hearing needed error",
                "In the grounds for the application section:",
                "• Grounds for application error",
                "In the applicant's details section:",
                "• Applicant error Sol 1",
                "• Applicant error Sol 2",
                "In the child's details section:",
                "• Children error",
                "In the respondents' details section:",
                "• Respondent error 1",
                "• Respondent error 2",
                "In the allocation proposal section:",
                "• Allocation proposal error"
            );

        }

        @Test
        void shouldOnlyShowValidEventsIfContactWithChildInCareApplication() {
            when(caseData.isC1Application()).thenReturn(true);
            when(caseData.isContactWithChildInCareApplication()).thenReturn(true);

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);
            final List<String> errors = caseSubmissionValidator.validate(caseData);

            assertThat(errors).containsExactly(
                "In the change case name section:",
                "• Case name error",
                "In the orders and directions sought section:",
                "• Orders needed error 1",
                "• Orders needed error 2",
                "In the hearing urgency section:",
                "• Hearing needed error",
                "In the grounds for the application section:",
                "• Grounds for application error",
                "In the applicant's details section:",
                "• Applicant error Sol 1",
                "• Applicant error Sol 2",
                "In the child's details section:",
                "• Children error",
                "In the respondents' details section:",
                "• Respondent error 1",
                "• Respondent error 2",
                "In the allocation proposal section:",
                "• Allocation proposal error"
            );

        }



        @Test
        void shouldReportEventsErrorIncludingCourtSelectionErrorWhenCourtNotSelected() {

            when(caseData.getMultiCourts()).thenReturn(YES);

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);
            when(eventsChecker.validate(SELECT_COURT, caseData)).thenReturn(courtSelectionErrors);

            final List<String> errors = caseSubmissionValidator.validate(caseData);
            final boolean isAvailable = caseSubmissionValidator.isAvailable(caseData);

            assertThat(errors).containsExactly(
                "In the change case name section:",
                "• Case name error",
                "In the orders and directions sought section:",
                "• Orders needed error 1",
                "• Orders needed error 2",
                "In the hearing urgency section:",
                "• Hearing needed error",
                "In the grounds for the application section:",
                "• Grounds for application error",
                "In the applicant's details section:",
                "• Applicant error Sol 1",
                "• Applicant error Sol 2",
                "In the child's details section:",
                "• Children error",
                "In the respondents' details section:",
                "• Respondent error 1",
                "• Respondent error 2",
                "In the allocation proposal section:",
                "• Allocation proposal error",
                "In the select court to issue section:",
                "• Court selection error"
            );

            assertThat(isAvailable).isFalse();
        }

        @Test
        void shouldValidateLocalAuthorityInsteadOfLegacyApplicantWhenAdditionalContactFeatureIsToggledOn() {

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);

            final List<String> errors = caseSubmissionValidator.validate(caseData);
            final boolean isAvailable = caseSubmissionValidator.isAvailable(caseData);

            assertThat(errors).containsExactly(
                "In the change case name section:",
                "• Case name error",
                "In the orders and directions sought section:",
                "• Orders needed error 1",
                "• Orders needed error 2",
                "In the hearing urgency section:",
                "• Hearing needed error",
                "In the grounds for the application section:",
                "• Grounds for application error",
                "In the applicant's details section:",
                "• Applicant error Sol 1",
                "• Applicant error Sol 2",
                "In the child's details section:",
                "• Children error",
                "In the respondents' details section:",
                "• Respondent error 1",
                "• Respondent error 2",
                "In the allocation proposal section:",
                "• Allocation proposal error"
            );

            assertThat(isAvailable).isFalse();
        }

        @Test
        void shouldExcludeGroundForApplicationWhenDischargeOfCareApplication() {
            when(caseData.isDischargeOfCareApplication()).thenReturn(true);

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);

            final List<String> errors = caseSubmissionValidator.validate(caseData);
            final boolean isAvailable = caseSubmissionValidator.isAvailable(caseData);

            assertThat(errors).containsExactly(
                "In the change case name section:",
                "• Case name error",
                "In the orders and directions sought section:",
                "• Orders needed error 1",
                "• Orders needed error 2",
                "In the hearing urgency section:",
                "• Hearing needed error",
                "In the applicant's details section:",
                "• Applicant error Sol 1",
                "• Applicant error Sol 2",
                "In the child's details section:",
                "• Children error",
                "In the respondents' details section:",
                "• Respondent error 1",
                "• Respondent error 2",
                "In the allocation proposal section:",
                "• Allocation proposal error"
            );

            assertThat(isAvailable).isFalse();

            verify(eventsChecker, never()).validate(eq(GROUNDS), any());
        }

        @Test
        void shouldValidateApplicantLAIfLocalAuthorityIsYes() {
            when(caseData.checkIfCaseIsSubmittedByLA()).thenReturn(true);

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);

            final List<String> errors = caseSubmissionValidator.validate(caseData);
            final boolean isAvailable = caseSubmissionValidator.isAvailable(caseData);

            assertThat(errors).containsExactly(
                "In the change case name section:",
                "• Case name error",
                "In the orders and directions sought section:",
                "• Orders needed error 1",
                "• Orders needed error 2",
                "In the hearing urgency section:",
                "• Hearing needed error",
                "In the grounds for the application section:",
                "• Grounds for application error",
                "In the applicant's details section:",
                "• Applicant error LA 1",
                "• Applicant error LA 2",
                "In the child's details section:",
                "• Children error",
                "In the respondents' details section:",
                "• Respondent error 1",
                "• Respondent error 2",
                "In the allocation proposal section:",
                "• Allocation proposal error"
            );

            assertThat(isAvailable).isFalse();
            verify(eventsChecker, never()).validate(eq(APPLICANT_DETAILS_SOLICITOR), any());
        }

        @Test
        void shouldValidateApplicantSolIfLocalAuthorityIsNo() {
            when(caseData.checkIfCaseIsSubmittedByLA()).thenReturn(false);

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);

            final List<String> errors = caseSubmissionValidator.validate(caseData);
            final boolean isAvailable = caseSubmissionValidator.isAvailable(caseData);

            assertThat(errors).containsExactly(
                "In the change case name section:",
                "• Case name error",
                "In the orders and directions sought section:",
                "• Orders needed error 1",
                "• Orders needed error 2",
                "In the hearing urgency section:",
                "• Hearing needed error",
                "In the grounds for the application section:",
                "• Grounds for application error",
                "In the applicant's details section:",
                "• Applicant error Sol 1",
                "• Applicant error Sol 2",
                "In the child's details section:",
                "• Children error",
                "In the respondents' details section:",
                "• Respondent error 1",
                "• Respondent error 2",
                "In the allocation proposal section:",
                "• Allocation proposal error"
            );

            assertThat(isAvailable).isFalse();
            verify(eventsChecker, never()).validate(eq(APPLICANT_DETAILS_LA), any());
        }

        @Test
        void shouldReportGroupedErrorsOnlyForRelevantEventsWithErrors() {
            final List<String> ordersNeededErrors = List.of("Orders needed error 1", "Orders needed error 2");
            final List<String> childrenErrors = List.of("Children error 1");

            when(eventsChecker.validate(any(), any())).thenReturn(emptyList());
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);

            final List<String> errors = caseSubmissionValidator.validate(caseData);
            final boolean isAvailable = caseSubmissionValidator.isAvailable(caseData);

            assertThat(errors).containsExactly(
                "In the orders and directions sought section:",
                "• Orders needed error 1",
                "• Orders needed error 2",
                "In the child's details section:",
                "• Children error 1");

            assertThat(isAvailable).isFalse();
        }

        @Test
        void shouldReportOnlyUniqueErrors() {
            final List<String> ordersNeededErrors = List.of("Orders needed error", "Orders needed error");
            final List<String> childrenErrors = List.of("Children error", "Children error", "Children error");

            when(eventsChecker.validate(any(), any())).thenReturn(emptyList());
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);

            final List<String> errors = caseSubmissionValidator.validate(caseData);
            final boolean isAvailable = caseSubmissionValidator.isAvailable(caseData);

            assertThat(errors).containsExactly(
                "In the orders and directions sought section:",
                "• Orders needed error",
                "In the child's details section:",
                "• Children error");

            assertThat(isAvailable).isFalse();
        }

        @Test
        void shouldReportEmptyGroupedErrorsWhenNoErrorsForRelevantEvents() {
            when(eventsChecker.validate(any(), any())).thenReturn(emptyList());

            final List<String> errors = caseSubmissionValidator.validate(caseData);
            final boolean isAvailable = caseSubmissionValidator.isAvailable(caseData);

            assertThat(errors).isEmpty();
            assertThat(isAvailable).isTrue();
        }
    }

    @Nested
    class GroupedValidation {

        @Test
        void shouldValidateApplicantLAIfLocalAuthorityIsYes() {
            when(caseData.checkIfCaseIsSubmittedByLA()).thenReturn(true);

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);

            final List<EventValidationErrors> errors = caseSubmissionValidator.validateAsGroups(caseData);

            assertThat(errors).containsExactly(
                eventValidationErrors(CASE_NAME, caseNameErrors),
                eventValidationErrors(ORDERS_SOUGHT, ordersNeededErrors),
                eventValidationErrors(HEARING_URGENCY, hearingNeededErrors),
                eventValidationErrors(GROUNDS, groundsErrors),
                eventValidationErrors(APPLICANT_DETAILS_LA, applicantLaErrors),
                eventValidationErrors(CHILDREN, childrenErrors),
                eventValidationErrors(RESPONDENTS, respondentsErrors),
                eventValidationErrors(ALLOCATION_PROPOSAL, allocationProposalErrors)
            );

            verify(eventsChecker, never()).validate(eq(APPLICANT_DETAILS_SOLICITOR), any());
        }



        @Test
        void shouldValidateApplicantLAIfLocalAuthorityIsNo() {
            when(caseData.checkIfCaseIsSubmittedByLA()).thenReturn(false);

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_LA, caseData)).thenReturn(applicantLaErrors);
            when(eventsChecker.validate(APPLICANT_DETAILS_SOLICITOR, caseData)).thenReturn(applicantSolicitorErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);
            when(eventsChecker.validate(RESPONDENTS, caseData)).thenReturn(respondentsErrors);
            when(eventsChecker.validate(ALLOCATION_PROPOSAL, caseData)).thenReturn(allocationProposalErrors);

            final List<EventValidationErrors> errors = caseSubmissionValidator.validateAsGroups(caseData);

            assertThat(errors).containsExactly(
                eventValidationErrors(CASE_NAME, caseNameErrors),
                eventValidationErrors(ORDERS_SOUGHT, ordersNeededErrors),
                eventValidationErrors(HEARING_URGENCY, hearingNeededErrors),
                eventValidationErrors(GROUNDS, groundsErrors),
                eventValidationErrors(APPLICANT_DETAILS_SOLICITOR, applicantSolicitorErrors),
                eventValidationErrors(CHILDREN, childrenErrors),
                eventValidationErrors(RESPONDENTS, respondentsErrors),
                eventValidationErrors(ALLOCATION_PROPOSAL, allocationProposalErrors)
            );

            verify(eventsChecker, never()).validate(eq(APPLICANT_DETAILS_LA), any());
        }

        @Test
        void shouldReportGroupedErrorsOnlyForRelevantEventsWithErrors() {
            when(eventsChecker.validate(any(), any())).thenReturn(emptyList());
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);

            final List<EventValidationErrors> errors = caseSubmissionValidator.validateAsGroups(caseData);

            assertThat(errors).containsExactly(
                eventValidationErrors(ORDERS_SOUGHT, ordersNeededErrors),
                eventValidationErrors(CHILDREN, childrenErrors)
            );
        }

        @Test
        void shouldReportOnlyUniqueErrors() {
            final List<String> ordersNeededErrors = List.of("Orders needed error", "Orders needed error");
            final List<String> childrenErrors = List.of("Children error 1", "Children error 1", "Children error 2");

            when(eventsChecker.validate(any(), any())).thenReturn(emptyList());
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(CHILDREN, caseData)).thenReturn(childrenErrors);

            final List<EventValidationErrors> errors = caseSubmissionValidator.validateAsGroups(caseData);

            assertThat(errors).containsExactly(
                eventValidationErrors(ORDERS_SOUGHT, List.of("Orders needed error")),
                eventValidationErrors(CHILDREN, List.of("Children error 1", "Children error 2")));
        }
    }

    private static EventValidationErrors eventValidationErrors(Event event, List<String> errors) {
        return EventValidationErrors.builder()
            .event(event)
            .errors(errors)
            .build();
    }
}
