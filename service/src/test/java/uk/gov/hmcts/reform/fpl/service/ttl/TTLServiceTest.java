package uk.gov.hmcts.reform.fpl.service.ttl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.ccd.CoreCaseDataService;

import java.util.Map;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class TTLServiceTest {

    @Mock
    private CoreCaseDataService ccdService;

    @Captor
    private ArgumentCaptor<Function<CaseDetails, Map<String, Object>>> captor;

    @InjectMocks
    private TTLService underTest;

    private static final long CASE_ID = 1L;
    private static final String TTL_DUMMY_EVENT = "trigger-ttl-increment";

    @Test
    void shouldTriggerWorkAllocationDummyEvent() {
        CaseData caseData = CaseData.builder()
            .id(CASE_ID)
            .build();

        underTest.triggerTimeToLiveIncrement(caseData);

        verify(ccdService).performPostSubmitCallbackWithoutChange(eq(CASE_ID), eq(TTL_DUMMY_EVENT));
    }
}
