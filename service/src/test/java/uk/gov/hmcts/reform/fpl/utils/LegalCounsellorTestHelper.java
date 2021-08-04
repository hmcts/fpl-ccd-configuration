package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.Optional;

import static org.mockito.Mockito.when;

public class LegalCounsellorTestHelper {

    private LegalCounsellorTestHelper() {
        //NO-OP
    }

    public static Pair<String, LegalCounsellor> buildLegalCounsellorWithOrganisationAndMockUserId(
        OrganisationService mockOrganisationService,
        String uniqueIdentifier) {

        return buildLegalCounsellor(mockOrganisationService, uniqueIdentifier, true);
    }

    public static Pair<String, LegalCounsellor> buildLegalCounsellorAndMockUserId(
        OrganisationService mockOrganisationService,
        String uniqueIdentifier) {

        return buildLegalCounsellor(mockOrganisationService, uniqueIdentifier, false);
    }

    private static Pair<String, LegalCounsellor> buildLegalCounsellor(OrganisationService mockOrganisationService,
                                                                      String uniqueIdentifier,
                                                                      boolean addOrganisation) {
        LegalCounsellor legalCounsellor = LegalCounsellor.builder()
            .firstName("TestFirstName" + uniqueIdentifier)
            .lastName("TestLastName" + uniqueIdentifier)
            .email("legalcounsellor" + uniqueIdentifier + "@example.com")
            .build();

        if (addOrganisation) {
            legalCounsellor = legalCounsellor.toBuilder()
                .organisation(Organisation.organisation("123"))
                .build();
        }

        String userId = "testUserId" + uniqueIdentifier;
        when(mockOrganisationService.findUserByEmail(legalCounsellor.getEmail())).thenReturn(Optional.of(userId));

        return Pair.of(userId, legalCounsellor);
    }

}
