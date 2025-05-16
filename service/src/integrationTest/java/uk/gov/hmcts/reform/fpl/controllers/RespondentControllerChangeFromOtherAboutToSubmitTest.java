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
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareConfidentialOthers;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareConfidentialRespondentsFromRespondents1;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareExpectedExistingConfidentialRespondent;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareExpectedTransformedConfidentialRespondent;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareExpectedTransformedRespondent;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareOtherRepresentative;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareOthers;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareRespondentsTestingData;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.prepareTransformedRespondentTestingData;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.resolveOtherRepresentativeRole;
import static uk.gov.hmcts.reform.fpl.controllers.ChangeFromOtherUtils.resolveRespondentRepresentativeRole;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;

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
    private static final int NUM_OF_OTHERS = 3;
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
        List<Element<Other>> others = prepareOthers(NUM_OF_OTHERS, null);
        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);

        CaseData caseData = CaseData.builder()
            .localAuthorities(localAuthorities())
            .othersV2(others)
            .respondents1(wrapElementsWithUUIDs(respondents))
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getOthersV2()).hasSize(NUM_OF_OTHERS - 1);
        assertThat(responseCaseData.getOthersV2()).containsExactlyInAnyOrder(others.get(1), others.get(2));

        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS)).contains(transformedRespondent);
    }

    @WithMockUser
    @Test
    void shouldConvertOthersWithHiddenDetailsToRespondentWhereNoConfidentialRespondent() {
        List<Element<Other>> others = prepareOthers(NUM_OF_OTHERS, List.of(0));
        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        CaseData caseData = CaseData.builder()
            .confidentialOthers(prepareConfidentialOthers(others))
            .localAuthorities(localAuthorities())
            .othersV2(others)
            .respondents1(wrapElementsWithUUIDs(respondents))
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getOthersV2()).hasSize(NUM_OF_OTHERS - 1);
        assertThat(responseCaseData.getOthersV2()).containsExactlyInAnyOrder(others.get(1), others.get(2));
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
        List<Element<Other>> others = prepareOthers(NUM_OF_OTHERS, List.of(0));

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS, true);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        List<Element<Respondent>> respondents1 = wrapElementsWithUUIDs(respondents);

        CaseData caseData = CaseData.builder()
            .confidentialRespondents(prepareConfidentialRespondentsFromRespondents1(respondents1))
            .confidentialOthers(prepareConfidentialOthers(others))
            .localAuthorities(localAuthorities())
            .othersV2(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getOthersV2()).hasSize(NUM_OF_OTHERS - 1);
        assertThat(responseCaseData.getOthersV2()).containsExactlyInAnyOrder(others.get(1), others.get(2));
        assertThat(responseCaseData.getConfidentialOthers()).hasSize(0);

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
        List<Element<Other>> others = prepareOthers(NUM_OF_OTHERS, List.of(0, 1));
        List<Element<Other>> confidentialOthers = prepareConfidentialOthers(others);

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS, true);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        List<Element<Respondent>> respondents1 = wrapElementsWithUUIDs(respondents);

        CaseData caseData = CaseData.builder()
            .confidentialRespondents(prepareConfidentialRespondentsFromRespondents1(respondents1))
            .confidentialOthers(confidentialOthers)
            .localAuthorities(localAuthorities())
            .respondents1(respondents1)
            .othersV2(others)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getOthersV2()).hasSize(NUM_OF_OTHERS - 1);
        assertThat(responseCaseData.getOthersV2()).containsExactlyInAnyOrder(others.get(1), others.get(2));
        assertThat(responseCaseData.getConfidentialOthers()).containsExactlyInAnyOrder(confidentialOthers.get(1));

        assertThat(responseCaseData.getConfidentialOthers()).hasSize(1);
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
        List<Element<Other>> others = prepareOthers(NUM_OF_OTHERS, null);
        Element<Representative> representativeForOther = element(prepareOtherRepresentative(SELECTED_OTHER));
        others.get(SELECTED_OTHER).getValue().addRepresentative(representativeForOther.getId());

        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);
        transformedRespondent.addRepresentative(representativeForOther.getId());

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        List<Element<Respondent>> respondents1 = wrapElementsWithUUIDs(respondents);

        CaseData caseData = CaseData.builder()
            .representatives(List.of(representativeForOther))
            .localAuthorities(localAuthorities())
            .othersV2(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getOthersV2()).hasSize(NUM_OF_OTHERS - 1);
        assertThat(responseCaseData.getOthersV2()).containsExactlyInAnyOrder(others.get(1), others.get(2));

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
        List<Element<Other>> others = prepareOthers(NUM_OF_OTHERS, null);
        List<Element<Representative>> representatives = new ArrayList<>();
        List<UUID> representativeIdsInTransformedRespondent = new ArrayList<>();
        for (int i = 0; i < NUM_OF_OTHERS; i++) {
            Element<Representative> representativeElement = element(prepareOtherRepresentative(i));
            others.get(i).getValue().addRepresentative(representativeElement.getId());
            if (i == SELECTED_OTHER) {
                representativeIdsInTransformedRespondent.add(representativeElement.getId());
            }
            representatives.add(representativeElement);
        }

        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);
        representativeIdsInTransformedRespondent.forEach(transformedRespondent::addRepresentative);

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        List<Element<Respondent>> respondents1 = wrapElementsWithUUIDs(respondents);

        CaseData caseData = CaseData.builder()
            .representatives(representatives)
            .localAuthorities(localAuthorities())
            .othersV2(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getOthersV2()).hasSize(NUM_OF_OTHERS - 1);
        assertThat(responseCaseData.getOthersV2()).containsExactlyInAnyOrder(others.get(1), others.get(2));

        // verify the respondent count should be increased by 1
        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        // verify respondent content was transferred successfully
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getParty))
            .contains(prepareExpectedTransformedRespondent(false).getParty());
        // verify if the transformed respondent's representative ids are migrated to respondent's representedBy
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getRepresentedBy).map(
            ElementUtils::unwrapElements).orElse(List.of())).isEqualTo(representativeIdsInTransformedRespondent);
        // verify if the representative's count = other's count
        assertThat(responseCaseData.getRepresentatives()).hasSize(NUM_OF_OTHERS);
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
        List<Element<Other>> responseAllOthers = responseCaseData.getOthersV2();
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
        List<Element<Other>> others = prepareOthers(NUM_OF_OTHERS, List.of(0, 1));
        List<Element<Representative>> representatives = new ArrayList<>();
        List<UUID> representativeIdsInTransformedRespondent = new ArrayList<>();
        for (int i = 0; i < NUM_OF_OTHERS; i++) {
            Element<Representative> representativeElement = element(prepareOtherRepresentative(i));
            others.get(i).getValue().addRepresentative(representativeElement.getId());
            if (i == SELECTED_OTHER) {
                representativeIdsInTransformedRespondent.add(representativeElement.getId());
            }
            representatives.add(representativeElement);
        }

        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);
        representativeIdsInTransformedRespondent.forEach(transformedRespondent::addRepresentative);

        List<Respondent> respondents = prepareRespondentsTestingData(NUM_RESPONDENTS);
        List<Element<Respondent>> respondents1 = wrapElementsWithUUIDs(respondents);

        CaseData caseData = CaseData.builder()
            .representatives(representatives)
            .localAuthorities(localAuthorities())
            .othersV2(others)
            .confidentialOthers(prepareConfidentialOthers(others))
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getOthersV2()).hasSize(NUM_OF_OTHERS - 1);
        assertThat(responseCaseData.getOthersV2()).containsExactlyInAnyOrder(others.get(1), others.get(2));

        // verify the respondent count should be increased by 1
        assertThat(responseCaseData.getAllRespondents()).hasSize(NUM_RESPONDENTS + 1);
        // verify respondent content was transferred successfully
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getParty))
            .contains(prepareExpectedTransformedRespondent(true).getParty());
        // verify if the transformed respondent's representative ids are migrated to respondent's representedBy
        assertThat(responseCaseData.findRespondent(NUM_RESPONDENTS).map(Respondent::getRepresentedBy).map(
            ElementUtils::unwrapElements).orElse(List.of())).isEqualTo(representativeIdsInTransformedRespondent);
        // verify if the representative's count = other's count
        assertThat(responseCaseData.getRepresentatives()).hasSize(NUM_OF_OTHERS);
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
        List<Element<Other>> responseAllOthers = responseCaseData.getOthersV2();
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
        UUID refugeOtherId = UUID.randomUUID();

        List<Element<Other>> others = List.of(element(refugeOtherId,
            Other.builder().firstName("Johnny").hideAddress("Yes").hideTelephone("Yes").build()));

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

        CaseData caseData = CaseData.builder()
            .confidentialOthers(List.of(element(refugeOtherId, Other.builder()
                .firstName("Johnny")
                .addressKnowV2(IsAddressKnowType.LIVE_IN_REFUGE)
                .address(Address.builder().build())
                .telephone("123456789")
                .hideAddress("Yes")
                .hideTelephone("Yes")
                .build())))
            .othersV2(others)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, SELECTED_OTHER))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);


        assertThat(responseCaseData.getConfidentialRespondents().get(0).getValue().getParty())
            .extracting("hideAddress", "hideTelephone").containsExactly("Yes", "Yes");
        assertThat(responseCaseData.getConfidentialRespondents().get(0).getValue().getParty().getTelephoneNumber()
            .getTelephoneNumber()).isEqualTo("123456789");
        assertThat(responseCaseData.getConfidentialRespondents().get(0).getValue().getParty().getAddress())
            .isEqualTo(buildHiddenAddress("Converting"));
        assertThat(responseCaseData.getAllRespondents().get(0).getValue().getParty().getAddress())
            .isNull();
        assertThat(responseCaseData.getAllRespondents().get(0).getValue().getParty().getTelephoneNumber()).isNull();
    }

}
