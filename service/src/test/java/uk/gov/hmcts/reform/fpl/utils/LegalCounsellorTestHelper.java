package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;

public class LegalCounsellorTestHelper {

    private LegalCounsellorTestHelper() {
        //NO-OP
    }

    public static LegalCounsellor buildLegalCounsellor(String uniqueIdentifier) {
        return LegalCounsellor.builder()
            .firstName("TestFirstName" + uniqueIdentifier)
            .lastName("TestLastName" + uniqueIdentifier)
            .email("legalcounsellor" + uniqueIdentifier + "@example.com")
            .userId("testUserId" + uniqueIdentifier)
            .organisation(Organisation.organisation("123"))
            .build();
    }

}
