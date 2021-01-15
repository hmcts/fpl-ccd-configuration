package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.allocatedjudge.AllocatedJudgeTemplateForGeneratedOrder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

public class OrderIssuedNotificationTestHelper {

    private OrderIssuedNotificationTestHelper() {
    }

    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String EXAMPLE_COURT = "Family Court";
    private static final String callout = "^Jones, SACCCCCCCC5676576567, hearing " + LocalDateTime.now().plusMonths(3)
        .toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK));

    public static Map<String, Object> getExpectedParametersMap(String orderType, boolean withCallout) {
        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject()
            .put("file", fileContent)
            .put("is_csv", false);

        return Map.of(
            "orderType", orderType.toLowerCase(),
            "callout", withCallout ? callout : "",
            "courtName", EXAMPLE_COURT,
            "documentLink", jsonFileObject.toMap(),
            "caseUrl", "http://fake-url/cases/case-details/12345#Orders",
            "respondentLastName", "Jones");
    }

    public static OrderIssuedNotifyData getExpectedParameters(String orderType, boolean withCallout) {
        return OrderIssuedNotifyData.builder()
            .orderType(orderType.toLowerCase())
            .callout(withCallout ? callout : "")
            .courtName(EXAMPLE_COURT)
            .documentLink("http://fake-url/testUrl")
            .caseUrl("http://fake-url/cases/case-details/12345#Orders")
            .respondentLastName("Jones")
            .build();
    }

    public static OrderIssuedNotifyData getExpectedParametersForRepresentatives(String orderType, boolean withCallout) {
        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject()
            .put("file", fileContent)
            .put("is_csv", false);

        return OrderIssuedNotifyData.builder()
            .orderType(orderType.toLowerCase())
            .callout(withCallout ? callout : "")
            .courtName(EXAMPLE_COURT)
            .documentLink(jsonFileObject.toMap())
            .respondentLastName("Jones")
            .build();
    }

    public static Map<String, Object> getExpectedParametersMapForRepresentatives(String orderType,
                                                                                 boolean withCallout) {
        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject()
            .put("file", fileContent)
            .put("is_csv", false);

        return Map.of("orderType", orderType.toLowerCase(),
            "respondentLastName", "Jones",
            "courtName", EXAMPLE_COURT,
            "callout", withCallout ? callout : "",
            "documentLink", jsonFileObject);
    }

    public static AllocatedJudgeTemplateForGeneratedOrder getExpectedAllocatedJudgeParameters() {
        return AllocatedJudgeTemplateForGeneratedOrder.builder()
            .orderType("blank order (c21)")
            .callout(callout)
            .respondentLastName("Jones")
            .judgeTitle("Deputy District Judge")
            .judgeName("Scott")
            .caseUrl("http://fake-url/cases/case-details/12345#Orders")
            .build();
    }

    public static List<Element<Representative>> buildRepresentatives() {
        return wrapElements(Representative.builder()
                .email("paul@example.com")
                .fullName("Paul Blart")
                .address(Address.builder()
                    .addressLine1("Street")
                    .postTown("Town")
                    .postcode("Postcode")
                    .build())
                .servingPreferences(DIGITAL_SERVICE)
                .build(),
            Representative.builder()
                .email("bill@example.com")
                .fullName("Bill Bailey")
                .address(Address.builder()
                    .addressLine1("Street")
                    .postTown("Town")
                    .postcode("Postcode")
                    .build())
                .servingPreferences(EMAIL)
                .build());
    }
}

