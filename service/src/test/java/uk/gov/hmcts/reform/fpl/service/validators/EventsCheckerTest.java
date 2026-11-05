package uk.gov.hmcts.reform.fpl.service.validators;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import uk.gov.hmcts.reform.fpl.enums.Event;
import uk.gov.hmcts.reform.fpl.model.CaseData;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.Event.ALLOCATION_PROPOSAL;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT_DETAILS_LA;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICANT_DETAILS_THIRD_PARTY;
import static uk.gov.hmcts.reform.fpl.enums.Event.APPLICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.fpl.enums.Event.CHILDREN;
import static uk.gov.hmcts.reform.fpl.enums.Event.COURT_SERVICES;
import static uk.gov.hmcts.reform.fpl.enums.Event.FACTORS_AFFECTING_PARENTING;
import static uk.gov.hmcts.reform.fpl.enums.Event.GROUNDS;
import static uk.gov.hmcts.reform.fpl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.fpl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.fpl.enums.Event.LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.ORDERS_SOUGHT;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.Event.RESPONDENTS_3RD_PARTY;
import static uk.gov.hmcts.reform.fpl.enums.Event.RISK_AND_HARM;
import static uk.gov.hmcts.reform.fpl.enums.Event.SELECT_COURT;
import static uk.gov.hmcts.reform.fpl.enums.Event.SUBMIT_APPLICATION;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {EventsChecker.class, LocalValidatorFactoryBean.class})
@TestInstance(PER_CLASS)
class EventsCheckerTest {
    @MockitoBean
    private DocumentsChecker documentsChecker;
    @MockitoBean
    private CaseNameChecker caseNameChecker;
    @MockitoBean
    private ChildrenChecker childrenChecker;
    @MockitoBean
    private RespondentsChecker respondentsChecker;
    @MockitoBean
    private Respondents3rdPartyChecker respondents3rdPartyChecker;
    @MockitoBean
    private HearingUrgencyChecker hearingUrgencyChecker;
    @MockitoBean
    private OrdersSoughtChecker ordersSoughtChecker;
    @MockitoBean
    private GroundsChecker groundsChecker;
    @MockitoBean(name = "localAuthorityDetailsChecker")
    private LocalAuthorityDetailsChecker localAuthorityDetailsChecker;
    @MockitoBean(name = "localAuthorityDetailsChecker")
    private ThirdPartyApplicantDetailsChecker thirdPartyApplicantDetailsChecker;
    @MockitoBean
    private AllocationProposalChecker allocationProposalChecker;
    @MockitoBean
    private CaseSubmissionChecker caseSubmissionChecker;
    @MockitoBean
    private RiskAndHarmChecker riskAndHarmChecker;
    @MockitoBean
    private ProceedingsChecker proceedingsChecker;
    @MockitoBean
    private InternationalElementChecker internationalElementChecker;
    @MockitoBean
    private OthersChecker othersChecker;
    @MockitoBean
    private CourtServiceChecker courtServiceChecker;
    @MockitoBean
    private FactorsAffectingParentingChecker factorsAffectingParentingChecker;
    @MockitoBean
    private ApplicationDocumentChecker applicationDocumentChecker;
    @MockitoBean
    private LanguageRequirementsChecker languageRequirementsChecker;
    @MockitoBean
    private CourtSelectionChecker courtSelectionChecker;
    @MockitoBean
    private C1WithSupplementChecker c1WithSupplementChecker;
    @Autowired
    private EventsChecker eventsChecker;

    private final CaseData caseData = CaseData.builder().build();

    @ParameterizedTest
    @MethodSource("getEventsValidators")
    void shouldValidateEvent(Event event, EventChecker validator) {
        final List<String> expectedErrors = List.of("Case name error");

        when(validator.validate(caseData)).thenReturn(expectedErrors);

        assertThat(eventsChecker.validate(event, caseData)).isEqualTo(expectedErrors);

        verify(validator).validate(caseData);
    }

