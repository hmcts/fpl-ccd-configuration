package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

abstract class AbstractUploadDraftOrdersControllerTest extends AbstractControllerTest {

    static final String JUDGE_EMAIL = "judge@hmcts.gov.uk";

    AbstractUploadDraftOrdersControllerTest() {
        super("upload-draft-orders");
    }

    Pair<String, UUID> option(String label, UUID code) {
        return Pair.of(label, code);
    }

    Pair<String, UUID> option(String label, Element element) {
        return option(label, element.getId());
    }

    Map<String, Object> convert(Object o) {
        return mapper.convertValue(o, new TypeReference<>() {
        });
    }

    @SafeVarargs
    final Map<String, Object> dynamicList(Pair<String, UUID>... options) {
        return mapper.convertValue(dynamicListWithoutSelected(options), new TypeReference<>() {
        });
    }

    @SafeVarargs
    final DynamicList dynamicListWithFirstSelected(Pair<String, UUID>... pairs) {
        return dynamicListWithSelected(0, pairs);
    }

    @SafeVarargs
    final DynamicList dynamicListWithSelected(int index, Pair<String, UUID>... pairs) {
        return dynamicListWithSelected(pairs[index].getLeft(), pairs[index].getRight(), pairs);
    }

    @SafeVarargs
    final DynamicList dynamicListWithSelected(String label, UUID code, Pair<String, UUID>... pairs) {
        return DynamicList.builder()
            .value(DynamicListElement.builder().label(label).code(code).build())
            .listItems(listItems(pairs))
            .build();
    }

    @SafeVarargs
    private DynamicList dynamicListWithoutSelected(Pair<String, UUID>... items) {
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(listItems(items))
            .build();
    }

    @SafeVarargs
    private List<DynamicListElement> listItems(Pair<String, UUID>... pairs) {
        return Arrays.stream(pairs)
            .map(pair -> DynamicListElement.builder().label(pair.getLeft()).code(pair.getRight()).build())
            .collect(Collectors.toList());
    }

    List<Element<HearingBooking>> hearings() {
        return List.of(
            hearing(LocalDateTime.of(2020, 3, 15, 20, 20)),
            hearing(LocalDateTime.of(2020, 3, 16, 10, 10))
        );
    }

    Element<HearingBooking> hearingWithCMOId(LocalDateTime startDate, UUID cmoId) {
        return element(hearing(startDate, cmoId));
    }

    Element<HearingBooking> hearing(UUID hearingId, LocalDateTime startDate) {
        return element(hearingId, hearing(startDate, null));
    }

    Element<HearingBooking> hearing(LocalDateTime startDate) {
        return hearingWithCMOId(startDate, null);
    }

    private HearingBooking hearing(LocalDateTime startDate, UUID cmoId) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .endDate(startDate.plusHours(1))
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .judgeEmailAddress(JUDGE_EMAIL)
                .build())
            .caseManagementOrderId(cmoId)
            .build();
    }
}
