package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.legalcounsel.ManageLegalCounselService;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.populatedCaseDetails;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(ManageOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class ManageLegalCounselControllerTest extends AbstractCallbackTest {

    @MockBean
    private ManageLegalCounselService manageLegalCounselService;

    private CaseDetails caseDetails;

    protected ManageLegalCounselControllerTest() {
        super("manage-legal-counsel");
    }

    @BeforeEach
    void setUp() {
        caseDetails = populatedCaseDetails();
    }

    @Test
    void shouldReturnNoLegalCounselForSolicitorUserWithNoLegalCounsel() {
        when(manageLegalCounselService.retrieveLegalCounselForLoggedInSolicitor(caseDetails)).thenReturn(emptyList());

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        CaseData caseData = extractCaseData(response);
        assertThat(caseData.getManageLegalCounselEventData().getLegalCounsellors()).isEmpty();
    }

    @Test
    void shouldReturnNoErrorMessageWhenMidEventValidationPasses() {
        when(manageLegalCounselService.validateEventData(caseDetails)).thenReturn(List.of());

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, 200);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

    @Test
    void shouldReturnErrorMessageWhenMidEventValidationFails() {
        when(manageLegalCounselService.validateEventData(caseDetails)).thenReturn(List.of("Mandatory field missing"));

        AboutToStartOrSubmitCallbackResponse response = postMidEvent(caseDetails, 200);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).hasSize(1).contains("Mandatory field missing");
        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

    @Test
    void shouldReturnExistingLegalCounselForSolicitorUserWithExistingLegalCounsel() {
        List<Element<LegalCounsellor>> legalCounsellors = asList(element(LegalCounsellor.builder()
            .firstName("Ted")
            .lastName("Robinson")
            .email("ted.robinson@example.com")
            .organisation(Organisation.organisation("123")).build()));
        when(manageLegalCounselService.retrieveLegalCounselForLoggedInSolicitor(caseDetails))
            .thenReturn(legalCounsellors);

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);

        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        CaseData caseData = extractCaseData(response);
        assertThat(caseData.getManageLegalCounselEventData().getLegalCounsellors()).isEqualTo(legalCounsellors);
    }

    @Test
    void shouldRemoveLegalCounsellorsFromEventBeforeSubmitting() {
        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);

        verify(manageLegalCounselService).updateLegalCounsel(caseDetails);
        assertThat(response.getWarnings()).isNullOrEmpty();
        assertThat(response.getErrors()).isNullOrEmpty();
        assertThat(response.getData()).isEqualTo(caseDetails.getData());
    }

}
