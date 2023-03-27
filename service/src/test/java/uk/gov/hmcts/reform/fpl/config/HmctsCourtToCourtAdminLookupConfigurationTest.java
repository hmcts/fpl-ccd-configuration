package uk.gov.hmcts.reform.fpl.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmctsCourtToCourtAdminLookupConfigurationTest {

    private HmctsCourtToCourtAdminLookupConfiguration underTest;

    @BeforeEach
    void setUp() {
        underTest = new HmctsCourtToCourtAdminLookupConfiguration(
            "344=>FamilyPublicLaw+ctsc@gmail.com;332=>FamilyPublicLaw+PublicLawEmail@gmail.com"
        );
    }

    @Test
    void shouldReturnEmailIdWhenCourtMappingPresent() {
        String email = underTest.getEmail("344");
        assertThat(email).isEqualTo("FamilyPublicLaw+ctsc@gmail.com");
    }

    @Test
    void shouldThrowExceptionWhenCourtMappingPresent() {
        assertThatThrownBy(() -> underTest.getEmail("111"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Court admin email not found for court code 111");

    }

}
