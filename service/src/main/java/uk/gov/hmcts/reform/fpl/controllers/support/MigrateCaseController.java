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
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CourtBundle;
import uk.gov.hmcts.reform.fpl.model.HearingCourtBundle;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.TaskListRenderer;
import uk.gov.hmcts.reform.fpl.service.TaskListService;
import uk.gov.hmcts.reform.fpl.service.document.DocumentListService;
import uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.reform.fpl.service.validators.CaseSubmissionChecker;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.List.of;
import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private static final List<State> IGNORED_STATES = of(State.OPEN, State.RETURNED, State.CLOSED, State.DELETED);
    private static final int MAX_CHILDREN = 15;

    private final TaskListService taskListService;
    private final TaskListRenderer taskListRenderer;
    private final CaseSubmissionChecker caseSubmissionChecker;

    private final NoticeOfChangeFieldPopulator populator;
    private final DocumentListService documentListService;
    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "DFPL-465", this::run465,
        "DFPL-82", this::run82
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
        List<Element<CourtBundle>> oldCourBundles = caseData.getCourtBundleList();

        Map<String, Object> caseDetailsData = caseDetails.getData();
        if (oldCourBundles != null) {
            log.info("Migration {id = DFPL-82, case reference = {}} courtbundles start", caseId);
            Map<String, List<Element<CourtBundle>>> courtBundles = oldCourBundles.stream()
                .map(Element::getValue)
                .collect(
                    groupingBy(CourtBundle::getHearing,
                        mapping(data -> Element.<CourtBundle>builder().value(data).build(),
                            toList()))
                );

            List<Element<HearingCourtBundle>> hearingBundles = courtBundles.entrySet().stream()
                .map(entry -> {
                        HearingCourtBundle hearingCourtBundle = HearingCourtBundle.builder()
                            .hearing(entry.getKey())
                            .courtBundle(entry.getValue())
                            .courtBundleNC(entry.getValue().stream()
                                .filter(e -> !e.getValue().isConfidentialDocument())
                                .collect(toList())
                            )
                            .build();
                        return Element.<HearingCourtBundle>builder().value(hearingCourtBundle).build();
                    }
                ).collect(toList());

            caseDetailsData.remove("courtBundleList");
            caseDetailsData.put("courtBundleListV2", hearingBundles);
            log.info("Migration {id = DFPL-82, case reference = {}} courtbundles finish", caseId);
        } else {
            log.warn("Migration {id = DFPL-82, case reference = {}, case state = {}} doesn't have court bundles ",
                caseId, caseData.getState().getValue());
        }
    }

    private void run465(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        var caseId = caseData.getId();

        if (caseId != 1639997900244470L) {
            throw new AssertionError(format(
                "Migration {id = DFPL-465, case reference = %s}, expected case id 1639997900244470",
                caseId
            ));
        }
        List<String> namesToRemove = of("Stacey Halbert", "Della Phillips", "Natalie Beardsmore", "Donna Bird");

        List<LegalRepresentative> legalRepresentatives = unwrapElements(caseData.getLegalRepresentatives());

        List<LegalRepresentative> legalRepresentativesToRemain = legalRepresentatives.stream()
                .filter(not(legalRepresentative -> namesToRemove.contains(legalRepresentative.getFullName())))
                .collect(Collectors.toList());

        List<Element<LegalRepresentative>> filteredLegalRepresentatives = wrapElements(legalRepresentativesToRemain);

        caseDetails.getData().put("legalRepresentatives", filteredLegalRepresentatives);
    }
}
