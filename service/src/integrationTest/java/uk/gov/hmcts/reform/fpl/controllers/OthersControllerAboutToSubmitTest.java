package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
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
class OthersControllerAboutToSubmitTest extends AbstractCallbackTest {
    private static final Element<Other> CONFIDENTIAL_OTHER = element(Other.builder()
        .firstName("other")
        .address(Address.builder().addressLine1("506 Abbey Lane").build())
        .telephone("01227 123456")
        .addressKnowV2(IsAddressKnowType.YES)
        .hideAddress("Yes")
        .hideTelephone("Yes")
        .build());

    private static final Element<Other> CONFIDENTIAL_OTHER_DETAILS_REMOVED = element(CONFIDENTIAL_OTHER.getId(),
        CONFIDENTIAL_OTHER.getValue().toBuilder().addressKnowV2(null).address(null).telephone(null).build());

    private static final Element<Other> OTHER = element(Other.builder()
        .firstName("additional other")
        .address(Address.builder().addressLine1("101 London Road").build())
        .telephone("07122 123456")
        .addressKnowV2(IsAddressKnowType.YES)
        .hideAddress("No")
        .hideTelephone("No")
        .build());

    private static final Element<Other> CONFIDENTIAL_OTHER_WITHOUT_ADDRESS = element(Other.builder()
        .firstName("additional other")
        .telephone("07122 123456")
        .addressKnowV2(IsAddressKnowType.NO)
        .hideAddress("Yes")
        .hideTelephone("Yes")
        .build());

    private static final Element<Other> CONFIDENTIAL_OTHER_WITHOUT_ADDRESS_DETAILS_REMOVED = element(
        CONFIDENTIAL_OTHER_WITHOUT_ADDRESS.getId(),
        CONFIDENTIAL_OTHER_WITHOUT_ADDRESS.getValue().toBuilder()
            .addressKnowV2(null).address(null).telephone(null).build());

    OthersControllerAboutToSubmitTest() {
        super("enter-others");
    }

    @Test
    void shouldOnlyRemoveConfidentialDetailsWhenOtherIsMarkedAsConfidential() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("othersV2", List.of(CONFIDENTIAL_OTHER, OTHER)))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getOthersV2())
            .isEqualTo(List.of(CONFIDENTIAL_OTHER_DETAILS_REMOVED, OTHER));
        assertThat(caseData.getConfidentialOthers()).isEqualTo(List.of(CONFIDENTIAL_OTHER));
    }

    @Test
    void shouldNotSaveConfidentialAddressWhenAddressIsNotKnown() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("othersV2", List.of(CONFIDENTIAL_OTHER, CONFIDENTIAL_OTHER_WITHOUT_ADDRESS)))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getConfidentialOthers())
            .contains(CONFIDENTIAL_OTHER, CONFIDENTIAL_OTHER_WITHOUT_ADDRESS);
        assertThat(caseData.getOthersV2())
            .contains(CONFIDENTIAL_OTHER_DETAILS_REMOVED, CONFIDENTIAL_OTHER_WITHOUT_ADDRESS_DETAILS_REMOVED);
    }
}
