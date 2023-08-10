package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType;
import uk.gov.hmcts.reform.fpl.enums.CaseExtensionReasonList;
import uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType;
import uk.gov.hmcts.reform.fpl.model.ApplicationDocument;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseSummary;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Court;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingFurtherEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.IncorrectCourtCodeConfig;
import uk.gov.hmcts.reform.fpl.model.ManagedDocument;
import uk.gov.hmcts.reform.fpl.model.Placement;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.RespondentStatementV2;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.SkeletonArgument;
import uk.gov.hmcts.reform.fpl.model.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.BIRTH_CERTIFICATE;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CARE_PLAN;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.CHECKLIST_DOCUMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.GENOGRAM;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_CHRONOLOGY;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SOCIAL_WORK_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.SWET;
import static uk.gov.hmcts.reform.fpl.enums.ApplicationDocumentType.THRESHOLD;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.APPLICANT_STATEMENT;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.EXPERT_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.GUARDIAN_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE;
import static uk.gov.hmcts.reform.fpl.enums.FurtherEvidenceType.OTHER_REPORTS;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType.PROFESSIONAL_DRUG;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType.PROFESSIONAL_HAIR;
import static uk.gov.hmcts.reform.fpl.enums.ccd.fixedlists.ExpertReportType.TOXICOLOGY_REPORT;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

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

    @SuppressWarnings("unchecked")
    public void rollbackCourtBundleMigration(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();

        List<Element<HearingCourtBundle>> newHearingCourtBundleList = new ArrayList<>();

        if (caseDataMap.get("courtBundleListV2") != null) {
            newHearingCourtBundleList.addAll((List) caseDataMap.get("courtBundleListV2"));
        }
        if (caseDataMap.get("courtBundleListLA") != null) {
            newHearingCourtBundleList.addAll((List) caseDataMap.get("courtBundleListLA"));
        }
        if (caseDataMap.get("courtBundleListCTSC") != null) {
            newHearingCourtBundleList.addAll((List) caseDataMap.get("courtBundleListCTSC"));
        }

        caseDataMap.put("courtBundleListV2", newHearingCourtBundleList);
        caseDataMap.remove("courtBundleListLA");
        caseDataMap.remove("courtBundleListCTSC");
    }

    public Map<String, Object> migrateCourtBundle(CaseData caseData) {
        List<Element<HearingCourtBundle>> newHearingCourtBundleList = new ArrayList<>();
        List<Element<HearingCourtBundle>> hearingCourtBundleListLA = new ArrayList<>();
        List<Element<HearingCourtBundle>> hearingCourtBundleListCTSC = new ArrayList<>();

        List<Element<HearingCourtBundle>> hearingCourtBundles = caseData.getHearingDocuments().getCourtBundleListV2();

        for (Element<HearingCourtBundle> hearingCourtBundle : hearingCourtBundles) {
            List<Element<CourtBundle>> newCourtBundleList = hearingCourtBundle.getValue().getCourtBundle().stream()
                .filter(cs -> !cs.getValue().isConfidentialDocument())
                .collect(toList());

            List<Element<CourtBundle>> courtBundleListLA = hearingCourtBundle.getValue().getCourtBundle().stream()
                .filter(cs -> !cs.getValue().isUploadedByHMCTS() && cs.getValue().isConfidentialDocument())
                .collect(toList());

            List<Element<CourtBundle>> courtBundleListCTSC = hearingCourtBundle.getValue().getCourtBundle().stream()
                .filter(cs -> cs.getValue().isUploadedByHMCTS() && cs.getValue().isConfidentialDocument())
                .collect(toList());

            if (!newCourtBundleList.isEmpty()) {
                Element<HearingCourtBundle> bundle = element(hearingCourtBundle.getId(),
                    HearingCourtBundle.builder().build());
                bundle.getValue().setHearing(hearingCourtBundle.getValue().getHearing());
                bundle.getValue().setCourtBundle(newCourtBundleList);
                newHearingCourtBundleList.add(bundle);
            }

            if (!courtBundleListLA.isEmpty()) {
                Element<HearingCourtBundle> bundle = element(hearingCourtBundle.getId(),
                    HearingCourtBundle.builder().build());
                bundle.getValue().setHearing(hearingCourtBundle.getValue().getHearing());
                bundle.getValue().setCourtBundle(courtBundleListLA);
                hearingCourtBundleListLA.add(bundle);
            }

            if (!courtBundleListCTSC.isEmpty()) {
                Element<HearingCourtBundle> bundle = element(hearingCourtBundle.getId(),
                    HearingCourtBundle.builder().build());
                bundle.getValue().setHearing(hearingCourtBundle.getValue().getHearing());
                bundle.getValue().setCourtBundle(courtBundleListCTSC);
                hearingCourtBundleListCTSC.add(bundle);
            }
        }

        Map<String, Object> ret = new HashMap<>();
        ret.put("courtBundleListV2", newHearingCourtBundleList);
        ret.put("courtBundleListLA", hearingCourtBundleListLA);
        ret.put("courtBundleListCTSC", hearingCourtBundleListCTSC);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public void rollbackCaseSummaryMigration(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();
        List<Element<CaseSummary>> newCaseSummaryList = new ArrayList<>();

        if (caseDataMap.get("caseSummaryListLA") != null) {
            newCaseSummaryList.addAll((List) caseDataMap.get("caseSummaryListLA"));
        }
        if (caseDataMap.get("caseSummaryList") != null) {
            newCaseSummaryList.addAll((List) caseDataMap.get("caseSummaryList"));
        }

        caseDataMap.put("caseSummaryList", newCaseSummaryList);
        caseDataMap.remove("caseSummaryListLA");
    }

    public Map<String, Object> moveCaseSummaryWithConfidentialAddressToCaseSummaryListLA(CaseData caseData) {
        List<Element<CaseSummary>> caseSummaryListLA = caseData.getHearingDocuments().getCaseSummaryList().stream()
            .filter(cs -> YesNo.YES.getValue().equals(cs.getValue().getHasConfidentialAddress()))
            .collect(toList());

        List<Element<CaseSummary>> newCaseSummaryList = caseData.getHearingDocuments().getCaseSummaryList().stream()
            .filter(cs -> YesNo.NO.getValue().equals(Optional.ofNullable(cs.getValue().getHasConfidentialAddress())
                .orElse(YesNo.NO.getValue())))
            .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("caseSummaryListLA", caseSummaryListLA);
        ret.put("caseSummaryList", newCaseSummaryList);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public void rollbackMigratePositionStatementChild(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();

        List<Element<PositionStatementChild>> newPositionStatementChilds = new ArrayList<>();

        if (caseDataMap.get("posStmtChildListLA") != null) {
            newPositionStatementChilds.addAll((List) caseDataMap.get("posStmtChildListLA"));
        }
        if (caseDataMap.get("posStmtChildList") != null) {
            newPositionStatementChilds.addAll((List) caseDataMap.get("posStmtChildList"));
        }

        caseDetails.getData().put("positionStatementChildListV2", newPositionStatementChilds);
        caseDetails.getData().remove("posStmtChildListLA");
        caseDetails.getData().remove("posStmtChildList");
    }

    @SuppressWarnings("unchecked")
    public void rollbackMigratePositionStatementRespondent(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();

        List<Element<PositionStatementRespondent>> newPositionStatementRespondents = new ArrayList<>();

        if (caseDataMap.get("posStmtRespListLA") != null) {
            newPositionStatementRespondents.addAll((List) caseDataMap.get("posStmtRespListLA"));
        }
        if (caseDataMap.get("posStmtRespList") != null) {
            newPositionStatementRespondents.addAll((List) caseDataMap.get("posStmtRespList"));
        }

        caseDetails.getData().put("positionStatementRespondentListV2", newPositionStatementRespondents);
        caseDetails.getData().remove("posStmtRespListLA");
        caseDetails.getData().remove("posStmtRespList");
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public Map<String, Object> migratePositionStatementChild(CaseData caseData) {
        List<Element<PositionStatementChild>> posStmtChildListLA = caseData.getHearingDocuments()
            .getPositionStatementChildListV2().stream()
            .filter(cs -> YesNo.YES.getValue().equals(cs.getValue().getHasConfidentialAddress()))
            .collect(toList());

        List<Element<PositionStatementChild>> posStmtChildList = caseData.getHearingDocuments()
            .getPositionStatementChildListV2().stream()
            .filter(cs -> YesNo.NO.getValue().equals(Optional.ofNullable(cs.getValue().getHasConfidentialAddress())
                .orElse(YesNo.NO.getValue())))
            .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("positionStatementChildListV2", null);
        ret.put("posStmtChildListLA", posStmtChildListLA);
        ret.put("posStmtChildList", posStmtChildList);
        return ret;
    }

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public Map<String, Object> migratePositionStatementRespondent(CaseData caseData) {
        List<Element<PositionStatementRespondent>> posStmtRespListLA = caseData.getHearingDocuments()
            .getPositionStatementRespondentListV2().stream()
            .filter(cs -> YesNo.YES.getValue().equals(cs.getValue().getHasConfidentialAddress()))
            .collect(toList());

        List<Element<PositionStatementRespondent>> posStmtRespList = caseData.getHearingDocuments()
            .getPositionStatementRespondentListV2().stream()
            .filter(cs -> YesNo.NO.getValue().equals(Optional.ofNullable(cs.getValue().getHasConfidentialAddress())
                .orElse(YesNo.NO.getValue())))
            .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("positionStatementRespondentListV2", null);
        ret.put("posStmtRespListLA", posStmtRespListLA);
        ret.put("posStmtRespList", posStmtRespList);
        return ret;
    }

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

    public Map<String, Object> removePositionStatementChild(CaseData caseData,
                                                            String migrationId,
                                                            UUID expectedPositionStatementId) {
        Long caseId = caseData.getId();
        List<Element<PositionStatementChild>> positionStatementChildListResult =
            caseData.getHearingDocuments().getPositionStatementChildListV2().stream()
                .filter(el -> !el.getId().equals(expectedPositionStatementId))
                .toList();

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
                .toList();

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

    private Element<RespondentStatementV2> toRespondentStatementV2(UUID respondentId, String respondentName,
                                                                   Element<SupportingEvidenceBundle> sebElement) {
        SupportingEvidenceBundle seb = sebElement.getValue();
        return element(sebElement.getId(), RespondentStatementV2.builder()
            .respondentId(respondentId)
            .respondentName(respondentName)
            .confidential(seb.getConfidential())
            .dateTimeReceived(seb.getDateTimeReceived())
            .dateTimeUploaded(seb.getDateTimeUploaded())
            .document(seb.getDocument())
            .documentAcknowledge(seb.getDocumentAcknowledge())
            .expertReportType(seb.getExpertReportType())
            .hasConfidentialAddress(seb.getHasConfidentialAddress())
            .name(seb.getName())
            .notes(seb.getNotes())
            .removalReason(seb.getRemovalReason())
            .translatedDocument(seb.getTranslatedDocument())
            .translationRequirements(seb.getTranslationRequirements())
            .translationUploadDateTime(seb.getTranslationUploadDateTime())
            .type(seb.getType())
            .uploadedBy(seb.getUploadedBy())
            .uploadedBySolicitor(seb.getUploadedBySolicitor())
            .build());
    }

    public Map<String, Object> migrateRespondentStatement(CaseData caseData) {
        List<Element<RespondentStatementV2>> respStmtList =
            caseData.getRespondentStatements().stream()
                .flatMap(rs -> {
                    UUID respondentId = rs.getValue().getRespondentId();
                    String respondentName = rs.getValue().getRespondentName();
                    return rs.getValue().getSupportingEvidenceNC().stream().map(
                        seb -> toRespondentStatementV2(respondentId, respondentName, seb)
                    );
                })
                .collect(toList());
        List<Element<RespondentStatementV2>> respStmtListLA =
            caseData.getRespondentStatements().stream()
                .flatMap(rs -> {
                    UUID respondentId = rs.getValue().getRespondentId();
                    String respondentName = rs.getValue().getRespondentName();
                    return rs.getValue().getSupportingEvidenceLA().stream().map(
                        seb -> toRespondentStatementV2(respondentId, respondentName, seb)
                    );
                })
                .filter(seb -> !respStmtList.contains(seb))
                .collect(toList());
        List<Element<RespondentStatementV2>> respStmtListCTSC =
            caseData.getRespondentStatements().stream()
                .flatMap(rs -> {
                    UUID respondentId = rs.getValue().getRespondentId();
                    String respondentName = rs.getValue().getRespondentName();
                    return rs.getValue().getSupportingEvidenceBundle().stream().map(
                        seb -> toRespondentStatementV2(respondentId, respondentName, seb)
                    );
                })
                .filter(seb -> !respStmtList.contains(seb) && !respStmtListLA.contains(seb))
                .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("respStmtList", respStmtList);
        ret.put("respStmtListLA", respStmtListLA);
        ret.put("respStmtListCTSC", respStmtListCTSC);
        ret.put("respondentStatements", null);
        return ret;
    }

    private Map<String, Object> migrateFurtherEvidenceDocuments(CaseData caseData,
                                                                FurtherEvidenceType furtherEvidenceType,
                                                                String newFieldName) {
        final List<ExpertReportType> drugAndAlcoholReportTypes = List.of(PROFESSIONAL_DRUG,
            PROFESSIONAL_HAIR,
            TOXICOLOGY_REPORT);

        Predicate<Element<SupportingEvidenceBundle>> expertReportTypePredicate = a -> {
            if (EXPERT_REPORTS.equals(furtherEvidenceType)) {
                switch (newFieldName) {
                    case "expertReportList":
                        return !drugAndAlcoholReportTypes.contains(a.getValue().getExpertReportType());
                    case "drugAndAlcoholReportList":
                        return drugAndAlcoholReportTypes.contains(a.getValue().getExpertReportType());
                    default:
                        return true;
                }
            } else {
                return true;
            }
        };

        // uploaded by LA
        final List<Element<ManagedDocument>> newDocListLA =
            Optional.ofNullable(caseData.getFurtherEvidenceDocumentsLA()).orElse(List.of()).stream()
                .filter(fed -> furtherEvidenceType.equals(fed.getValue().getType()))
                .filter(expertReportTypePredicate)
                .filter(fed -> fed.getValue().isConfidentialDocument())
                .map(fed -> element(fed.getId(), ManagedDocument.builder()
                    .document(fed.getValue().getDocument())
                    .build()))
                .collect(toList());

        final List<Element<ManagedDocument>> newDocList = new ArrayList<>(
            Optional.ofNullable(caseData.getFurtherEvidenceDocumentsLA()).orElse(List.of()).stream()
                .filter(fed -> furtherEvidenceType.equals(fed.getValue().getType()))
                .filter(expertReportTypePredicate)
                .filter(fed -> !fed.getValue().isConfidentialDocument())
                .map(fed -> element(fed.getId(), ManagedDocument.builder()
                    .document(fed.getValue().getDocument())
                    .build()))
                .collect(toList()));

        // uploaded by admin
        final List<Element<ManagedDocument>> newDocListCTSC =
            Optional.ofNullable(caseData.getFurtherEvidenceDocuments()).orElse(List.of()).stream()
                .filter(fed -> furtherEvidenceType.equals(fed.getValue().getType()))
                .filter(expertReportTypePredicate)
                .filter(fed -> fed.getValue().isConfidentialDocument())
                .map(fed -> element(fed.getId(), ManagedDocument.builder()
                    .document(fed.getValue().getDocument())
                    .build()))
                .collect(toList());

        newDocList.addAll(
            Optional.ofNullable(caseData.getFurtherEvidenceDocuments()).orElse(List.of()).stream()
                .filter(fed -> furtherEvidenceType.equals(fed.getValue().getType()))
                .filter(expertReportTypePredicate)
                .filter(fed -> !fed.getValue().isConfidentialDocument())
                .map(fed -> element(fed.getId(), ManagedDocument.builder()
                    .document(fed.getValue().getDocument())
                    .build()))
                .collect(toList()));

        newDocList.addAll(
            Optional.ofNullable(caseData.getFurtherEvidenceDocumentsSolicitor()).orElse(List.of()).stream()
                .filter(fed -> furtherEvidenceType.equals(fed.getValue().getType()))
                .filter(expertReportTypePredicate)
                .map(fed -> element(fed.getId(), ManagedDocument.builder()
                    .document(fed.getValue().getDocument())
                    .build()))
                .collect(toList()));


        Map<String, Object> ret = new HashMap<>();
        ret.put(newFieldName, newDocList);
        ret.put(newFieldName + "LA", newDocListLA);
        ret.put(newFieldName + "CTSC", newDocListCTSC);
        return ret;
    }

    public Map<String, Object> migrateApplicantWitnessStatements(CaseData caseData) {
        FurtherEvidenceType furtherEvidenceType = APPLICANT_STATEMENT;
        return migrateFurtherEvidenceDocuments(caseData, furtherEvidenceType, "applicantWitnessStmtList");
    }

    public Map<String, Object> migrateGuardianReports(CaseData caseData) {
        FurtherEvidenceType furtherEvidenceType = GUARDIAN_REPORTS;
        return migrateFurtherEvidenceDocuments(caseData, furtherEvidenceType, "guardianEvidenceList");
    }

    public Map<String, Object> migrateNoticeOfActingOrIssue(CaseData caseData) {
        FurtherEvidenceType furtherEvidenceType = NOTICE_OF_ACTING_OR_NOTICE_OF_ISSUE;
        return migrateFurtherEvidenceDocuments(caseData, furtherEvidenceType, "noticeOfActingOrIssueList");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> migrateExpertReports(CaseData caseData) {
        Map<String, Object> ret = new HashMap<>();
        migrateFurtherEvidenceDocuments(caseData, OTHER_REPORTS, "expertReportList").entrySet().stream();

        Map<String, Object> mergedMap = new HashMap<>(migrateFurtherEvidenceDocuments(caseData, OTHER_REPORTS,
            "expertReportList"));
        migrateFurtherEvidenceDocuments(caseData, EXPERT_REPORTS, "expertReportList").forEach((key, value) ->
            mergedMap.merge(key, value, (v1, v2) -> {
                Collection<Element<ManagedDocument>> mergedCollection = new ArrayList<>();
                mergedCollection.addAll((Collection<Element<ManagedDocument>>) v1);
                mergedCollection.addAll((Collection<Element<ManagedDocument>>) v2);
                return mergedCollection;
            }));

        ret.putAll(mergedMap);
        ret.putAll(migrateFurtherEvidenceDocuments(caseData, EXPERT_REPORTS, "drugAndAlcoholReportList"));
        return ret;
    }

    public void rollbackMigrateApplicantWitnessStatements(CaseDetails caseDetails) {
        caseDetails.getData().remove("applicantWitnessStmtList");
        caseDetails.getData().remove("applicantWitnessStmtListLA");
        caseDetails.getData().remove("applicantWitnessStmtListCTSC");
    }

    public void rollbackMigrateGuardianReports(CaseDetails caseDetails) {
        caseDetails.getData().remove("guardianEvidenceList");
        caseDetails.getData().remove("guardianEvidenceListLA");
        caseDetails.getData().remove("guardianEvidenceListCTSC");
    }

    public void rollbackMigrateExpertReports(CaseDetails caseDetails) {
        caseDetails.getData().remove("drugAndAlcoholReportList");
        caseDetails.getData().remove("drugAndAlcoholReportListLA");
        caseDetails.getData().remove("drugAndAlcoholReportListCTSC");
        caseDetails.getData().remove("lettersOfInstructionList");
        caseDetails.getData().remove("lettersOfInstructionListLA");
        caseDetails.getData().remove("lettersOfInstructionListCTSC");
        caseDetails.getData().remove("expertReportList");
        caseDetails.getData().remove("expertReportListLA");
        caseDetails.getData().remove("expertReportListCTSC");
    }

    public void rollbackMigrateNoticeOfActingOrIssue(CaseDetails caseDetails) {
        caseDetails.getData().remove("noticeOfActingOrIssueList");
        caseDetails.getData().remove("noticeOfActingOrIssueListLA");
        caseDetails.getData().remove("noticeOfActingOrIssueListCTSC");
        caseDetails.getData().remove("noticeOfActingOrIssueListRemoved");
    }

    public Map<String, Object> migrateSkeletonArgumentList(CaseData caseData) {
        List<Element<SkeletonArgument>> skeletonArgumentList =
            caseData.getHearingDocuments().getSkeletonArgumentList().stream()
                .filter(skeletonArgument ->
                    YesNo.NO.getValue().equals(skeletonArgument.getValue().getHasConfidentialAddress()))
                .collect(toList());

        List<Element<SkeletonArgument>> skeletonArgumentListLA =
            caseData.getHearingDocuments().getSkeletonArgumentList().stream()
                .filter(skeletonArgument ->
                    YesNo.YES.getValue().equals(skeletonArgument.getValue().getHasConfidentialAddress()))
                .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("skeletonArgumentList", skeletonArgumentList);
        ret.put("skeletonArgumentListLA", skeletonArgumentListLA);
        return ret;
    }

    @SuppressWarnings("unchecked")
    public void rollbackMigratedSkeletonArgumentList(CaseDetails caseDetails) {
        Map<String, Object> caseDataMap = caseDetails.getData();

        List<Element<SkeletonArgument>> skeletonArgumentList = new ArrayList<>();
        if (caseDataMap.get("skeletonArgumentList") != null) {
            skeletonArgumentList.addAll((List) caseDataMap.get("skeletonArgumentList"));
        }
        if (caseDataMap.get("skeletonArgumentListLA") != null) {
            skeletonArgumentList.addAll((List) caseDataMap.get("skeletonArgumentListLA"));
        }

        caseDataMap.put("skeletonArgumentList", skeletonArgumentList);
        caseDataMap.remove("skeletonArgumentListLA");
    }

    private Map<String, Object> migrateApplicationDocuments(CaseData caseData,
                                                            List<ApplicationDocumentType> applicationDocumentTypes,
                                                            String newFieldName) {
        final List<Element<ManagedDocument>> newDocListLA =
            Optional.ofNullable(caseData.getApplicationDocuments()).orElse(List.of()).stream()
                .filter(fed -> applicationDocumentTypes.contains(fed.getValue().getDocumentType()))
                .filter(fed -> fed.getValue().isConfidentialDocument())
                .map(fed -> element(fed.getId(), ManagedDocument.builder()
                    .document(fed.getValue().getDocument())
                    .build()))
                .collect(toList());

        final List<Element<ManagedDocument>> newDocList =
            Optional.ofNullable(caseData.getApplicationDocuments()).orElse(List.of()).stream()
                .filter(fed -> applicationDocumentTypes.contains(fed.getValue().getDocumentType()))
                .filter(fed -> !fed.getValue().isConfidentialDocument())
                .map(fed -> element(fed.getId(), ManagedDocument.builder()
                    .document(fed.getValue().getDocument())
                    .build()))
                .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put(newFieldName, newDocList);
        ret.put(newFieldName + "LA", newDocListLA);
        return ret;
    }

    public Map<String, Object> migrateApplicationDocumentsToThresholdList(CaseData caseData) {
        return migrateApplicationDocuments(caseData, List.of(THRESHOLD), "thresholdList");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> migrateApplicationDocumentsToDocumentsFiledOnIssueList(CaseData caseData) {
        Map<String, Object> ret = new HashMap<>();
        ret.putAll(migrateApplicationDocuments(caseData, List.of(SWET), "swetList"));
        ret.putAll(migrateApplicationDocuments(caseData, List.of(SOCIAL_WORK_CHRONOLOGY), "socialWorkChronList"));
        ret.putAll(migrateApplicationDocuments(caseData, List.of(SOCIAL_WORK_STATEMENT), "otherDocFiledList"));
        ret.putAll(migrateApplicationDocuments(caseData, List.of(GENOGRAM), "genogramList"));
        ret.putAll(migrateApplicationDocuments(caseData, List.of(CHECKLIST_DOCUMENT), "checklistDocList"));
        ret.putAll(migrateApplicationDocuments(caseData, List.of(BIRTH_CERTIFICATE), "birthCertList"));
        if (ret.containsKey("otherDocFiledList") || ret.containsKey("otherDocFiledListLA")
            || ret.containsKey("otherDocFiledListCTSC")) {
            Map<String, Object> temp = migrateApplicationDocuments(caseData, List.of(OTHER), "otherDocFiledList");
            for (String key : temp.keySet()) {
                if (ret.containsKey(key)) {
                    ((List) ret.get(key)).addAll((List) temp.get(key));
                } else {
                    ret.put(key, temp.get(key));
                }
            }
        }
        return ret;
    }

    public Map<String, Object> migrateApplicationDocumentsToCarePlanList(CaseData caseData) {
        return migrateApplicationDocuments(caseData, List.of(CARE_PLAN), "carePlanList");
    }

    public void rollbackApplicationDocuments(CaseDetails caseDetails) {
        caseDetails.getData().remove("thresholdList");
        caseDetails.getData().remove("thresholdListLA");
        caseDetails.getData().remove("documentsFiledOnIssueList");
        caseDetails.getData().remove("documentsFiledOnIssueListLA");
        caseDetails.getData().remove("carePlanList");
        caseDetails.getData().remove("carePlanListLA");
        caseDetails.getData().remove("swetList");
        caseDetails.getData().remove("swetListLA");
        caseDetails.getData().remove("socialWorkChronList");
        caseDetails.getData().remove("socialWorkChronListLA");
        caseDetails.getData().remove("genogramList");
        caseDetails.getData().remove("genogramListLA");
        caseDetails.getData().remove("checklistDocList");
        caseDetails.getData().remove("checklistDocListLA");
        caseDetails.getData().remove("birthCertList");
        caseDetails.getData().remove("birthCertListLA");
        caseDetails.getData().remove("otherDocFiledList");
        caseDetails.getData().remove("otherDocFiledListLA");
    }

    public Map<String, Object> migrateCorrespondenceDocuments(CaseData caseData) {
        List<Element<ManagedDocument>> correspondenceDocList =
            Stream.of(Optional.ofNullable(caseData.getCorrespondenceDocuments()),
                Optional.ofNullable(caseData.getCorrespondenceDocumentsLA()),
                Optional.ofNullable(caseData.getCorrespondenceDocumentsSolicitor()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(Collection::stream)
                .filter(bundleElement -> !bundleElement.getValue().isConfidentialDocument())
                .map(bundleElement ->
                    element(bundleElement.getId(),
                        ManagedDocument.builder().document(bundleElement.getValue().getDocument()).build()))
                .collect(toList());

        List<Element<ManagedDocument>> correspondenceDocListLA =
            Stream.of(Optional.ofNullable(caseData.getCorrespondenceDocumentsLA()),
                Optional.ofNullable(caseData.getCorrespondenceDocumentsSolicitor()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(Collection::stream)
                .filter(bundleElement -> bundleElement.getValue().isConfidentialDocument())
                .map(bundleElement ->
                    element(bundleElement.getId(),
                        ManagedDocument.builder().document(bundleElement.getValue().getDocument()).build()))
                .collect(toList());

        List<Element<ManagedDocument>> correspondenceDocListCTSC =
            Optional.ofNullable(caseData.getCorrespondenceDocuments()).orElse(List.of()).stream()
                .filter(bundleElement -> bundleElement.getValue().isConfidentialDocument())
                .map(bundleElement ->
                    element(bundleElement.getId(),
                        ManagedDocument.builder().document(bundleElement.getValue().getDocument()).build()))
                .collect(toList());

        Map<String, Object> ret = new HashMap<>();
        ret.put("correspondenceDocList", correspondenceDocList);
        ret.put("correspondenceDocListLA", correspondenceDocListLA);
        ret.put("correspondenceDocListCTSC", correspondenceDocListCTSC);
        return ret;
    }

    public void rollbackMigrateCorrespondenceDocuments(CaseDetails caseDetails) {
        caseDetails.getData().remove("correspondenceDocList");
        caseDetails.getData().remove("correspondenceDocListLA");
        caseDetails.getData().remove("correspondenceDocListCTSC");
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
}
