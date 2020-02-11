package uk.gov.hmcts.reform.fpl.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper.formatCaseUrl;

public class NotifyAdminOrderIssuedTestHelper {

    private NotifyAdminOrderIssuedTestHelper() {
    }

    private static final byte[] PDF = {1, 2, 3, 4, 5};
    private static final String LOCAL_AUTHORITY_NAME = "Example Local Authority";

    public static MapDifference<String, Object> verifyNotificationSentToAdminWhenOrderIssued(
        ArgumentCaptor<Map<String, Object>> dataCaptor) {

        Map<String, Object> resultData = new HashMap<>(dataCaptor.getValue());
        Map<String, Object> expectedParameters = getExpectedParametersForAdminWhenRepresentativesNeedServingByPost();

        assertThat(((JSONObject) resultData.get("caseUrlOrDocumentLink")).get("file")).isEqualTo(
            ((JSONObject) expectedParameters.get("caseUrlOrDocumentLink")).get("file"));

        resultData.remove("caseUrlOrDocumentLink");
        expectedParameters.remove("caseUrlOrDocumentLink");

        return Maps.difference(expectedParameters, resultData);
    }

    public static Map<String, Object> getExpectedParametersForAdminWhenNoRepresentativesServedByPost() {
        return ImmutableMap.<String, Object>builder()
            .put("needsPosting", "No")
            .put("doesNotNeedPosting", "Yes")
            .put("courtName", LOCAL_AUTHORITY_NAME)
            .put("caseUrlOrDocumentLink", formatCaseUrl("http://fake-url", 12345L))
            .put("respondentLastName", "Jones")
            .put("representatives", "")
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

    private static Map<String, Object> getExpectedParametersForAdminWhenRepresentativesNeedServingByPost() {

        String fileContent = new String(Base64.encodeBase64(PDF), ISO_8859_1);
        JSONObject jsonFileObject = new JSONObject().put("file", fileContent);

        Map<String, Object> expectedMap = new HashMap<>();

        expectedMap.put("needsPosting", "Yes");
        expectedMap.put("doesNotNeedPosting", "No");
        expectedMap.put("courtName", LOCAL_AUTHORITY_NAME);
        expectedMap.put("respondentLastName", "Jones");
        expectedMap.put("representatives", List.of("Paul Blart\nStreet, Town, Postcode"));
        expectedMap.put("caseUrlOrDocumentLink", jsonFileObject);

        return expectedMap;
    }
}
