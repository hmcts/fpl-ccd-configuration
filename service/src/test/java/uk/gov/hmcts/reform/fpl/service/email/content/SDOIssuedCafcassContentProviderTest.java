package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.StandardDirectionOrder;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {SDOIssuedCafcassContentProvider.class})
class SDOIssuedCafcassContentProviderTest extends AbstractEmailContentProviderTest {

    private static final DocumentReference ORDER = TestDataHelper.testDocumentReference();
    @Autowired
    private SDOIssuedCafcassContentProvider contentProviderSDOIssued;

    private static final byte[] ORDER_BINARY = TestDataHelper.DOCUMENT_CONTENT;
    private static final String ENCODED_BINARY = Base64.getEncoder().encodeToString(ORDER_BINARY);

    @Test
    void shouldReturnNotifyData() {
        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .familyManCaseNumber("FAM NUM")
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Smith").build())
                .build()))
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(LocalDateTime.of(2020, 1, 1, 0, 0, 0))
                .build()))
            .standardDirectionOrder(StandardDirectionOrder.builder()
                .orderDoc(ORDER)
                .build())
            .build();

        when(documentDownloadService.downloadDocument(ORDER.getBinaryUrl())).thenReturn(ORDER_BINARY);

        NotifyData expectedParameters = SDONotifyData.builder()
            .leadRespondentsName("Smith")
            .documentLink(Map.of("file", ENCODED_BINARY, "is_csv", false))
            .callout("Smith, FAM NUM, hearing 1 Jan 2020")
            .build();

        NotifyData actualParameters = contentProviderSDOIssued.getNotifyData(caseData);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

}
