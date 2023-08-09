package uk.gov.hmcts.reform.fpl.service.orders;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.orders.prepopulator.modifier.ManageOrdersCaseDataFixer;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageOrderPostSubmitHelperTest {

    @Mock
    private ManageOrdersCaseDataFixer fixer;

    @Mock
    private OrderProcessingService processingService;

    @Mock
    private CaseConverter caseConverter;

    @TestLogs
    private final TestLogger logs = new TestLogger(ManageOrderPostSubmitHelper.class);

    @InjectMocks
    private ManageOrderPostSubmitHelper underTest;

    private final CaseDetails caseDetails = CaseDetails.builder()
        .id(12345L)
        .build();

    private final CaseData caseData = CaseData.builder().id(12345L).build();

    @BeforeEach
    void setup() {
        when(fixer.fixAndRetriveCaseDetails(caseDetails)).thenReturn(caseDetails);
        when(caseConverter.convert(caseDetails)).thenReturn(caseData);
        when(fixer.fix(caseData)).thenReturn(caseData);
    }

    //@Test
    void getPostSubmitUpdates() {
        underTest.getPostSubmitUpdates(caseDetails);

        verify(processingService).postProcessDocument(any());
    }

    //@Test
    void shouldThrowErrorIfIssueWhenPostProcessing() {
        when(processingService.postProcessDocument(caseData)).thenThrow(FeignException.GatewayTimeout.class);

        underTest.getPostSubmitUpdates(caseDetails);

        assertThat(logs.getErrors())
            .containsExactly("Error while processing manage orders document for case id 12345.");
    }
}
