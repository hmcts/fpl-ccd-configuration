package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildExtension;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.event.ChildExtensionEventData;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList.INTERNATIONAL_ASPECT;
import static uk.gov.hmcts.reform.fpl.enums.CaseExtensionTime.OTHER_EXTENSION;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@WebMvcTest(CaseExtensionController.class)
@OverrideAutoConfiguration(enabled = true)
class CaseExtensionControllerAboutToSubmitTest extends AbstractCallbackTest {

    CaseExtensionControllerAboutToSubmitTest() {
        super("case-extension");
    }

    @Autowired
    CaseExtensionController caseExtensionController;

    @Test
    @SuppressWarnings("unchecked")
    void shouldPopulateChildrenWithSameExtensionWhenOtherDateExtensionAppliedToAll() {
        List<Child> children = List.of(
                getChild(LocalDate.of(2024, 7, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(LocalDate.of(2024, 10, 8), "Julie", "Jane")
        );
        LocalDate caseCompletionDate = LocalDate.of(2030, 11, 12);
        LocalDate extensionDateOther = LocalDate.of(2031, 9, 12);

        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
            .extensionForAllChildren(YES.getValue())
            .sameExtensionForAllChildren(YES.getValue())
            .childExtensionAll(ChildExtension.builder()
                .caseExtensionTimeList(OTHER_EXTENSION)
                .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                .extensionDateOther(extensionDateOther)
                .build())
            .build();

        CaseData caseData = CaseData.builder()
            .caseCompletionDate(caseCompletionDate)
            .dateSubmitted(LocalDate.of(2030, 8, 10))
            .children1(ElementUtils.wrapElements(children))
            .childExtensionEventData(childExtensionEventData)
            .build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2031-09-12");

        String caseSummaryExtensionDetails = join(lineSeparator(),
                "Daisy French - 12 September 2031 - International Aspect",
                "Archie Turner - 12 September 2031 - International Aspect",
                "Julie Jane - 12 September 2031 - International Aspect");

        assertThat(callbackResponse.getData().get("caseSummaryExtensionDetails"))
                .isEqualTo(caseSummaryExtensionDetails);

        List<Map<String, Object>> children1 = (List<Map<String, Object>>) callbackResponse.getData().get("children1");

        assertThat(children1)
            .isEqualTo(List.of(
                    getChildMap("2031-09-12", "Daisy", "French"),
                    getChildMap("2031-09-12", "Archie", "Turner"),
                    getChildMap("2031-09-12", "Julie", "Jane")
                )
            );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPopulateChildrenWithSameExtensionWhenSelectedChildrenHaveSameExtensionOfOtherDateExtension() {
        List<Child> children = List.of(
                getChild(LocalDate.of(2024, 7, 2), "Daisy", "French"),
                getChild(null, "Archie", "Turner"),
                getChild(LocalDate.of(2024, 10, 8), "Julie", "Jane")
        );
        LocalDate caseCompletionDate = LocalDate.of(2030, 11, 12);
        LocalDate extensionDateOther = LocalDate.of(2031, 9, 12);

        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extensionForAllChildren(NO.getValue())
                .sameExtensionForAllChildren(YES.getValue())
                .childSelectorForExtension(Selector.builder()
                        .selected(List.of(1,2))
                        .build())
                .childExtensionAll(ChildExtension.builder()
                        .caseExtensionTimeList(OTHER_EXTENSION)
                        .caseExtensionReasonList(INTERNATIONAL_ASPECT)
                        .extensionDateOther(extensionDateOther)
                        .build())
                .build();

        CaseData caseData = CaseData.builder()
                .caseCompletionDate(caseCompletionDate)
                .dateSubmitted(LocalDate.of(2030, 8, 10))
                .children1(ElementUtils.wrapElements(children))
                .childExtensionEventData(childExtensionEventData)
                .build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2031-09-12");

        String caseSummaryExtensionDetails = join(lineSeparator(),
                "Daisy French - 2 July 2024 - International Aspect",
                "Archie Turner - 12 September 2031 - International Aspect",
                "Julie Jane - 12 September 2031 - International Aspect");

        assertThat(callbackResponse.getData().get("caseSummaryExtensionDetails"))
                .isEqualTo(caseSummaryExtensionDetails);

        List<Map<String, Object>> children1 = (List<Map<String, Object>>) callbackResponse.getData().get("children1");

        assertThat(children1)
            .isEqualTo(List.of(
                    getChildMap("2024-07-02", "Daisy", "French"),
                    getChildMap("2031-09-12", "Archie", "Turner"),
                    getChildMap("2031-09-12", "Julie", "Jane")
                )
            );
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPopulateChildrenWithSameExtensionWhenSelectedChildrenHaveDifferentDateExtension() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        List<Element<Child>> children = List.of(
            getChild(id1,LocalDate.of(2024, 7, 2), "Daisy", "French"),
            getChild(id2, null, "Archie", "Turner"),
            getChild(id3, LocalDate.of(2024, 10, 8), "Julie", "Jane")
        );
        LocalDate caseCompletionDate = LocalDate.of(2030, 11, 12);

        ChildExtensionEventData childExtensionEventData = ChildExtensionEventData.builder()
                .extendTimelineHearingDate(LocalDate.of(2020, 1, 1))
                .extendTimelineApprovedAtHearing(NO)
                .extensionForAllChildren(NO.getValue())
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
                        .extensionDateOther(LocalDate.of(2024, 3, 4))
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
                .dateSubmitted(LocalDate.of(2023, 8, 10))
                .children1(children)
                .childExtensionEventData(childExtensionEventData)
                .build();
        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);

        assertThat(callbackResponse.getData().get("caseCompletionDate")).isEqualTo("2024-03-04");

        String caseSummaryExtensionDetails = join(lineSeparator(),
                "Daisy French - 26 February 2020 - International Aspect",
                "Archie Turner - 4 March 2024 - International Aspect",
                "Julie Jane - 26 February 2020 - International Aspect");

        assertThat(callbackResponse.getData().get("caseSummaryExtensionDetails"))
                .isEqualTo(caseSummaryExtensionDetails);

        List<Map<String, Object>> children1 = (List<Map<String, Object>>) callbackResponse.getData().get("children1");

        assertThat(children1)
            .isEqualTo(List.of(
                    getChildMap(id1.toString(), "2020-02-26", "Daisy", "French"),
                    getChildMap(id2.toString(), "2024-03-04", "Archie", "Turner"),
                    getChildMap(id3.toString(), "2020-02-26", "Julie", "Jane")
                )
            );
    }

    private Map<String, Object> getChildMap(String completionDate,
                                            String firstName,
                                            String lastName) {
        return getChildMap(null, completionDate, firstName, lastName);
    }

    private Map<String, Object> getChildMap(String id,
                                            String completionDate,
                                            String firstName,
                                            String lastName) {
        Map<String, Object> child1 = new HashMap<>();
        child1.put("id", id);
        child1.put("value", Map.of(
                "party", Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "completionDate", completionDate,
                        "extensionReason", "InternationalAspect")));
        return child1;
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

    private Element<Child> getChild(UUID id, LocalDate completionDate,
                                   String firstName,
                                   String lastName) {
        return element(id, getChild(completionDate, firstName, lastName));
    }
}
