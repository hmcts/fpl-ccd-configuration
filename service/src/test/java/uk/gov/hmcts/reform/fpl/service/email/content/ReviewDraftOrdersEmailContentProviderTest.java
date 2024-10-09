package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ApprovedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.RejectedOrdersTemplate;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.service.email.content.cmo.ReviewDraftOrdersEmailContentProvider;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.TabUrlAnchor.ORDERS;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createRespondents;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.DOCUMENT_CONTENT;

@ContextConfiguration(classes = {ReviewDraftOrdersEmailContentProvider.class})
class ReviewDraftOrdersEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final LocalDate SOME_DATE = LocalDate.of(2020, 2, 20);
    @MockBean
    private EmailNotificationHelper helper;
    @Autowired
    private ReviewDraftOrdersEmailContentProvider underTest;

    @Test
    void shouldBuildApprovedOrdersContentForCaseAccessUsers() {
        HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(SOME_DATE, LocalTime.of(0, 0)))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("SN2000")
            .respondents1(createRespondents())
            .build();

        DocumentReference orderDocument1 = DocumentReference.builder().binaryUrl("/testUrl1").build();
        DocumentReference orderDocument2 = DocumentReference.builder().binaryUrl("/testUrl2").build();

        List<HearingOrder> orders = List.of(HearingOrder.builder()
                .title("Order 1")
                .order(orderDocument1)
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .order(orderDocument2)
                .build());

        ApprovedOrdersTemplate expectedTemplate = ApprovedOrdersTemplate.builder()
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .orderList("Order 1\nOrder 2")
            .lastName("Davies")
            .subjectLineWithHearingDate("Jones, SN2000, case management hearing, 20 February 2020")
            .documentLinks(List.of("http://fake-url/testUrl1", "http://fake-url/testUrl2"))
            .digitalPreference("Yes")
            .build();

        given(helper.getEldestChildLastName(caseData.getAllChildren())).willReturn("Davies");

        assertThat(underTest.buildOrdersApprovedContent(
            caseData, hearing, orders, DIGITAL_SERVICE)).isEqualTo(expectedTemplate);
    }

    @Test
    void shouldBuildApprovedOrdersContentForNonCaseAccessUsers() {
        given(documentDownloadService.downloadDocument(anyString())).willReturn(DOCUMENT_CONTENT);

        HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(SOME_DATE, LocalTime.of(0, 0)))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("SN2000")
            .respondents1(createRespondents())
            .build();

        DocumentReference orderDocument1 = DocumentReference.builder().binaryUrl("testUrl1").build();
        DocumentReference orderDocument2 = DocumentReference.builder().binaryUrl("testUrl2").build();

        List<HearingOrder> orders = List.of(HearingOrder.builder()
                .title("Order 1")
                .order(orderDocument1)
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .order(orderDocument2)
                .build());

        String fileContent = Base64.getEncoder().encodeToString(DOCUMENT_CONTENT);
        Map<String, Object> jsonFileObject =  new HashMap<>() {{
            put("retention_period", null);
            put("filename", null);
            put("confirm_email_before_download", null);
            put("file", fileContent);
        }};

        ApprovedOrdersTemplate expectedTemplate = ApprovedOrdersTemplate.builder()
            .caseUrl("")
            .orderList("Order 1\nOrder 2")
            .lastName("Davies")
            .subjectLineWithHearingDate("Jones, SN2000, case management hearing, 20 February 2020")
            .attachedDocuments(List.of(jsonFileObject, jsonFileObject))
            .digitalPreference("No")
            .documentLinks(List.of())
            .build();

        given(helper.getEldestChildLastName(caseData.getAllChildren())).willReturn("Davies");

        assertThat(underTest.buildOrdersApprovedContent(caseData, hearing, orders, EMAIL)).isEqualTo(expectedTemplate);
    }

    @Test
    void shouldBuildOrdersRejectedContent() {
        HearingBooking hearing = HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(LocalDateTime.of(SOME_DATE, LocalTime.of(0, 0)))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .familyManCaseNumber("SN2000")
            .respondents1(createRespondents())
            .build();

        List<HearingOrder> orders = List.of(HearingOrder.builder()
                .title("Order 1")
                .requestedChanges("Missing information about XYZ")
                .build(),
            HearingOrder.builder()
                .title("Order 2")
                .requestedChanges("Please change ABC")
                .build());

        RejectedOrdersTemplate expectedTemplate = RejectedOrdersTemplate.builder()
            .ordersAndRequestedChanges(List.of(
                "Order 1 - Missing information about XYZ",
                "Order 2 - Please change ABC"))
            .caseUrl(caseUrl(CASE_REFERENCE, ORDERS))
            .lastName("Davies")
            .subjectLineWithHearingDate("Jones, SN2000, case management hearing, 20 February 2020")
            .build();

        given(helper.getEldestChildLastName(caseData.getAllChildren())).willReturn("Davies");

        assertThat(underTest.buildOrdersRejectedContent(caseData, hearing, orders)).isEqualTo(expectedTemplate);
    }

}
