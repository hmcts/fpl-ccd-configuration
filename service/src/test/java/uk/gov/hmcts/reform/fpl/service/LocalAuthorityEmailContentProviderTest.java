package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.HmctsCourtLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration.LocalAuthority;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, LocalAuthorityEmailContentProvider.class,
    DateFormatterService.class, HearingBookingService.class})
class LocalAuthorityEmailContentProviderTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_NAME = "Test local authority";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+la@gmail.com";

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @MockBean
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    @MockBean
    private HmctsCourtLookupConfiguration hmctsCourtLookupConfiguration;

    @Autowired
    private ObjectMapper mapper;

    private final DateFormatterService dateFormatterService = new DateFormatterService();
    private final HearingBookingService hearingBookingService = new HearingBookingService();

    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @BeforeEach
    void setup() {
        this.localAuthorityEmailContentProvider = new LocalAuthorityEmailContentProvider(
            localAuthorityNameLookupConfiguration, "", mapper, dateFormatterService,
            hearingBookingService);
    }

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() throws IOException {
        Map<String, Object> expectedMap = standardDirectionTemplateParameters();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS)));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Test local authority");

        assertThat(localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(populatedCaseDetails(),
                LOCAL_AUTHORITY_CODE)).isEqualTo(expectedMap);
    }

    @Test
    void shouldReturnExpectedMapWithNullSDODetails() {
        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(Optional.of(new LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS)));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Test local authority");

        assertThat(localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(
                CaseDetails.builder().data(ImmutableMap.of(
                    "respondents1", ImmutableList.of(
                        ImmutableMap.of(
                            "value", Respondent.builder()
                                .party(RespondentParty.builder()
                                    .lastName("Moley")
                                    .build())
                                .build())))).build(), LOCAL_AUTHORITY_CODE))
            .isEqualTo(emptyTemplateParameters());
    }

    private Map<String, Object> standardDirectionTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("title", LOCAL_AUTHORITY_NAME)
            .put("familyManCaseNumber", "12345,")
            .put("leadRespondentsName", "Smith,")
            .put("hearingDate", "1 January 2999")
            .put("reference", "12345")
            .put("caseUrl", "/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();
    }

    private Map<String, Object> emptyTemplateParameters() {
        return ImmutableMap.<String, Object>builder()
            .put("title", LOCAL_AUTHORITY_NAME)
            .put("familyManCaseNumber", "")
            .put("hearingDate", "")
            .put("leadRespondentsName", "Moley,")
            .put("reference", "null")
            .put("caseUrl", "/case/" + JURISDICTION + "/" + CASE_TYPE + "/null")
            .build();
    }
}
