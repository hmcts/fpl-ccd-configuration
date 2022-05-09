package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.SentDocument;
import uk.gov.hmcts.reform.fpl.model.SentDocuments;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-500", this::run500,
        "DFPL-451", this::run451,
        "DFPL-482", this::run482,
        "DFPL-82", this::run82,
        "DFPL-82-rollback", this::run82Rollback,
        "DFPL-572", this::run572,
        "DFPL-635", this::run635
    );

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        String migrationId = (String) caseDetails.getData().get(MIGRATION_ID_KEY);
        Long id = caseDetails.getId();

        log.info("Migration {id = {}, case reference = {}} started", migrationId, id);

        if (!migrations.containsKey(migrationId)) {
            throw new NoSuchElementException("No migration mapped to " + migrationId);
        }

        migrations.get(migrationId).accept(caseDetails);

        log.info("Migration {id = {}, case reference = {}} finished", migrationId, id);

        caseDetails.getData().remove(MIGRATION_ID_KEY);
        return respond(caseDetails);
    }

    private void run82(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        List<Element<CourtBundle>> oldCourtBundles = caseData.getCourtBundleList();

        Map<String, Object> caseDetailsData = caseDetails.getData();
        if (isNotEmpty(oldCourtBundles)) {
            log.info("Migration {id = DFPL-82, case reference = {}} courtbundles start", caseId);

            Map<UUID, List<Element<CourtBundle>>> courtBundles = oldCourtBundles.stream()
                .collect(
                    groupingBy(Element::getId,
                        mapping(data -> element(data.getId(), data.getValue()),
                            toList()))
                );

            List<Element<HearingCourtBundle>> hearingBundles = courtBundles.entrySet().stream()
                .map(entry -> {
                        String hearing = entry.getValue().stream().findFirst()
                            .orElse(element(CourtBundle.builder().build())).getValue().getHearing();

                        HearingCourtBundle hearingCourtBundle = HearingCourtBundle.builder()
                            .hearing(hearing)
                            .courtBundle(entry.getValue())
                            .courtBundleNC(entry.getValue()) //existing bundles marked as not confidential by default
                            .build();
                        return element(entry.getKey(), hearingCourtBundle);
                    }
                ).collect(toList());

            caseDetailsData.remove("courtBundleList");
            caseDetailsData.put("courtBundleListV2", hearingBundles);
            log.info("Migration {id = DFPL-82, case reference = {}} courtbundles finish", caseId);
        } else {
            log.warn("Migration {id = DFPL-82, case reference = {}, case staøte = {}} doesn't have court bundles ",
                caseId, caseData.getState().getValue());
        }
    }

    private void run82Rollback(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        List<Element<HearingCourtBundle>> newCourtBundles = caseData.getCourtBundleListV2();

        Map<String, Object> caseDetailsData = caseDetails.getData();
        if (isNotEmpty(newCourtBundles)) {
            log.info("Migration {id = DFPL-82-Rollback, case reference = {}} courtbundles start", caseId);

            List<Element<CourtBundle>> courtBundles = newCourtBundles.stream()
                .map(element -> element.getValue().getCourtBundle().stream()
                        .map(bundle -> element(element.getId(), bundle.getValue()))
                        .collect(toList())
                )
                .flatMap(Collection::stream)
                .collect(toList());

            caseDetailsData.remove("courtBundleListV2");
            caseDetailsData.put("courtBundleList", courtBundles);
            log.info("Migration {id = DFPL-82-rollback, case reference = {}} courtbundles finish", caseId);
        } else {
            log.warn("Migration {id = DFPL-82-rollback, case reference = {}, case state = {}} doesn't have hearing"
                    +
                    " court bundles ",
                caseId, caseData.getState().getValue());
        }
    }

    private void run451(CaseDetails caseDetails) {
        var casesWithHearingOption = List.of(
            1603370139459131L, 1618403849028418L, 1592492643062277L, 1615809514849016L, 1605537316992153L);

        var caseId = caseDetails.getId();
        if (!casesWithHearingOption.contains(caseId)) {
            throw new AssertionError(
                format("Migration {id = DFPL-451, case reference = %s}, Unexpected case reference", caseId));
        }

        if (isNotEmpty(caseDetails.getData().get("hearingOption"))) {
            caseDetails.getData().remove("hearingOption");
        } else {
            throw new IllegalStateException(format("Case %s does not have hearing option", caseId));
        }
    }

    private void run500(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        List<UUID> docIds = List.of(UUID.fromString("ad5c738e-d7aa-4ccf-b53b-0b1e40a19182"),
                UUID.fromString("61f97374-360b-4759-9329-af10fae1317e"));

        if (caseId != 1643728359576136L) {
            throw new AssertionError(format(
                    "Migration {id = DFPL-500, case reference = %s}, expected case id 1643728359576136",
                    caseId
            ));
        }

        updateDocumentsSentToParties(caseDetails, caseData, docIds);
    }

    private void run482(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        var expectedCaseId = 1636970654155393L;
        List<UUID> docIds = List.of(UUID.fromString("75dcdc34-7f13-4c56-aad6-8dcf7b2261b6"),
                UUID.fromString("401d9cd0-50ae-469d-b355-d467742d7ef3"));

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                    "Migration {id = DFPL-482, case reference = %s}, expected case id %d",
                    caseId, expectedCaseId
            ));
        }

        updateDocumentsSentToParties(caseDetails, caseData, docIds);
    }

    /**
     * Removes a C110A Generated PDF document from the case.
     * Make sure to update:
     *  - expectedCaseId
     *  - expectedDocId
     *  - migrationId
     * @param caseDetails - the caseDetails to update
     */
    private void run635(CaseDetails caseDetails) {
        var migrationId = "DFPL-635";
        var expectedCaseId = 1642758673379744L;
        var expectedDocId = UUID.fromString("9f0d570a-2cb8-48eb-90cb-3d4f26a2350a");

        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected case id %d",
                migrationId, caseId, expectedCaseId
            ));
        }

        var documentUrl = caseData.getC110A().getDocument().getUrl();
        var docId = UUID.fromString(documentUrl.substring(documentUrl.length() - 36));
        if (!docId.equals(expectedDocId)) {
            throw new AssertionError(format(
                "Migration {id = %s, case reference = %s}, expected c110a document id %s",
                migrationId, caseId, expectedDocId
            ));
        }
        caseDetails.getData().put("submittedForm", null);
    }

    private void run572(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();
        var expectedCaseId = 1646391317671957L;
        var expectedDocId = UUID.fromString("0d30f8e4-cf44-47f6-ab1b-7fc11fdc34a8");

        if (caseId != expectedCaseId) {
            throw new AssertionError(format(
                "Migration {id = DFPL-572, case reference = %s}, expected case id %d",
                caseId, expectedCaseId
            ));
        }

        var documentUrl = caseData.getUrgentHearingOrder().getDocument().getUrl();
        var docId = UUID.fromString(documentUrl.substring(documentUrl.length() - 36));
        if (!docId.equals(expectedDocId)) {
            throw new AssertionError(format(
                "Migration {id = DFPL-572, case reference = %s}, expected urgent hearing order document id %s",
                caseId, expectedDocId
            ));
        }

        caseDetails.getData().put("urgentHearingOrder", null);
    }

    private void updateDocumentsSentToParties(CaseDetails caseDetails, CaseData caseData, List<UUID> docIds) {
        List<Element<SentDocuments>> sentDocuments = caseData.getDocumentsSentToParties();

        for (Element<SentDocuments> docsSentToParties : sentDocuments) {
            List<Element<SentDocument>> filteredList =
                getDocToRemove(docsSentToParties.getValue().getDocumentsSentToParty(),
                    docIds);
            docsSentToParties.getValue().getDocumentsSentToParty().removeAll(filteredList);
        }

        caseDetails.getData().put("documentsSentToParties", sentDocuments);
    }

    private List<Element<SentDocument>> getDocToRemove(List<Element<SentDocument>> sentDocument,
                                                       List<UUID> docIds) {
        return sentDocument.stream()
                .filter(element -> docIds.contains(element.getId()))
                .collect(Collectors.toList());
    }

}
