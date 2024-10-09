package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.notify.PlacementNotifyData;

import java.util.HashMap;

import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.PLACEMENT;

@ContextConfiguration(classes = {PlacementContentProvider.class})
class PlacementApplicationContentProviderTest extends AbstractEmailContentProviderTest {

    private static final int GOV_NOTIFY_MAX_FILE_SIZE = 2 * 1024 * 1024;

    @Autowired
    private PlacementContentProvider underTest;

    private final CaseData caseData = CaseData.builder()
        .id(Long.valueOf(CASE_REFERENCE))
        .caseLocalAuthorityName("Local Authority One")
        .build();

    private final Placement placement = Placement.builder()
        .childName("Alex Brown")
        .placementNotice(testDocument)
        .build();

    @Test
    void shouldGetApplicationChangedCourtData() {

        final PlacementNotifyData actual = underTest.getApplicationChangedCourtData(caseData, placement);

        final PlacementNotifyData expected = PlacementNotifyData.builder()
            .childName(placement.getChildName())
            .localAuthority(caseData.getCaseLocalAuthorityName())
            .caseUrl(caseUrl(CASE_REFERENCE, PLACEMENT))
            .build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shoudldGetNoticeChangedData() {

        final PlacementNotifyData actual = underTest.getNoticeChangedData(caseData, placement);

        final PlacementNotifyData expected = PlacementNotifyData.builder()
            .childName(placement.getChildName())
            .localAuthority(caseData.getCaseLocalAuthorityName())
            .caseUrl(caseUrl(CASE_REFERENCE, PLACEMENT))
            .build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldGetNoticeChangedCafcassDataWithoutDownloadLinkWhenDocumentBiggerThanAllowed() {

        byte[] result = new byte[GOV_NOTIFY_MAX_FILE_SIZE + 1];

        when(documentDownloadService.downloadDocument(testDocument.getBinaryUrl())).thenReturn(result);

        final PlacementNotifyData actual = underTest.getNoticeChangedCafcassData(caseData, placement);

        final PlacementNotifyData expected = PlacementNotifyData.builder()
            .childName(placement.getChildName())
            .ccdNumber(CASE_REFERENCE)
            .localAuthority(caseData.getCaseLocalAuthorityName())
            .documentUrl("http://fake-url/testUrl")
            .hasDocumentDownloadUrl("no")
            .documentDownloadUrl("")
            .build();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldGetNoticeChangedCafcassDataWithDownloadLinkWhenDocumentSmallerThanAllowed() {

        byte[] result = new byte[GOV_NOTIFY_MAX_FILE_SIZE];

        when(documentDownloadService.downloadDocument(testDocument.getBinaryUrl())).thenReturn(result);

        final PlacementNotifyData actual = underTest.getNoticeChangedCafcassData(caseData, placement);

        final PlacementNotifyData expected = PlacementNotifyData.builder()
            .childName(placement.getChildName())
            .ccdNumber(CASE_REFERENCE)
            .localAuthority(caseData.getCaseLocalAuthorityName())
            .documentUrl("http://fake-url/testUrl")
            .hasDocumentDownloadUrl("yes")
            .documentDownloadUrl(new HashMap<>() {{
                put("retention_period", null);
                put("filename", null);
                put("confirm_email_before_download", null);
                put("file", new String(encodeBase64(result)));
            }})
            .build();

        assertThat(actual).isEqualTo(expected);
    }
}
