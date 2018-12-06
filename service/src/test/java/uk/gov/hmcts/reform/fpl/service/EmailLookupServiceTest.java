package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.UserEmailLookupConfiguration;

import java.util.List;

import static org.mockito.BDDMockito.given;

@ExtendWith(SpringExtension.class)
class EmailLookupServiceTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String HMCTS_EMAIL = "user@example.com";

    @Mock
    private UserEmailLookupConfiguration userEmailLookupConfiguration;

    @InjectMocks
    private EmailLookUpService emailLookUpService;

    @Test
    void shouldReturnHmctsEmail() {
        given(userEmailLookupConfiguration.getLookupTable()).willReturn(
            ImmutableMap.<String, List<String>>builder()
                .put(LOCAL_AUTHORITY_CODE, ImmutableList.<String>builder()
                    .add(HMCTS_EMAIL)
                    .build())
                .build()
        );

        List<String> emails = emailLookUpService.getEmails(LOCAL_AUTHORITY_CODE);

        Assertions.assertThat(emails).contains(HMCTS_EMAIL);
    }

    @Test
    void shouldThrowNullPointerExceptionWhenLocalAuthorityCodeIsNull() {
        Assertions.assertThatThrownBy(() -> emailLookUpService.getEmails(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("Case does not have local authority assigned");
    }
}
