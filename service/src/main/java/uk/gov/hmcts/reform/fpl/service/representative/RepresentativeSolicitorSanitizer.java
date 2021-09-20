package uk.gov.hmcts.reform.fpl.service.representative;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@Component
public class RepresentativeSolicitorSanitizer {

    public static final Organisation ORGANISATION = Organisation.builder().build();
    public static final Address ADDRESS = Address.builder().build();
    public static final UnregisteredOrganisation UNREGISTERED_ORGANISATION = UnregisteredOrganisation.builder()
        .address(ADDRESS)
        .build();

    /**
     * It was noticed during the validation of {@link RespondentSolicitor} objects that CCD returns a populated but
     * empty object in the pre-existing case data
     * ({@link uk.gov.hmcts.reform.ccd.client.model.CallbackRequest#getCaseDetailsBefore()}).
     *
     * <p>As such to ensure that we can compare equality we update the object in this decorator to ensure that
     * they will be equal.
     *
     * <p>This involves performing:
     * <table>
     *     <tr>
     *         <th>Field</th>
     *         <th>Default Value</th>
     *         <th>Other Actions</th>
     *     </tr>
     *     <tr>
     *         <td>{@link RespondentSolicitor#getOrganisation()}</td>
     *         <td>{@link #ORGANISATION}</td>
     *         <td></td>
     *     </tr>
     *     <tr>
     *         <td>{@link RespondentSolicitor#getRegionalOfficeAddress()}</td>
     *         <td>{@link #ADDRESS}</td>
     *         <td>If a non null value is supplied then any blank field of the address is set to null</td>
     *     </tr>
     *     <tr>
     *         <td>{@link RespondentSolicitor#getUnregisteredOrganisation()}</td>
     *         <td>{@link #UNREGISTERED_ORGANISATION}</td>
     *         <td>If a non null value is supplied then the address is processed as discussed previously</td>
     *     </tr>
     * </table>
     *
     * <p>The original object is not updated and instead a new object is created and returned
     *
     * @param solicitor Value to be sanitized
     * @return A value that has been sanitized
     */
    public RespondentSolicitor sanitize(RespondentSolicitor solicitor) {
        return solicitor.toBuilder()
            .organisation(sanitize(solicitor.getOrganisation()))
            .regionalOfficeAddress(sanitize(solicitor.getRegionalOfficeAddress()))
            .unregisteredOrganisation(sanitize(solicitor.getUnregisteredOrganisation()))
            .build();
    }

    private Organisation sanitize(Organisation organisation) {
        return defaultIfNull(organisation, ORGANISATION);
    }

    private UnregisteredOrganisation sanitize(UnregisteredOrganisation organisation) {
        if (null == organisation) {
            return UNREGISTERED_ORGANISATION;
        }

        return organisation.toBuilder()
            .address(sanitize(organisation.getAddress()))
            .build();
    }

    private Address sanitize(Address address) {
        if (null == address) {
            return ADDRESS;
        }

        return Address.builder()
            .addressLine1(defaultIfBlank(address.getAddressLine1(), null))
            .addressLine2(defaultIfBlank(address.getAddressLine2(), null))
            .addressLine3(defaultIfBlank(address.getAddressLine3(), null))
            .postTown(defaultIfBlank(address.getPostTown(), null))
            .county(defaultIfBlank(address.getCounty(), null))
            .postcode(defaultIfBlank(address.getPostcode(), null))
            .country(defaultIfBlank(address.getCountry(), null))
            .build();
    }
}
