package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiAddress;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiColleague;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.utils.CafcassApiHelper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public abstract class CafcassApiConverterTestBase {
    CafcassApiCaseDataConverter underTest;

    private CafcassApiConverterTestBase() {
    }

    CafcassApiConverterTestBase(CafcassApiCaseDataConverter converterUnderTest) {
        underTest = converterUnderTest;
    }

    public CafcassApiCaseData testConvert(CaseData caseData, CafcassApiCaseData expected) {
        CafcassApiCaseData actual = underTest.convert(caseData, CafcassApiCaseData.builder()).build();
        assertEquals(actual, expected);
        return actual;
    }

    public CafcassApiCaseData testConvert(CaseData caseData) {
        return underTest.convert(caseData, CafcassApiCaseData.builder()).build();
    }

    public static Address getTestAddress() {
        return getTestAddress("address line 1");
    }

    public static Address getTestAddress(String line1) {
        return Address.builder().addressLine1(line1).build();
    }

    public static CafcassApiAddress getExpectedAddress() {
        return getExpectedAddress("address line 1");
    }

    public static CafcassApiAddress getExpectedAddress(String line1) {
        return CafcassApiHelper.getCafcassApiAddress(getTestAddress(line1));
    }

    public static Colleague getTestColleague() {
        return getTestColleague(1);
    }

    public static Colleague getTestColleague(int colleagueNo) {
        return Colleague.builder()
            .role(ColleagueRole.SOLICITOR)
            .title("Colleague" + colleagueNo + "'s title'")
            .email("colleague@test.com")
            .fullName("Colleague " + colleagueNo)
            .mainContact(YesNo.YES.getValue())
            .notificationRecipient(YesNo.YES.getValue())
            .build();
    }

    public static CafcassApiColleague getExpectedColleague() {
        return getExpectedColleague(1);
    }

    public static CafcassApiColleague getExpectedColleague(int colleagueNo) {
        return CafcassApiColleague.builder()
            .role(ColleagueRole.SOLICITOR.toString())
            .title("Colleague" + colleagueNo + "'s title'")
            .email("colleague@test.com")
            .fullName("Colleague " + colleagueNo)
            .mainContact(true)
            .notificationRecipient(true)
            .build();
    }

    public static Telephone getTestTelephone() {
        return Telephone.builder()
            .telephoneNumber("0123456789")
            .contactDirection("contactDirection")
            .telephoneUsageType("telephoneUsageType")
            .build();
    }

    public static String getExpectedTelephone() {
        return "0123456789";
    }

    public static Organisation getTestOrganisation() {
        return Organisation.builder()
            .organisationID("ORGID")
            .organisationName("organisationName")
            .build();
    }

    public static RespondentSolicitor getTestRespondentSolicitor() {
        return getTestRespondentSolicitor(1);
    }

    public static RespondentSolicitor getTestRespondentSolicitor(int solicitorNo) {
        return RespondentSolicitor.builder()
            .firstName("Solicitor" + solicitorNo + "FirstName")
            .lastName("Solicitor" + solicitorNo + "LastName")
            .email("respondentSolicitor" + solicitorNo + "@test.com")
            .telephoneNumber(getTestTelephone())
            .regionalOfficeAddress(getTestAddress("Solicitor " + solicitorNo + "'s address"))
            .organisation(getTestOrganisation())
            .colleaguesToBeNotified(wrapElements(getTestColleague(1),
                getTestColleague(2)))
            .build();
    }

    public static CafcassApiSolicitor getExpectedSolicitor() {
        return getExpectedSolicitor(1);
    }

    public static CafcassApiSolicitor getExpectedSolicitor(int solicitorNo) {
        Organisation testOrg = getTestOrganisation();
        return CafcassApiSolicitor.builder()
            .firstName("Solicitor" + solicitorNo + "FirstName")
            .lastName("Solicitor" + solicitorNo + "LastName")
            .email("respondentSolicitor" + solicitorNo + "@test.com")
            .organisationId(testOrg.getOrganisationID())
            .organisationName(testOrg.getOrganisationName())
            .build();
    }

    public static DocumentReference getTestDocumentReference() {
        UUID docId = UUID.randomUUID();
        return DocumentReference.builder()
            .filename(randomAlphanumeric(10))
            .url("test.link/" + docId)
            .binaryUrl("test.link/" + docId + "/binary")
            .uploadedTimestamp(LocalDateTime.now())
            .build();
    }
}
