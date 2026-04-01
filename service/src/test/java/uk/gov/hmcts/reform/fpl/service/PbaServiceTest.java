package uk.gov.hmcts.reform.fpl.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.rd.client.OrganisationApi;
import uk.gov.hmcts.reform.rd.model.Organisation;
import uk.gov.hmcts.reform.rd.model.PbaOrganisationResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.apache.http.HttpStatus.SC_GATEWAY_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.feignException;

@ExtendWith(MockitoExtension.class)
class PbaServiceTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private UserService userService;

    @Mock
    private OrganisationApi pbaRefDataClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private DynamicListService dynamicListService;

    @InjectMocks
    private PbaService pbaService;

    protected static final String USER_AUTH_TOKEN = "Bearer token";
    private static final String SERVICE_AUTH_TOKEN = "Bearer authorised service";
    private static final String USER_EMAIL = "test@test.com";
    private static final String USER_ID = "12345678";
    private static final PbaOrganisationResponse POPULATED_ORGANISATION_RESPONSE = buildOrganisation();
    private static final List<String> PAYMENT_ACCOUNTS = List.of("PBA1234567", "PBA7654321");
    private static final DynamicList PBA_NUMBER_DYNAMIC_LIST = DynamicList.builder()
        .listItems(List.of(
            DynamicListElement.builder()
                .code("PBA1234567")
            .build(),
            DynamicListElement.builder()
                .code("PBA7654321")
            .build()))
        .build();

    @BeforeEach
    void setup() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(USER_AUTH_TOKEN);
        when(userService.getUserEmail()).thenReturn(USER_EMAIL);
    }

    @Test
    void shouldFindPbaNumbersIfUserRegisteredToOrganisation() {
        when(dynamicListService.asDynamicList(eq(PAYMENT_ACCOUNTS), eq(""), any(), any()))
            .thenReturn(PBA_NUMBER_DYNAMIC_LIST);
        when(pbaRefDataClient.retrievePbaNumbers(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_EMAIL))
            .thenReturn(POPULATED_ORGANISATION_RESPONSE);

        DynamicList pbaDynamicList = pbaService.populatePbaDynamicList("");

        assertThat(pbaDynamicList).isEqualTo(PBA_NUMBER_DYNAMIC_LIST);
    }

    @Test
    void shouldReturnEmptyListIfNoPbaFound() {
        PbaOrganisationResponse pbaOrganisationResponse = PbaOrganisationResponse.builder()
            .organisationEntityResponse(Organisation.builder()
                .name("Organisation")
                .organisationIdentifier(UUID.randomUUID().toString())
                .build())
            .build();

        when(pbaRefDataClient.retrievePbaNumbers(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_EMAIL))
            .thenReturn(pbaOrganisationResponse);
        when(userService.getUserInfo()).thenReturn(UserInfo.builder().uid(USER_ID).build());

        Optional<List<String>> pbaNumbers = pbaService.retrievePbaNumbers();
        assertThat(pbaNumbers).isEmpty();
    }

    @Test
    void shouldRethrowExceptionOtherThanNotFound() {
        Exception expectedException = feignException(SC_GATEWAY_TIMEOUT);

        when(pbaRefDataClient.retrievePbaNumbers(USER_AUTH_TOKEN, SERVICE_AUTH_TOKEN, USER_EMAIL))
            .thenThrow(expectedException);

        Exception actualException = assertThrows(Exception.class, pbaService::retrievePbaNumbers);

        assertThat(actualException).isEqualTo(expectedException);
    }

    private static PbaOrganisationResponse buildOrganisation() {
        return PbaOrganisationResponse.builder().organisationEntityResponse(Organisation.builder()
                .name("Organisation")
                .organisationIdentifier(UUID.randomUUID().toString())
                .paymentAccount(List.of("PBA1234567", "PBA7654321"))
                .build())
            .build();
    }
}
