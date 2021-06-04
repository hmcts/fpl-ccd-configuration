package uk.gov.hmcts.reform.fpl.service.email.content;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
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
import java.util.List;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
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
            .respondentLastName("Jones")
            .subjectLineWithHearingDate("Jones, SN2000, case management hearing, 20 February 2020")
            .documentLinks(List.of("http://fake-url/testUrl1", "http://fake-url/testUrl2"))
            .digitalPreference("Yes")
            .build();

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

        String fileContent = new String(Base64.encodeBase64(new byte[] {1, 2, 3, 4, 5}), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject()
            .put("file", fileContent)
            .put("is_csv", false);

        ApprovedOrdersTemplate expectedTemplate = ApprovedOrdersTemplate.builder()
            .caseUrl("")
            .orderList("Order 1\nOrder 2")
            .respondentLastName("Jones")
            .subjectLineWithHearingDate("Jones, SN2000, case management hearing, 20 February 2020")
            .attachedDocuments(List.of(jsonFileObject.toMap(), jsonFileObject.toMap()))
            .digitalPreference("No")
            .documentLinks(List.of())
            .build();

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

        given(helper.getSubjectLineLastName(caseData)).willReturn("Davies");

        assertThat(underTest.buildOrdersRejectedContent(caseData, hearing, orders)).isEqualTo(expectedTemplate);
    }

}
