package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.testingsupport.DynamicListHelper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

abstract class AbstractUploadDraftOrdersControllerTest extends AbstractCallbackTest {

    @Autowired
    DynamicListHelper dynamicLists;

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
