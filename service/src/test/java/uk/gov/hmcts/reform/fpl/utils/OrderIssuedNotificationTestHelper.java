package uk.gov.hmcts.reform.fpl.utils;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;

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
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

public class OrderIssuedNotificationTestHelper {

    private OrderIssuedNotificationTestHelper() {
    }

    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String EXAMPLE_COURT = "Family Court";
    private static final String callout = "^Jones, SACCCCCCCC5676576567, hearing " + LocalDateTime.now().plusMonths(3)
        .toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK));

    public static Map<String, Object> getExpectedCaseUrlParameters(String orderType, boolean withCallout) {
        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        return Map.of(
            "orderType", orderType.toLowerCase(),
            "callout", withCallout ? callout : "",
            "courtName", EXAMPLE_COURT,
            "documentLink", jsonFileObject,
            "caseUrl", formatCaseUrl("http://fake-url", 12345L),
            "respondentLastName", "Jones");
    }

    public static Map<String, Object> getExpectedParametersForRepresentatives(String orderType, boolean withCallout) {
        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        return Map.of("orderType", orderType.toLowerCase(),
            "respondentLastName", "Jones",
            "courtName", EXAMPLE_COURT,
            "callout", withCallout ? callout : "",
            "documentLink", jsonFileObject);
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

