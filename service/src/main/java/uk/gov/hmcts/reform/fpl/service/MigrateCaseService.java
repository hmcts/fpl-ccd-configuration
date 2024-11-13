package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.CaseLocation;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.CloseCase;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.Grounds;
import uk.gov.hmcts.reform.fpl.model.Hearing;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.IncorrectCourtCodeConfig;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.Proceeding;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.common.DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.SubmittedC1WithSupplementBundle;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.rd.model.JudicialUserProfile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ACCELERATED_DISCHARGE_OF_CARE;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.EMERGENCY_PROTECTION_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FACT_FINDING;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FAMILY_DRUG_ALCOHOL_COURT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FURTHER_CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.INTERIM_CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.JUDGMENT_AFTER_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.PLACEMENT_HEARING;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.nullSafeList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseService {

    private static final String CANCELLED_HEARING_DETAIL = "cancelledHearingDetails";
    private static final String CLOSE_CASE_TAB = "closeCaseTabField";
    private static final String CASE_MANAGEMENT_LOCATION = "caseManagementLocation";
    private static final String COURT = "court";
    private static final String HEARING_DETAILS = "hearingDetails";
    private static final String ORDERS = "orders";
    private static final String PLACEMENT = "placements";
    private static final String PLACEMENT_NON_CONFIDENTIAL = "placementsNonConfidential";
    private static final String PLACEMENT_NON_CONFIDENTIAL_NOTICES = "placementsNonConfidentialNotices";
    private final CaseNoteService caseNoteService;
    private final CourtService courtService;
    private static final String CORRECT_COURT_NAME = "Family Court Sitting at West London";
    private static final String ORDER_TYPE = "orderType";
    public final MigrateRelatingLAService migrateRelatingLAService;
    public final OrganisationService organisationService;
    public final CourtLookUpService courtLookUpService;

    private static final Map<String, HearingType>  HEARING_TYPE_DETAILS_MAPPING = initialiseHearingMapping();


    private static Map<String, HearingType> initialiseHearingMapping() {
        Map<String, HearingType> hearingMapping = new LinkedHashMap<>();
        hearingMapping.put("EPO", EMERGENCY_PROTECTION_ORDER);
        hearingMapping.put("EMERGENCY", EMERGENCY_PROTECTION_ORDER);
        hearingMapping.put("URGENT OUT OF HOURS", EMERGENCY_PROTECTION_ORDER);
        hearingMapping.put("ICO", INTERIM_CARE_ORDER);
        hearingMapping.put("INTERIM", INTERIM_CARE_ORDER);
        hearingMapping.put("REMOVAL", INTERIM_CARE_ORDER);
        hearingMapping.put("FCMH", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("CMH", CASE_MANAGEMENT);
        hearingMapping.put("CASE MANAGEMENT", CASE_MANAGEMENT);
        hearingMapping.put("PCMH", CASE_MANAGEMENT);
        hearingMapping.put("SECURE ORDER REVIEW", CASE_MANAGEMENT);
        hearingMapping.put("RECOVERY", CASE_MANAGEMENT);
        hearingMapping.put("SECURE ACCOMMODATION", CASE_MANAGEMENT);
        hearingMapping.put("SECURE ACCOMODATION", CASE_MANAGEMENT);
        hearingMapping.put("ISO", CASE_MANAGEMENT);
        hearingMapping.put("DISCHARGE", ACCELERATED_DISCHARGE_OF_CARE);
        hearingMapping.put("RE W", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("FURTHER", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("CAPACITY", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("C2", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("DIRECTIONS", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("PTR", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("C1", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("GROUND RULES", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("DIRECTION", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("MENTION", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("DOLS", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("DOL", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("PERMISSION TO APPEAL", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("PENDING APPEAL", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("LEAVE TO APPEAL", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("POLICE DISCLOSURE", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("DESIGNATION HEARING", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("PHR", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("DIRS", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("PRE TRIAL", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("INFORMATION", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("LAWYER REVIEW", FAMILY_DRUG_ALCOHOL_COURT);
        hearingMapping.put("NLR", FAMILY_DRUG_ALCOHOL_COURT);
        hearingMapping.put("REVIEW", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("COMPLIANCE", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("NEH", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("FACT", FACT_FINDING);
        hearingMapping.put("ISSUE", ISSUE_RESOLUTION);
        hearingMapping.put("IRH", ISSUE_RESOLUTION);
        hearingMapping.put("EFH", ISSUE_RESOLUTION);
        hearingMapping.put("EARLY FINAL", ISSUE_RESOLUTION);
        hearingMapping.put("JUDGEMENT", JUDGMENT_AFTER_HEARING);
        hearingMapping.put("JUDGMENT", JUDGMENT_AFTER_HEARING);
        hearingMapping.put("CONTESTED", FINAL);
        hearingMapping.put("WELFARE", FINAL);
        hearingMapping.put("THRESHOLD", FINAL);
        hearingMapping.put("FINAL", FINAL);
        hearingMapping.put("SUBMISSIONS", FINAL);
        hearingMapping.put("URGENT APPEAL", CASE_MANAGEMENT);
        hearingMapping.put("APPEAL", FINAL);
        hearingMapping.put("DRUG", FAMILY_DRUG_ALCOHOL_COURT);
        hearingMapping.put("CONTEST", FINAL);
        hearingMapping.put("SEPARATION HEARING", FINAL);
        hearingMapping.put("PART HEARD", FINAL);
        hearingMapping.put("CONTACT HEARING", FINAL);
        hearingMapping.put("DEFAULT NOTICE", FAMILY_DRUG_ALCOHOL_COURT);
        hearingMapping.put("FDAC", FAMILY_DRUG_ALCOHOL_COURT);
        hearingMapping.put("PSMC", FAMILY_DRUG_ALCOHOL_COURT);
        hearingMapping.put("NON-LAWYER", FAMILY_DRUG_ALCOHOL_COURT);
        hearingMapping.put("EXIT", FAMILY_DRUG_ALCOHOL_COURT);
        hearingMapping.put("NEUTRAL EVALUATION", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("PLACEMENT", PLACEMENT_HEARING);
        hearingMapping.put("APPLICATION", FURTHER_CASE_MANAGEMENT);
        hearingMapping.put("HEARING", CASE_MANAGEMENT);
        hearingMapping.put("URGENT", CASE_MANAGEMENT);
        return hearingMapping;
    }

    private static final List<HearingType> MIGRATED_HEARING_TYPES = List.of(
        EMERGENCY_PROTECTION_ORDER,
        INTERIM_CARE_ORDER,
        CASE_MANAGEMENT,
        ACCELERATED_DISCHARGE_OF_CARE,
        FURTHER_CASE_MANAGEMENT,
        FACT_FINDING,
        ISSUE_RESOLUTION,
        JUDGMENT_AFTER_HEARING,
        FINAL,
        FAMILY_DRUG_ALCOHOL_COURT,
        PLACEMENT_HEARING
    );

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

    public void doStateCheck(String state, String expectedState, long caseId, String migrationId)
        throws AssertionError {
        if (!state.equals(expectedState)) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, state was %s, expected %s",
                migrationId, caseId, state, expectedState
            ));
        }
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
            return Map.of(COURT, caseData.getCourt().toBuilder()
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
                    HEARING_DETAILS, hearingDetails,
                    "selectedHearingId", hearingDetails.get(hearingDetails.size() - 1).getId()
                );
            } else {
                Map<String, Object> ret = new HashMap<>(Map.of(
                    HEARING_DETAILS, hearingDetails
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

    public void verifyReturnApplicationExists(CaseData caseData, String migrationId, UUID documentId) {
        if (isEmpty(caseData.getReturnApplication())) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, Returned Application not found",
                migrationId, caseData.getId()));
        }

        String caseDocumentUrl = caseData.getReturnApplication().getDocument().getUrl();

        if (!documentId
            .equals(UUID.fromString(caseDocumentUrl.substring(caseDocumentUrl.length() - 36)))) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s},"
                    + " Returned Application document with Id %s not found",
                migrationId, caseData.getId(), documentId));
        }
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

    public Map<String, Object> removeDocumentFiledOnIssue(CaseData caseData, UUID documentFiledOnIssueId,
                                                      String migrationId) {
        Long caseId = caseData.getId();
        List<Element<ManagedDocument>> documentsFiledOnIssueList = caseData.getDocumentsFiledOnIssueList();

        if (documentsFiledOnIssueList.isEmpty()) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, documentsFiledOnIssueList is empty",
                migrationId, caseId
            ));
        }

        List<Element<ManagedDocument>> updatedDocumentsFiledOnIssueList =
            ElementUtils.removeElementWithUUID(documentsFiledOnIssueList, documentFiledOnIssueId);

        if (updatedDocumentsFiledOnIssueList.size() != documentsFiledOnIssueList.size() - 1) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, document filed on issue %s not found",
                migrationId, caseId, documentFiledOnIssueId
            ));
        }

        return Map.of("documentsFiledOnIssueList", updatedDocumentsFiledOnIssueList);
    }

    public Map<String, Object> addCourt(String courtId) {
        Optional<Court> court = courtService.getCourt(courtId);

        if (court.isPresent()) {
            return Map.of(COURT, court.get());
        } else {
            throw new IllegalArgumentException(format("Court not found with ID %s", courtId));
        }
    }

    protected static String stripIllegalCharacters(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replace("<", "")
            .replace(">", "");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fixOrderTypeTypo(String migrationId, CaseDetails caseDetails) {
        String invalidOrderType = "EDUCATION_SUPERVISION__ORDER";
        String validOrderType = "EDUCATION_SUPERVISION_ORDER";

        Optional<Map> orders = Optional.ofNullable((Map) caseDetails.getData().get(ORDERS));
        if (orders.isPresent()) {
            Optional<List<String>> orderType = Optional.ofNullable((List<String>) orders.get().get(ORDER_TYPE));
            if (orderType.isPresent() && orderType.get().contains(invalidOrderType)) {
                Map ordersMap = new HashMap<>(orders.get());
                List<String> newOrderType = new ArrayList<>(((List<String>) ordersMap.get(ORDER_TYPE)));
                newOrderType.replaceAll(target -> target.equals(invalidOrderType) ? validOrderType : target);
                ordersMap.put(ORDER_TYPE, newOrderType);
                return Map.of(ORDERS, ordersMap);
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

    public void clearHearingOption(CaseDetails caseDetails) {
        caseDetails.getData().remove("hearingOption");
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
            .get(CASE_MANAGEMENT_LOCATION);
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

        return Map.of(COURT, court.toBuilder().epimmsId(correctBaseLocation).build(),
            CASE_MANAGEMENT_LOCATION, CaseLocation.builder()
            .baseLocation(correctBaseLocation)
            .region(correctRegion)
            .build());
    }

    /**
     * NB when calculating the index: spaces, \n, \t, etc. count as one character.
     */
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

    public Map<String, OrganisationPolicy> changeThirdPartyStandaloneApplicant(CaseData caseData, String orgId,
                                                                               String applicantCaseRole) {
        String orgName = organisationService.findOrganisation(orgId)
            .map(uk.gov.hmcts.reform.rd.model.Organisation::getName)
            .orElseThrow();

        Organisation newOrganisation = Organisation.builder()
            .organisationID(orgId)
            .organisationName(orgName)
            .build();

        applicantCaseRole = caseData.getOutsourcingPolicy() != null
            ? caseData.getOutsourcingPolicy().getOrgPolicyCaseAssignedRole() : applicantCaseRole;

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

    public Map<String, Object> setCaseManagementLocation(CaseData caseData, String migrationId) {
        String courtCode = caseData.getCourt().getCode();
        Optional<Court> lookedUpCourt = courtLookUpService.getCourtByCode(courtCode);

        if (lookedUpCourt.isPresent()) {
            return Map.of(CASE_MANAGEMENT_LOCATION, CaseLocation.builder()
                .baseLocation(lookedUpCourt.get().getEpimmsId())
                .region(lookedUpCourt.get().getRegionId())
                .build());
        } else {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, could not find correct caseManagementLocation",
                migrationId, caseData.getId()));
        }
    }

    public Map<String, Object> removeSocialWorkerTelephone(CaseData caseData, String migrationId, UUID childId) {
        List<Element<Child>> children = caseData.getAllChildren();
        Element<Child> targetChild = ElementUtils.findElement(childId, children)
            .orElseThrow(() -> new AssertionError(format(
                "Migration {id = %s, case reference = %s}, could not find child with UUID %s",
                migrationId, caseData.getId(), childId))
            );

        final Child child = targetChild.getValue();

        if (isEmpty(child.getParty().getSocialWorkerTelephoneNumber())) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, child did not have social worker telephone",
                migrationId, caseData.getId()));
        }

        Child updatedChild = child.toBuilder()
            .party(child.getParty().toBuilder()
                .socialWorkerTelephoneNumber(child.getParty().getSocialWorkerTelephoneNumber().toBuilder()
                    .telephoneNumber(null)
                    .build())
                .build())
            .build();

        targetChild.setValue(updatedChild);
        return Map.of("children1", children);
    }

    public Map<String, Object> migrateCaseClosedDateToLatestFinalOrderApprovalDate(CaseData caseData,
                                                                                   String migrationId) {
        if (!State.CLOSED.equals(caseData.getState())) {
            throw new AssertionError(format("Migration {id = %s, case reference = %s} Case is not closed yet",
                migrationId, caseData.getId()));
        }

        if (isEmpty(caseData.getOrderCollection())) {
            throw new AssertionError(format("Migration {id = %s, case reference = %s} Order collection is null/empty",
                migrationId, caseData.getId()));
        }

        List<GeneratedOrder> finalOrders = unwrapElements(caseData.getOrderCollection()).stream()
            .filter(GeneratedOrder::isFinalOrder)
            .toList();

        if (finalOrders.isEmpty()) {
            throw new AssertionError(format("Migration {id = %s, case reference = %s} No final order found",
                migrationId, caseData.getId()));
        }

        LocalDate latestApprovalDate = finalOrders.stream()
            .filter(order -> order.getApprovalDate() != null || order.getApprovalDateTime() != null)
            .map(order -> {
                if (order.getApprovalDateTime() == null) {
                    return order.getApprovalDate();
                } else {
                    LocalDate convertedApprovalDateTime = order.getApprovalDateTime().toLocalDate();
                    if (order.getApprovalDate() == null) {
                        return convertedApprovalDateTime;
                    } else {
                        return (convertedApprovalDateTime.isAfter(order.getApprovalDate()))
                            ? convertedApprovalDateTime : order.getApprovalDate();
                    }
                }
            })
            .sorted(Comparator.reverseOrder())
            .findFirst()
            .orElseThrow(() ->
                new AssertionError(format("Migration {id = %s, case reference = %s} approval date not found",
                migrationId, caseData.getId())));

        CloseCase existingCloseCaseField = Optional.ofNullable(caseData.getCloseCaseTabField())
            .orElse(CloseCase.builder().build());

        return Map.of(CLOSE_CASE_TAB, existingCloseCaseField.toBuilder()
            .date(latestApprovalDate)
            .dateBackup((isEmpty(existingCloseCaseField.getDateBackup())
                ? existingCloseCaseField.getDate() : existingCloseCaseField.getDateBackup()))
            .build());
    }

    public Map<String, Object> rollbackCloseCaseTabFieldMigration(CaseData caseData, String migrationId) {
        CloseCase closeCaseField = caseData.getCloseCaseTabField();
        if (closeCaseField == null) {
            throw new AssertionError(format("Migration {id = %s, case reference = %s} closeCaseField is null",
                migrationId, caseData.getId()));
        }
        return Map.of(CLOSE_CASE_TAB, closeCaseField.toBuilder()
            .date(closeCaseField.getDateBackup())
            .dateBackup(null).build());
    }

    public Map<String, Object> clearCloseCaseTabBackupField(CaseData caseData) {
        CloseCase closeCaseField = caseData.getCloseCaseTabField();
        return Map.of(CLOSE_CASE_TAB, closeCaseField.toBuilder().dateBackup(null).build());
    }

    public Map<String, Object> migrateCaseRemoveUnknownAllocatedJudgeTitle(CaseData caseData,
                                                                           String migrationId) {

        if (caseData.getAllocatedJudge().getJudgeTitle() == JudgeOrMagistrateTitle.OTHER
            && caseData.getAllocatedJudge().getOtherTitle().equalsIgnoreCase("Unknown")) {
            return Map.of("allocatedJudge", caseData.getAllocatedJudge().toBuilder()
                .otherTitle(JudicialUserProfile.builder()
                    .fullName(caseData.getAllocatedJudge().getJudgeFullName())
                    .build().getTitle())
                .build());
        } else {
            throw new AssertionError(format("Migration {id = %s, case reference = %s} otherTitle is %s",
                migrationId, caseData.getId(), caseData.getAllocatedJudge().getOtherTitle()));
        }
    }

    private static void processHearingBooking(Element<HearingBooking> element) {
        HearingBooking hearingBooking = element.getValue();
        if (OTHER.equals(element.getValue().getType())) {
            Optional<HearingType> hearingType = evaluateType(hearingBooking.getTypeDetails());
            hearingBooking.setType(hearingType.orElse(FURTHER_CASE_MANAGEMENT));
        }
    }

    public static Map<String, Object> migrateHearingType(CaseData caseData) {
        List<Element<HearingBooking>> updatedHearingDetails = Optional.ofNullable(caseData.getHearingDetails())
            .map(List::stream)
            .orElseGet(Stream::empty)
            .peek(MigrateCaseService::processHearingBooking)
            .collect(toList());

        List<Element<HearingBooking>> updatedCancelledHearingDetails = Optional.ofNullable(caseData
                .getCancelledHearingDetails())
            .map(List::stream)
            .orElseGet(Stream::empty)
            .peek(MigrateCaseService::processHearingBooking)
            .collect(toList());

        Map<String, Object> hearingDetailsMap = new HashMap<>();
        if (!updatedHearingDetails.isEmpty()) {
            hearingDetailsMap.put(HEARING_DETAILS, updatedHearingDetails);
        }
        if (!updatedCancelledHearingDetails.isEmpty()) {
            hearingDetailsMap.put(CANCELLED_HEARING_DETAIL, updatedCancelledHearingDetails);
        }

        return hearingDetailsMap;
    }

    public Map<String, Object> updateCancelledHearingDetailsType(CaseData caseData, String migrationId) {
        List<Element<HearingBooking>> updatedCancelledHearingDetails = Optional.ofNullable(caseData
                .getCancelledHearingDetails())
            .map(List::stream)
            .orElseGet(Stream::empty)
            .peek(MigrateCaseService::processHearingBooking)
            .collect(toList());

        Map<String, Object> hearingDetailsMap = new HashMap<>();

        if (!updatedCancelledHearingDetails.isEmpty()) {
            hearingDetailsMap.put(CANCELLED_HEARING_DETAIL, updatedCancelledHearingDetails);
        } else {
            throw new AssertionError(format("Migration {id = %s}, CancelledHearingDetails not found", migrationId));
        }

        return hearingDetailsMap;
    }

    private static Optional<HearingType> evaluateType(String typeDetails) {
        return HEARING_TYPE_DETAILS_MAPPING.entrySet().stream()
            .filter(key -> typeDetails.toUpperCase().contains(key.getKey()))
            .map(Map.Entry::getValue)
            .findFirst();
    }

    public Map<String, Object> rollbackHearingType(CaseData caseData) {
        Map<String, Object> hearingDetailsMap = new HashMap<>();

        List<Element<HearingBooking>> hearingDetails = caseData.getHearingDetails();
        if (!isNull(hearingDetails) && !hearingDetails.isEmpty()) {
            rollbackHearingBooking(hearingDetails);
            hearingDetailsMap.put(HEARING_DETAILS, hearingDetails);
        }

        List<Element<HearingBooking>> cancelledHearingDetails = caseData.getCancelledHearingDetails();
        if (!isNull(cancelledHearingDetails) && !cancelledHearingDetails.isEmpty()) {
            rollbackHearingBooking(cancelledHearingDetails);
            hearingDetailsMap.put(CANCELLED_HEARING_DETAIL, cancelledHearingDetails);
        }

        return hearingDetailsMap.isEmpty() ? emptyMap() : hearingDetailsMap;
    }

    private void rollbackHearingBooking(List<Element<HearingBooking>> hearingBookings) {
        hearingBookings.stream()
            .map(Element::getValue)
            .forEach(booking -> {
                if (Objects.isNull(booking.getType()) || MIGRATED_HEARING_TYPES.contains(booking.getType())
                    && isNotEmpty(booking.getTypeDetails())) {
                    booking.setType(OTHER);
                }
            });
    }

    public Map<String, Object> removeSubmittedC1Document(CaseData caseData, String migrationId) {
        SubmittedC1WithSupplementBundle submittedC1WithSupplement = caseData.getSubmittedC1WithSupplement();

        if (submittedC1WithSupplement == null) {
            throw new AssertionError(format("Migration {id = %s}, submittedC1WithSupplement not found", migrationId));
        }

        return Map.of("submittedC1WithSupplement", submittedC1WithSupplement.toBuilder().document(null).build());
    }

    public Map<String, Object> removeNamesFromOtherProceedings(CaseData caseData, String migrationId) {

        if (caseData.getProceeding() == null) {
            throw new AssertionError(format("Migration {id = %s}, proceedings not found", migrationId));
        }

        final List<Element<Proceeding>> additionalProceedings = caseData.getProceeding().getAdditionalProceedings()
            .stream().map(el -> element(el.getId(), el.getValue().toBuilder().children(null).build())).toList();

        Proceeding updatedProceeding = caseData.getProceeding().toBuilder()
            .additionalProceedings(additionalProceedings)
            .children(null)
            .build();

        return Map.of("proceeding", updatedProceeding);
    }


    public Map<String, Object> removeRespondentTelephoneNumber(CaseData caseData, UUID respondentId,
                                                               String migrationId) {
        List<Element<Respondent>> respondents = caseData.getAllRespondents();

        Element<Respondent> targetRespondent = ElementUtils.findElement(respondentId, respondents)
            .orElseThrow(() -> new AssertionError(format(
                "Migration {id = %s, case reference = %s}, could not find respondent with UUID %s",
                migrationId, caseData.getId(), respondentId))
            );

        final Respondent respondent = targetRespondent.getValue();

        if (isEmpty(respondent.getParty().getTelephoneNumber())) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, respondent did not have telephone number",
                migrationId, caseData.getId()));
        }

        Respondent updatedRespondent = respondent.toBuilder()
            .party(respondent.getParty().toBuilder()
                .telephoneNumber(respondent.getParty().getTelephoneNumber().toBuilder()
                    .telephoneNumber(null)
                    .build())
                .build())
            .build();

        targetRespondent.setValue(updatedRespondent);

        return Map.of("respondents1", respondents);
    }

    public Map<String, Object> removeRespondentsAwareReason(CaseData caseData, String migrationId) {

        if (caseData.getHearing() == null) {
            throw new AssertionError(format("Migration {id = %s}, hearing not found", migrationId));
        }

        Hearing hearing = caseData.getHearing().toBuilder()
            .respondentsAwareReason(null)
            .build();

        return Map.of("hearing",hearing);
    }

    public Map<String, Object> redactTypeReason(CaseData caseData, String migrationId, int startLoc, int endLoc) {
        if (isEmpty(caseData.getHearing()) || isEmpty(caseData.getHearing().getTypeGiveReason())) {
            throw new AssertionError(format("Migration {id = %s}, hearing not found", migrationId));
        }

        final String typeGiveReason = caseData.getHearing().getTypeGiveReason();

        Hearing hearing = caseData.getHearing().toBuilder()
            .typeGiveReason(typeGiveReason.replace(typeGiveReason.substring(startLoc, endLoc), "***"))
            .build();

        return Map.of("hearing", hearing);
    }

    public Map<String, Object> removeAddressFromEPO(CaseData caseData, String migrationId) {
        if (!caseData.getOrders().getOrderType().contains(OrderType.EMERGENCY_PROTECTION_ORDER)) {
            throw new AssertionError(format("Migration {id = %s}, this is not an EPO", migrationId));
        }

        return Map.of(ORDERS, caseData.getOrders().toBuilder().address(null).build());
    }
}
