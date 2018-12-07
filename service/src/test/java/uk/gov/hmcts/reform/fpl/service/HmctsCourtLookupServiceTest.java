package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration.Court;

import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class HmctsCourtLookupServiceTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String HMCTS_EMAIL = "user@example.com";

    @Mock
    private HmctsCourtLookupConfiguration config;

    @InjectMocks
    private HmctsCourtLookUpService service;

    @Test
    void shouldReturnHmctsEmail() {
        given(config.getLookupTable()).willReturn(
            ImmutableMap.<String, Court>builder()
                .put(LOCAL_AUTHORITY_CODE, new Court("", HMCTS_EMAIL))
                .build()
        );

        String email = service.getCourt(LOCAL_AUTHORITY_CODE).getEmail();

        Assertions.assertThat(email).contains(HMCTS_EMAIL);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityCodeIsNull() {
        Assertions.assertThatThrownBy(() -> service.getCourt(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Case does not have local authority assigned");
    }

    @Test
    void shouldThrowNullPointerExceptionWhenCourtIsNull() {
        Assertions.assertThatThrownBy(() -> service.getCourt("FAKE"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Court information not found");
    }
}
