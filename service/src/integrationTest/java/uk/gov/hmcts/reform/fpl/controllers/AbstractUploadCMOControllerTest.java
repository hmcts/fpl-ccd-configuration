package uk.gov.hmcts.reform.fpl.controllers;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

public abstract class AbstractUploadCMOControllerTest extends AbstractControllerTest {
    AbstractUploadCMOControllerTest(String eventName) {
        super(eventName);
    }

    @MockBean
    private FeatureToggleService toggleService;

    protected void givenLegacyFlow() {
        when(toggleService.isUploadDraftCMOEnabled()).thenReturn(false);
    }

    protected void givenNewFlow() {
        when(toggleService.isUploadDraftCMOEnabled()).thenReturn(true);
    }

    @SafeVarargs
    protected final DynamicList dynamicListWithoutSelected(Pair<String, UUID>... pairs) {
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(listItems(pairs))
            .build();
    }

    @SafeVarargs
    protected final DynamicList dynamicListWithFirstSelected(Pair<String, UUID>... pairs) {
        return dynamicListWithSelected(0, pairs);
    }

    @SafeVarargs
    protected final DynamicList dynamicListWithSelected(int index, Pair<String, UUID>... pairs) {
        return dynamicListWithSelected(pairs[index].getLeft(), pairs[index].getRight(), pairs);
    }

    @SafeVarargs
    protected final DynamicList dynamicListWithSelected(String label, UUID code, Pair<String, UUID>... pairs) {
        return DynamicList.builder()
            .value(DynamicListElement.builder().label(label).code(code).build())
            .listItems(listItems(pairs))
            .build();
    }

    @SafeVarargs
    private List<DynamicListElement> listItems(Pair<String, UUID>... pairs) {
        return Arrays.stream(pairs)
            .map(pair -> DynamicListElement.builder().label(pair.getLeft()).code(pair.getRight()).build())
            .collect(Collectors.toList());
    }

    protected List<Element<HearingBooking>> hearings() {
        return List.of(
            hearing(LocalDateTime.of(2020, 3, 15, 20, 20)),
            hearing(LocalDateTime.of(2020, 3, 16, 10, 10))
        );
    }

    protected Element<HearingBooking> hearing(UUID hearingId, LocalDateTime startDate) {
        return element(hearingId, hearing(startDate, null));
    }

    protected Element<HearingBooking> hearing(LocalDateTime startDate) {
        return hearingWithCMOId(startDate, null);
    }

    protected Element<HearingBooking> hearingWithCMOId(LocalDateTime startDate, UUID cmoId) {
        return element(hearing(startDate, cmoId));
    }

    private HearingBooking hearing(LocalDateTime startDate, UUID cmoId) {
        return HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(startDate)
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeLastName("Judy")
                .build())
            .caseManagementOrderId(cmoId)
            .build();
    }
}
