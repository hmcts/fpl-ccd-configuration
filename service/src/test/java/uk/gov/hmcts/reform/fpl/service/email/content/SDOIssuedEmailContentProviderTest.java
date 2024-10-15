package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.sdo.SDONotifyData;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.DirectionsOrderType.SDO;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ContextConfiguration(classes = {SDOIssuedContentProvider.class})
class SDOIssuedEmailContentProviderTest extends AbstractEmailContentProviderTest {

    @MockBean
    private EmailNotificationHelper helper;

    @Autowired
    private SDOIssuedContentProvider underTest;

    @BeforeEach
    void setUp() {
        when(helper.getEldestChildLastName(any(CaseData.class))).thenReturn("name");
    }

    @Test
    void buildNotificationParameters() {
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

        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn("Smith");

        SDONotifyData actualData = underTest.buildNotificationParameters(caseData, SDO);

        SDONotifyData expectedData = SDONotifyData.builder()
            .callout("Smith, FAM NUM, hearing 1 Jan 2020")
            .lastName("Smith")
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .directionsOrderTypeShort(SDO.getShortForm())
            .directionsOrderTypeLong(SDO.getLongForm())
            .build();

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldBuildNotificationParametersWhenReasonPresent() {
        String reason = "Please complete";
        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .familyManCaseNumber("FAM NUM")
            .court(Court.builder()
                .code("344")
                .name("Family Court")
                .build()
            )
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Smith").build())
                .build()))
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(LocalDateTime.of(2020, 1, 1, 0, 0, 0))
                .build()))
            .build();

        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn("Smith");
        when(documentDownloadService.downloadDocument(any()))
            .thenReturn(DOCUMENT_CONTENT);

        SDONotifyData actualData = underTest.buildNotificationParameters(caseData, testDocument, reason);

        SDONotifyData expectedData = SDONotifyData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .documentLink( new HashMap<>() {{
                put("retention_period", null);
                put("filename", null);
                put("confirm_email_before_download", null);
                put("file", "AQIDBAU=");
            }})
            .courtName("Family Court")
            .caseNumber("FAM NUM")
            .isReasonPresent("yes")
            .reason(reason)
            .build();

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void shouldBuildNotificationParametersWhenReasonNotPresent() {
        String reason = null;
        CaseData caseData = CaseData.builder()
            .id(Long.valueOf(CASE_REFERENCE))
            .familyManCaseNumber("FAM NUM")
            .court(Court.builder()
                .code("344")
                .name("Family Court")
                .build()
            )
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(Respondent.builder()
                .party(RespondentParty.builder().lastName("Smith").build())
                .build()))
            .hearingDetails(wrapElements(HearingBooking.builder()
                .startDate(LocalDateTime.of(2020, 1, 1, 0, 0, 0))
                .build()))
            .build();

        when(helper.getEldestChildLastName(caseData.getAllChildren())).thenReturn("Smith");
        when(documentDownloadService.downloadDocument(any()))
            .thenReturn(DOCUMENT_CONTENT);

        SDONotifyData actualData = underTest.buildNotificationParameters(caseData, testDocument, reason);

        SDONotifyData expectedData = SDONotifyData.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .documentLink( new HashMap<>() {{
                put("retention_period", null);
                put("filename", null);
                put("confirm_email_before_download", null);
                put("file", "AQIDBAU=");
            }})
            .courtName("Family Court")
            .caseNumber("FAM NUM")
            .isReasonPresent("no")
            .reason("")
            .build();

        assertThat(actualData).isEqualTo(expectedData);
    }
}
