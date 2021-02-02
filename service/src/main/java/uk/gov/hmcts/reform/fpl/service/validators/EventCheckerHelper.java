package uk.gov.hmcts.reform.fpl.service.validators;

import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;

import java.util.stream.Stream;

public class EventCheckerHelper {

    private EventCheckerHelper() {
    }

    public static boolean isEmptyAddress(Address address) {
        return ObjectUtils.isEmpty(address) || allEmpty(
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getAddressLine3(),
                address.getPostTown(),
                address.getCounty(),
                address.getCountry(),
                address.getPostcode());
    }

    public static boolean isEmptyTelephone(Telephone telephone) {
        return ObjectUtils.isEmpty(telephone)
                || allEmpty(
                telephone.getTelephoneNumber(),
                telephone.getContactDirection(),
                telephone.getTelephoneUsageType());
    }

    public static boolean isEmptyEmail(EmailAddress email) {
        return ObjectUtils.isEmpty(email) || allEmpty(email.getEmail(), email.getEmailUsageType());
    }

    public static boolean allEmpty(Object... properties) {
        return Stream.of(properties).allMatch(ObjectUtils::isEmpty);
    }

    public static boolean anyEmpty(Object... properties) {
        return Stream.of(properties).anyMatch(ObjectUtils::isEmpty);
    }

    public static boolean anyNonEmpty(Object... properties) {
        return Stream.of(properties).anyMatch(ObjectUtils::isNotEmpty);
    }
}
