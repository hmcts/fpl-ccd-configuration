package uk.gov.hmcts.reform.fpl.service.children;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.children.ChildRepresentationDetails;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class ChildRepresentativeSolicitorValidatorTest {
    private static final String EMAIL = "some@email.com";
    private static final RespondentSolicitor REPRESENTATIVE = mock(RespondentSolicitor.class);

    private final ValidateEmailService emailValidator = mock(ValidateEmailService.class);
    private final ChildRepresentativeSolicitorValidator underTest = new ChildRepresentativeSolicitorValidator(
        emailValidator
    );

    @Test
    void validateChildRepresentationDetailsWithAllUsingMainRepresentative() {
        CaseData caseData = CaseData.builder()
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveSameRepresentation("Yes")
                .build())
            .build();

        assertThat(underTest.validateChildRepresentationDetails(caseData)).isEmpty();
    }

    @Test
    void validateChildRepresentationDetailsWithUsingMainRepresentative() {
        CaseData caseData = CaseData.builder()
            .children1(wrapElements(mock(Child.class)))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(ChildRepresentationDetails.builder().useMainSolicitor("Yes").build())
                .build())
            .build();

        assertThat(underTest.validateChildRepresentationDetails(caseData)).isEmpty();
    }

    @Test
    void validateChildRepresentationDetailsWithUsingEmptyFields() {
        when(REPRESENTATIVE.hasOrganisationDetails()).thenReturn(false);
        when(REPRESENTATIVE.hasFullName()).thenReturn(false);
        when(REPRESENTATIVE.getEmail()).thenReturn("");

        CaseData caseData = CaseData.builder()
            .children1(wrapElements(mock(Child.class)))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(REPRESENTATIVE)
                    .build())
                .build())
            .build();

        assertThat(underTest.validateChildRepresentationDetails(caseData)).isEqualTo(List.of(
            "Add the full name of child 1's legal representative",
            "Add the email address of child 1's legal representative",
            "Add the organisation details for child 1's legal representative"
        ));
    }

    @Test
    void validateChildRepresentationDetailsWithInvalidEmail() {
        when(REPRESENTATIVE.hasOrganisationDetails()).thenReturn(true);
        when(REPRESENTATIVE.hasFullName()).thenReturn(true);
        when(REPRESENTATIVE.getEmail()).thenReturn(EMAIL);
        when(emailValidator.validate(eq(EMAIL), anyString())).thenReturn(Optional.of("bad email"));

        CaseData caseData = CaseData.builder()
            .children1(wrapElements(mock(Child.class)))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(REPRESENTATIVE)
                    .build())
                .build())
            .build();

        assertThat(underTest.validateChildRepresentationDetails(caseData)).isEqualTo(List.of("bad email"));
    }

    @Test
    void validateChildRepresentationDetailsWithValidEmail() {
        when(REPRESENTATIVE.hasOrganisationDetails()).thenReturn(true);
        when(REPRESENTATIVE.hasFullName()).thenReturn(true);
        when(REPRESENTATIVE.getEmail()).thenReturn(EMAIL);
        when(emailValidator.validate(eq(EMAIL), anyString())).thenReturn(Optional.empty());

        CaseData caseData = CaseData.builder()
            .children1(wrapElements(mock(Child.class)))
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveSameRepresentation("No")
                .childRepresentationDetails0(ChildRepresentationDetails.builder()
                    .useMainSolicitor("No")
                    .solicitor(REPRESENTATIVE)
                    .build())
                .build())
            .build();

        assertThat(underTest.validateChildRepresentationDetails(caseData)).isEmpty();
    }

    @Test
    void validateMainChildRepresentativeNoMainRepresentative() {
        CaseData caseData = CaseData.builder()
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("No")
                .build())
            .build();

        assertThat(underTest.validateMainChildRepresentative(caseData)).isEmpty();
    }

    @Test
    void validateMainChildRepresentativeWithMainRepresentativeInvalidEmail() {
        when(REPRESENTATIVE.getEmail()).thenReturn(EMAIL);
        when(emailValidator.validate(eq(EMAIL), anyString())).thenReturn(Optional.of("bad email"));

        CaseData caseData = CaseData.builder()
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(REPRESENTATIVE)
                .build())
            .build();

        assertThat(underTest.validateMainChildRepresentative(caseData)).isEqualTo(List.of("bad email"));
    }

    @Test
    void validateMainChildRepresentativeWithMainRepresentativeValidEmail() {
        when(REPRESENTATIVE.getEmail()).thenReturn(EMAIL);
        when(emailValidator.validate(eq(EMAIL), anyString())).thenReturn(Optional.empty());

        CaseData caseData = CaseData.builder()
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("Yes")
                .childrenMainRepresentative(REPRESENTATIVE)
                .build())
            .build();

        assertThat(underTest.validateMainChildRepresentative(caseData)).isEmpty();
    }
}
