package uk.gov.hmcts.reform.fpl.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import uk.gov.hmcts.reform.fpl.controllers.orders.UploadDraftOrdersController;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.CMOType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testDocumentReference;

@WebMvcTest(UploadDraftOrdersController.class)
@OverrideAutoConfiguration(enabled = true)
class UploadDraftOrdersPopulateOrdersInfoMidEventControllerTest extends AbstractUploadDraftOrdersControllerTest {

    private static final DocumentReference DOCUMENT = testDocumentReference();

    @Test
    void shouldExtractHearingInfo() {
        List<Element<HearingBooking>> hearings = hearings();

        DynamicList pastHearingList = dynamicLists.from(0,
            option("Case management hearing, 15 March 2020", hearings.get(0).getId()),
            option("Case management hearing, 16 March 2020", hearings.get(1).getId())
        );

        Map<String, Object> pastHearingListAsMap = mapper.convertValue(pastHearingList, new TypeReference<>() {
        });

        UploadDraftOrdersData prevEventData = UploadDraftOrdersData.builder()
            .hearingOrderDraftKind(List.of(CMO, C21))
            .cmoUploadType(CMOType.AGREED)
            .pastHearingsForCMO(pastHearingList)
            .build();

        CaseData caseData = CaseData.builder()
            .uploadDraftOrdersEventData(prevEventData)
            .hearingDetails(hearings)
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "populate-drafts-info"));
        UploadDraftOrdersData eventData = updatedCaseData.getUploadDraftOrdersEventData();

        assertThat(eventData.getHearingOrderDraftKind()).isEqualTo(prevEventData.getHearingOrderDraftKind());
        assertThat(eventData.getCmoHearingInfo()).isEqualTo("Case management hearing, 15 March 2020");
        assertThat(eventData.getShowReplacementCMO()).isEqualTo(YesNo.NO);
        assertThat(eventData.getPastHearingsForCMO()).isEqualTo(pastHearingListAsMap);
        assertThat(eventData.getCmoJudgeInfo()).isEqualTo("Her Honour Judge Judy");
        assertThat(eventData.getCurrentHearingOrderDrafts()).extracting(Element::getValue)
            .containsExactly(HearingOrder.builder().build());
    }

    @Test
    void shouldExtractPreviousCMODataWhenPresent() {
        List<Element<SupportingEvidenceBundle>> supportingDocs = List.of(element(SupportingEvidenceBundle.builder()
            .name("some doc")
            .build()));

        List<Element<HearingOrder>> unsealedCMOs = List.of(
            element(HearingOrder.builder()
                .order(DOCUMENT)
                .status(CMOStatus.DRAFT)
                .supportingDocs(supportingDocs)
                .build()));

        List<Element<HearingBooking>> hearings = new ArrayList<>(hearings());
        hearings.add(hearingWithCMOId(LocalDateTime.of(2020, 3, 17, 11, 11), unsealedCMOs.get(0).getId()));

        DynamicList pastHearingList = dynamicLists.from(
            2,
            Pair.of("Case management hearing, 15 March 2020", hearings.get(0).getId()),
            Pair.of("Case management hearing, 16 March 2020", hearings.get(1).getId()),
            Pair.of("Case management hearing, 17 March 2020", hearings.get(2).getId())
        );

        Map<String, Object> pastHearingListAsMap = mapper.convertValue(pastHearingList, new TypeReference<>() {
        });

        UploadDraftOrdersData prevEventData = UploadDraftOrdersData.builder()
            .pastHearingsForCMO(pastHearingList)
            .hearingOrderDraftKind(List.of(CMO))
            .cmoUploadType(CMOType.AGREED)
            .cmoSupportingDocs(supportingDocs)
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .draftUploadedCMOs(unsealedCMOs)
            .uploadDraftOrdersEventData(prevEventData)
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "populate-drafts-info"));
        UploadDraftOrdersData updatedEventData = updatedCaseData.getUploadDraftOrdersEventData();

        assertThat(updatedEventData.getHearingOrderDraftKind()).isEqualTo(prevEventData.getHearingOrderDraftKind());
        assertThat(updatedEventData.getPreviousCMO()).isEqualTo(DOCUMENT);
        assertThat(updatedEventData.getCmoToSend()).isEqualTo(DOCUMENT);
        assertThat(updatedEventData.getShowReplacementCMO()).isEqualTo(YesNo.YES);
        assertThat(updatedEventData.getCmoSupportingDocs()).isEqualTo(supportingDocs);
        assertThat(updatedEventData.getCmoHearingInfo()).isEqualTo("Case management hearing, 17 March 2020");
        assertThat(updatedEventData.getPastHearingsForCMO()).isEqualTo(pastHearingListAsMap);
        assertThat(updatedEventData.getCmoJudgeInfo()).isEqualTo("Her Honour Judge Judy");
    }

    @Test
    void shouldReplaceSupportingDocumentsInTheDraftCMOWhenSupportingDocumentIsPresent() {
        List<Element<SupportingEvidenceBundle>> supportingDocs = List.of(element(SupportingEvidenceBundle.builder()
            .name("some doc")
            .build()));

        List<Element<SupportingEvidenceBundle>> updatedSupportingDocs = List.of(
            element(SupportingEvidenceBundle.builder()
                .name("new document")
                .build()));

        Element<HearingOrder> draftCMO = element(HearingOrder.builder()
            .order(DOCUMENT)
            .status(CMOStatus.DRAFT)
            .supportingDocs(supportingDocs)
            .build());

        List<Element<HearingOrder>> unsealedCMOs = List.of(draftCMO);

        List<Element<HearingBooking>> hearings = new ArrayList<>(hearings());
        hearings.add(hearingWithCMOId(LocalDateTime.of(2021, 3, 17, 11, 11), unsealedCMOs.get(0).getId()));

        DynamicList hearingsList = dynamicLists.from(
            2,
            Pair.of("Case management hearing, 15 March 2020", hearings.get(0).getId()),
            Pair.of("Case management hearing, 16 March 2020", hearings.get(1).getId()),
            Pair.of("Case management hearing, 17 March 2021", hearings.get(2).getId())
        );

        Map<String, Object> hearingListAsMap = mapper.convertValue(hearingsList, new TypeReference<>() {
        });

        UploadDraftOrdersData prevEventData = UploadDraftOrdersData.builder()
            .futureHearingsForCMO(hearingsList)
            .hearingOrderDraftKind(List.of(CMO))
            .cmoUploadType(CMOType.DRAFT)
            .previousCMO(draftCMO.getValue().getOrder())
            .showCMOsSentToJudge(YesNo.NO)
            .cmoSupportingDocs(updatedSupportingDocs)
            .build();

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearings)
            .draftUploadedCMOs(unsealedCMOs)
            .uploadDraftOrdersEventData(prevEventData)
            .build();

        CaseData updatedCaseData = extractCaseData(postMidEvent(caseData, "populate-drafts-info"));
        UploadDraftOrdersData updatedEventData = updatedCaseData.getUploadDraftOrdersEventData();

        assertThat(updatedEventData.getHearingOrderDraftKind()).isEqualTo(prevEventData.getHearingOrderDraftKind());
        assertThat(updatedEventData.getPreviousCMO()).isEqualTo(DOCUMENT);
        assertThat(updatedEventData.getCmoToSend()).isEqualTo(DOCUMENT);
        assertThat(updatedEventData.getShowReplacementCMO()).isEqualTo(YesNo.YES);
        assertThat(updatedEventData.getCmoSupportingDocs()).isEqualTo(updatedSupportingDocs);
        assertThat(updatedEventData.getCmoHearingInfo()).isEqualTo("Case management hearing, 17 March 2021");
        assertThat(updatedEventData.getFutureHearingsForCMO()).isEqualTo(hearingListAsMap);
        assertThat(updatedEventData.getCmoJudgeInfo()).isEqualTo("Her Honour Judge Judy");
    }
}
