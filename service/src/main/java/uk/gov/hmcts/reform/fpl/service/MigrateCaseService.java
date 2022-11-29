package uk.gov.hmcts.reform.fpl.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.fpl.model.CaseData;
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
}
