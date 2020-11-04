package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.events.cmo.AgreedCMOUploaded;
import uk.gov.hmcts.reform.fpl.events.cmo.DraftCMOUploaded;
import uk.gov.hmcts.reform.fpl.events.cmo.UploadCMOEvent;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.event.UploadCMOEventData;
import uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.model.order.CaseManagementOrder.from;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UploadCMOService {

    private final ObjectMapper mapper;
    private final Time time;

    public UploadCMOEventData getInitialPageData(CaseData caseData) {
        List<Element<HearingBooking>> futureHearings = caseData.getFutureHearings();
        List<Element<HearingBooking>> pastHearings = caseData.getPastHearings();
        List<Element<CaseManagementOrder>> unsealedOrders = caseData.getDraftUploadedCMOs();

        sortHearings(futureHearings);
        sortHearings(pastHearings);

        DynamicList futureHearingsList = buildDynamicList(futureHearings);
        DynamicList pastHearingsList = buildDynamicList(getHearingsWithoutCMO(pastHearings, unsealedOrders));

        String hearingsInfo = buildHearingsWithCMOsText(unsealedOrders, pastHearings);

        return UploadCMOEventData.builder()
            .futureHearingsForCMO(futureHearingsList)
            .pastHearingsForCMO(pastHearingsList)
            .cmosSentToJudge(hearingsInfo)
            .showCMOsSentToJudge(YesNo.from(!hearingsInfo.isBlank()))
            .build();
    }

    public UploadCMOEventData getCMOInfo(CaseData caseData) {
        UploadCMOEventData eventData = caseData.getUploadCMOEventData();

        UUID selectedHearingId = eventData.getSelectedHearingId(mapper);

        HearingBooking hearing = getSelectedHearing(selectedHearingId, caseData.getHearingDetails());

        UploadCMOEventData.UploadCMOEventDataBuilder newEventDataBuilder = UploadCMOEventData.builder();

        if (hearing.hasCMOAssociation()) {
            CaseManagementOrder cmo = findElement(hearing.getCaseManagementOrderId(), caseData.getDraftUploadedCMOs())
                .map(Element::getValue)
                .orElseThrow(() -> new CMONotFoundException("CMO for related hearing could not be found"));

            newEventDataBuilder.previousCMO(cmo.getOrder())
                .cmoSupportingDocs(cmo.getSupportingDocs());
        }

        return newEventDataBuilder
            .showReplacementCMO(YesNo.from(hearing.hasCMOAssociation()))
            .futureHearingsForCMO(regenerateList(eventData.getFutureHearingsForCMO(), caseData.getFutureHearings()))
            .pastHearingsForCMO(regenerateList(
                eventData.getPastHearingsForCMO(),
                getHearingsWithoutCMO(caseData.getPastHearings(), caseData.getDraftUploadedCMOs())
            ))
            .cmoHearingInfo(hearing.toLabel())
            .build();
    }

    public UploadCMOEventData getReviewData(CaseData caseData) {
        UploadCMOEventData eventData = caseData.getUploadCMOEventData();

        UUID selectedHearingId = eventData.getSelectedHearingId(mapper);
        HearingBooking hearing = getSelectedHearing(selectedHearingId, caseData.getHearingDetails());

        return UploadCMOEventData.builder()
            .cmoJudgeInfo(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
            .cmoToSend(getUploadedCMO(eventData, hearing, caseData.getDraftUploadedCMOs()))
            .futureHearingsForCMO(regenerateList(eventData.getFutureHearingsForCMO(), caseData.getFutureHearings()))
            .pastHearingsForCMO(regenerateList(
                eventData.getPastHearingsForCMO(),
                getHearingsWithoutCMO(caseData.getPastHearings(), caseData.getDraftUploadedCMOs())
            ))
            .build();
    }

    public void updateHearingsAndOrders(UploadCMOEventData eventData, List<Element<HearingBooking>> hearings,
                                        List<Element<CaseManagementOrder>> unsealedOrders,
                                        List<Element<HearingFurtherEvidenceBundle>> evidenceBundles) {

        UUID selectedHearingId = eventData.getSelectedHearingId(mapper);
        HearingBooking hearing = getSelectedHearing(selectedHearingId, hearings);

        List<Element<SupportingEvidenceBundle>> supportingDocs = eventData.getCmoSupportingDocs();

        Element<CaseManagementOrder> order = element(from(
            getUploadedCMO(eventData, hearing, unsealedOrders),
            hearing,
            time.now().toLocalDate(),
            eventData.isAgreed() ? SEND_TO_JUDGE : DRAFT,
            supportingDocs
        ));

        Optional<UUID> uuid = updateHearingWithCmoId(hearing, order);

        insertOrder(unsealedOrders, order, uuid);

        if (eventData.isAgreed() && !supportingDocs.isEmpty()) {
            migrateDocuments(evidenceBundles, selectedHearingId, hearing, supportingDocs);
        }
    }

    public UploadCMOEvent buildEventToPublish(CaseData caseData, CaseData caseDataBefore) {
        List<Element<CaseManagementOrder>> unsealedCMOs = new ArrayList<>(caseData.getDraftUploadedCMOs());
        unsealedCMOs.removeAll(caseDataBefore.getDraftUploadedCMOs());

        Element<CaseManagementOrder> cmo = unsealedCMOs.get(0);

        Optional<Element<HearingBooking>> optionalHearing = caseData.getHearingDetails().stream()
            .filter(hearingElement -> cmo.getId().equals(hearingElement.getValue().getCaseManagementOrderId()))
            .findFirst();

        if (optionalHearing.isPresent()) {
            HearingBooking hearing = optionalHearing.get().getValue();
            CMOStatus status = cmo.getValue().getStatus();

            if (SEND_TO_JUDGE == status) {
                return new AgreedCMOUploaded(caseData, hearing);
            } else if (DRAFT == status) {
                return new DraftCMOUploaded(caseData, hearing);
            } else {
                throw new IllegalStateException("Unexpected cmo status: " + status);
            }
        } else {
            throw new HearingNotFoundException("No hearing found for cmo: " + cmo.getId());
        }
    }

    private void migrateDocuments(List<Element<HearingFurtherEvidenceBundle>> evidenceBundles, UUID selectedHearingId,
                                  HearingBooking hearing, List<Element<SupportingEvidenceBundle>> supportingDocs) {
        Optional<Element<HearingFurtherEvidenceBundle>> bundle = findElement(selectedHearingId, evidenceBundles);

        if (bundle.isPresent()) {
            bundle.get().getValue().getSupportingEvidenceBundle().addAll(supportingDocs);
        } else {
            evidenceBundles.add(element(selectedHearingId, HearingFurtherEvidenceBundle.builder()
                .hearingName(hearing.toLabel())
                .supportingEvidenceBundle(supportingDocs)
                .build()));
        }
    }

    private List<Element<HearingBooking>> getHearingsWithoutCMO(List<Element<HearingBooking>> hearings,
                                                                List<Element<CaseManagementOrder>> unsealedOrders) {
        return hearings.stream()
            .filter(hearing -> associatedToUnreviewedCMO(hearing, unsealedOrders)
                || !hearing.getValue().hasCMOAssociation())
            .collect(toList());
    }

    private void insertOrder(List<Element<CaseManagementOrder>> unsealedOrders, Element<CaseManagementOrder> order,
                             Optional<UUID> id) {
        if (id.isPresent()) {
            // overwrite old draft CMO
            int index = -1;
            for (int i = 0; i < unsealedOrders.size(); i++) {
                if (unsealedOrders.get(i).getId().equals(id.get())) {
                    index = i;
                    break;
                }
            }
            unsealedOrders.set(index, order);
        } else {
            unsealedOrders.add(order);
        }
    }

    private Optional<UUID> updateHearingWithCmoId(HearingBooking hearing, Element<CaseManagementOrder> cmo) {
        UUID previousCMOId = hearing.getCaseManagementOrderId();
        hearing.setCaseManagementOrderId(cmo.getId());
        return Optional.ofNullable(previousCMOId);
    }

    private HearingBooking getSelectedHearing(UUID id, List<Element<HearingBooking>> hearings) {
        return findElement(id, hearings)
            .orElseThrow(() -> new HearingNotFoundException("No hearing found with id: " + id))
            .getValue();
    }

    private DocumentReference getUploadedCMO(UploadCMOEventData currentEventData,
                                             HearingBooking selectedHearing,
                                             List<Element<CaseManagementOrder>> unsealedCMOs) {

        DocumentReference uploadedCMO = currentEventData.getUploadedCaseManagementOrder();

        if (uploadedCMO == null && (uploadedCMO = currentEventData.getReplacementCMO()) == null) {
            // search for the cmo to get the previous order
            uploadedCMO = findElement(selectedHearing.getCaseManagementOrderId(), unsealedCMOs)
                .map(cmo -> cmo.getValue().getOrder())
                .orElseThrow(() -> new CMONotFoundException("CMO for related hearing could not be found"));
        }

        return uploadedCMO;
    }

    /*
     reconstruct dynamic list
     see RDM-5696 and RDM-6651
     can be deleted when above is fixed
    */
    private DynamicList regenerateList(Object dynamicList, List<Element<HearingBooking>> hearings) {
        if (dynamicList instanceof String) {
            UUID selectedId = getDynamicListSelectedValue(dynamicList, mapper);
            sortHearings(hearings);
            return buildDynamicList(hearings, selectedId);
        }
        return null;
    }

    private DynamicList buildDynamicList(List<Element<HearingBooking>> hearings) {
        return buildDynamicList(hearings, null);
    }

    private DynamicList buildDynamicList(List<Element<HearingBooking>> hearings, UUID selected) {
        return asDynamicList(hearings, selected, HearingBooking::toLabel);
    }

    private boolean associatedToUnreviewedCMO(Element<HearingBooking> hearing,
                                              List<Element<CaseManagementOrder>> unsealedCMOs) {
        return unsealedCMOs.stream()
            .filter(cmo -> cmo.getValue().getStatus() != SEND_TO_JUDGE)
            .anyMatch(cmo -> cmo.getId().equals(hearing.getValue().getCaseManagementOrderId()));
    }

    private String buildHearingsWithCMOsText(List<Element<CaseManagementOrder>> unsealedOrders,
                                             List<Element<HearingBooking>> hearings) {

        List<HearingBooking> filtered = new ArrayList<>();
        hearings.forEach(hearing -> unsealedOrders.stream()
            .filter(order -> includeHearing(order, hearing.getValue()))
            .map(order -> hearing.getValue())
            .findFirst()
            .ifPresent(filtered::add)
        );

        filtered.sort(Comparator.comparing(HearingBooking::getStartDate));
        return filtered.stream().map(HearingBooking::toLabel).collect(Collectors.joining("\n"));
    }

    private boolean includeHearing(Element<CaseManagementOrder> cmo, HearingBooking hearing) {
        return SEND_TO_JUDGE == cmo.getValue().getStatus() && cmo.getId().equals(hearing.getCaseManagementOrderId());
    }

    private void sortHearings(List<Element<HearingBooking>> hearings) {
        hearings.sort(Comparator.comparing(hearing -> hearing.getValue().getStartDate()));
    }
}
