package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.PositionStatementChild;
import uk.gov.hmcts.reform.fpl.model.PositionStatementRespondent;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class MigrateCaseService {

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
        if (nonNull(caseData.getCourt())) {
            if ("544".equals(caseData.getCourt().getCode())) {
                // BHC
                if (nonNull(caseData.getLocalAuthorityPolicy())
                    && nonNull(caseData.getLocalAuthorityPolicy().getOrganisation())
                    && "0F6AZIR".equals(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID())) {
                    return Map.of("court", caseData.getCourt().toBuilder()
                        .code("554")
                        .name("Family Court sitting at Brighton")
                        .build());
                }
                // WSX
                if (nonNull(caseData.getLocalAuthorityPolicy())
                    && nonNull(caseData.getLocalAuthorityPolicy().getOrganisation())
                    && "HLT7S0M".equals(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID())) {
                    return Map.of("court", caseData.getCourt().toBuilder()
                        .code("554")
                        .name("Family Court Sitting at Brighton County Court")
                        .build());
                }
            } else if ("117".equals(caseData.getCourt().getCode())) {
                // BNT
                if (nonNull(caseData.getLocalAuthorityPolicy())
                    && nonNull(caseData.getLocalAuthorityPolicy().getOrganisation())
                    && "SPUL3VV".equals(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID())) {
                    return Map.of("court", caseData.getCourt().toBuilder()
                        .code("332")
                        .name("Family Court Sitting at West London")
                        .build());
                }
                // HRW
                if (nonNull(caseData.getLocalAuthorityPolicy())
                    && nonNull(caseData.getLocalAuthorityPolicy().getOrganisation())
                    && "L3HSA4L".equals(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID())) {
                    return Map.of("court", caseData.getCourt().toBuilder()
                        .code("332")
                        .name("Family Court Sitting at West London")
                        .build());
                }
                // HLW
                if (nonNull(caseData.getLocalAuthorityPolicy())
                    && nonNull(caseData.getLocalAuthorityPolicy().getOrganisation())
                    && "6I4Z3OO".equals(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID())) {
                    return Map.of("court", caseData.getCourt().toBuilder()
                        .code("332")
                        .name("Family Court Sitting at West London")
                        .build());
                }
            } else if ("164".equals(caseData.getCourt().getCode())) {
                // RCT
                if (nonNull(caseData.getLocalAuthorityPolicy())
                    && nonNull(caseData.getLocalAuthorityPolicy().getOrganisation())
                    && "68MNZN8".equals(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID())) {
                    return Map.of("court", caseData.getCourt().toBuilder()
                        .code("159")
                        .name("Family Court sitting at Cardiff")
                        .build());
                }
            } else if ("3403".equals(caseData.getCourt().getCode())) {
                // BAD
                if (nonNull(caseData.getLocalAuthorityPolicy())
                    && nonNull(caseData.getLocalAuthorityPolicy().getOrganisation())
                    && "3FG3URQ".equals(caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID())) {
                    return Map.of("court", caseData.getCourt().toBuilder()
                        .code("121")
                        .name("Family Court Sitting at East London Family Court")
                        .build());
                }
            }
        }
        throw new IllegalStateException(format("It does not match the migration condition. (courtCode = %s, "
                + "localAuthorityPolicy.organisation.organisationID = %s)",
            nonNull(caseData.getCourt()) ? caseData.getCourt().getCode() : "null",
            nonNull(caseData.getLocalAuthorityPolicy())
                ? (nonNull(caseData.getLocalAuthorityPolicy().getOrganisation())
                    ? caseData.getLocalAuthorityPolicy().getOrganisation().getOrganisationID()
                    : "null")
                : "null"));
    }
}
