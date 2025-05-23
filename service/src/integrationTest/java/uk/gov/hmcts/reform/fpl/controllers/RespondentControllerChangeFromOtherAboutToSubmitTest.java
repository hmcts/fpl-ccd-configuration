package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.IsAddressKnowType;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.service.CaseService;
import uk.gov.hmcts.reform.fpl.service.ConfidentialDetailsService;
import uk.gov.hmcts.reform.fpl.service.NoticeOfChangeService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;
import uk.gov.hmcts.reform.fpl.service.OthersService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeCaseRoleService;
import uk.gov.hmcts.reform.fpl.service.RepresentativeService;
import uk.gov.hmcts.reform.fpl.service.RespondentAfterSubmissionRepresentationService;
import uk.gov.hmcts.reform.fpl.service.RespondentService;
import uk.gov.hmcts.reform.fpl.service.UserService;
import uk.gov.hmcts.reform.fpl.service.ValidateEmailService;
import uk.gov.hmcts.reform.fpl.service.legalcounsel.RepresentableLegalCounselUpdater;
import uk.gov.hmcts.reform.fpl.service.noc.NoticeOfChangeFieldPopulator;
import uk.gov.hmcts.reform.fpl.service.others.OthersListGenerator;
import uk.gov.hmcts.reform.fpl.service.representative.ChangeOfRepresentationService;
import uk.gov.hmcts.reform.fpl.service.respondent.RespondentValidator;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.buildHiddenAddress;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.localAuthorities;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.otherToRespondentEventData;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareConfidentialOthersFromAllOthers;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareConfidentialOthersTestingData;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareConfidentialRespondentsFromRespondents1;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareExpectedExistingConfidentialRespondent;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareExpectedTransformedConfidentialRespondent;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareExpectedTransformedRespondent;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareOtherRepresentative;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareOthersTestingData;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareRespondentsTestingData;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareSingleConfidentialOther;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareTransformedRespondentTestingData;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.resolveOtherRepresentativeRole;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.resolveRespondentRepresentativeRole;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithRandomUUID;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
@Import({OthersService.class, ConfidentialDetailsService.class, RespondentService.class,
    RespondentAfterSubmissionRepresentationService.class, RepresentableLegalCounselUpdater.class,
    RepresentativeService.class})
class RespondentControllerChangeFromOtherAboutToSubmitTest extends AbstractCallbackTest {

    @MockBean
    private RequestData requestData;

    @MockBean
    private OthersListGenerator othersListGenerator;

    @MockBean
    private CaseService caseService;

    @MockBean
    private RepresentativeCaseRoleService representativeCaseRoleService;

    @MockBean
    private ValidateEmailService validateEmailService;

    @MockBean
    private ChangeOfRepresentationService changeOfRepresentationService;

    @MockBean
    private NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator;

    @MockBean
    private RespondentValidator respondentValidator;

    @MockBean
    private NoticeOfChangeService noticeOfChangeService;

    @MockBean
    private OrganisationService organisationService;

    @MockBean
    private UserService userService;

    private static final int SELECTED_OTHER = 0;
    private static final int NUM_ADDITIONAL_OTHERS = 3;
    private static final int NUM_RESPONDENTS = 2;

    RespondentControllerChangeFromOtherAboutToSubmitTest() {
        super("enter-respondents/change-from-other");
    }

    @BeforeEach
    void before() {
        given(requestData.userRoles()).willReturn(Set.of(UserRole.HMCTS_ADMIN.getRoleName()));
    }

    @WithMockUser
    @Test
    void shouldConvertOthersToRespondent() {
        Others others = prepareOthersTestingData(NUM_ADDITIONAL_OTHERS, false, false);
        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);

