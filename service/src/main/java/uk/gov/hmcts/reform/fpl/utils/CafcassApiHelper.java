package uk.gov.hmcts.reform.fpl.utils;

import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiAddress;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

public class CafcassApiHelper {
    public static CafcassApiAddress getCafcassApiAddress(Address address) {
        return Optional.ofNullable(address)
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
        CafcassApiSolicitor.CafcassApiSolicitorBuilder builder = CafcassApiSolicitor.builder();
        if (respondentSolicitor != null) {
            builder = builder.email(respondentSolicitor.getEmail());
            builder = builder.firstName(respondentSolicitor.getFirstName());
            builder = builder.lastName(respondentSolicitor.getLastName());

            if (respondentSolicitor.getOrganisation() != null) {
                builder = builder.organisationId(respondentSolicitor.getOrganisation().getOrganisationID());
                builder = builder.organisationName(respondentSolicitor.getOrganisation().getOrganisationName());
            }
        }

        return builder.build();
    }
}
