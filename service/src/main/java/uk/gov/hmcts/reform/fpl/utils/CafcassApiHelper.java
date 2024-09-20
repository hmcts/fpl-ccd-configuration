package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiAddress;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.robotics.Gender;

import java.util.Optional;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

public class CafcassApiHelper {
    private static final CafcassApiAddress EMPTY_ADDRESS = CafcassApiAddress.builder().build();
    private static final CafcassApiSolicitor EMPTY_SOLICITOR = CafcassApiSolicitor.builder().build();

    public static CafcassApiAddress getCafcassApiAddress(Address address) {
        CafcassApiAddress cafcassApiAddress = Optional.ofNullable(address)
            .map(add -> CafcassApiAddress.builder()
                .addressLine1(add.getAddressLine1())
                .addressLine2(add.getAddressLine2())
                .addressLine3(add.getAddressLine3())
                .postTown(add.getPostTown())
                .county(add.getCounty())
                .postcode(add.getPostcode())
                .country(add.getCountry())
                .build())
            .orElse(null);

        return EMPTY_ADDRESS.equals(cafcassApiAddress) ? null : cafcassApiAddress;
    }

    public static boolean isYes(String yesNo) {
        return YES.getValue().equalsIgnoreCase(yesNo);
    }

    public static String getTelephoneNumber(Telephone telephone) {
        return Optional.ofNullable(telephone)
            .map(Telephone::getTelephoneNumber)
            .orElse(null);
    }

    public static CafcassApiSolicitor getCafcassApiSolicitor(RespondentSolicitor respondentSolicitor) {
        CafcassApiSolicitor cafcassApiSolicitor = Optional.ofNullable(respondentSolicitor)
            .map(solicitor  -> {
                CafcassApiSolicitor.CafcassApiSolicitorBuilder builder = CafcassApiSolicitor.builder()
                    .email(solicitor.getEmail())
                    .firstName(solicitor.getFirstName())
                    .lastName(solicitor.getLastName());

                if (solicitor.getOrganisation() != null) {
                    builder = builder.organisationId(solicitor.getOrganisation().getOrganisationID())
                        .organisationName(solicitor.getOrganisation().getOrganisationName());
                }
                return builder.build();
            })
            .orElse(null);

        return EMPTY_SOLICITOR.equals(cafcassApiSolicitor) ? null : cafcassApiSolicitor;
    }

    public static String getGenderForApiResponse(String genderStr) {
        return (isEmpty(genderStr)) ? null :
            Stream.of(Gender.MALE, Gender.FEMALE, Gender.OTHER)
                .filter(gender -> gender.toString().equalsIgnoreCase(genderStr)
                                  || gender.getLabel().equalsIgnoreCase(genderStr)
                                  || gender.getValue().equalsIgnoreCase(genderStr))
                .findFirst()
                .map(Gender::getValue)
                .orElse(Gender.OTHER.getValue());
    }
}