        CaseData caseData = CaseData.builder()
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(wrapElementsWithRandomUUID(respondents))
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(NUM_ADDITIONAL_OTHERS);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", SELECTED_OTHER).equals(o.getValue().getName()))).isEmpty();
        assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
            Other.builder()
                .detailsHidden("No")
                .name(String.format("Marco %s", 1))
                .build());

        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS)).contains(transformedRespondent);
    }

    @WithMockUser
    @Test
    void shouldConvertOthersWithHiddenDetailsToRespondentWhereNoConfidentialRespondent() {
        Others others = prepareOthersTestingData(NUM_ADDITIONAL_OTHERS, false,
            (i) -> (SELECTED_OTHER - 1) == i);
        Other firstOther = others.getFirstOther();
        List<Element<Other>> additionalOthers = others.getAdditionalOthers();
        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        CaseData caseData = CaseData.builder()
            .confidentialOthers(prepareSingleConfidentialOther(SELECTED_OTHER, firstOther, additionalOthers))
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(wrapElementsWithRandomUUID(respondents))
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(NUM_ADDITIONAL_OTHERS);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", SELECTED_OTHER).equals(o.getValue().getName()))).isEmpty();
        assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
            Other.builder()
                .name(String.format("Marco %s", 1))
                .detailsHidden("No")
                .build());
        assertThat(responseCaseData.getConfidentialOthers()).hasSize(0);
        assertThat(responseCaseData.getConfidentialRespondents()).hasSize(1);
        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS)).contains(
            prepareExpectedTransformedRespondent(true));

        UUID targetUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(NUM_RESPONDENTS).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> targetUUID.equals(e.getId())).map(Element::getValue).findFirst())
            .contains(prepareExpectedTransformedConfidentialRespondent());
    }

    @WithMockUser
    @Test
    void shouldConvertConfidentialOthersToConfidentialRespondentAndNoMoreConfidentialOthers() {
        Others others = prepareOthersTestingData(NUM_ADDITIONAL_OTHERS, SELECTED_OTHER == 0,
            (i) -> (SELECTED_OTHER - 1) == i);
        Other firstOther = others.getFirstOther();
        List<Element<Other>> additionalOthers = others.getAdditionalOthers();

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS, true);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .confidentialRespondents(prepareConfidentialRespondentsFromRespondents1(respondents1))
            .confidentialOthers(prepareSingleConfidentialOther(SELECTED_OTHER, firstOther, additionalOthers))
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(NUM_ADDITIONAL_OTHERS);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", SELECTED_OTHER).equals(o.getValue().getName()))).isEmpty();
        assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
            Other.builder()
                .name(String.format("Marco %s", 1))
                .detailsHidden("No")
                .build());
        assertThat(responseCaseData.getConfidentialOthers()).hasSize(0);
        assertThat(responseCaseData.getConfidentialRespondents()).hasSize(NUM_RESPONDENTS + 1);
        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS)).contains(
            prepareExpectedTransformedRespondent(true));
        // Check converted respondent's confidential address
        UUID targetUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(NUM_RESPONDENTS).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> targetUUID.equals(e.getId()))
            .map(Element::getValue)
            .findFirst())
            .contains(prepareExpectedTransformedConfidentialRespondent());
        // Check existing respondent address
        UUID prevExistingRespondentUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(NUM_RESPONDENTS - 1).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> prevExistingRespondentUUID.equals(e.getId()))
            .map(Element::getValue)
            .findFirst())
            .contains(prepareExpectedExistingConfidentialRespondent(NUM_RESPONDENTS - 1));
    }

    @WithMockUser
    @Test
    void shouldConvertConfidentialOthersToConfidentialRespondentAndRetainConfidentialOthers() {
        boolean firstOtherDetailsHidden = true;
        boolean additionalOtherDetailsHidden = true;
        Others others = prepareOthersTestingData(NUM_ADDITIONAL_OTHERS, firstOtherDetailsHidden,
            additionalOtherDetailsHidden);
        List<Element<Other>> allOthers = new ArrayList<>();
        allOthers.add(element(others.getFirstOther()));
        allOthers.addAll(others.getAdditionalOthers());

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS, true);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .confidentialRespondents(prepareConfidentialRespondentsFromRespondents1(respondents1))
            .confidentialOthers(prepareConfidentialOthersFromAllOthers(allOthers))
            .localAuthorities(localAuthorities())
            .respondents1(respondents1)
            .others(others)
            .confidentialOthers(prepareConfidentialOthersTestingData(others, firstOtherDetailsHidden,
                (i) -> additionalOtherDetailsHidden))
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(NUM_ADDITIONAL_OTHERS);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", SELECTED_OTHER).equals(o.getValue().getName()))).isEmpty();
        assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
            Other.builder()
                .name(String.format("Marco %s", 1))
                .detailsHidden("Yes")
                .build());
        assertThat(responseCaseData.getConfidentialOthers()).hasSize(NUM_ADDITIONAL_OTHERS);
        assertThat(responseCaseData.getConfidentialRespondents()).hasSize(NUM_RESPONDENTS + 1);
        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS)).contains(
            prepareExpectedTransformedRespondent(true));
        // Check converted respondent's confidential address
        UUID targetUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(NUM_RESPONDENTS).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> targetUUID.equals(e.getId())).map(Element::getValue).findFirst())
            .contains(prepareExpectedTransformedConfidentialRespondent());
        // Check existing respondent address
        UUID prevExistingRespondentUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(NUM_RESPONDENTS - 1).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> prevExistingRespondentUUID.equals(e.getId()))
            .map(Element::getValue)
            .findFirst())
            .contains(prepareExpectedExistingConfidentialRespondent(NUM_RESPONDENTS - 1));
    }

    @WithMockUser
    @Test
    void shouldConvertOthersWithRepresentativeToRespondent() {
        Others others = prepareOthersTestingData(NUM_ADDITIONAL_OTHERS, false, false);
        Element<Representative> representativeForOther = element(prepareOtherRepresentative(SELECTED_OTHER));
        if (SELECTED_OTHER == 0) {
            others.getFirstOther().addRepresentative(representativeForOther.getId());
        } else {
            others.getAdditionalOthers().get(SELECTED_OTHER - 1).getValue()
                .addRepresentative(representativeForOther.getId());
        }

        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);
        transformedRespondent.addRepresentative(representativeForOther.getId());

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .representatives(List.of(representativeForOther))
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(NUM_ADDITIONAL_OTHERS);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", SELECTED_OTHER).equals(o.getValue().getName()))).isEmpty();
        assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
            Other.builder()
                .name(String.format("Marco %s", 1))
                .detailsHidden("No")
                .build());
        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getParty))
            .contains(prepareExpectedTransformedRespondent(false).getParty());
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getRepresentedBy).map(
            ElementUtils::unwrapElements).orElse(List.of())).isEqualTo(List.of(representativeForOther.getId()));
        assertThat(responseCaseData.getRepresentatives()).hasSize(1);
        assertThat(unwrapElements(responseCaseData.getRepresentatives()).stream()
            .map(Representative::getRole).findFirst())
            .contains(resolveRespondentRepresentativeRole(NUM_RESPONDENTS + 1));
    }

    @WithMockUser
    @Test
    void shouldConvertOthersWithRepresentativesToRespondent() {
        Others others = prepareOthersTestingData(NUM_ADDITIONAL_OTHERS, false, false);
        List<Element<Representative>> representatives = new ArrayList<>();
        List<UUID> representativeIdsInTransformedRespondent = new ArrayList<>();
        for (int i = 0; i < NUM_ADDITIONAL_OTHERS + 1; i++) {
            Element<Representative> representativeElement = element(prepareOtherRepresentative(i));
            if (i == 0) {
                others.getFirstOther().addRepresentative(representativeElement.getId());
            } else {
                others.getAdditionalOthers().get(i - 1).getValue().addRepresentative(representativeElement.getId());
            }
            if (i == SELECTED_OTHER) {
                representativeIdsInTransformedRespondent.add(representativeElement.getId());
            }
            representatives.add(representativeElement);
        }

        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);
        representativeIdsInTransformedRespondent.forEach(uuid -> transformedRespondent.addRepresentative(uuid));

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .representatives(representatives)
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(NUM_ADDITIONAL_OTHERS);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", SELECTED_OTHER).equals(o.getValue().getName()))).isEmpty();
        Other expectedFirstOther = Other.builder()
            .name(String.format("Marco %s", 1))
            .detailsHidden("No")
            .build();

        expectedFirstOther.getRepresentedBy().addAll(
            caseData.getOthers().getAdditionalOthers().get(0).getValue().getRepresentedBy());

        assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(expectedFirstOther);

        // verify the respondent count should be increased by 1
        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        // verify respondent content was transferred successfully
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getParty))
            .contains(prepareExpectedTransformedRespondent(false).getParty());
        // verify if the transformed respondent's representative ids are migrated to respondent's representedBy
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getRepresentedBy).map(
            ElementUtils::unwrapElements).orElse(List.of())).isEqualTo(representativeIdsInTransformedRespondent);
        // verify if the representative's count = other's count
        assertThat(responseCaseData.getRepresentatives()).hasSize(NUM_ADDITIONAL_OTHERS + 1);
        // verify the new respondent's roles
        representativeIdsInTransformedRespondent.forEach(representativeId -> {
            assertThat(
                ElementUtils.findElement(representativeId, responseCaseData.getRepresentatives())
                    .map(Element::getValue)
                    .map(Representative::getRole)
                    .stream().collect(toSet())
            ).isEqualTo(Set.of(resolveRespondentRepresentativeRole(NUM_RESPONDENTS + 1)));
        });
        // verify the non-affected other's representatives
        List<Element<Other>> responseAllOthers = responseCaseData.getAllOthers();
        for (int i = 0; i < responseAllOthers.size(); i++) {
            final int finalI = i;
            assertThat(unwrapElements(responseAllOthers).get(i).getRepresentedBy()).isNotEmpty();
            unwrapElements(responseAllOthers).get(i).getRepresentedBy()
                .stream().map(Element::getValue).collect(toSet()).forEach(
                    representativeId -> {
                        assertThat(
                            ElementUtils.findElement(representativeId, responseCaseData.getRepresentatives())
                                .map(Element::getValue)
                                .map(Representative::getRole)
                                .stream().collect(toSet())
                        ).isEqualTo(Set.of(resolveOtherRepresentativeRole(finalI)));
                    });
        }
    }

    @WithMockUser
    @Test
    void shouldConvertConfidentialOthersWithRepresentativesToRespondent() {
        boolean firstOtherDetailsHidden = true;
        boolean additionalOtherDetailsHidden = true;
        Others others = prepareOthersTestingData(NUM_ADDITIONAL_OTHERS, firstOtherDetailsHidden,
            additionalOtherDetailsHidden);
        List<Element<Representative>> representatives = new ArrayList<>();
        List<UUID> representativeIdsInTransformedRespondent = new ArrayList<>();
        for (int i = 0; i < NUM_ADDITIONAL_OTHERS + 1; i++) {
            Element<Representative> representativeElement = element(prepareOtherRepresentative(i));
            if (i == 0) {
                others.getFirstOther().addRepresentative(representativeElement.getId());
            } else {
                others.getAdditionalOthers().get(i - 1).getValue().addRepresentative(representativeElement.getId());
            }
            if (i == SELECTED_OTHER) {
                representativeIdsInTransformedRespondent.add(representativeElement.getId());
            }
            representatives.add(representativeElement);
        }

        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);
        representativeIdsInTransformedRespondent.forEach(uuid -> transformedRespondent.addRepresentative(uuid));

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .representatives(representatives)
            .localAuthorities(localAuthorities())
            .others(others)
            .confidentialOthers(prepareConfidentialOthersTestingData(others, firstOtherDetailsHidden,
                (i) -> additionalOtherDetailsHidden))
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(NUM_ADDITIONAL_OTHERS);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", SELECTED_OTHER).equals(o.getValue().getName()))).isEmpty();

        Other expectedFirstOther = Other.builder()
            .name(String.format("Marco %s", SELECTED_OTHER == 0 ? 1 : 0))
            .detailsHidden("Yes")
            .build();
        expectedFirstOther.getRepresentedBy().addAll(
            caseData.getOthers().getAdditionalOthers().get(0).getValue().getRepresentedBy());
        assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(expectedFirstOther);

        // verify the respondent count should be increased by 1
        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        // verify respondent content was transferred successfully
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getParty))
            .contains(prepareExpectedTransformedRespondent(true).getParty());
        // verify if the transformed respondent's representative ids are migrated to respondent's representedBy
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getRepresentedBy).map(
            ElementUtils::unwrapElements).orElse(List.of())).isEqualTo(representativeIdsInTransformedRespondent);
        // verify if the representative's count = other's count
        assertThat(responseCaseData.getRepresentatives()).hasSize(NUM_ADDITIONAL_OTHERS + 1);
        // verify the new respondent's roles
        representativeIdsInTransformedRespondent.forEach(representativeId -> {
            assertThat(
                ElementUtils.findElement(representativeId, responseCaseData.getRepresentatives())
                    .map(Element::getValue)
                    .map(Representative::getRole)
                    .stream().collect(toSet())
            ).isEqualTo(Set.of(resolveRespondentRepresentativeRole(NUM_RESPONDENTS + 1)));
        });
        // verify the non-affected other's representatives
        List<Element<Other>> responseAllOthers = responseCaseData.getAllOthers();
        for (int i = 0; i < responseAllOthers.size(); i++) {
            final int finalI = i;
            assertThat(unwrapElements(responseAllOthers).get(i).getRepresentedBy()).isNotEmpty();
            unwrapElements(responseAllOthers).get(i).getRepresentedBy()
                .stream().map(Element::getValue).collect(toSet()).forEach(
                    representativeId -> {
                        assertThat(
                            ElementUtils.findElement(representativeId, responseCaseData.getRepresentatives())
                                .map(Element::getValue)
                                .map(Representative::getRole)
                                .stream().collect(toSet())
                        ).isEqualTo(Set.of(resolveOtherRepresentativeRole(finalI)));
                    });
        }
    }

    @Test
    void shouldConvertRefugeOtherToRefugeRespondentAndKeepHidingAddressTelephone() {
        Others others = Others.builder()
            .firstOther(Other.builder()
                .name("Johnny")
                .build())
            .build();
        Respondent transformedRespondent = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(Telephone.builder().telephoneNumber("123456789").build())
                .address(buildHiddenAddress("Converting"))
                .addressKnow(IsAddressKnowType.LIVE_IN_REFUGE)
                .contactDetailsHidden(YesNo.YES.getValue())
                .build())
            .legalRepresentation("No")
            .build();

        UUID refugeOtherId = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .confidentialOthers(List.of(element(refugeOtherId, Other.builder()
                .name("Johnny")
                .addressKnowV2(IsAddressKnowType.LIVE_IN_REFUGE)
                .address(Address.builder().build())
                .build())))
            .others(others)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);


        assertThat(responseCaseData.getConfidentialRespondents().get(0).getValue().getParty())
            .extracting("hideAddress", "hideTelephone").containsExactly("Yes", "Yes");
        assertThat(responseCaseData.getAllRespondents().get(0).getValue().getParty().getAddress())
            .isNull();
        assertThat(responseCaseData.getAllRespondents().get(0).getValue().getParty().getTelephoneNumber())
            .isNull();
    }

}
