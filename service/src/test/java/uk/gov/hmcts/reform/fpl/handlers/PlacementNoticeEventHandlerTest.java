package uk.gov.hmcts.reform.fpl.handlers;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.events.PlacementNoticeAdded;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PlacementNoticeDocument;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogger;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogs;
import uk.gov.hmcts.reform.fpl.utils.extension.TestLogsExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TestLogsExtension.class)
class PlacementNoticeEventHandlerTest {

    @InjectMocks
    private PlacementNoticeEventHandler underTest;

    @TestLogs
    private TestLogger logs = new TestLogger(PlacementNoticeEventHandler.class);

    @ParameterizedTest
    @EnumSource(PlacementNoticeDocument.RecipientType.class)
    void shouldHandleNoticeDocument(PlacementNoticeDocument.RecipientType recipientType) {

        final CaseData caseData = CaseData.builder().build();
        final PlacementNoticeDocument notice = PlacementNoticeDocument.builder()
            .type(recipientType)
            .notice(testDocumentReference())
            .build();

        final PlacementNoticeAdded event = PlacementNoticeAdded.builder()
            .caseData(caseData)
            .notice(notice)
            .build();

        underTest.takePayment(event);

        assertThat(logs.get()).containsExactly("To be implemented by DFPL-112");
    }

}
