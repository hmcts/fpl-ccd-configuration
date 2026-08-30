package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(OthersController.class)
@OverrideAutoConfiguration(enabled = true)
class OthersControllerAboutToStartTest extends AbstractCallbackTest {
    private static final Element<Other> CONFIDENTIAL_OTHER = element(Other.builder()
        .firstName("other")
        .lastName("lastName")
        .address(Address.builder().addressLine1("506 Abbey Lane").build())
        .telephone("01227 123456")
        .hideAddress("Yes")
        .hideTelephone("Yes")
        .build());

    private static final Element<Other> CONFIDENTIAL_OTHER_FIELD_REMOVED = element(CONFIDENTIAL_OTHER.getId(),
        CONFIDENTIAL_OTHER.getValue().toBuilder()
            .address(null)
            .telephone(null)
            .build());

    private static final Element<Other> OTHER = element(Other.builder()
        .firstName("additional")
        .lastName("other")
        .address(Address.builder().addressLine1("101 London Road").build())
        .telephone("07122 123456")
        .hideAddress("No")
        .hideTelephone("No")
        .build());

    OthersControllerAboutToStartTest() {
        super("enter-others");
    }

    @Test
    void shouldReturnOthersWhenTheyAreMarkedAsConfidential() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of(
                "othersV2", List.of(CONFIDENTIAL_OTHER_FIELD_REMOVED, OTHER),
                "confidentialOthers", List.of(CONFIDENTIAL_OTHER)))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToStartEvent(caseDetails);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getOthersV2()).isEqualTo(List.of(CONFIDENTIAL_OTHER, OTHER));
    }
}
