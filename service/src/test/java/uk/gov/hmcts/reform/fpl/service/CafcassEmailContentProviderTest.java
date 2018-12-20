package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration.Cafcass;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.service.email.content.CafcassEmailContentProvider;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.emptyCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;

@ExtendWith(SpringExtension.class)
class CafcassEmailContentProviderTest {

    private static final String LOCAL_AUTHORITY_CODE = "example";
    private static final String CAFCASS_NAME = "Test cafcass";
    private static final String COURT_EMAIL_ADDRESS = "FamilyPublicLaw+test@gmail.com";

    @Mock
    private CafcassLookupConfiguration cafcassLookupConfiguration;

    @Mock
    private LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;

    @InjectMocks
    private CafcassEmailContentProvider cafcassEmailContentProvider;

    @Test
    void shouldReturnExpectedMapWithValidCaseDetails() throws IOException {
        Map<String, String> expectedMap = ImmutableMap.<String, String>builder()
            .put("cafcass", CAFCASS_NAME)
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "Yes")
            .put("fullStop", "No")
            .put("orders0", "^Emergency protection order")
            .put("orders1", "")
            .put("orders2", "")
            .put("orders3", "")
            .put("orders4", "")
            .put("timeFramePresent", "Yes")
            .put("timeFrameValue", "Same day")
            .put("directionsAndInterim", "^Information on the whereabouts of the child")
            .put("reference", "12345")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/12345")
            .build();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new Cafcass(CAFCASS_NAME, COURT_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        assertThat(cafcassEmailContentProvider.buildCafcassSubmissionNotification(populatedCaseDetails(),
            LOCAL_AUTHORITY_CODE), is(expectedMap));
    }

    @Test
    void shouldReturnSuccessfullyWithEmptyCaseDetails() throws IOException {
        Map<String, String> expectedMap = ImmutableMap.<String, String>builder()
            .put("cafcass", CAFCASS_NAME)
            .put("localAuthority", "Example Local Authority")
            .put("dataPresent", "No")
            .put("fullStop", "Yes")
            .put("orders0", "")
            .put("orders1", "")
            .put("orders2", "")
            .put("orders3", "")
            .put("orders4", "")
            .put("directionsAndInterim", "")
            .put("timeFramePresent", "No")
            .put("timeFrameValue", "")
            .put("reference", "123")
            .put("caseUrl", "null/case/" + JURISDICTION + "/" + CASE_TYPE + "/123")
            .build();

        given(cafcassLookupConfiguration.getCafcass(LOCAL_AUTHORITY_CODE))
            .willReturn(new Cafcass(CAFCASS_NAME, COURT_EMAIL_ADDRESS));

        given(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .willReturn("Example Local Authority");

        assertThat(cafcassEmailContentProvider.buildCafcassSubmissionNotification(emptyCaseDetails(),
            LOCAL_AUTHORITY_CODE), is(expectedMap));
    }
}
