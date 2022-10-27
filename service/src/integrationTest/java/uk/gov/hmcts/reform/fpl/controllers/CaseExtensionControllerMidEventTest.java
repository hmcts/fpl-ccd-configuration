package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.event.ChildExtensionEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList.INTERNATIONAL_ASPECT;

@WebMvcTest(CaseExtensionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseExtensionControllerMidEventTest extends AbstractCallbackTest {

    CaseExtensionControllerMidEventTest() {
        super("case-extension");
    }

    @Autowired
    CaseExtensionController caseExtensionController;

    @ParameterizedTest
    @EnumSource(value = YesNo.class, names = {"YES", "NO"})
    void shouldPassValidation(YesNo yesNo) {
        UUID id1= UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        List<Child> children = List.of(
                getChild(LocalDate.of(2024, 7, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(LocalDate.of(2024, 10, 8), "Julie", "Jane")
        );
        LocalDate caseCompletionDate = LocalDate.of(2030, 11, 12);

        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extensionForAllChildren(yesNo.getValue())
                .childSelectorForExtension(Selector.builder()
                        .selected(List.of(0, 1, 2))
                        .build())
                .childExtension0(ChildExtension.builder()
                        .id(id1)
                        .caseExtensionTimeList(CaseExtensionTime.EIGHT_WEEK_EXTENSION)
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .build())
                .childExtension1(ChildExtension.builder()
                        .id(id2)
                        .caseExtensionTimeList(CaseExtensionTime.OTHER_EXTENSION)
                        .extensionDateOther(LocalDate.of(2024, 3, 4))
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .build())
                .childExtension3(ChildExtension.builder()
                        .id(id3)
                        .caseExtensionTimeList(CaseExtensionTime.EIGHT_WEEK_EXTENSION)
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .build())
                .build();
        CaseData caseData = CaseData.builder()
            .caseCompletionDate(caseCompletionDate)
            .dateSubmitted(LocalDate.of(2030, 8, 10))
            .children1(ElementUtils.wrapElements(children))
            .childExtensionEventData(childExtensionEventData)
            .build();



        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);
        assertThat(callbackResponse.getErrors()).isEmpty();
    }

    private Child getChild(LocalDate completionDate,
                           String firstName,
                           String lastName) {
        ChildParty childParty = ChildParty.builder()
                .completionDate(completionDate)
                .extensionReason(INTERNATIONAL_ASPECT)
                .firstName(firstName)
                .lastName(lastName)
                .build();
        return Child.builder()
                .party(childParty)
                .build();
    }
}
