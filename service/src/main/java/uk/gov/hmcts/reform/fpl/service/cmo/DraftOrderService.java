package uk.gov.hmcts.reform.fpl.service.cmo;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CMOStatus;
import uk.gov.hmcts.reform.fpl.enums.CaseRole;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderKind;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.LanguageTranslationRequirement;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.exceptions.CMONotFoundException;
import uk.gov.hmcts.reform.fpl.exceptions.HearingNotFoundException;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.ConfidentialOrderBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.event.UploadDraftOrdersData;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundles;
import uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.PolicyHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
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
import static uk.gov.hmcts.reform.fpl.service.document.ManageDocumentService.DOCUMENT_ACKNOWLEDGEMENT_KEY;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.asDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.findElement;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.getDynamicListSelectedValue;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.JudgeAndLegalAdvisorHelper.formatJudgeTitleAndName;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DraftOrderService {

    private static final DynamicListElement NO_HEARING = DynamicListElement.defaultListItem("No hearing");
    private final ObjectMapper mapper;
    private final Time time;
    private final HearingOrderKindEventDataBuilder hearingOrderKindEventDataBuilder;
    private final ManageDocumentService manageDocumentService;
    private final UserService userService;

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

        Optional<HearingBooking> hearingMaybe = getSelectedHearing(selectedHearingId, caseData.getHearingDetails());

        if (hearingOrderKinds.contains(HearingOrderKind.CMO)) {
            HearingBooking hearing = hearingMaybe.orElseThrow(() -> new HearingNotFoundException(
                "No hearing found with id: " + selectedHearingId));

            if (hearing.hasCMOAssociation()) {
                HearingOrder cmo = findElement(hearing.getCaseManagementOrderId(), caseData.getDraftUploadedCMOs())
                    .map(Element::getValue)
                    .orElseThrow(() -> new CMONotFoundException("CMO for related hearing could not be found"));
                newEventDataBuilder.previousCMO(cmo.getOrder());
                newEventDataBuilder.uploadCMOMessageAcknowledge(List.of(DOCUMENT_ACKNOWLEDGEMENT_KEY));
            } else {
                newEventDataBuilder.uploadCMOMessageAcknowledge(List.of());
            }

            newEventDataBuilder
                .cmoUploadType(eventData.getCmoUploadType())
                .cmoToSend(getCMO(eventData, hearing, caseData.getDraftUploadedCMOs()))
                .showReplacementCMO(YesNo.from(hearing.hasCMOAssociation()))
                .cmoHearingInfo(hearing.toLabel());
        }

        if (hearingOrderKinds.contains(HearingOrderKind.C21)) {
            hearingOrderKindEventDataBuilder.build(selectedHearingId, caseData, eventData, newEventDataBuilder);
        }

        hearingMaybe
            .ifPresent(h -> newEventDataBuilder.cmoJudgeInfo(formatJudgeTitleAndName(h.getJudgeAndLegalAdvisor())));

        return newEventDataBuilder.build();
    }

    public UUID updateCase(CaseData caseData, List<Element<HearingBooking>> hearings,
                           List<Element<HearingOrder>> cmoDrafts,
                           Map<HearingOrderType, List<Element<HearingOrdersBundle>>> combinedBundles) {
        UploadDraftOrdersData eventData = caseData.getUploadDraftOrdersEventData();
        final UUID selectedHearingId = getSelectedHearingId(eventData);
        final List<HearingOrderKind> hearingOrderKinds = getHearingOrderKinds(eventData);

        if (hearingOrderKinds.contains(CMO)) {

            final Element<HearingBooking> hearing = ofNullable(selectedHearingId)
                .flatMap(id -> findElement(id, hearings))
                .orElseThrow(() -> new HearingNotFoundException(selectedHearingId));

            boolean isNewCMO = isNewCMO(eventData);
            DocumentReference cmo = getCMO(eventData, hearing.getValue(), cmoDrafts);

            Element<HearingOrder> order = element(from(
                cmo,
                hearing.getValue(),
                time.now().toLocalDate(),
                eventData.isCmoAgreed() ? AGREED_CMO : DRAFT_CMO,
                null,
                eventData.getCmoToSendTranslationRequirements(),
                hearing.getId()
            ));
            if (isNewCMO) {
                order = element(order.getValue().toBuilder()
                    .uploaderType(manageDocumentService.getUploaderType(caseData))
                    .uploaderCaseRoles(manageDocumentService.getUploaderCaseRoles(caseData))
                    .build());
            }

            Optional<UUID> previousCmoId = updateHearingWithCmoId(hearing.getValue(), order);

            insertOrder(cmoDrafts, order, previousCmoId.orElse(null));
            HearingOrderType hearingOrderType = eventData.isCmoAgreed() ? AGREED_CMO : DRAFT_CMO;

            addOrdersToBundle(combinedBundles.get(hearingOrderType),
                List.of(order),
                hearing,
                hearingOrderType,
                hearingId ->  {
                    if (AGREED_CMO.equals(hearingOrderType)) {
                        nullSafeList(combinedBundles.get(DRAFT_CMO)).stream()
                            .filter(bundle -> Objects.equals(bundle.getValue().getHearingId(), hearingId))
                            .forEach(hearingOrdersBundleElement ->
                                hearingOrdersBundleElement.getValue().getOrders()
                                    .removeIf(order1 -> Objects.equals(order1.getValue().getType(), DRAFT_CMO)));
                    }
                }
            );
        }

        if (hearingOrderKinds.contains(HearingOrderKind.C21)) {
            List<Element<HearingOrdersBundle>>  bundles = combinedBundles.get(C21);
            final Element<HearingBooking> hearing = ofNullable(selectedHearingId)
                .flatMap(id -> findElement(id, hearings))
                 .orElse(null);

            List<HearingOrder> existingC21Documents = unwrapElements(unwrapElements(bundles).stream()
                .filter(bundle -> Objects.equals(bundle.getHearingId(), selectedHearingId))
                .findFirst()
                .map(HearingOrdersBundle::getOrders)
                .orElse(List.of()));

            for (int i = 0; i < eventData.getCurrentHearingOrderDrafts().size(); i++) {
                Element<HearingOrder> hearingOrder = eventData.getCurrentHearingOrderDrafts().get(i);

                if (!existingC21Documents.contains(hearingOrder.getValue())) {
                    hearingOrder.getValue().setDateSent(time.now().toLocalDate());
                    hearingOrder.getValue().setStatus(SEND_TO_JUDGE);
                    hearingOrder.getValue().setUploaderType(manageDocumentService.getUploaderType(caseData));
                    hearingOrder.getValue().setUploaderCaseRoles(manageDocumentService.getUploaderCaseRoles(caseData));
                }
                hearingOrder.getValue().setTranslationRequirements(eventData.getOrderToSendTranslationRequirements(i));
            }
            addOrdersToBundle(bundles,
                eventData.getCurrentHearingOrderDrafts(),
                hearing,
                C21,
                hearingId -> { }
            );
        }

        nullSafeList(combinedBundles.get(AGREED_CMO)).removeIf(bundle -> isEmpty(bundle.getValue().getOrders()));
        nullSafeList(combinedBundles.get(DRAFT_CMO)).removeIf(bundle -> isEmpty(bundle.getValue().getOrders()));

        return selectedHearingId;
    }

    public void additionalApplicationUpdateCase(List<Element<HearingOrder>> draftOrders,
                                                 List<Element<HearingOrdersBundle>> bundles) {

        for (Element<HearingOrder> hearingOrder : draftOrders) {
            hearingOrder.getValue().setDateSent(time.now().toLocalDate());
            hearingOrder.getValue().setStatus(SEND_TO_JUDGE);
            hearingOrder.getValue().setTranslationRequirements(LanguageTranslationRequirement.NO);
        }

        addOrdersToBundle(bundles,
            draftOrders,
            null,
            C21,
            hearingId -> { }
        );
    }

    public void confidentialAdditionalApplicationUpdateCase(CaseData caseData,
                                                            List<Element<HearingOrder>> draftOrders,
                                                            List<Element<HearingOrdersBundle>> bundles) {
        for (Element<HearingOrder> hearingOrder : draftOrders) {
            hearingOrder.getValue().setDateSent(time.now().toLocalDate());
            hearingOrder.getValue().setStatus(SEND_TO_JUDGE);
            hearingOrder.getValue().setTranslationRequirements(LanguageTranslationRequirement.NO);
            if (isNotEmpty(hearingOrder.getValue().getOrder())) {
                hearingOrder.getValue().setOrderConfidential(hearingOrder.getValue().getOrder());
                hearingOrder.getValue().setOrder(null);
            }
            hearingOrder.getValue().setUploaderEmail(userService.getUserEmail());
        }

        HearingOrdersBundle hearingOrdersBundle = bundles.stream()
            .filter(bundle -> isEmpty(bundle.getValue().getHearingId()))
            .map(Element::getValue)
            .findFirst()
            .orElseGet(() -> addNewDraftBundle(bundles));

        final Set<CaseRole> caseRoles = userService.getCaseRoles(caseData.getId());

        List<String> suffixes = new ArrayList<>();
        if (userService.isHmctsUser()) {
            suffixes.add(ConfidentialOrderBundle.SUFFIX_CTSC);
        } else {
            if (PolicyHelper.isPolicyMatchingCaseRoles(caseData.getLocalAuthorityPolicy(), caseRoles)) {
                suffixes.add(ConfidentialOrderBundle.SUFFIX_LA);
            }
            PolicyHelper.processFieldByPolicyDatas(caseData.getRespondentPolicyData(),
                ConfidentialOrderBundle.SUFFIX_RESPONDENT, caseRoles, suffixes::add);
            PolicyHelper.processFieldByPolicyDatas(caseData.getChildPolicyData(),
                ConfidentialOrderBundle.SUFFIX_CHILD, caseRoles, suffixes::add);
        }
        hearingOrdersBundle.updateConfidentialOrders(draftOrders, C21, suffixes);
    }

    private boolean isInCmoDrafts(Element<HearingOrder> draft, List<Element<HearingOrder>> cmoDrafts) {
        return cmoDrafts.stream()
            .map(Element::getId)
            .collect(toList())
            .contains(draft.getId());
    }

    @SuppressWarnings("unchecked")
    public HearingOrdersBundles migrateCmoDraftToOrdersBundles(CaseData caseData) {

        List<Element<HearingOrder>> cmoDrafts = caseData.getDraftUploadedCMOs();
        List<Element<HearingBooking>> hearings = defaultIfNull(caseData.getHearingDetails(), new ArrayList<>());
        List<Element<HearingOrdersBundle>> bundles = defaultIfNull(caseData.getHearingOrdersBundlesDrafts(),
            new ArrayList<>());
        List<Element<HearingOrdersBundle>> bundlesForReview =
            defaultIfNull(caseData.getHearingOrdersBundlesDraftReview(), new ArrayList<>());

        Stream.concat(bundles.stream(), bundlesForReview.stream())
            .forEach(bundle -> bundle.getValue().getListOfOrders()
                .forEach(o -> o.removeIf(draft -> draft.getValue().getType().isCmo()
                    || isInCmoDrafts(draft, cmoDrafts))));

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

        bundlesForReview.addAll(bundles.stream().filter(this::containsHearingDraftCmo)
                .map(element -> element(cloneHearingOrdersBundle(element.getValue())))
                .collect(toList()));

        filterOrders(bundlesForReview, not(DRAFT_CMO::equals));
        filterOrders(bundles, DRAFT_CMO::equals);

        bundles.removeIf(b -> isEmpty(b.getValue().getOrders()) && isEmpty(b.getValue().getAllConfidentialOrders()));
        bundlesForReview.removeIf(b -> isEmpty(b.getValue().getOrders()));

        return HearingOrdersBundles.builder()
                .draftCmos(bundlesForReview)
                .agreedCmos(bundles)
                .build();
    }

    private HearingOrdersBundle cloneHearingOrdersBundle(HearingOrdersBundle hearingOrdersBundle) {
        List<Element<HearingOrder>> orders = hearingOrdersBundle.getOrders().stream()
                .map(hearingOrderElement -> element(
                    hearingOrderElement.getId(),
                    hearingOrderElement.getValue().toBuilder().build())
                )
                .collect(toList());
        HearingOrdersBundle clonedHearingOrdersBundle = hearingOrdersBundle.toBuilder()
                .orders(new ArrayList<>())
                .build();

        clonedHearingOrdersBundle.getOrders().addAll(orders);

        return clonedHearingOrdersBundle;
    }

    private void filterOrders(List<Element<HearingOrdersBundle>> bundlesForReview, Predicate<HearingOrderType> filter) {
        bundlesForReview.forEach(
            hearingOrdersBundleElement -> hearingOrdersBundleElement.getValue().getOrders()
                .removeIf(hearingOrderElement -> filter.test(hearingOrderElement.getValue().getType()))
        );
    }

    private boolean containsHearingDraftCmo(Element<HearingOrdersBundle> bundle) {
        return bundle.getValue().getOrders().stream()
                .map(Element::getValue)
                .anyMatch(hearingOrder -> hearingOrder.getType().equals(DRAFT_CMO));
    }

    private void addOrdersToBundle(List<Element<HearingOrdersBundle>> bundles,
                                   List<Element<HearingOrder>> orders,
                                   Element<HearingBooking> hearing,
                                   HearingOrderType type,
                                   Consumer<UUID> update) {
        UUID hearingId = Optional.ofNullable(hearing).map(Element::getId).orElse(null);
        HearingBooking hearingBooking = Optional.ofNullable(hearing).map(Element::getValue).orElse(null);

        HearingOrdersBundle hearingOrdersBundle = nullSafeList(bundles).stream()
            .filter(bundle -> Objects.equals(bundle.getValue().getHearingId(), hearingId))
            .map(Element::getValue)
            .findFirst()
            .orElseGet(() -> addNewDraftBundle(bundles));

        hearingOrdersBundle
            .updateHearing(hearingId, hearingBooking)
            .updateOrders(orders, type);

        update.accept(hearingId);
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

    private Optional<HearingBooking> getSelectedHearing(UUID id, List<Element<HearingBooking>> hearings) {
        return findElement(id, hearings).map(Element::getValue);
    }

    private UUID getSelectedHearingId(UploadDraftOrdersData eventData) {
        final Object hearingDynamicList = eventData.getHearingDynamicList();
        final UUID selectedHearingId = getDynamicListSelectedValue(hearingDynamicList, mapper);

        return DEFAULT_CODE.equals(selectedHearingId) ? null : selectedHearingId;
    }

    private List<HearingOrderKind> getHearingOrderKinds(UploadDraftOrdersData eventData) {
        return ofNullable(eventData)
            .map(UploadDraftOrdersData::getHearingOrderDraftKind)
            .orElse(emptyList());
    }

    private boolean isNewCMO(UploadDraftOrdersData currentEventData) {
        return !(currentEventData.getUploadedCaseManagementOrder() == null
            && currentEventData.getReplacementCMO() == null);
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

    private void sortHearings(List<Element<HearingBooking>> hearings) {
        hearings.sort(Comparator.comparing(hearing -> hearing.getValue().getStartDate()));
    }
}
