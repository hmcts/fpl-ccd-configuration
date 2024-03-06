package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList.INTERNATIONAL_ASPECT;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;

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
        UUID id1 = UUID.randomUUID();
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
                        .index("1")
                        .build())
                .childExtension1(ChildExtension.builder()
                        .id(id2)
                        .caseExtensionTimeList(CaseExtensionTime.OTHER_EXTENSION)
                        .extensionDateOther(LocalDate.now().plusYears(1))
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .index("2")
                        .build())
                .childExtension3(ChildExtension.builder()
                        .id(id3)
                        .caseExtensionTimeList(CaseExtensionTime.EIGHT_WEEK_EXTENSION)
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .index("3")
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

    @ParameterizedTest
    @EnumSource(value = YesNo.class, names = {"YES", "NO"})
    void shouldFailValidation(YesNo yesNo) {
        UUID id1 = UUID.randomUUID();
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
                        .index("1")
                        .build())
                .childExtension1(ChildExtension.builder()
                        .id(id2)
                        .caseExtensionTimeList(CaseExtensionTime.OTHER_EXTENSION)
                        .extensionDateOther(LocalDate.of(2000, 3, 4))
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .index("2")
                        .build())
                .childExtension3(ChildExtension.builder()
                        .id(id3)
                        .caseExtensionTimeList(CaseExtensionTime.EIGHT_WEEK_EXTENSION)
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .index("3")
                        .build())
                .build();
        CaseData caseData = CaseData.builder()
            .caseCompletionDate(caseCompletionDate)
            .dateSubmitted(LocalDate.of(2030, 8, 10))
            .children1(ElementUtils.wrapElements(children))
            .childExtensionEventData(childExtensionEventData)
            .build();



        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData);
        assertThat(callbackResponse.getErrors())
                .contains("Enter an end date in the future for child 2");
    }

    @ParameterizedTest
    @EnumSource(value = YesNo.class, names = {"YES", "NO"})
    void shouldSetSelectedChildrens(YesNo yesNo) {
        List<Child> children = List.of(
                getChild(LocalDate.of(2024, 7, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(LocalDate.of(2024, 10, 8), "Julie", "Jane")
        );
        LocalDate caseCompletionDate = LocalDate.of(2030, 11, 12);

        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
            .extensionForAllChildren(yesNo.getValue())
            .childSelectorForExtension(Selector.builder()
                .selected(List.of(0, 2))
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .caseCompletionDate(caseCompletionDate)
            .dateSubmitted(LocalDate.of(2030, 8, 10))
            .children1(ElementUtils.wrapElements(children))
            .childExtensionEventData(childExtensionEventData)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "pre-populate");
        assertThat(callbackResponse.getData().get("childExtension0"))
            .isEqualTo(Map.of(
            "label", "Daisy French",
            "index", "1")
            );

        assertThat(callbackResponse.getData().get("childSelected0"))
                .isEqualTo(YES.getValue());

        if (NO == yesNo) {
            assertThat(callbackResponse.getData().get("childExtension1")).isNull();
        } else {
            assertThat(callbackResponse.getData().get("childExtension1"))
                    .isEqualTo(Map.of("label", "Archie Turner",
                            "index", "2"));

            assertThat(callbackResponse.getData().get("childSelected1"))
                .isEqualTo(YES.getValue());
        }

        assertThat(callbackResponse.getData().get("childExtension2"))
                .isEqualTo(Map.of("label", "Julie Jane",
                "index", "3"));
        assertThat(callbackResponse.getData().get("childSelected2"))
                .isEqualTo(YES.getValue());

    }

    @Test
    void shouldSetSelectChildrensError() {

        LocalDate caseCompletionDate = LocalDate.of(2030, 11, 12);

        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .childSelectorForExtension(Selector.builder().build())
            .extensionForAllChildren(NO.getValue())
            .build();

        CaseData caseData = CaseData.builder()
            .caseCompletionDate(caseCompletionDate)
            .dateSubmitted(LocalDate.of(2030, 8, 10))
            .childExtensionEventData(childExtensionEventData)
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postMidEvent(caseData, "pre-populate");
        assertThat(callbackResponse.getErrors())
                .contains("Select the children requiring an extension");

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
