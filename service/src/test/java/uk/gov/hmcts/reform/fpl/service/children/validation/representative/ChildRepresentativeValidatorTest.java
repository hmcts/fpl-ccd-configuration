package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class ChildRepresentativeValidatorTest {
    private static final String EMAIL = "some@email.com";
    private static final RespondentSolicitor REPRESENTATIVE = mock(RespondentSolicitor.class);
    private static final ChildParty PARTY = mock(ChildParty.class);
    private static final Child CHILD = mock(Child.class);
    private static final Element<Child> CHILD_ELEMENT = element(CHILD);
    private static final List<Element<Child>> CHILDREN = List.of(CHILD_ELEMENT);

    private final ValidateEmailService emailValidator = mock(ValidateEmailService.class);
    private final ChildRepresentativeValidator underTest = new ChildRepresentativeValidator(
        emailValidator
    );

    @BeforeEach
    void setUp() {
        when(CHILD.getParty()).thenReturn(PARTY);
        when(PARTY.getFullName()).thenReturn("Dave Davidson");
    }

    @Test
    void validateChildRepresentationDetailsWithUsingMainRepresentative() {
        CaseData caseData = CaseData.builder()
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childRepresentationDetails0(ChildRepresentationDetails.builder().useMainSolicitor("Yes").build())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEmpty();
    }

    @Test
    void validateNoRepresentation() {
        CaseData caseData = CaseData.builder()
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder().build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(
           "Confirm Dave Davidson’s legal representation"
        ));
    }

    @Test
    void validateChildRepresentationDetailsWithUsingEmptyFields() {
        when(REPRESENTATIVE.hasOrganisationDetails()).thenReturn(false);
        when(REPRESENTATIVE.hasFullName()).thenReturn(false);
        when(REPRESENTATIVE.getEmail()).thenReturn("");

        CaseData caseData = CaseData.builder()
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childRepresentationDetails0(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(REPRESENTATIVE)
                    .build())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of(
            "Add the full name of Dave Davidson's legal representative",
            "Add the email address of Dave Davidson's legal representative",
            "Add the organisation details for Dave Davidson's legal representative"
        ));
    }

    @Test
    void validateChildRepresentationDetailsWithInvalidEmail() {
        when(REPRESENTATIVE.hasOrganisationDetails()).thenReturn(true);
        when(REPRESENTATIVE.hasFullName()).thenReturn(true);
        when(REPRESENTATIVE.getEmail()).thenReturn(EMAIL);

        when(emailValidator.validate(eq(EMAIL), anyString())).thenReturn(Optional.of("bad email"));

        CaseData caseData = CaseData.builder()
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childRepresentationDetails0(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(REPRESENTATIVE)
                    .build())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEqualTo(List.of("bad email"));
    }

    @Test
    void validateChildRepresentationDetailsWithValidEmail() {
        when(REPRESENTATIVE.hasOrganisationDetails()).thenReturn(true);
        when(REPRESENTATIVE.hasFullName()).thenReturn(true);
        when(REPRESENTATIVE.getEmail()).thenReturn(EMAIL);

        when(emailValidator.validate(eq(EMAIL), anyString())).thenReturn(Optional.empty());

        CaseData caseData = CaseData.builder()
            .children1(CHILDREN)
            .childrenEventData(ChildrenEventData.builder()
                .childRepresentationDetails0(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(REPRESENTATIVE)
                    .build())
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEmpty();
    }
}
