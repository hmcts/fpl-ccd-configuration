package uk.gov.hmcts.reform.fpl.controllers.support;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.controllers.CallbackController;
import uk.gov.hmcts.reform.fpl.enums.ColleagueRole;
import uk.gov.hmcts.reform.fpl.enums.State;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Colleague;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Solicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.EmailAddress;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.springframework.util.ObjectUtils.isEmpty;
import static uk.gov.hmcts.reform.fpl.enums.ColleagueRole.SOLICITOR;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.Representing.CHILD;
import static uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator.NoticeOfChangeAnswersPopulationStrategy.BLANK;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@Api
@RestController
@RequestMapping("/callback/migrate-case")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class MigrateCaseController extends CallbackController {
    private static final String MIGRATION_ID_KEY = "migrationId";
    private static final List<State> IGNORED_STATES = List.of(State.OPEN, State.RETURNED, State.CLOSED, State.DELETED);
    private static final int MAX_CHILDREN = 15;

    private final NoticeOfChangeFieldPopulator populator;
    private final Map<String, Consumer<CaseDetails>> migrations = Map.of(
        "FPLA-3132", this::run3132,
        "FPLA-3238", this::run3238
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

    private void run3132(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);
        Long id = caseData.getId();
        State state = caseData.getState();
        if (IGNORED_STATES.contains(state)) {
            throw new AssertionError(format(
                "Migration {id = FPLA-3132, case reference = %s} not migrating when state = %s", id, state
            ));
        }
        int numChildren = caseData.getAllChildren().size();
        if (MAX_CHILDREN < numChildren) {
            throw new AssertionError(format(
                "Migration {id = FPLA-3132, case reference = %s} not migrating when number of children = %d (max = %d)",
                id, numChildren, MAX_CHILDREN
            ));
        }

        caseDetails.getData().putAll(populator.generate(caseData, CHILD, BLANK));
    }

    private void run3238(CaseDetails caseDetails) {
        CaseData caseData = getCaseData(caseDetails);

        if (isEmpty(caseData.getLocalAuthorities())) {
            Optional<LocalAuthority> designatedLocalAuthority = migrateFromLegacyApplicant(caseData);

            if (designatedLocalAuthority.isPresent()) {
                caseDetails.getData().put("localAuthorities", wrapElements(designatedLocalAuthority.get()));
                log.info("Migration 3238. Case {} migrated", caseDetails.getId());
            } else {
                log.warn("Migration 3238. Could not find designated local authority for case {}", caseDetails.getId());
            }
        } else {
            log.warn("Migration 3238. Case {} already have local authority. Migration skipped", caseDetails.getId());
        }
    }

    private Optional<LocalAuthority> migrateFromLegacyApplicant(CaseData caseData) {

        if (isEmpty(caseData.getAllApplicants())) {
            log.warn("Migration 3238. Case {} does not have legacy applicant", caseData.getId());
            return empty();
        }

        final Optional<String> designatedOrgId = ofNullable(caseData.getLocalAuthorityPolicy())
            .map(OrganisationPolicy::getOrganisation)
            .map(uk.gov.hmcts.reform.ccd.model.Organisation::getOrganisationID);

        if (designatedOrgId.isEmpty()) {
            log.warn("Migration 3238. Case {} does not have organisation policy", caseData.getId());
            return empty();
        }

        final Optional<Applicant> legacyApplicant = ofNullable(caseData.getAllApplicants().get(0).getValue());
        final Optional<Solicitor> legacySolicitor = ofNullable(caseData.getSolicitor());


        if (legacyApplicant.isEmpty()) {
            log.warn("Migration 3238. Case {} does not have legacy applicant", caseData.getId());
        }

        if (legacySolicitor.isEmpty()) {
            log.warn("Migration 3238. Case {} does not have legacy solicitor", caseData.getId());
        }

        return legacyApplicant
            .map(Applicant::getParty)
            .map(party -> LocalAuthority.builder()
                .id(designatedOrgId.get())
                .designated(YesNo.YES.getValue())
                .name(party.getOrganisationName())
                .email(ofNullable(party.getEmail()).map(EmailAddress::getEmail).orElse(null))
                .pbaNumber(party.getPbaNumber())
                .customerReference(party.getCustomerReference())
                .clientCode(party.getClientCode())
                .legalTeamManager(party.getLegalTeamManager())
                .phone(ofNullable(party.getTelephoneNumber())
                    .map(Telephone::getTelephoneNumber)
                    .orElse(ofNullable(party.getMobileNumber())
                        .map((Telephone::getTelephoneNumber))
                        .orElse(null)))
                .address(party.getAddress())
                .colleagues(migrateColleagues(party, legacySolicitor.orElse(null)))
                .build());
    }

    private List<Element<Colleague>> migrateColleagues(ApplicantParty applicantParty, Solicitor solicitor) {
        final Optional<Colleague> solicitorColleague = migrateFromLegacySolicitor(solicitor);
        final Optional<Colleague> contactColleague = migrateFromApplicant(applicantParty);

        if (contactColleague.isEmpty() && solicitorColleague.isPresent()) {
            solicitorColleague.get().setMainContact("Yes");
        }

        final List<Colleague> colleagues = new ArrayList<>();

        contactColleague.ifPresent(colleagues::add);
        solicitorColleague.ifPresent(colleagues::add);

        return wrapElements(colleagues);
    }

    private Optional<Colleague> migrateFromApplicant(ApplicantParty applicantParty) {
        final Optional<String> mobile = ofNullable(applicantParty.getMobileNumber())
            .map(Telephone::getTelephoneNumber)
            .filter(StringUtils::isNotBlank);

        final Optional<String> phone = ofNullable(applicantParty.getTelephoneNumber())
            .map(Telephone::getTelephoneNumber)
            .filter(StringUtils::isNotBlank);

        final Optional<String> colleagueName = ofNullable(applicantParty.getTelephoneNumber())
            .map(Telephone::getContactDirection)
            .filter(StringUtils::isNotBlank);

        final Optional<String> colleagueTitle = ofNullable(applicantParty.getJobTitle())
            .filter(StringUtils::isNotBlank);

        final Optional<String> colleagueEmail = ofNullable(applicantParty.getEmail())
            .map(EmailAddress::getEmail)
            .filter(StringUtils::isNotBlank);


        if (mobile.isPresent() || phone.isPresent() || colleagueName.isPresent() || colleagueTitle.isPresent()
            || colleagueEmail.isPresent()) {
            return Optional.of(Colleague.builder()
                .role(ColleagueRole.OTHER)
                .title(colleagueTitle.orElse(null))
                .fullName(colleagueName.orElse(null))
                .email(colleagueEmail.orElse(null))
                .phone(phone.orElse(mobile.orElse(null)))
                .notificationRecipient("Yes")
                .mainContact("Yes")
                .build());
        }

        return Optional.empty();
    }

    private Optional<Colleague> migrateFromLegacySolicitor(Solicitor solicitor) {

        return ofNullable(solicitor).map(sol -> Colleague.builder()
            .role(SOLICITOR)
            .fullName(sol.getName())
            .email(sol.getEmail())
            .phone(Stream.of(sol.getTelephone(), sol.getMobile())
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(null))
            .dx(sol.getDx())
            .reference(sol.getReference())
            .notificationRecipient(YesNo.YES.getValue())
            .mainContact("No")
            .build());
    }
}
