package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.TestDataHelper;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {SDOIssuedCafcassContentProvider.class})
class SDOIssuedCafcassContentProviderTest extends AbstractEmailContentProviderTest {
    private static final DocumentReference ORDER = TestDataHelper.testDocumentReference();
    private static final byte[] ORDER_BINARY = TestDataHelper.DOCUMENT_CONTENT;
    private static final String ENCODED_BINARY = Base64.getEncoder().encodeToString(ORDER_BINARY);

    @MockBean
    private FeatureToggleService toggleService;

    @MockBean
    private EmailNotificationHelper helper;

    @Autowired
    private SDOIssuedCafcassContentProvider underTest;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(any())).thenReturn("name");
    }

    @Test
    void shouldReturnNotifyDataWithFalseToggle() {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(false);
        when(documentDownloadService.downloadDocument(ORDER.getBinaryUrl())).thenReturn(ORDER_BINARY);

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
            .build();

        NotifyData expectedParameters = SDONotifyData.builder()
            .lastName("Smith")
            .documentLink(Map.of("file", ENCODED_BINARY, "is_csv", false))
            .callout("Smith, FAM NUM, hearing 1 Jan 2020")
            .build();

        NotifyData actualParameters = underTest.getNotifyData(caseData, ORDER);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

    @Test
    void shouldReturnNotifyDataWithTrueToggle() {
        when(toggleService.isEldestChildLastNameEnabled()).thenReturn(true);
        when(documentDownloadService.downloadDocument(ORDER.getBinaryUrl())).thenReturn(ORDER_BINARY);

        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .familyManCaseNumber("FAM NUM")
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .children1(wrapElements(mock(Child.class)))
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Smith").build())
                .build()))
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(LocalDateTime.of(2020, 1, 1, 0, 0, 0))
                .build()))
            .build();

        NotifyData expectedParameters = SDONotifyData.builder()
            .lastName("name")
            .documentLink(Map.of("file", ENCODED_BINARY, "is_csv", false))
            .callout("Smith, FAM NUM, hearing 1 Jan 2020")
            .build();

        NotifyData actualParameters = underTest.getNotifyData(caseData, ORDER);

        assertThat(actualParameters).isEqualTo(expectedParameters);
    }

}
