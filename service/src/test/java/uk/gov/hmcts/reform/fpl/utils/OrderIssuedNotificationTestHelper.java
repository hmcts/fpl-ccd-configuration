package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.gov.hmcts.reform.fpl.enums.IssuedOrderType;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static uk.gov.hmcts.reform.fpl.enums.GeneratedOrderType.BLANK_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.IssuedOrderType.NOTICE_OF_PLACEMENT_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

public class OrderIssuedNotificationTestHelper {

    private OrderIssuedNotificationTestHelper() {
    }

    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String EXAMPLE_COURT = "Family Court";
    private static final String callout = "^Jones, SACCCCCCCC5676576567, hearing " + LocalDateTime.now().plusMonths(3)
        .toLocalDate().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).localizedBy(Locale.UK));

    public static MapDifference<String, Object> verifyNotification(ArgumentCaptor<Map<String, Object>> captor,
        Supplier<Map<String, Object>> expectedParametersFunction, String jsonObjectFile) {
        Map<String, Object> results = new HashMap<>(captor.getValue());
        Map<String, Object> expected = expectedParametersFunction.get();

        JSONAssert.assertEquals(((JSONObject) results.get(jsonObjectFile)), ((JSONObject) expected.get(jsonObjectFile)),
            true);

        results.remove(jsonObjectFile);
        expected.remove(jsonObjectFile);

        return Maps.difference(expected, results);
    }

    public static Map<String, Object> getExpectedPlacementParametersForAdminWhenNoRepresentativesServedByPost() {
        return ImmutableMap.<String, Object>builder()
            .put("callout", "")
            .putAll(commonParametersNoPostingNeeded())
            .build();
    }

    public static Map<String, Object> getExpectedParametersForAdminWhenNoRepresentativesServedByPost() {
        return ImmutableMap.<String, Object>builder()
            .put("callout", callout)
            .putAll(commonParametersNoPostingNeeded())
            .build();
    }

    public static List<Element<Representative>> buildRepresentativesServedByPost() {
        return wrapElements(Representative.builder()
            .email("paul@example.com")
            .fullName("Paul Blart")
            .address(Address.builder()
                .addressLine1("Street")
                .postTown("Town")
                .postcode("Postcode")
                .build())
            .servingPreferences(POST)
            .build());
    }

    public static List<Element<Representative>> buildRepresentativesServedByEmail() {
        return wrapElements(Representative.builder()
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

    public static Map<String, Object> getExpectedParametersForAdmin(IssuedOrderType issuedOrderType) {
        if (issuedOrderType != NOTICE_OF_PLACEMENT_ORDER) {
            return getExpectedParametersForAdminWhenRepresentativesNeedServingByPost();
        } else {
            return getExpectedPlacementParametersForAdminWhenRepresentativesNeedServingByPost();
        }
    }

    public static Map<String, Object> getExpectedParametersForRepresentatives(IssuedOrderType issuedOrderType) {
        if (issuedOrderType != NOTICE_OF_PLACEMENT_ORDER) {
            return getExpectedParametersForRepresentativesServedByEmail();
        } else {
            return getExpectedPlacementParametersForRepresentativesServedByEmail();
        }
    }

    private static Map<String, Object> commonParametersNoPostingNeeded() {
        return Map.of("needsPosting", "No",
            "doesNotNeedPosting", "Yes",
            "courtName", EXAMPLE_COURT,
            "caseUrlOrDocumentLink", formatCaseUrl("http://fake-url", 12345L),
            "respondentLastName", "Jones",
            "representatives", "");
    }

    private static Map<String, Object> commonParametersPostingNeeded() {
        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        return Map.of("needsPosting", "Yes",
            "doesNotNeedPosting", "No",
            "courtName", EXAMPLE_COURT,
            "respondentLastName", "Jones",
            "representatives", List.of("Paul Blart\nStreet, Town, Postcode"),
            "caseUrlOrDocumentLink", jsonFileObject);
    }

    private static Map<String, Object> getExpectedParametersForRepresentativesServedByEmail() {
        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);
        Map<String, Object> expectedMap = new HashMap<>();

        expectedMap.put("orderType", BLANK_ORDER.getLabel().toLowerCase());
        expectedMap.put("respondentLastName", "Jones");
        expectedMap.put("courtName", EXAMPLE_COURT);
        expectedMap.put("callout", callout);
        expectedMap.put("documentLink", jsonFileObject);

        return expectedMap;
    }

    private static Map<String, Object> getExpectedPlacementParametersForRepresentativesServedByEmail() {
        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);
        Map<String, Object> expectedMap = new HashMap<>();

        expectedMap.put("orderType", NOTICE_OF_PLACEMENT_ORDER.getLabel().toLowerCase());
        expectedMap.put("respondentLastName", "Jones");
        expectedMap.put("courtName", EXAMPLE_COURT);
        expectedMap.put("callout", "");
        expectedMap.put("documentLink", jsonFileObject);

        return expectedMap;
    }

    private static Map<String, Object> getExpectedPlacementParametersForAdminWhenRepresentativesNeedServingByPost() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("callout", "");
        expectedMap.putAll(commonParametersPostingNeeded());

        return expectedMap;
    }

    private static Map<String, Object> getExpectedParametersForAdminWhenRepresentativesNeedServingByPost() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("callout", callout);
        expectedMap.putAll(commonParametersPostingNeeded());

        return expectedMap;
    }
}

