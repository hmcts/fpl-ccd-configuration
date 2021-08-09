package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@WebMvcTest(OthersController.class)
@OverrideAutoConfiguration(enabled = true)
class OthersControllerAboutToSubmitTest extends AbstractCallbackTest {

    OthersControllerAboutToSubmitTest() {
        super("enter-others");
    }

    private static final UUID ADDITIONAL_OTHER_ID = randomUUID();

    @Test
    void shouldOnlyRemoveConfidentialDetailsWhenOtherIsMarkedAsConfidential() {
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("others", Others.builder()
                .firstOther(other())
                .additionalOthers(additionalOthers())
                .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getOthers().getFirstOther()).isEqualTo(otherWithDetailsRemoved());
        assertThat(caseData.getOthers().getAdditionalOthers()).isEqualTo(additionalOthers());
        assertThat(unwrapElements(caseData.getConfidentialOthers())).containsOnly(confidentialOther());
    }

    @Test
    void shouldNotSaveFirstOtherWhenItIsEmpty() {
        List<Element<Other>> additionalOthers = additionalOthers();
        CaseDetails caseDetails = CaseDetails.builder()
            .data(Map.of("others", Others.builder()
                .firstOther(Other.builder().build())
                .additionalOthers(additionalOthers)
                .build()))
            .build();

        AboutToStartOrSubmitCallbackResponse response = postAboutToSubmitEvent(caseDetails);
        CaseData caseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(caseData.getOthers().getFirstOther()).isEqualTo(additionalOthers.get(0).getValue());
        assertThat(caseData.getOthers().getAdditionalOthers()).isEmpty();
    }

    private Other other() {
        return Other.builder()
            .name("other")
            .address(Address.builder().addressLine1("506 Abbey Lane").build())
            .telephone("01227 123456")
            .detailsHidden("Yes")
            .build();
    }

    private Other otherWithDetailsRemoved() {
        return Other.builder()
            .name("other")
            .detailsHidden("Yes")
            .build();
    }

    private List<Element<Other>> additionalOthers() {
        return newArrayList(element(ADDITIONAL_OTHER_ID, Other.builder()
            .name("additional other")
            .address(Address.builder().addressLine1("101 London Road").build())
            .telephone("07122 123456")
            .detailsHidden("No")
            .build()));
    }

    private Other confidentialOther() {
        return Other.builder()
            .name("other")
            .address(Address.builder().addressLine1("506 Abbey Lane").build())
            .telephone("01227 123456")
            .build();
    }
}
