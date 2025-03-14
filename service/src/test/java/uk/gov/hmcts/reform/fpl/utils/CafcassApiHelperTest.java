package uk.gov.hmcts.reform.fpl.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiAddress;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.robotics.Gender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CafcassApiHelperTest {
    @Test
    public void testGetCafcassApiAddress() {
        Address address = Address.builder()
            .addressLine1("Address Line 1")
            .addressLine2("Address Line 2")
            .addressLine3("Address Line 3")
            .postTown("Post Town")
            .county("County")
            .postcode("A123 B123")
            .country("UK")
            .build();

        CafcassApiAddress expected = CafcassApiAddress.builder()
            .addressLine1(address.getAddressLine1())
            .addressLine2(address.getAddressLine2())
            .addressLine3(address.getAddressLine3())
            .postTown(address.getPostTown())
            .county(address.getCounty())
            .postcode(address.getPostcode())
            .country(address.getCountry())
            .build();

        CafcassApiAddress actual = CafcassApiHelper.getCafcassApiAddress(address);
        assertEquals(expected, actual);
        assertNull(CafcassApiHelper.getCafcassApiAddress(null));
        assertNull(CafcassApiHelper.getCafcassApiAddress(Address.builder().build()));
    }

    @Test
    public void testIsYes() {
        assertTrue(CafcassApiHelper.isYes("yes"));
        assertFalse(CafcassApiHelper.isYes("no"));
        assertNull(CafcassApiHelper.isYes(null));
        assertNull(CafcassApiHelper.isYes(""));
    }

    @Test
    public void testConvertAddressKnow() {
        assertTrue(CafcassApiHelper.convertAddressKnow(IsAddressKnowType.YES));
        assertTrue(CafcassApiHelper.convertAddressKnow(IsAddressKnowType.LIVE_IN_REFUGE));
        assertFalse(CafcassApiHelper.convertAddressKnow(IsAddressKnowType.NO));
        assertNull(CafcassApiHelper.convertAddressKnow(null));
    }

    @Test
    public void testGetTelephoneNumber() {
        Telephone telephone = TestDataHelper.testTelephone();
        assertEquals(telephone.getTelephoneNumber(), CafcassApiHelper.getTelephoneNumber(telephone));
        assertNull(CafcassApiHelper.getTelephoneNumber(null));
    }

    @Test
    public void testGetCafcassApiSolicitor() {
        RespondentSolicitor solicitor = RespondentSolicitor.builder()
            .email("solicitor@test.com")
            .firstName("SolicitorFirstName")
            .lastName("SolicitorLastName")
            .unregisteredOrganisation(UnregisteredOrganisation.builder().build())
            .organisation(Organisation.builder()
                .organisationID("organisation ID")
                .organisationName("organisation name")
                .build())
            .build();

        CafcassApiSolicitor expected = CafcassApiSolicitor.builder()
            .email(solicitor.getEmail())
            .firstName(solicitor.getFirstName())
            .lastName(solicitor.getLastName())
            .organisationId(solicitor.getOrganisation().getOrganisationID())
            .organisationName(solicitor.getOrganisation().getOrganisationName())
            .build();

        assertEquals(expected, CafcassApiHelper.getCafcassApiSolicitor(solicitor));
        assertNull(CafcassApiHelper.getCafcassApiSolicitor(null));
        assertNull(CafcassApiHelper.getCafcassApiSolicitor(RespondentSolicitor.builder().build()));
    }

    @Test
    public void testGetCafcassApiSolicitorIfUnregistered() {
        RespondentSolicitor solicitor = RespondentSolicitor.builder()
            .email("solicitor@test.com")
            .firstName("SolicitorFirstName")
            .lastName("SolicitorLastName")
            .unregisteredOrganisation(UnregisteredOrganisation.builder()
                .name("organisation name")
                .build())
            .organisation(Organisation.builder().build())
            .build();

        CafcassApiSolicitor expected = CafcassApiSolicitor.builder()
            .email(solicitor.getEmail())
            .firstName(solicitor.getFirstName())
            .lastName(solicitor.getLastName())
            .organisationName(solicitor.getUnregisteredOrganisation().getName())
            .build();

        assertEquals(expected, CafcassApiHelper.getCafcassApiSolicitor(solicitor));
    }



    @Test
    public void testGetCafcassApiSolicitorWithoutOrganisationDetails() {
        RespondentSolicitor solicitor = RespondentSolicitor.builder()
            .email("solicitor@test.com")
            .firstName("SolicitorFirstName")
            .lastName("SolicitorLastName")
            .build();

        CafcassApiSolicitor expected = CafcassApiSolicitor.builder()
            .email(solicitor.getEmail())
            .firstName(solicitor.getFirstName())
            .lastName(solicitor.getLastName())
            .build();

        assertEquals(expected, CafcassApiHelper.getCafcassApiSolicitor(solicitor));
    }

    @Test
    public void testGetGenderForApiResponse() {
        assertEquals("MALE", CafcassApiHelper.getGenderForApiResponse(Gender.MALE.getValue()));
        assertEquals("MALE", CafcassApiHelper.getGenderForApiResponse(Gender.MALE.toString()));
        assertEquals("MALE", CafcassApiHelper.getGenderForApiResponse(Gender.MALE.getLabel()));
        assertEquals("FEMALE", CafcassApiHelper.getGenderForApiResponse(Gender.FEMALE.getValue()));
        assertEquals("FEMALE", CafcassApiHelper.getGenderForApiResponse(Gender.FEMALE.toString()));
        assertEquals("FEMALE", CafcassApiHelper.getGenderForApiResponse(Gender.FEMALE.getLabel()));
        assertEquals("OTHER", CafcassApiHelper.getGenderForApiResponse(Gender.OTHER.getValue()));
        assertEquals("OTHER", CafcassApiHelper.getGenderForApiResponse(Gender.OTHER.toString()));
        assertEquals("OTHER", CafcassApiHelper.getGenderForApiResponse(Gender.OTHER.getLabel()));
        assertEquals(null,  CafcassApiHelper.getGenderForApiResponse(""));
        assertEquals(null,  CafcassApiHelper.getGenderForApiResponse(null));
        assertEquals(null, CafcassApiHelper.getGenderForApiResponse(Gender.NONE.getLabel()));
    }
}
