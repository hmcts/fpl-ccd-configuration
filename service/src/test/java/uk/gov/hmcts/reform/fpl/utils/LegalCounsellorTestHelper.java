package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.Optional;

import static org.mockito.Mockito.when;

public class LegalCounsellorTestHelper {

    private LegalCounsellorTestHelper() {
    }

    public static Pair<String, LegalCounsellor> buildLegalCounsellorAndMockUserId(
        OrganisationService mockOrganisationService,
        String uniqueIdentifier) {

        LegalCounsellor legalCounsellor = LegalCounsellor.builder()
            .firstName("TestFirstName" + uniqueIdentifier)
            .lastName("TestLastName" + uniqueIdentifier)
            .email("legalcounsellor" + uniqueIdentifier + "@example.com")
            .build();

        String userId = "testUserId" + uniqueIdentifier;
        when(mockOrganisationService.findUserByEmail(legalCounsellor.getEmail())).thenReturn(Optional.of(userId));

        return Pair.of(userId, legalCounsellor);
    }

}
