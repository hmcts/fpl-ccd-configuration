package uk.gov.hmcts.reform.fpl.controllers.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.events.UpdateGuardianEvent;
import uk.gov.hmcts.reform.fpl.handlers.UpdateGuardiansEventHandler;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Guardian;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.cafcass.api.CafcassApiGuardianService;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class CafcassApiUpdateGuardianControllerTest extends CafcassApiControllerBaseTest {
    private static final String VALID_PAYLOAD = """
                    [
                        {
                            "guardianName": "John Smith",
                            "telephoneNumber": "01234567890",
                            "email": "john.smith@example.com",
                            "children": ["Child one"]
                        }
                    ]""";
    private static final List<Guardian> VALID_UPDATE_LIST = List.of(Guardian.builder().guardianName("John Smith")
        .telephoneNumber("01234567890").email("john.smith@example.com").children(List.of("Child one")).build());

    private static final String CASE_ID = "1";
    private static final CaseDetails CASE_DETAILS = mock(CaseDetails.class);
    private static final CaseData CASE_DATA = CaseData.builder().build();
    private static final CaseDetails CASE_DETAILS_UPDATED = mock(CaseDetails.class);
    private static final CaseData CASE_DATA_UPDATED =
        CaseData.builder().guardians(wrapElements(VALID_UPDATE_LIST)).build();
    @MockBean
    private CaseConverter caseConverter;
    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private UpdateGuardiansEventHandler updateGuardiansEventHandler;
    @MockBean
    private CafcassApiGuardianService cafcassApiGuardianService;
    @Captor
    private ArgumentCaptor<UpdateGuardianEvent> updateGuardianEventCaptor;

    @BeforeEach
    void setup() {
        when(coreCaseDataService.findCaseDetailsById(CASE_ID)).thenReturn(CASE_DETAILS);
        when(caseConverter.convert(CASE_DETAILS)).thenReturn(CASE_DATA);
    }

    @Test
    void shouldUpdateGuardiansAndNotifyParties() throws Exception {
        when(caseConverter.convert(CASE_DETAILS_UPDATED)).thenReturn(CASE_DATA_UPDATED);
        when(cafcassApiGuardianService.validateGuardians(any())).thenCallRealMethod();
        when(cafcassApiGuardianService.checkIfAnyGuardianUpdated(any(), any())).thenCallRealMethod();
        when(cafcassApiGuardianService.updateGuardians(CASE_DATA, VALID_UPDATE_LIST)).thenReturn(CASE_DETAILS_UPDATED);

        sendRequest(buildPostRequest(String.format("/cases/%s/guardians", CASE_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PAYLOAD),
            200);

        verify(updateGuardiansEventHandler).notifyLocalAuthorities(updateGuardianEventCaptor.capture());
        assertEquals(updateGuardianEventCaptor.getValue().getCaseData(), CASE_DATA_UPDATED);

        verify(updateGuardiansEventHandler).notifyChildSolicitors(updateGuardianEventCaptor.capture());
        assertEquals(updateGuardianEventCaptor.getValue().getCaseData(), CASE_DATA_UPDATED);

        verify(updateGuardiansEventHandler).notifyRespondentSolicitors(updateGuardianEventCaptor.capture());
        assertEquals(updateGuardianEventCaptor.getValue().getCaseData(), CASE_DATA_UPDATED);

        verifyNoMoreInteractions(updateGuardiansEventHandler);
    }

    @Test
    void shouldReturn400IfInputNotValid() throws Exception {
        sendRequest(buildPostRequest(String.format("/cases/%s/guardians", CASE_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(""),
            400);


        sendRequest(buildPostRequest(String.format("/cases/%s/guardians", CASE_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    [
                        {
                            "guardianName": "John Smith",
                            "telephoneNumber": "01234567890",
                            "email": "john.smith@example.com"
                        }
                    ]"""),
            400);
    }

    @Test
    void shouldNotUpdateGuardianIfNoChanges() throws Exception {
        when(caseConverter.convert(CASE_DETAILS)).thenReturn(CASE_DATA_UPDATED);
        when(cafcassApiGuardianService.validateGuardians(any())).thenCallRealMethod();
        when(cafcassApiGuardianService.checkIfAnyGuardianUpdated(any(), any())).thenCallRealMethod();

        sendRequest(buildPostRequest(String.format("/cases/%s/guardians", CASE_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PAYLOAD),
            200);

        verify(cafcassApiGuardianService, times(0)).updateGuardians(any(), any());
        verifyNoInteractions(updateGuardiansEventHandler);
    }
}
