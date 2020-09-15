package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {LocalAuthorityValidationService.class, OrganisationService.class})
public class LocalAuthorityValidationServiceTest {

    @Autowired
    private LocalAuthorityValidationService validationService;

    @MockBean
    private OrganisationService organisationService;

    @Test
    void shouldSuccessfullyValidateWhenLaIsOnboarded() {
        String localAuthorityCode = "SA";
        String userId = "a3850cb6-36ce-4612-b8c0-da00d57f1537";

        given(organisationService.findUserIdsInSameOrganisation(localAuthorityCode)).willReturn(Set.of(userId));

        final List<String> validationErrors = validationService.validateIfLaIsOnboarded(localAuthorityCode, userId);

        assertThat(validationErrors).isEmpty();
    }
}
