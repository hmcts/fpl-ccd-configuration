package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProviderSDOIssued;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class, CafcassEmailContentProviderSDOIssued.class,
    DateFormatterService.class, HearingBookingService.class})
class CafcassEmailContentProviderSDOIssuedTest {
    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String CAFCASS_NAME = "Test cafcass";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";

    @MockBean
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @MockBean
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @Autowired
    private ObjectMapper mapper;

    private final DateFormatterService dateFormatterService = new DateFormatterService();
    private final HearingBookingService hearingBookingService = new HearingBookingService();

    private CafcassEmailContentProviderSDOIssued contentProviderSDOIssued;

    @BeforeEach
    void setup() {
        this.contentProviderSDOIssued = new CafcassEmailContentProviderSDOIssued(
            cafcassLookupConfiguration, "", mapper, dateFormatterService, hearingBookingService);
    }

    @Test
    void shouldReturnExpectedMapWithValidSDODetails() throws IOException {
        Map<String, Object> expectedMap = getStandardDirectionTemplateParameters();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new Cafcass(CAFCASS_NAME, COURT_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        assertThat(contentProviderSDOIssued.buildCafcassStandardDirectionOrderIssuedNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE)).isEqualTo(expectedMap);
    }

    private Map<String, Object> getStandardDirectionTemplateParameters() {
        return Map.of(
            "title", CAFCASS_NAME,
            "familyManCaseNumber", "12345,",
            "leadRespondentsName", "Smith,",
            "hearingDate", "1 January 2020",
            "reference", "12345",
            "caseUrl", "/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345");
    }
}
