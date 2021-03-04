package uk.gov.hmcts.reform.fpl.service.validators;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORGANISATION_DETAILS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;

@ExtendWith(SpringExtension.class)
class CaseSubmissionCheckerTest {

    @Mock
    private EventsChecker eventsChecker;

    @InjectMocks
    private CaseSubmissionChecker caseSubmissionValidator;

    private final CaseData caseData = CaseData.builder().build();

    @Nested
    class EventsForCaseSubmissionExcludingDocuments {

        @Test
        void shouldNotReportDocumentErrorsForDocumentEvent() {
            final List<String> caseNameErrors = List.of("Case name error");
            final List<String> ordersNeededErrors = List.of("Orders needed error 1", "Orders needed error 2");
            final List<String> hearingNeededErrors = List.of("Hearing needed error");
            final List<String> groundsErrors = List.of("Grounds for application error");
            final List<String> applicantErrors = List.of("Applicant error 1", "Applicant error 2");
            final List<String> childrenErrors = List.of("Children error");
            final List<String> respondentsErrors = List.of("Respondent error 1", "Respondent error 2");
            final List<String> allocationProposalErrors = List.of("Allocation proposal error");

            when(eventsChecker.validate(any(), any())).thenReturn(List.of("Error not included"));
            when(eventsChecker.validate(CASE_NAME, caseData)).thenReturn(caseNameErrors);
            when(eventsChecker.validate(ORDERS_SOUGHT, caseData)).thenReturn(ordersNeededErrors);
            when(eventsChecker.validate(HEARING_URGENCY, caseData)).thenReturn(hearingNeededErrors);
            when(eventsChecker.validate(GROUNDS, caseData)).thenReturn(groundsErrors);
            when(eventsChecker.validate(ORGANISATION_DETAILS, caseData)).thenReturn(applicantErrors);
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
                "• Applicant error 1",
                "• Applicant error 2",
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
    }
}
