package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.event.annotation.BeforeTestMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityEmailLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.email.content.LocalAuthorityEmailContentProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
class LocalAuthorityEmailContentProviderTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String LOCAL_AUTHORITY_NAME = "Test local authority";
    private static final String LOCAL_AUTHORITY_EMAIL_ADDRESS = "FamilyPublicLaw+la@gmail.com";

    @Mock
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Mock
    private LocalAuthorityEmailLookupConfiguration localAuthorityEmailLookupConfiguration;

    @Mock
    MapperService mapperService;

    @Mock
    HearingBookingService hearingBookingService;

    @Mock
    DateFormatterService dateFormatterService;

    @InjectMocks
    private LocalAuthorityEmailContentProvider localAuthorityEmailContentProvider;

    @BeforeEach
    void setup(){
        given(mapperService.mapObject(Mockito.any(), Mockito.any()))
            .willReturn(CaseData.builder().familyManCaseNumber("12345").respondents1(ImmutableList.of(
                Element.<Respondent>builder()
                    .value(Respondent.builder()
                        .party(RespondentParty.builder()
                            .lastName("Moley")
                            .build())
                        .build())
                    .build()))
                .hearingDetails(ImmutableList.of(
                    Element.<HearingBooking>builder()
                        .id(UUID.randomUUID())
                        .value(HearingBooking.builder().date(LocalDate.of(2020,10, 27)).build())
                        .build())).build());

        given(hearingBookingService.getMostUrgentHearingBooking(Mockito.any())).willReturn(HearingBooking.builder()
            .date(LocalDate.of(2020,10,27)).build());

        given(dateFormatterService.formatLocalDateToString(Mockito.any(),Mockito.any()))
            .willReturn("27 October 2020");
    }

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() throws IOException {
        Map<String, Object> expectedMap = getStandardDirectionTemplateParameters();

        given(localAuthorityEmailLookupConfiguration.getLocalAuthority(LOCAL_AUTHORITY_CODE))
            .willReturn(new LocalAuthorityEmailLookupConfiguration
                .LocalAuthority(LOCAL_AUTHORITY_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Test local authority");

        assertThat(localAuthorityEmailContentProvider
            .buildLocalAuthorityStandardDirectionOrderIssuedNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualTo(expectedMap);
    }

    private Map<String, Object> getStandardDirectionTemplateParameters() {
        Map<String, Object> expectedMap = ImmutableMap.<String, Object>builder()
        .put("title", LOCAL_AUTHORITY_NAME)
        .put("familyManCaseNumber", "12345,")
        .put("leadRespondentsName", "Moley,")
        .put("hearingDate", "27 October 2020")
        .put("reference", "12345")
        .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
        .build();

        return expectedMap;
    }
}
