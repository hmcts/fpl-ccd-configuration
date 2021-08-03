package uk.gov.hmcts.reform.fpl.service.representative;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.UnregisteredOrganisation;

import static org.assertj.core.api.Assertions.assertThat;

class RepresentativeSolicitorSanitizerTest {

    private static final Organisation POPULATED_ORGANISATION = Organisation.builder().organisationID("value").build();
    private static final Organisation BLANK_ORGANISATION = Organisation.builder().build();
    private static final Address POPULATED_ADDRESS = Address.builder().addressLine1("value").build();
    private static final Address BLANK_ADDRESS = Address.builder().build();
    private static final UnregisteredOrganisation POPULATED_UNREGISTERED_ORG = UnregisteredOrganisation.builder()
        .address(POPULATED_ADDRESS)
        .build();
    private static final UnregisteredOrganisation BLANK_UNREGISTERED_ORG = UnregisteredOrganisation.builder()
        .address(BLANK_ADDRESS)
        .build();

    private final RepresentativeSolicitorSanitizer underTest = new RepresentativeSolicitorSanitizer();

    @Test
    void sanitizeOrganisation() {
        RespondentSolicitor sanitized = underTest.sanitize(RespondentSolicitor.builder()
            .organisation(null)
            .regionalOfficeAddress(POPULATED_ADDRESS)
            .unregisteredOrganisation(POPULATED_UNREGISTERED_ORG)
            .build()
        );

        assertThat(sanitized).isEqualTo(RespondentSolicitor.builder()
            .organisation(BLANK_ORGANISATION)
            .regionalOfficeAddress(POPULATED_ADDRESS)
            .unregisteredOrganisation(POPULATED_UNREGISTERED_ORG)
            .build()
        );
    }

    @Test
    void sanitizeUnregisteredOrganisation() {
        RespondentSolicitor sanitized = underTest.sanitize(RespondentSolicitor.builder()
            .organisation(POPULATED_ORGANISATION)
            .regionalOfficeAddress(POPULATED_ADDRESS)
            .unregisteredOrganisation(null)
            .build()
        );

        assertThat(sanitized).isEqualTo(RespondentSolicitor.builder()
            .organisation(POPULATED_ORGANISATION)
            .regionalOfficeAddress(POPULATED_ADDRESS)
            .unregisteredOrganisation(BLANK_UNREGISTERED_ORG)
            .build()
        );
    }

    @Test
    void sanitizeAddress() {
        RespondentSolicitor sanitized = underTest.sanitize(RespondentSolicitor.builder()
            .organisation(POPULATED_ORGANISATION)
            .regionalOfficeAddress(null)
            .unregisteredOrganisation(POPULATED_UNREGISTERED_ORG)
            .build()
        );

        assertThat(sanitized).isEqualTo(RespondentSolicitor.builder()
            .organisation(POPULATED_ORGANISATION)
            .regionalOfficeAddress(BLANK_ADDRESS)
            .unregisteredOrganisation(POPULATED_UNREGISTERED_ORG)
            .build()
        );
    }
}
