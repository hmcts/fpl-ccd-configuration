package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.IncorrectCourtCodeConfig;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseService {

    private static final String PLACEMENT = "placements";
    private static final String PLACEMENT_NON_CONFIDENTIAL = "placementsNonConfidential";
    private static final String PLACEMENT_NON_CONFIDENTIAL_NOTICES = "placementsNonConfidentialNotices";
    private final CaseNoteService caseNoteService;

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
            .collect(toList());

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
                                .collect(Collectors.toList())).build());
                }
            }).collect(Collectors.toList());

        return Map.of("documentsSentToParties", resultDocumentsSentToParties);
    }

    public Map<String, Object> removePositionStatementChild(CaseData caseData,
                                                            String migrationId,
                                                            UUID expectedPositionStatementId) {
        Long caseId = caseData.getId();
        List<Element<PositionStatementChild>> positionStatementChildListResult =
            caseData.getHearingDocuments().getPositionStatementChildListV2().stream()
                .filter(el -> !el.getId().equals(expectedPositionStatementId))
                .collect(toList());

        if (positionStatementChildListResult.size() != caseData.getHearingDocuments()
            .getPositionStatementChildListV2().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid position statement child",
                migrationId, caseId));
        }
        return Map.of("positionStatementChildListV2", positionStatementChildListResult);
    }

    public Map<String, Object> removePositionStatementRespondent(CaseData caseData,
                                                                 String migrationId,
                                                                 UUID expectedPositionStatementId) {
        Long caseId = caseData.getId();
        List<Element<PositionStatementRespondent>> positionStatementRespondentListResult =
            caseData.getHearingDocuments().getPositionStatementRespondentListV2().stream()
                .filter(el -> !el.getId().equals(expectedPositionStatementId))
                .collect(toList());

        if (positionStatementRespondentListResult.size() != caseData.getHearingDocuments()
            .getPositionStatementRespondentListV2().size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, invalid position statement respondent",
                migrationId, caseId));
        }
        return Map.of("positionStatementRespondentListV2", positionStatementRespondentListResult);
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
            .correctCourtName("Family Court Sitting at West London")
            .organisationId("SPUL3VV")
            .build();
        IncorrectCourtCodeConfig hrw = IncorrectCourtCodeConfig.builder()
            .incorrectCourtCode("117")
            .correctCourtCode("332")
            .correctCourtName("Family Court Sitting at West London")
            .organisationId("L3HSA4L")
            .build();
        IncorrectCourtCodeConfig hlw = IncorrectCourtCodeConfig.builder()
            .incorrectCourtCode("117")
            .correctCourtCode("332")
            .correctCourtName("Family Court Sitting at West London")
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
                    .collect(toList());

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
            if (hearingDetails.size() > 0) {
                return Map.of(
                    "hearingDetails", hearingDetails,
                    "selectedHearingId", hearingDetails.get(hearingDetails.size() - 1).getId()
                );
            } else {
                Map<String, Object> ret =  new HashMap<String, Object>(Map.of(
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

    public Map<String, Object> removeApplicationDocument(CaseData caseData,
                                                                 String migrationId,
                                                                 UUID expectedApplicationDocumentId) {
        Long caseId = caseData.getId();
        List<Element<ApplicationDocument>> applicationDocuments =
            caseData.getApplicationDocuments().stream()
                .filter(el -> !el.getId().equals(expectedApplicationDocumentId))
                .collect(toList());

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
                .collect(toList());

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
                }).collect(toList());

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

    public Map<String, Object> removeSpecificPlacements(CaseData caseData, UUID placementToRemove) {
        List<Element<Placement>> placementsToKeep = caseData.getPlacementEventData().getPlacements().stream()
            .filter(x -> !x.getId().equals(placementToRemove)).collect(toList());
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

    public Map<String, Object> renameApplicationDocuments(CaseData caseData) {
        List<Element<ApplicationDocument>> updatedList = caseData.getApplicationDocuments().stream()
            .map(el -> {
                String currentName = el.getValue().getDocumentName();
                el.getValue().setDocumentName(stripIllegalCharacters(currentName));
                return el;
            }).collect(toList());

        return Map.of("applicationDocuments", updatedList);
    }

    public Map<String, Object> removeJudicialMessage(CaseData caseData, String migrationId, String messageId) {
        return Map.of("judicialMessages",
                removeJudicialMessageFormList(caseData.getJudicialMessages(), messageId, migrationId,
                    caseData.getId()));
    }

    private List<Element<JudicialMessage>> removeJudicialMessageFormList(List<Element<JudicialMessage>> messages,
                                                              String messageId, String migrationId, Long caseId) {

        UUID targetMessageId = UUID.fromString(messageId);
        List<Element<JudicialMessage>> resultList = Optional.ofNullable(messages).orElseThrow(() ->
                new AssertionError(format("Migration {id = %s, case reference = %s}, judicial message is null",
                    migrationId, caseId)))
            .stream().filter(message -> !message.getId().equals(targetMessageId))
            .collect(toList());

        if (resultList.size() != messages.size() - 1) {
            throw new AssertionError(format("Migration {id = %s, case reference = %s}, judicial message %s not found",
                migrationId, caseId, messageId));
        }

        return resultList;
    }

    private String stripIllegalCharacters(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replace("<", "")
            .replace(">", "");
    }
}
