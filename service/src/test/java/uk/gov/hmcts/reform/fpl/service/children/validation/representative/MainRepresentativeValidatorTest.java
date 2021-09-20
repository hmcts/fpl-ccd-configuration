package uk.gov.hmcts.reform.fpl.service.children.validation.representative;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.event.ChildrenEventData;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainRepresentativeValidatorTest {
    private static final String EMAIL = "some@email.com";
    private static final RespondentSolicitor REPRESENTATIVE = mock(RespondentSolicitor.class);

    private final ValidateEmailService emailValidator = mock(ValidateEmailService.class);

    private final MainRepresentativeValidator underTest = new MainRepresentativeValidator(emailValidator);

    @Test
    void validateMainChildRepresentativeNoMainRepresentative() {
        CaseData caseData = CaseData.builder()
            .childrenEventData(ChildrenEventData.builder()
                .childrenHaveRepresentation("No")
                .build())
            .build();

        assertThat(underTest.validate(caseData)).isEmpty();
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

        assertThat(underTest.validate(caseData)).isEqualTo(List.of("bad email"));
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

        assertThat(underTest.validate(caseData)).isEmpty();
    }
}