    @ParameterizedTest
    @MethodSource("getEventsValidators")
    void shouldCheckEventIsCompletedEvent(Event event, EventChecker validator) {
        final boolean isCompleted = RandomUtils.nextBoolean();

        when(validator.isCompleted(caseData)).thenReturn(isCompleted);

        assertThat(eventsChecker.isCompleted(event, caseData)).isEqualTo(isCompleted);

        verify(validator).isCompleted(caseData);
    }

    @ParameterizedTest
    @MethodSource("getEventsValidators")
    void shouldCheckEventIsInProgress(Event event, EventChecker validator) {
        final boolean isInProgress = RandomUtils.nextBoolean();

        when(validator.isStarted(caseData)).thenReturn(isInProgress);

        assertThat(eventsChecker.isInProgress(event, caseData)).isEqualTo(isInProgress);

        verify(validator).isStarted(caseData);
    }

    @ParameterizedTest
    @MethodSource("getEventsValidators")
    void shouldCheckEventIsAvailableAndShouldNotValidateAgainstDocumentsChecker(Event event, EventChecker validator) {
        final boolean isAvailable = RandomUtils.nextBoolean();

        when(validator.isAvailable(caseData)).thenReturn(isAvailable);

        assertThat(eventsChecker.isAvailable(event, caseData)).isEqualTo(isAvailable);

        verify(validator).isAvailable(caseData);
        verify(documentsChecker, never()).validate(any());
    }

    @AfterEach
    void verifyNoMoreInteractionsWithValidators() {
        verifyNoMoreInteractions(
            caseNameChecker,
            childrenChecker,
            respondentsChecker,
            respondents3rdPartyChecker,
            hearingUrgencyChecker,
            ordersSoughtChecker,
            groundsChecker,
            localAuthorityDetailsChecker,
            thirdPartyApplicantDetailsChecker,
            allocationProposalChecker,
            applicationDocumentChecker,
            caseSubmissionChecker,
            riskAndHarmChecker,
            proceedingsChecker,
            internationalElementChecker,
            othersChecker,
            courtServiceChecker,
            factorsAffectingParentingChecker,
            languageRequirementsChecker,
            courtSelectionChecker,
            c1WithSupplementChecker);
    }

    private Stream<Arguments> getEventsValidators() {
        return Stream.of(
            Arguments.of(CASE_NAME, caseNameChecker),
            Arguments.of(CHILDREN, childrenChecker),
            Arguments.of(RESPONDENTS, respondentsChecker),
            Arguments.of(RESPONDENTS_3RD_PARTY, respondents3rdPartyChecker),
            Arguments.of(HEARING_URGENCY, hearingUrgencyChecker),
            Arguments.of(ORDERS_SOUGHT, ordersSoughtChecker),
            Arguments.of(GROUNDS, groundsChecker),
            Arguments.of(APPLICANT_DETAILS_LA, localAuthorityDetailsChecker),
            Arguments.of(APPLICANT_DETAILS_THIRD_PARTY, thirdPartyApplicantDetailsChecker),
            Arguments.of(ALLOCATION_PROPOSAL, allocationProposalChecker),
            Arguments.of(APPLICATION_DOCUMENTS, applicationDocumentChecker),
            Arguments.of(SUBMIT_APPLICATION, caseSubmissionChecker),
            Arguments.of(RISK_AND_HARM, riskAndHarmChecker),
            Arguments.of(OTHER_PROCEEDINGS, proceedingsChecker),
            Arguments.of(INTERNATIONAL_ELEMENT, internationalElementChecker),
            Arguments.of(OTHERS, othersChecker),
            Arguments.of(COURT_SERVICES, courtServiceChecker),
            Arguments.of(FACTORS_AFFECTING_PARENTING, factorsAffectingParentingChecker),
            Arguments.of(LANGUAGE_REQUIREMENTS, languageRequirementsChecker),
            Arguments.of(SELECT_COURT, courtSelectionChecker),
            Arguments.of(C1_WITH_SUPPLEMENT, c1WithSupplementChecker));
    }

}
