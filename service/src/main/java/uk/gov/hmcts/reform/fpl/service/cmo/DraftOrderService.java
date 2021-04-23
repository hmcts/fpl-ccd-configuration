package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderKind;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.DocumentUploadHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderKind.CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement.DEFAULT_CODE;
import static uk.gov.hmcts.reform.fpl.model.order.HearingOrder.from;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftOrderService {

    private static final DynamicListElement NO_HEARING = DynamicListElement.defaultListItem("No hearing");
    private final ObjectMapper mapper;
    private final Time time;
    private final DocumentUploadHelper documentUploadHelper;

    public UploadDraftOrdersData getInitialData(CaseData caseData) {
        final UploadDraftOrdersData eventData = caseData.getUploadDraftOrdersEventData();

        List<Element<HearingBooking>> futureHearings = caseData.getFutureHearings();
        List<Element<HearingBooking>> pastHearings = caseData.getPastHearings();
        List<Element<HearingBooking>> allHearings = defaultIfNull(caseData.getHearingDetails(), new ArrayList<>());

        sortHearings(futureHearings);
        sortHearings(pastHearings);
        sortHearings(allHearings);

        List<Element<HearingOrder>> unsealedOrders = caseData.getDraftUploadedCMOs();
        String hearingsInfo = buildHearingsWithCMOsText(unsealedOrders, pastHearings);

        return eventData.toBuilder()
            .hearingOrderDraftKind(getHearingOrderKinds(eventData))
            .futureHearingsForCMO(rebuildDynamicList(eventData.getFutureHearingsForCMO(), futureHearings))
            .pastHearingsForCMO(rebuildDynamicList(eventData.getPastHearingsForCMO(),
                getHearingsWithoutCMO(pastHearings, unsealedOrders)))
            .hearingsForHearingOrderDrafts(rebuildDynamicList(eventData.getHearingsForHearingOrderDrafts(),
                allHearings, NO_HEARING))
            .cmosSentToJudge(hearingsInfo)
            .showCMOsSentToJudge(YesNo.from(!hearingsInfo.isBlank()))
            .build();
    }

    public UploadDraftOrdersData getDraftsInfo(CaseData caseData) {
        UploadDraftOrdersData eventData = caseData.getUploadDraftOrdersEventData();

        UUID selectedHearingId = getSelectedHearingId(eventData);

        List<HearingOrderKind> hearingOrderKinds = getHearingOrderKinds(eventData);

        UploadDraftOrdersData.UploadDraftOrdersDataBuilder newEventDataBuilder = UploadDraftOrdersData.builder()
            .hearingOrderDraftKind(hearingOrderKinds)
            .futureHearingsForCMO(rebuildDynamicList(eventData.getFutureHearingsForCMO(), caseData.getFutureHearings()))
            .pastHearingsForCMO(rebuildDynamicList(eventData.getPastHearingsForCMO(),
                getHearingsWithoutCMO(caseData.getPastHearings(), caseData.getDraftUploadedCMOs())))
            .hearingsForHearingOrderDrafts(rebuildDynamicList(eventData.getHearingsForHearingOrderDrafts(),
                caseData.getHearingDetails(), NO_HEARING));

        if (hearingOrderKinds.contains(HearingOrderKind.CMO)) {
            HearingBooking hearing = getSelectedHearing(selectedHearingId, caseData.getHearingDetails());

            if (hearing.hasCMOAssociation()) {
                HearingOrder cmo = findElement(hearing.getCaseManagementOrderId(), caseData.getDraftUploadedCMOs())
                    .map(Element::getValue)
                    .orElseThrow(() -> new CMONotFoundException("CMO for related hearing could not be found"));

                newEventDataBuilder.previousCMO(cmo.getOrder());

                if (eventData.getPreviousCMO() == null) {
                    newEventDataBuilder.cmoSupportingDocs(cmo.getSupportingDocs());
                } else {
                    newEventDataBuilder.cmoSupportingDocs(eventData.getCmoSupportingDocs());
                }
            }

            newEventDataBuilder
                .cmoUploadType(eventData.getCmoUploadType())
                .cmoJudgeInfo(formatJudgeTitleAndName(hearing.getJudgeAndLegalAdvisor()))
                .cmoToSend(getCMO(eventData, hearing, caseData.getDraftUploadedCMOs()))
                .showReplacementCMO(YesNo.from(hearing.hasCMOAssociation()))
                .cmoHearingInfo(hearing.toLabel());
        }

        if (hearingOrderKinds.contains(HearingOrderKind.C21)) {
            newEventDataBuilder.currentHearingOrderDrafts(getC21Drafts(caseData, selectedHearingId));
        }
        return newEventDataBuilder.build();
    }

    private List<Element<HearingOrder>> getC21Drafts(CaseData caseData, UUID selectedHearingId) {

        final List<Element<HearingOrder>> draftOrders = unwrapElements(caseData.getHearingOrdersBundlesDrafts())
            .stream()
            .filter(bundle -> Objects.equals(bundle.getHearingId(), selectedHearingId))
            .findFirst()
            .map(HearingOrdersBundle::getOrders)
            .orElse(new ArrayList<>());

        final List<Element<HearingOrder>> nonCMODraftOrders = draftOrders.stream()
            .filter(draftOrder -> C21.equals(draftOrder.getValue().getType()))
            .collect(toList());

        if (isEmpty(nonCMODraftOrders)) {
            nonCMODraftOrders.add(element(HearingOrder.builder().build()));
        }

        return nonCMODraftOrders;
    }

    public UUID updateCase(UploadDraftOrdersData eventData, List<Element<HearingBooking>> hearings,
                           List<Element<HearingOrder>> cmoDrafts,
                           List<Element<HearingFurtherEvidenceBundle>> evidenceBundles,
                           List<Element<HearingOrdersBundle>> bundles) {

        final UUID selectedHearingId = getSelectedHearingId(eventData);
        final List<HearingOrderKind> hearingOrderKinds = getHearingOrderKinds(eventData);

        if (hearingOrderKinds.contains(CMO)) {

            final Element<HearingBooking> hearing = ofNullable(selectedHearingId)
                .flatMap(id -> findElement(id, hearings))
                .orElseThrow(() -> new HearingNotFoundException(selectedHearingId));

            DocumentReference cmo = getCMO(eventData, hearing.getValue(), cmoDrafts);

            List<Element<SupportingEvidenceBundle>> supportingDocs = eventData.getCmoSupportingDocs();

            Element<HearingOrder> order = element(from(
                cmo,
                hearing.getValue(),
                time.now().toLocalDate(),
                eventData.isCmoAgreed() ? AGREED_CMO : DRAFT_CMO,
                supportingDocs
            ));

            Optional<UUID> previousCmoId = updateHearingWithCmoId(hearing.getValue(), order);

            insertOrder(cmoDrafts, order, previousCmoId.orElse(null));

            if (eventData.isCmoAgreed() && !supportingDocs.isEmpty()) {
                migrateDocuments(evidenceBundles, selectedHearingId, hearing.getValue(), supportingDocs);
            }

            addOrdersToBundle(bundles, List.of(order), hearing, eventData.isCmoAgreed() ? AGREED_CMO : DRAFT_CMO);
        }

        if (hearingOrderKinds.contains(HearingOrderKind.C21)) {
            final Element<HearingBooking> hearing = ofNullable(selectedHearingId)
                .flatMap(id -> findElement(id, hearings))
                .orElse(null);

            eventData.getCurrentHearingOrderDrafts().forEach(o -> {
                o.getValue().setDateSent(time.now().toLocalDate());
                o.getValue().setStatus(SEND_TO_JUDGE);
            });
            addOrdersToBundle(bundles, eventData.getCurrentHearingOrderDrafts(), hearing, C21);
        }

        bundles.removeIf(bundle -> isEmpty(bundle.getValue().getOrders()));

        return selectedHearingId;
    }

    public List<Element<HearingOrdersBundle>> migrateCmoDraftToOrdersBundles(CaseData caseData) {

        List<Element<HearingOrder>> cmoDrafts = caseData.getDraftUploadedCMOs();
        List<Element<HearingBooking>> hearings = defaultIfNull(caseData.getHearingDetails(), new ArrayList<>());
        List<Element<HearingOrdersBundle>> bundles = defaultIfNull(caseData.getHearingOrdersBundlesDrafts(),
            new ArrayList<>());

        bundles.forEach(bundle -> bundle.getValue().getOrders().removeIf(draft -> draft.getValue().getType().isCmo()));

        unwrapElements(cmoDrafts).forEach(draft -> {
            HearingOrderType type = draft.getStatus() == DRAFT ? DRAFT_CMO : AGREED_CMO;
            String title = type == AGREED_CMO ? "Agreed CMO discussed at hearing" : "Draft CMO from advocates' meeting";
            draft.setTitle(defaultIfNull(draft.getTitle(), title));
            draft.setType(type);
        });

        for (Element<HearingBooking> hearing : hearings) {
            final Optional<Element<HearingOrder>> hearingCmoDrafts = cmoDrafts.stream()
                .filter(draft -> Objects.equals(draft.getId(), hearing.getValue().getCaseManagementOrderId()))
                .findFirst();

            if (hearingCmoDrafts.isPresent()) {
                Optional<Element<HearingOrdersBundle>> bundle = bundles.stream()
                    .filter(b -> Objects.equals(b.getValue().getHearingId(), hearing.getId()))
                    .findFirst();

                if (bundle.isEmpty()) {
                    CMOStatus status = hearingCmoDrafts.get().getValue().getStatus();
                    bundles.add(element(HearingOrdersBundle.builder()
                        .build()
                        .updateHearing(hearing.getId(), hearing.getValue())
                        .updateOrders(List.of(hearingCmoDrafts.get()), status == DRAFT ? DRAFT_CMO : AGREED_CMO))
                    );
                } else {
                    bundle.get().getValue().getOrders().add(0, hearingCmoDrafts.get());
                }
            }
        }

        bundles.removeIf(b -> isEmpty(b.getValue().getOrders()));

        return bundles;
    }

    private void migrateDocuments(List<Element<HearingFurtherEvidenceBundle>> evidenceBundles, UUID selectedHearingId,
                                  HearingBooking hearing, List<Element<SupportingEvidenceBundle>> cmoSupportingDocs) {
        Optional<Element<HearingFurtherEvidenceBundle>> bundle = findElement(selectedHearingId, evidenceBundles);

        List<Element<SupportingEvidenceBundle>> supportingDocs = addAuditData(cmoSupportingDocs);

        if (bundle.isPresent()) {
            List<Element<SupportingEvidenceBundle>> hearingDocs = bundle.get().getValue().getSupportingEvidenceBundle();

            supportingDocs.forEach(supportingDoc -> {
                int hearingDocIndex = Iterables.indexOf(hearingDocs,
                    hearingDoc -> Objects.equals(hearingDoc.getId(), supportingDoc.getId()));
                if (hearingDocIndex < 0) {
                    hearingDocs.add(supportingDoc);
                } else {
                    hearingDocs.remove(hearingDocIndex);
                    hearingDocs.add(hearingDocIndex, supportingDoc);
                }
            });
        } else {
            evidenceBundles.add(element(selectedHearingId, HearingFurtherEvidenceBundle.builder()
                .hearingName(hearing.toLabel())
                .supportingEvidenceBundle(supportingDocs)
                .build()));
        }
    }

    private void addOrdersToBundle(List<Element<HearingOrdersBundle>> bundles,
                                   List<Element<HearingOrder>> orders,
                                   Element<HearingBooking> hearing,
                                   HearingOrderType type) {
        UUID hearingId = Optional.ofNullable(hearing).map(Element::getId).orElse(null);
        HearingBooking hearingBooking = Optional.ofNullable(hearing).map(Element::getValue).orElse(null);

        HearingOrdersBundle hearingOrdersBundle = bundles.stream()
            .filter(bundle -> Objects.equals(bundle.getValue().getHearingId(), hearingId))
            .map(Element::getValue)
            .findFirst()
            .orElseGet(() -> addNewDraftBundle(bundles));

        hearingOrdersBundle
            .updateHearing(hearingId, hearingBooking)
            .updateOrders(orders, type);

        if (AGREED_CMO.equals(type)) {
            hearingOrdersBundle.getOrders().removeIf(order -> Objects.equals(order.getValue().getType(), DRAFT_CMO));
        }
    }

    private HearingOrdersBundle addNewDraftBundle(List<Element<HearingOrdersBundle>> bundles) {
        HearingOrdersBundle newOrdersBundle = HearingOrdersBundle.builder().build();
        bundles.add(element(newOrdersBundle));
        return newOrdersBundle;
    }

    private List<Element<HearingBooking>> getHearingsWithoutCMO(List<Element<HearingBooking>> hearings,
                                                                List<Element<HearingOrder>> unsealedOrders) {
        return hearings.stream()
            .filter(hearing -> associatedToUnreviewedCMO(hearing, unsealedOrders)
                || !hearing.getValue().hasCMOAssociation())
            .collect(toList());
    }

    private void insertOrder(List<Element<HearingOrder>> unsealedOrders, Element<HearingOrder> order, UUID id) {
        if (isNotEmpty(id)) {
            // overwrite old draft CMO
            int index = -1;
            for (int i = 0; i < unsealedOrders.size(); i++) {
                if (unsealedOrders.get(i).getId().equals(id)) {
                    index = i;
                    break;
                }
            }
            unsealedOrders.set(index, order);
        } else {
            unsealedOrders.add(order);
        }
    }

    private Optional<UUID> updateHearingWithCmoId(HearingBooking hearing, Element<HearingOrder> cmo) {
        UUID previousCMOId = hearing.getCaseManagementOrderId();
        hearing.setCaseManagementOrderId(cmo.getId());
        return ofNullable(previousCMOId);
    }

    private HearingBooking getSelectedHearing(UUID id, List<Element<HearingBooking>> hearings) {
        return findElement(id, hearings)
            .orElseThrow(() -> new HearingNotFoundException("No hearing found with id: " + id))
            .getValue();
    }

    private UUID getSelectedHearingId(UploadDraftOrdersData eventData) {
        if (getHearingOrderKinds(eventData).contains(HearingOrderKind.CMO)) {
            Object dynamicList = defaultIfNull(eventData.getPastHearingsForCMO(), eventData.getFutureHearingsForCMO());
            return getDynamicListSelectedValue(dynamicList, mapper);
        } else {
            UUID selectedHearingId = getDynamicListSelectedValue(eventData.getHearingsForHearingOrderDrafts(), mapper);
            return DEFAULT_CODE.equals(selectedHearingId) ? null : selectedHearingId;
        }
    }

    private List<HearingOrderKind> getHearingOrderKinds(UploadDraftOrdersData eventData) {
        return ofNullable(eventData)
            .map(UploadDraftOrdersData::getHearingOrderDraftKind)
            .orElse(emptyList());
    }

    private DocumentReference getCMO(UploadDraftOrdersData currentEventData,
                                     HearingBooking selectedHearing,
                                     List<Element<HearingOrder>> unsealedCMOs) {
        DocumentReference uploadedCMO = currentEventData.getUploadedCaseManagementOrder();

        if (uploadedCMO == null && (uploadedCMO = currentEventData.getReplacementCMO()) == null) {
            // search for the cmo to get the previous order
            uploadedCMO = findElement(selectedHearing.getCaseManagementOrderId(), unsealedCMOs)
                .map(cmo -> cmo.getValue().getOrder())
                .orElse(null);
        }

        return uploadedCMO;
    }

    /*
     reconstruct dynamic list
     see RDM-5696 and RDM-6651
     can be deleted when above is fixed
    */
    private DynamicList rebuildDynamicList(Object dynamicList, List<Element<HearingBooking>> hearings) {
        if (dynamicList == null) {
            return buildDynamicList(hearings);
        }
        if (dynamicList instanceof String) {
            UUID selectedId = getDynamicListSelectedValue(dynamicList, mapper);
            sortHearings(hearings);
            return buildDynamicList(hearings, selectedId);
        }
        return mapper.convertValue(dynamicList, DynamicList.class);
    }

    private DynamicList rebuildDynamicList(Object dynamicList, List<Element<HearingBooking>> hearings,
                                           DynamicListElement item) {
        if (dynamicList == null) {
            return buildDynamicList(item, hearings);
        }
        if (dynamicList instanceof String) {
            UUID selectedId = getDynamicListSelectedValue(dynamicList, mapper);
            sortHearings(hearings);
            return buildDynamicList(item, hearings, selectedId);
        }
        return mapper.convertValue(dynamicList, DynamicList.class);
    }

    private DynamicList buildDynamicList(List<Element<HearingBooking>> hearings) {
        return buildDynamicList(hearings, null);
    }

    private DynamicList buildDynamicList(DynamicListElement item, List<Element<HearingBooking>> hearings) {
        return buildDynamicList(item, hearings, null);
    }

    private DynamicList buildDynamicList(List<Element<HearingBooking>> hearings, UUID selected) {
        return asDynamicList(hearings, selected, HearingBooking::toLabel);
    }

    private DynamicList buildDynamicList(DynamicListElement item, List<Element<HearingBooking>> hearings,
                                         UUID selected) {
        return asDynamicList(List.of(item), hearings, selected, HearingBooking::toLabel);
    }

    private boolean associatedToUnreviewedCMO(Element<HearingBooking> hearing,
                                              List<Element<HearingOrder>> unsealedCMOs) {
        return unsealedCMOs.stream()
            .filter(cmo -> cmo.getValue().getStatus() != SEND_TO_JUDGE)
            .anyMatch(cmo -> cmo.getId().equals(hearing.getValue().getCaseManagementOrderId()));
    }

    private String buildHearingsWithCMOsText(List<Element<HearingOrder>> unsealedOrders,
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

    private boolean includeHearing(Element<HearingOrder> cmo, HearingBooking hearing) {
        return SEND_TO_JUDGE == cmo.getValue().getStatus() && cmo.getId().equals(hearing.getCaseManagementOrderId());
    }

    private List<Element<SupportingEvidenceBundle>> addAuditData(
        List<Element<SupportingEvidenceBundle>> cmoSupportingDocs) {
        String uploadedBy = documentUploadHelper.getUploadedDocumentUserDetails();

        List<Element<SupportingEvidenceBundle>> supportingDocs = new ArrayList<>();
        cmoSupportingDocs.forEach(cmoSupportingDoc -> supportingDocs.add(
            element(cmoSupportingDoc.getId(), cmoSupportingDoc.getValue().toBuilder()
                .dateTimeUploaded(time.now())
                .uploadedBy(uploadedBy).build())));

        return supportingDocs;
    }

    private void sortHearings(List<Element<HearingBooking>> hearings) {
        hearings.sort(Comparator.comparing(hearing -> hearing.getValue().getStartDate()));
    }
}
