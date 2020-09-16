package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalAuthorityValidationService.class, OrganisationService.class})
class LocalAuthorityValidationServiceTest {

    @Autowired
    private LocalAuthorityValidationService validationService;

    @MockBean
    private OrganisationService organisationService;

    private static final String LOCAL_AUTHORITY_CODE = "SA";
    private static final String USER_ID = "a3850cb6-36ce-4612-b8c0-da00d57f1537";

    @Test
    void shouldSuccessfullyValidateWhenLaIsOnboarded() {
        given(organisationService.findUserIdsInSameOrganisation(LOCAL_AUTHORITY_CODE)).willReturn(Set.of(USER_ID));

        final List<String> validationErrors = validationService.validateIfLaIsOnboarded(LOCAL_AUTHORITY_CODE, USER_ID);

        assertThat(validationErrors).isEmpty();
    }

    @Test
    void shouldNotValidateWhenLaHasNotBeenOnboarded() {
        given(organisationService.findUserIdsInSameOrganisation(LOCAL_AUTHORITY_CODE)).willReturn(Set.of(""));

        List<String> errors = new ArrayList<>();
        errors.add("Register for an account");
        errors.add("You cannot start an online application until you’re fully registered.");
        errors.add("Press the back button on your browser to access the link.");

        final List<String> validationErrors = validationService.validateIfLaIsOnboarded(LOCAL_AUTHORITY_CODE, USER_ID);

        assertThat(validationErrors).isEqualTo(errors);
    }
}
