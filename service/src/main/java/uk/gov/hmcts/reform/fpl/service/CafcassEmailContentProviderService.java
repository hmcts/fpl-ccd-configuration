package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.config.CafcassLookupConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.fpl.CaseDefinitionConstants.JURISDICTION;

@Service
public class CafcassEmailContentProviderService {

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration;
    private final CafcassLookupConfiguration cafcassLookupConfiguration;
    private final String uiBaseUrl;

    private static final String ORDER_KEY = "orders";
    private static final String DIRECTIONS_KEY = "directionsAndInterim";

    @Autowired
    public CafcassEmailContentProviderService(LocalAuthorityNameLookupConfiguration
                                                      localAuthorityNameLookupConfiguration,
                                              CafcassLookupConfiguration cafcassLookupConfiguration,
                                              @Value("${ccd.ui.base.url}") String uiBaseUrl) {
        this.localAuthorityNameLookupConfiguration = localAuthorityNameLookupConfiguration;
        this.cafcassLookupConfiguration = cafcassLookupConfiguration;
        this.uiBaseUrl = uiBaseUrl;
    }

    public Map<String, String> buildCafcassSubmissionNotification(CaseDetails caseDetails, String localAuthorityCode) {
        Map orders =
            Optional.ofNullable((Map) caseDetails.getData().get(ORDER_KEY)).orElse(ImmutableMap.builder().build());

        List orderType = (List) Optional.ofNullable(orders.get("orderType")).orElse(ImmutableList.builder().build());
        String directions = (String) Optional.ofNullable(orders.get(DIRECTIONS_KEY)).orElse("");

        ImmutableMap.Builder<String, String> orderTypeArray = ImmutableMap.builder();
        for (int i = 0; i < 5; i++) {
            if (i < orderType.size()) {
                orderTypeArray.put(ORDER_KEY + i, "^" + orderType.get(i));
            } else {
                orderTypeArray.put(ORDER_KEY + i, "");
            }
        }

        return ImmutableMap.<String, String>builder()
            .put("cafcass", cafcassLookupConfiguration.getCafcass(localAuthorityCode).getName())
            .put("localAuthority", localAuthorityNameLookupConfiguration.getLocalAuthorityName(localAuthorityCode))
            .putAll(orderTypeArray.build())
            .put("dataPresent", orderType.isEmpty() ? ("No") : ("Yes"))
            .put("fullStop", orderType.isEmpty() ? ("Yes") : ("No"))
            .put(DIRECTIONS_KEY, !directions.isEmpty() ? "^" + directions : "")
            .put("reference", String.valueOf(caseDetails.getId()))
            .put("caseUrl", uiBaseUrl + "/case/" + JURISDICTION + "/" + CASE_TYPE + "/" + caseDetails.getId())
            .build();
    }
}
