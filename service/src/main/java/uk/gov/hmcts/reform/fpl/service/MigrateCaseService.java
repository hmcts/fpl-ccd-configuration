package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.IncorrectCourtCodeConfig;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseService {

    private static final String PLACEMENT = "placements";
    private static final String PLACEMENT_NON_CONFIDENTIAL = "placementsNonConfidential";
    private static final String PLACEMENT_NON_CONFIDENTIAL_NOTICES = "placementsNonConfidentialNotices";
    private final CaseNoteService caseNoteService;
    private final CourtService courtService;
    private final DocumentListService documentListService;
    private static final String CORRECT_COURT_NAME = "Family Court Sitting at West London";
    private static final String ORDER_TYPE = "orderType";
    public final MigrateRelatingLAService migrateRelatingLAService;
    public final OrganisationService organisationService;

    public Map<String, Object> removeHearingOrderBundleDraft(CaseData caseData, String migrationId, UUID bundleId,
                                                             UUID orderId) {

        Optional<Element<HearingOrdersBundle>> bundle = ElementUtils.findElement(bundleId,
            caseData.getHearingOrdersBundlesDrafts());

        if (bundle.isEmpty()) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected bundle id %s",
                migrationId, caseData.getId(), bundleId.toString()
            ));
        }

        List<Element<HearingOrdersBundle>> bundles = caseData.getHearingOrdersBundlesDrafts().stream()
            .map(el -> {
                if (el.getId().equals(bundleId)) {
                    List<Element<HearingOrder>> orders = el.getValue().getOrders().stream()
                        .filter(orderEl -> !orderEl.getId().equals(orderId))
                        .collect(toList());
                    el.getValue().setOrders(orders);
                }
                return el;
            })
            .filter(el -> !el.getValue().getOrders().isEmpty())
            .toList();

        return Map.of("hearingOrdersBundlesDrafts", bundles);
    }

    public void doCaseIdCheck(long caseId, long expectedCaseId, String migrationId) throws AssertionError {
        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }
    }

    public void doCaseIdCheckList(long caseId, List<Long> possibleIds, String migrationId) throws AssertionError {
        if (!possibleIds.contains(caseId)) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, case id not one of the expected options",
                migrationId, caseId
            ));
        }
    }

    public Map<String, Object> removeDocumentsSentToParties(CaseData caseData,
                                                            String migrationId,
                                                            UUID expectedPartyUuid,
                                                            List<UUID> docUuidsToBeRemoved) {
        Long caseId = caseData.getId();
        final Element<SentDocuments> targetDocumentsSentToParties = ElementUtils.findElement(expectedPartyUuid,
                caseData.getDocumentsSentToParties())
            .orElseThrow(() -> new AssertionError(format(
                "Migration {id = %s, case reference = %s}, party Id not found",
                migrationId, caseId)));

        docUuidsToBeRemoved.stream().forEach(docIdToBeRemoved -> {
                if (ElementUtils.findElement(docIdToBeRemoved,
                    targetDocumentsSentToParties.getValue().getDocumentsSentToParty()).isEmpty()) {
                    throw new AssertionError(format(
                        "Migration {id = %s, case reference = %s}, document Id not found",
                        migrationId, caseId));
                }
            }
        );

        final List<Element<SentDocuments>> resultDocumentsSentToParties = caseData.getDocumentsSentToParties().stream()
            .map(documentsSentToParty -> {
                if (!expectedPartyUuid.equals(documentsSentToParty.getId())) {
                    return documentsSentToParty;
                } else {
                    return element(documentsSentToParty.getId(),
                        documentsSentToParty.getValue().toBuilder()
                            .documentsSentToParty(documentsSentToParty.getValue().getDocumentsSentToParty().stream()
                                .filter(documentSent -> !docUuidsToBeRemoved.contains(documentSent.getId()))
                                .toList()).build());
                }
            }).toList();

        return Map.of("documentsSentToParties", resultDocumentsSentToParties);
    }

    public Map<String, Object> removePositionStatementChild(CaseData caseData, String migrationId, boolean isInLaList,
                                                            UUID... expectedIds) {
        List<Element<PositionStatementChild>> targetList = isInLaList
            ? caseData.getHearingDocuments().getPosStmtChildListLA()
            : caseData.getHearingDocuments().getPosStmtChildList();
        Long caseId = caseData.getId();
        List<Element<PositionStatementChild>> newList = targetList.stream()
                .filter(el -> !Arrays.asList(expectedIds).contains(el.getId()))
                .toList();

        if (newList.size() != targetList.size() - expectedIds.length) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid position statement child",
                migrationId, caseId));
        }
        return Map.of("posStmtChildList" + (isInLaList ? "LA" : ""), newList);
    }

    public Map<String, Object> removePositionStatementRespondent(CaseData caseData, String migrationId,
                                                                 boolean isInLaList, UUID... expectedIds) {
        List<Element<PositionStatementRespondent>> targetList = isInLaList
            ? caseData.getHearingDocuments().getPosStmtRespListLA()
            : caseData.getHearingDocuments().getPosStmtRespList();
        Long caseId = caseData.getId();
        List<Element<PositionStatementRespondent>> newList = targetList.stream()
            .filter(el -> !Arrays.asList(expectedIds).contains(el.getId()))
            .toList();

        if (newList.size() != targetList.size() - expectedIds.length) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid position statement respondent",
                migrationId, caseId));
        }
        return Map.of("posStmtRespList" + (isInLaList ? "LA" : ""), newList);
    }

    public Map<String, Object> updateIncorrectCourtCodes(CaseData caseData) {
        IncorrectCourtCodeConfig bhc = IncorrectCourtCodeConfig.builder()
            .incorrectCourtCode("544")
            .correctCourtCode("554")
            .correctCourtName("Family Court sitting at Brighton")
            .organisationId("0F6AZIR")
            .build();
        IncorrectCourtCodeConfig wsx = IncorrectCourtCodeConfig.builder()
            .incorrectCourtCode("544")
            .correctCourtCode("554")
            .correctCourtName("Family Court Sitting at Brighton County Court")
            .organisationId("HLT7S0M")
            .build();
        IncorrectCourtCodeConfig bnt = IncorrectCourtCodeConfig.builder()
            .incorrectCourtCode("117")
            .correctCourtCode("332")
            .correctCourtName(CORRECT_COURT_NAME)
            .organisationId("SPUL3VV")
            .build();
        IncorrectCourtCodeConfig hrw = IncorrectCourtCodeConfig.builder()
            .incorrectCourtCode("117")
            .correctCourtCode("332")
            .correctCourtName(CORRECT_COURT_NAME)
            .organisationId("L3HSA4L")
            .build();
        IncorrectCourtCodeConfig hlw = IncorrectCourtCodeConfig.builder()
            .incorrectCourtCode("117")
            .correctCourtCode("332")
            .correctCourtName(CORRECT_COURT_NAME)
            .organisationId("6I4Z3OO")
            .build();
        IncorrectCourtCodeConfig rct = IncorrectCourtCodeConfig.builder()
            .incorrectCourtCode("164")
            .correctCourtCode("159")
            .correctCourtName("Family Court sitting at Cardiff")
            .organisationId("68MNZN8")
            .build();
        IncorrectCourtCodeConfig bad = IncorrectCourtCodeConfig.builder()
            .incorrectCourtCode("3403")
            .correctCourtCode("121")
            .correctCourtName("Family Court Sitting at East London Family Court")
            .organisationId("3FG3URQ")
            .build();
        List<IncorrectCourtCodeConfig> configs = List.of(bhc, wsx, bnt, hrw, hlw, rct, bad);

        if (nonNull(caseData.getCourt()) && nonNull(caseData.getLocalAuthorityPolicy())
            && nonNull(caseData.getLocalAuthorityPolicy().getOrganisation())) {
            IncorrectCourtCodeConfig config = configs.stream()
                .filter(c ->
                    c.getIncorrectCourtCode().equals(caseData.getCourt().getCode())
                        && c.getOrganisationId().equals(caseData.getLocalAuthorityPolicy()
                        .getOrganisation().getOrganisationID()))
                .findAny().orElseThrow(() -> new AssertionError(format("It does not match any migration conditions. "
                        + "(courtCode = %s, localAuthorityPolicy.organisation.organisationID = %s)",
                    caseData.getCourt().getCode(),
                    caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID())));
            return Map.of("court", caseData.getCourt().toBuilder()
                .code(config.getCorrectCourtCode())
                .name(config.getCorrectCourtName())
                .build());

        }
        throw new AssertionError("The case does not have court or local authority policy's organisation.");
    }

    public Map<String, Object> removeHearingBooking(CaseData caseData, final String migrationId,
                                                     final UUID hearingIdToBeRemoved) {
        final Long caseId = caseData.getId();

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();
        if (hearingDetails != null) {
            // get the hearing with the expected UUID
            List<Element<HearingBooking>> hearingBookingsToBeRemoved =
                hearingDetails.stream().filter(hearingBooking -> hearingIdToBeRemoved.equals(hearingBooking.getId()))
                    .toList();

            if (hearingBookingsToBeRemoved.isEmpty()) {
                throw new AssertionError(format(
                    "Migration {id = %s, case reference = %s}, hearing booking %s not found",
                    migrationId, caseId, hearingIdToBeRemoved
                ));
            }

            if (hearingBookingsToBeRemoved.size() > 1) {
                throw new AssertionError(format(
                    "Migration {id = %s, case reference = %s}, more than one hearing booking %s found",
                    migrationId, caseId, hearingIdToBeRemoved
                ));
            }

            // remove the hearing from the hearing list
            hearingDetails.removeAll(hearingBookingsToBeRemoved);
            if (!hearingDetails.isEmpty()) {
                return Map.of(
                    "hearingDetails", hearingDetails,
                    "selectedHearingId", hearingDetails.get(hearingDetails.size() - 1).getId()
                );
            } else {
                Map<String, Object> ret = new HashMap<>(Map.of(
                        "hearingDetails", hearingDetails
                ));
                ret.put("selectedHearingId", null);
                return ret;
            }
        } else {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, hearing details not found",
                migrationId, caseId
            ));
        }
    }

    public Map<String, Object> removeCaseNote(CaseData caseData, String migrationId, UUID caseNoteIdToRemove) {
        if (ElementUtils.findElement(caseNoteIdToRemove, caseData.getCaseNotes()).isEmpty()) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, case note %s found",
                migrationId, caseData.getId(), caseNoteIdToRemove
            ));
        }

        return Map.of("caseNotes", caseNoteService.removeCaseNote(caseNoteIdToRemove, caseData.getCaseNotes()));
    }

    public void verifyGatekeepingOrderUrgentHearingOrderExist(CaseData caseData, String migrationId) {
        if (caseData.getUrgentHearingOrder() == null || caseData.getUrgentHearingOrder().getOrder() == null) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, GateKeeping order - Urgent hearing order not found",
                migrationId, caseData.getId()));
        }
    }

    public void verifyGatekeepingOrderUrgentHearingOrderExistWithGivenFileName(CaseData caseData, String migrationId,
                                                                               String fileName) {
        verifyGatekeepingOrderUrgentHearingOrderExist(caseData, migrationId);

        if (!fileName.equals(caseData.getUrgentHearingOrder().getOrder().getFilename())) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, GateKeeping order - Urgent hearing order %s not found",
                migrationId, caseData.getId(), fileName));
        }
    }

    public void verifyUrgentDirectionsOrderExists(CaseData caseData, String migrationId, UUID documentId) {
        if (caseData.getUrgentDirectionsOrder() == null || isEmpty(caseData.getUrgentDirectionsOrder())) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, GateKeeping order - Urgent directions order not found",
                migrationId, caseData.getId()));
        }

        String caseDocumentUrl = caseData.getUrgentDirectionsOrder().getDocument().getUrl();

        if (!documentId
            .equals(UUID.fromString(caseDocumentUrl.substring(caseDocumentUrl.length() - 36)))) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s},"
                + " GateKeeping order - Urgent directions order document with Id %s not found",
                migrationId, caseData.getId(), documentId));
        }
    }

    public void verifyStandardDirectionOrderExists(CaseData caseData, String migrationId, UUID documentId) {
        if (isEmpty(caseData.getStandardDirectionOrder())) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, GateKeeping order - Standard direction order not found",
                migrationId, caseData.getId()));
        }

        String caseDocumentUrl = caseData.getStandardDirectionOrder().getDocument().getUrl();

        if (!documentId
            .equals(UUID.fromString(caseDocumentUrl.substring(caseDocumentUrl.length() - 36)))) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s},"
                + " GateKeeping order - Standard direction order document with Id %s not found",
                migrationId, caseData.getId(), documentId));
        }
    }

    public Map<String, Object> removeApplicationDocument(CaseData caseData,
                                                                 String migrationId,
                                                                 UUID expectedApplicationDocumentId) {
        Long caseId = caseData.getId();
        List<Element<ApplicationDocument>> applicationDocuments =
            caseData.getApplicationDocuments().stream()
                .filter(el -> !el.getId().equals(expectedApplicationDocumentId))
                .toList();

        if (applicationDocuments.size() != caseData.getApplicationDocuments().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, application document",
                migrationId, caseId));
        }
        return Map.of("applicationDocuments", applicationDocuments);
    }

    public Map<String, Object> removeCaseSummaryByHearingId(CaseData caseData,
                                                            String migrationId,
                                                            UUID expectedHearingId) {
        Long caseId = caseData.getId();
        List<Element<CaseSummary>> caseSummaries =
            caseData.getHearingDocuments().getCaseSummaryList()
                .stream()
                .filter(el -> !el.getId().equals(expectedHearingId))
                .toList();

        if (caseSummaries.size() != caseData.getHearingDocuments().getCaseSummaryList().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, case summary",
                migrationId, caseId));
        }
        return Map.of("caseSummaryList", caseSummaries);
    }

    public Map<String, Object> revertChildExtensionDate(CaseData caseData, String migrationId, String childId,
                                                        LocalDate completionDate,
                                                        CaseExtensionReasonList extensionReason) {
        List<Element<Child>> childrenInCase = caseData.getAllChildren();
        UUID childUUID = UUID.fromString(childId);

        if (isNotEmpty(childrenInCase)) {
            if (ElementUtils.findElement(childUUID, childrenInCase).isEmpty()) {
                throw new AssertionError(format(
                    "Migration {id = %s}, case reference = %s} child %s not found",
                    migrationId, caseData.getId(), childId));
            }

            List<Element<Child>> children = childrenInCase.stream()
                .map(element -> {
                    if (element.getId().equals(childUUID)) {
                        return element(element.getId(),
                            element.getValue().toBuilder()
                                .party(element.getValue().getParty().toBuilder()
                                    .completionDate(completionDate)
                                    .extensionReason(extensionReason)
                                    .build())
                                .build());
                    } else {
                        return element;
                    }
                }).toList();

            return Map.of("children1", children);
        } else {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s} doesn't have children",
                migrationId, caseData.getId()));
        }
    }

    public void doHearingOptionCheck(long caseId, String hearingOption, String expectedHearingOption,
                                     String migrationId) throws AssertionError {
        if (!hearingOption.equals(expectedHearingOption)) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, unexpected hearing option %s",
                migrationId, caseId, hearingOption
            ));
        }
    }

    public Map<String, Object> refreshDocumentViews(CaseData caseData) {
        return documentListService.getDocumentView(caseData);
    }

    public void doDocumentViewNCCheck(long caseId, String migrationId, CaseDetails caseDetails) throws AssertionError {
        String documentViewNC = (String) caseDetails.getData().get("documentViewNC");
        if (!Optional.ofNullable(documentViewNC).orElse("").contains("title='Confidential'")) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected documentViewNC contains confidential doc.",
                migrationId, caseId
            ));
        }
    }

    public Map<String, Object> removeSpecificPlacements(CaseData caseData, UUID placementToRemove) {
        List<Element<Placement>> placementsToKeep = caseData.getPlacementEventData().getPlacements().stream()
            .filter(x -> !x.getId().equals(placementToRemove)).toList();
        caseData.getPlacementEventData().setPlacements(placementsToKeep);

        List<Element<Placement>> nonConfidentialPlacementsToKeep = caseData.getPlacementEventData()
            .getPlacementsNonConfidential(false);

        List<Element<Placement>> nonConfidentialNoticesPlacementsToKeep = caseData.getPlacementEventData()
            .getPlacementsNonConfidential(true);

        Map<String, Object> ret =  new HashMap<String, Object>();
        ret.put(PLACEMENT, placementsToKeep.isEmpty() ? null : placementsToKeep);
        ret.put(PLACEMENT_NON_CONFIDENTIAL, nonConfidentialPlacementsToKeep.isEmpty() ? null :
            nonConfidentialPlacementsToKeep);
        ret.put(PLACEMENT_NON_CONFIDENTIAL_NOTICES, nonConfidentialNoticesPlacementsToKeep.isEmpty() ? null :
            nonConfidentialNoticesPlacementsToKeep);
        return ret;
    }

    public Map<String, Object> removeDraftUploadedCMOs(CaseData caseData,
                                                       String migrationId,
                                                       UUID expectedOrderId) {
        Long caseId = caseData.getId();
        List<Element<HearingOrder>> draftUploadedCMOs = caseData.getDraftUploadedCMOs()
            .stream().filter(el -> !el.getId().equals(expectedOrderId)).toList();

        if (draftUploadedCMOs.size() != caseData.getDraftUploadedCMOs().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid draft uploaded CMO",
                migrationId, caseId));
        }
        return Map.of("draftUploadedCMOs", draftUploadedCMOs);
    }

    public Map<String, Object> removeHearingOrdersBundlesDrafts(CaseData caseData,
                                                                String migrationId,
                                                                UUID expectedHearingOrderBundleId) {
        Long caseId = caseData.getId();
        List<Element<HearingOrdersBundle>> hearingOrdersBundlesDrafts = caseData.getHearingOrdersBundlesDrafts()
            .stream().filter(el -> !el.getId().equals(expectedHearingOrderBundleId)).toList();

        if (hearingOrdersBundlesDrafts.size() != caseData.getHearingOrdersBundlesDrafts().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid hearing order bundle draft",
                migrationId, caseId));
        }
        return Map.of("hearingOrdersBundlesDrafts", hearingOrdersBundlesDrafts);
    }

    public Map<String, Object> renameApplicationDocuments(CaseData caseData) {
        List<Element<ApplicationDocument>> updatedList = caseData.getApplicationDocuments().stream()
            .map(el -> {
                String currentName = el.getValue().getDocumentName();
                el.getValue().setDocumentName(stripIllegalCharacters(currentName));
                return el;
            }).toList();

        return Map.of("applicationDocuments", updatedList);
    }

    public Map<String, Object> removeJudicialMessage(CaseData caseData, String migrationId, String messageId) {
        return Map.of("judicialMessages",
                removeJudicialMessageFormList(caseData.getJudicialMessages(), messageId, migrationId,
                    caseData.getId()));
    }

    public Map<String, Object> removeClosedJudicialMessage(CaseData caseData, String migrationId, String messageId) {
        UUID targetMessageId = UUID.fromString(messageId);
        return Map.of("closedJudicialMessages",
            removeJudicialMessageFormList(caseData.getClosedJudicialMessages(), messageId, migrationId,
                caseData.getId()));
    }

    private List<Element<JudicialMessage>> removeJudicialMessageFormList(List<Element<JudicialMessage>> messages,
                                                              String messageId, String migrationId, Long caseId) {
        if (messages == null) {
            throw new AssertionError(format("Migration {id = %s, case reference = %s}, judicial message is null",
                migrationId, caseId));
        }

        UUID targetMessageId = UUID.fromString(messageId);
        List<Element<JudicialMessage>> resultList = messages.stream()
            .filter(message -> !message.getId().equals(targetMessageId))
            .toList();

        if (resultList.size() != messages.size() - 1) {
            throw new AssertionError(format("Migration {id = %s, case reference = %s}, judicial message %s not found",
                migrationId, caseId, messageId));
        }

        return resultList;
    }

    public Map<String, Object> removeSkeletonArgument(CaseData caseData, String skeletonArgumentId,
                                                      String migrationId) {
        Long caseId = caseData.getId();
        List<Element<SkeletonArgument>> skeletonArguments = caseData.getHearingDocuments().getSkeletonArgumentList();

        if (skeletonArguments.isEmpty()) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, skeletonArgumentList is empty",
                migrationId, caseId
            ));
        }

        List<Element<SkeletonArgument>> updatedSkeletonArguments =
            ElementUtils.removeElementWithUUID(skeletonArguments, UUID.fromString(skeletonArgumentId));

        if (updatedSkeletonArguments.size() != skeletonArguments.size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, skeleton argument %s not found",
                migrationId, caseId, skeletonArgumentId
            ));
        }

        return Map.of("skeletonArgumentList", updatedSkeletonArguments);
    }

    public Map<String, Object> removeNoticeOfProceedingsBundle(CaseData caseData, String noticeOfProceedingsBundleId,
                                                      String migrationId) {
        Long caseId = caseData.getId();
        List<Element<DocumentBundle>> noticeOfProceedingsBundle = caseData.getNoticeOfProceedingsBundle();

        if (noticeOfProceedingsBundle.isEmpty()) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, noticeOfProceedingsBundle is empty",
                migrationId, caseId
            ));
        }

        List<Element<DocumentBundle>> updatedNoticeOfProceedingsBundle =
            ElementUtils.removeElementWithUUID(noticeOfProceedingsBundle, UUID.fromString(noticeOfProceedingsBundleId));

        if (updatedNoticeOfProceedingsBundle.size() != noticeOfProceedingsBundle.size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, notice of proceedings bundle %s not found",
                migrationId, caseId, noticeOfProceedingsBundleId
            ));
        }

        return Map.of("noticeOfProceedingsBundle", updatedNoticeOfProceedingsBundle);
    }

    public Map<String, Object> addCourt(String courtId) {
        Optional<Court> court = courtService.getCourt(courtId);

        if (court.isPresent()) {
            return Map.of("court", court.get());
        } else {
            throw new IllegalArgumentException(format("Court not found with ID %s", courtId));
        }
    }

    private String stripIllegalCharacters(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replace("<", "")
            .replace(">", "");
    }

    public Map<String, Object> removeHearingFurtherEvidenceDocuments(CaseData caseData,
                                                                     String migrationId,
                                                                     UUID expectedHearingId,
                                                                     UUID expectedDocId) {
        Long caseId = caseData.getId();

        Element<HearingFurtherEvidenceBundle> elementToBeUpdated = caseData.getHearingFurtherEvidenceDocuments()
            .stream()
            .filter(hfed -> expectedHearingId.equals(hfed.getId()))
            .findFirst().orElseThrow(() -> new AssertionError(format(
                "Migration {id = %s, case reference = %s}, hearing not found",
                migrationId, caseId)));
        List<Element<SupportingEvidenceBundle>> newSupportingEvidenceBundle =
            elementToBeUpdated.getValue().getSupportingEvidenceBundle().stream()
                .filter(el -> !expectedDocId.equals(el.getId()))
                .toList();
        if (newSupportingEvidenceBundle.size() != elementToBeUpdated.getValue().getSupportingEvidenceBundle()
            .size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, hearing further evidence documents not found",
                migrationId, caseId));
        }
        elementToBeUpdated.getValue().setSupportingEvidenceBundle(newSupportingEvidenceBundle);

        List<Element<HearingFurtherEvidenceBundle>> listOfHearingFurtherEvidenceBundle =
            caseData.getHearingFurtherEvidenceDocuments().stream()
                .filter(el -> !expectedHearingId.equals(el.getId()))
                .collect(toList());
        if (!newSupportingEvidenceBundle.isEmpty()) {
            listOfHearingFurtherEvidenceBundle.add(elementToBeUpdated);
        }
        if (listOfHearingFurtherEvidenceBundle.isEmpty()) {
            Map<String, Object> ret = new HashMap<>();
            ret.put("hearingFurtherEvidenceDocuments", null);
            return ret;
        } else {
            return Map.of("hearingFurtherEvidenceDocuments", listOfHearingFurtherEvidenceBundle);
        }
    }

    public Map<String, Object> removeFurtherEvidenceSolicitorDocuments(CaseData caseData,
                                                                       String migrationId,
                                                                       UUID expectedDocId) {
        Long caseId = caseData.getId();
        List<Element<SupportingEvidenceBundle>> furtherEvidenceDocumentsSolicitor =
            caseData.getFurtherEvidenceDocumentsSolicitor().stream()
                .filter(el -> !expectedDocId.equals(el.getId()))
                .toList();

        if (furtherEvidenceDocumentsSolicitor.size() != caseData.getFurtherEvidenceDocumentsSolicitor().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, further evidence documents solicitor not found",
                migrationId, caseId));
        }

        if (furtherEvidenceDocumentsSolicitor.isEmpty()) {
            Map<String, Object> ret = new HashMap<>();
            ret.put("furtherEvidenceDocumentsSolicitor", null);
            return ret;
        } else {
            return Map.of("furtherEvidenceDocumentsSolicitor", furtherEvidenceDocumentsSolicitor);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fixOrderTypeTypo(String migrationId, CaseDetails caseDetails) {
        String invalidOrderType = "EDUCATION_SUPERVISION__ORDER";
        String validOrderType = "EDUCATION_SUPERVISION_ORDER";

        Optional<Map> orders = Optional.ofNullable((Map) caseDetails.getData().get("orders"));
        if (orders.isPresent()) {
            Optional<List<String>> orderType = Optional.ofNullable((List<String>) orders.get().get(ORDER_TYPE));
            if (orderType.isPresent() && orderType.get().contains(invalidOrderType)) {
                Map ordersMap = new HashMap<>(orders.get());
                List<String> newOrderType = new ArrayList<>(((List<String>) ordersMap.get(ORDER_TYPE)));
                newOrderType.replaceAll(target -> target.equals(invalidOrderType) ? validOrderType : target);
                ordersMap.put(ORDER_TYPE, newOrderType);
                return Map.of("orders", ordersMap);
            } else {
                throw new AssertionError(format("Migration {id = %s}, case does not have [orders.orderType] "
                        + "or missing target invalid order type [%s]",
                    migrationId, invalidOrderType));
            }
        } else {
            throw new AssertionError(format("Migration {id = %s}, case does not have [orders]",
                migrationId));
        }
    }

    public Map<String, Object> removeCourtBundleByBundleId(CaseData caseData,
                                                           String migrationId,
                                                           UUID expectedHearingId,
                                                           UUID expectedBundleId) {
        Long caseId = caseData.getId();

        Element<HearingCourtBundle> elementToBeUpdated = caseData.getHearingDocuments().getCourtBundleListV2()
            .stream()
            .filter(hfed -> expectedHearingId.equals(hfed.getId()))
            .findFirst().orElseThrow(() -> new AssertionError(format(
                "Migration {id = %s, case reference = %s}, hearing not found",
                migrationId, caseId)));
        List<Element<CourtBundle>> newCourtBundleList =
            elementToBeUpdated.getValue().getCourtBundle().stream()
                .filter(el -> !expectedBundleId.equals(el.getId()))
                .toList();
        if (newCourtBundleList.size() != elementToBeUpdated.getValue().getCourtBundle()
            .size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, hearing court bundle not found",
                migrationId, caseId));
        }
        elementToBeUpdated.getValue().setCourtBundle(newCourtBundleList);

        List<Element<HearingCourtBundle>> listOfHearingCourtBundles =
            caseData.getHearingDocuments().getCourtBundleListV2().stream()
                .filter(el -> !expectedHearingId.equals(el.getId()))
                .collect(toList());
        if (!newCourtBundleList.isEmpty()) {
            listOfHearingCourtBundles.add(elementToBeUpdated);
        }
        if (listOfHearingCourtBundles.isEmpty() || (listOfHearingCourtBundles.size() == 1
            && listOfHearingCourtBundles.get(0).getValue().getCourtBundle().isEmpty())) {
            Map<String, Object> ret = new HashMap<>();
            ret.put("courtBundleListV2", null);
            return ret;
        } else {
            return Map.of("courtBundleListV2", listOfHearingCourtBundles);
        }
    }

    public Map<String, Object> removeCorrespondenceDocument(CaseData caseData,
                                                            String migrationId,
                                                            UUID expectedDocumentId) {

        List<Element<SupportingEvidenceBundle>> newCorrespondenceDocuments =
            caseData.getCorrespondenceDocuments().stream()
                .filter(el -> !expectedDocumentId.equals(el.getId()))
                .collect(toList());

        List<Element<SupportingEvidenceBundle>> newCorrespondenceDocumentsNC =
            newCorrespondenceDocuments.stream()
                .filter(el -> !el.getValue().isConfidentialDocument())
                .toList();

        if (newCorrespondenceDocuments.size() != caseData.getCorrespondenceDocuments().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, correspondence document not found",
                migrationId, caseData.getId()));
        }

        return Map.of("correspondenceDocuments", newCorrespondenceDocuments,
                    "correspondenceDocumentsNC", newCorrespondenceDocumentsNC);
    }

    public Map<String, Object> addRelatingLA(String migrationId, Long caseId) {
        // lookup in map
        Optional<String> relatingLA = migrateRelatingLAService.getRelatingLAString(caseId.toString());

        if (relatingLA.isEmpty()) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, case not found in migration list",
                migrationId, caseId));
        }

        return Map.of("relatingLA", relatingLA.get());
    }

    public Map<String, Object> removeSealedCMO(CaseData caseData,
                                               String migrationId,
                                               UUID expectedCMOId,
                                               boolean removeOrderToBeSent) {

        List<Element<HearingOrder>> updatedSealedCMOs = ElementUtils.removeElementWithUUID(
            caseData.getSealedCMOs(), expectedCMOId);

        if (updatedSealedCMOs.size() != caseData.getSealedCMOs().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Sealed CMO not found, %s",
                migrationId, caseData.getId(), expectedCMOId));
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sealedCMOs", updatedSealedCMOs);

        if (removeOrderToBeSent) {
            List<Element<HearingOrder>> updatedOrdersToBeSent = ElementUtils.removeElementWithUUID(
                caseData.getOrdersToBeSent(), expectedCMOId);
            if (updatedOrdersToBeSent.size() != nullSafeList(caseData.getOrdersToBeSent()).size() - 1) {
                throw new AssertionError(format(
                    "Migration {id = %s, case reference = %s}, Order to be sent not found, %s",
                    migrationId, caseData.getId(), expectedCMOId));
            }

            resultMap.put("ordersToBeSent", updatedOrdersToBeSent);
        }

        return resultMap;
    }

    public void clearChangeOrganisationRequest(CaseDetails caseDetails) {
        caseDetails.getData().remove("changeOrganisationRequestField");
    }

    public Map<String, List<Element<LocalAuthority>>> removeElementFromLocalAuthorities(CaseData caseData,
                                                                 String migrationId,
                                                                 UUID expectedLocalAuthorityId) {
        Long caseId = caseData.getId();
        List<Element<LocalAuthority>> localAuthoritiesList =
            caseData.getLocalAuthorities().stream()
                .filter(el -> !el.getId().equals(expectedLocalAuthorityId))
                .toList();

        if (localAuthoritiesList.size() != caseData.getLocalAuthorities()
            .size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid local authorities",
                migrationId, caseId));
        }
        return Map.of("localAuthorities", localAuthoritiesList);
    }

    private final CaseConverter caseConverter;

    protected CaseData getCaseData(CaseDetails caseDetails) {
        return caseConverter.convert(caseDetails);
    }

    public Map<String, Object> fixIncorrectCaseManagementLocation(CaseDetails caseDetails, String migrationId) {
        CaseData caseData = getCaseData(caseDetails);

        Court court = caseData.getCourt();
        final String targetCourt = "270"; // Middlesborough
        boolean isTargetCourtCode = isNotEmpty(court) && targetCourt.equals(court.getCode());
        final String correctBaseLocation = "195537";
        final String correctRegion = "3";

        @SuppressWarnings("unchecked")
        Map<String, Object> caseManagementLocation = (Map<String, Object>) caseDetails.getData()
            .get("caseManagementLocation");
        if (!isTargetCourtCode) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Case data does not contain the target court: %s",
                migrationId, caseData.getId(), targetCourt));
        }
        if (isNotEmpty(caseManagementLocation)
            && correctBaseLocation.equals(caseManagementLocation.get("baseLocation"))
            && correctRegion.equals(caseManagementLocation.get("region"))) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, `caseManagementLocation` is correct.",
                migrationId, caseData.getId()));
        }

        return Map.of("court", court.toBuilder().epimmsId(correctBaseLocation).build(),
            "caseManagementLocation", CaseLocation.builder()
            .baseLocation(correctBaseLocation)
            .region(correctRegion)
            .build());
    }

    public Map<String, Object> removeCharactersFromThresholdDetails(CaseData caseData,
                                                                    String migrationId,
                                                                    int startIndex,
                                                                    int endIndex) {
        Long caseId = caseData.getId();
        String thresholdDetails = caseData.getGrounds().getThresholdDetails();
        String textToRemove;

        try {
            textToRemove = caseData.getGrounds().getThresholdDetails().substring(startIndex, endIndex);
        } catch (StringIndexOutOfBoundsException ex) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, threshold details is shorter than provided index",
                migrationId, caseId));
        }

        if (textToRemove.strip().isEmpty()) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, threshold details does not contain provided text",
                migrationId, caseId));
        }

        thresholdDetails = thresholdDetails.replace(textToRemove, "");
        Grounds updatedGrounds = caseData.getGrounds().toBuilder().thresholdDetails(thresholdDetails).build();

        return Map.of("grounds", updatedGrounds);
    }

    public Map<String, OrganisationPolicy> changeThirdPartyStandaloneApplicant(CaseData caseData, String orgId) {
        String orgName = organisationService.findOrganisation(orgId)
            .map(uk.gov.hmcts.reform.rd.model.Organisation::getName)
            .orElseThrow();

        Organisation newOrganisation = Organisation.builder()
            .organisationID(orgId)
            .organisationName(orgName)
            .build();

        var applicantCaseRole = caseData.getOutsourcingPolicy().getOrgPolicyCaseAssignedRole();

        return Map.of("outsourcingPolicy", OrganisationPolicy.builder().organisation(newOrganisation)
            .orgPolicyCaseAssignedRole(applicantCaseRole).build());
    }

    public  Map<String, Object> removeApplicantEmailAndStopNotifyingTheirColleagues(CaseData caseData,
                                                                                    String migrationId,
                                                                                    String applicantUuid) {
        UUID targetApplicantUuid = UUID.fromString(applicantUuid);

        List<Element<LocalAuthority>> localAuthorities = caseData.getLocalAuthorities();
        LocalAuthority targetApplicant = ElementUtils.findElement(targetApplicantUuid, localAuthorities)
            .orElseThrow(() -> new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid local authorities (applicant)",
                migrationId, caseData.getId()))
            ).getValue();

        targetApplicant.setEmail(null);
        targetApplicant.getColleagues().stream().map(Element::getValue).forEach(colleague ->
            colleague.setNotificationRecipient(YesNo.NO.getValue())
        );

        return Map.of("localAuthorities", localAuthorities);
    }
}
