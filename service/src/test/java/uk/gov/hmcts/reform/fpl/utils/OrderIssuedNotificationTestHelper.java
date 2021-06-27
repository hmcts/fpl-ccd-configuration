package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import uk.gov.hmcts.reform.fpl.model.notify.OrderIssuedNotifyData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class OrderIssuedNotificationTestHelper {

    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String EXAMPLE_COURT = "Family Court";
    private static final String callout = "^Jones, SACCCCCCCC5676576567, hearing " + LocalDateTime.now().plusMonths(3)
        .toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK));
    private static final String cmoCallout = "^Jones, SACCCCCCCC5676576567, hearing " + LocalDateTime.now().minusDays(3)
        .toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK));
    private static final String ENCODED_PDF = new String(Base64.encodeBase64(PDF), ISO_8859_1);

    private OrderIssuedNotificationTestHelper() {
    }

    public static Map<String, Object> getExpectedParametersMap(String orderType, boolean withCallout) {
        return Map.of(
            "orderType", orderType.toLowerCase(),
            "callout", withCallout ? callout : "",
            "courtName", EXAMPLE_COURT,
            "documentLink", Map.of("file", ENCODED_PDF, "is_csv", false),
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
            .lastName("Jones")
            .build();
    }

    public static OrderIssuedNotifyData getExpectedCMOParameters(String orderType) {
        return OrderIssuedNotifyData.builder()
            .orderType(orderType.toLowerCase())
            .callout(cmoCallout)
            .courtName(EXAMPLE_COURT)
            .documentLink("http://fake-url/testUrl")
            .caseUrl("http://fake-url/cases/case-details/12345#Orders")
            .lastName("Jones")
            .build();
    }

    public static OrderIssuedNotifyData getExpectedParametersForRepresentatives(String orderType, boolean withCallout) {

        return OrderIssuedNotifyData.builder()
            .orderType(orderType.toLowerCase())
            .callout(withCallout ? callout : "")
            .courtName(EXAMPLE_COURT)
            .documentLink(Map.of("file", ENCODED_PDF, "is_csv", false))
            .lastName("Jones")
            .build();
    }

    public static Map<String, Object> getExpectedParametersMapForRepresentatives(String orderType,
                                                                                 boolean withCallout) {
        JSONObject jsonFileObject = new JSONObject()
            .put("file", ENCODED_PDF)
            .put("is_csv", false);

        return Map.of("orderType", orderType.toLowerCase(),
            "respondentLastName", "Jones",
            "courtName", EXAMPLE_COURT,
            "callout", withCallout ? callout : "",
            "documentLink", jsonFileObject);
    }
}

