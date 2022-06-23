package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.RepresentativeRole;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Representative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.OtherToRespondentEventData;
import uk.gov.hmcts.reform.fpl.request.RequestData;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_1;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_10;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_2;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_3;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_4;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_5;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_6;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_7;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_8;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeRole.REPRESENTING_RESPONDENT_9;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildDynamicListFromOthers;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithRandomUUID;

@WebMvcTest(RespondentController.class)
@OverrideAutoConfiguration(enabled = true)
class RespondentControllerChangeFromOtherAboutToSubmitTest extends AbstractCallbackTest {

    private String telephoneNumber = "123456789";

    @MockBean
    private RequestData requestData;

    RespondentControllerChangeFromOtherAboutToSubmitTest() {
        super("enter-respondents/change-from-other");
    }

    @BeforeEach
    void before() {
        given(requestData.userRoles()).willReturn(Set.of(UserRole.HMCTS_ADMIN.getRoleName()));
    }

    private List<Element<LocalAuthority>> localAuthorities() {
        return wrapElements(LocalAuthority.builder()
            .name(LOCAL_AUTHORITY_1_NAME)
            .email(LOCAL_AUTHORITY_1_INBOX)
            .designated(YesNo.YES.getValue())
            .build());
    }

    private OtherToRespondentEventData otherToRespondentEventData(Respondent transformedRespondent, Others others,
                                                                  int selectedOtherSeq) {
        return OtherToRespondentEventData.builder()
            .transformedRespondent(transformedRespondent)
            .othersList(buildDynamicListFromOthers(others, selectedOtherSeq))
            .build();
    }

    private RepresentativeRole resolveOtherRepresentativeRole(int i) {
        switch (i) {
            case 0:
                return RepresentativeRole.REPRESENTING_PERSON_1;
            case 1:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_1;
            case 2:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_2;
            case 3:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_3;
            case 4:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_4;
            case 5:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_5;
            case 6:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_6;
            case 7:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_7;
            case 8:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_8;
            case 9:
                return RepresentativeRole.REPRESENTING_OTHER_PERSON_9;
            default:
                return null;
        }
    }

    private RepresentativeRole resolveRespondentRepresentativeRole(int i) {
        switch (i) {
            case 1:
                return REPRESENTING_RESPONDENT_1;
            case 2:
                return REPRESENTING_RESPONDENT_2;
            case 3:
                return REPRESENTING_RESPONDENT_3;
            case 4:
                return REPRESENTING_RESPONDENT_4;
            case 5:
                return REPRESENTING_RESPONDENT_5;
            case 6:
                return REPRESENTING_RESPONDENT_6;
            case 7:
                return REPRESENTING_RESPONDENT_7;
            case 8:
                return REPRESENTING_RESPONDENT_8;
            case 9:
                return REPRESENTING_RESPONDENT_9;
            case 10:
                return REPRESENTING_RESPONDENT_10;
            default:
                return null;
        }
    }

    private Respondent prepareTransformedRespondentTestingData(boolean contactDetailsHidden) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(Telephone.builder().telephoneNumber(telephoneNumber).build())
                .address(buildHiddenAddress("Converting"))
                .addressKnow("Yes")
                .contactDetailsHidden(YesNo.from(contactDetailsHidden).getValue())
                .build())
            .legalRepresentation("No")
            .build();
    }

    private Respondent prepareExpectedTransformedRespondent(boolean contactDeatilsHidden) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(contactDeatilsHidden ? null : Telephone.builder()
                    .telephoneNumber(telephoneNumber).build())
                .address(contactDeatilsHidden ? null : buildHiddenAddress("Converting"))
                .addressKnow("Yes")
                .contactDetailsHidden(YesNo.from(contactDeatilsHidden).getValue())
                .build())
            .legalRepresentation("No")
            .build();
    }

    private Respondent prepareExpectedTransformedConfidentialRespondent() {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .addressKnow("Yes")
                .address(buildHiddenAddress("Converting"))
                .telephoneNumber(Telephone.builder().telephoneNumber(telephoneNumber).build())
                .build())
            .build();
    }

    private Respondent prepareExpectedExistingConfidentialRespondent(int seqNo) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(String.format("existing respondent %s", seqNo))
                .addressKnow("Yes")
                .address(buildHiddenAddress(String.format("existing respondent %s", seqNo)))
                .telephoneNumber(Telephone.builder().telephoneNumber(telephoneNumber).build())
                .build())
            .build();
    }

    private List<Respondent> prepareRespondentsTestingData(int numberOfRespondent) {
        return prepareRespondentsTestingData(numberOfRespondent, false);
    }

    private List<Respondent> prepareRespondentsTestingData(int numberOfRespondent, boolean respondentDetailsHidden) {
        List<Respondent> respondents = new ArrayList<>();
        for (int j = 0; j < numberOfRespondent; j++) {
            respondents.add(
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName(String.format("existing respondent %s", j))
                        .dateOfBirth(dateNow())
                        .telephoneNumber(respondentDetailsHidden ? null : Telephone.builder()
                            .telephoneNumber(telephoneNumber)
                            .build())
                        .address(respondentDetailsHidden ? null : buildHiddenAddress("" + j))
                        .contactDetailsHidden(YesNo.from(respondentDetailsHidden).getValue())
                        .build())
                    .legalRepresentation("No")
                    .build()
            );
        }
        return respondents;
    }

    private Others prepareOthersTestingData(int numberOfAdditionalOther,
                                                   boolean firstOtherDetailsHidden,
                                                   boolean additionalOtherDetailsHidden) {
        return prepareOthersTestingData(numberOfAdditionalOther, firstOtherDetailsHidden,
            (i) -> additionalOtherDetailsHidden);
    }

    private Others prepareOthersTestingData(int numberOfAdditionalOther,
                                                   boolean firstOtherDetailsHidden,
                                                   Predicate<Integer> condition) {
        List<Element<Other>> additionalOthers = new ArrayList<>();
        for (int i = 0; i < numberOfAdditionalOther; i++) {
            additionalOthers.add(element(Other.builder()
                .name(String.format("Marco %s", i + 1))
                .detailsHidden(YesNo.from(condition.test(i)).getValue())
                .build()));
        }
        Other firstOther = Other.builder()
            .name("Marco 0")
            .detailsHidden(YesNo.from(firstOtherDetailsHidden).getValue())
            .build();
        Others others = Others.builder()
            .firstOther(firstOther)
            .additionalOthers(additionalOthers)
            .build();
        return others;
    }

    private Representative prepareOtherRepresentative(int otherSequence) {
        RepresentativeRole role = resolveOtherRepresentativeRole(otherSequence);
        return Representative.builder()
            .fullName("Joyce")
            .role(role)
            .build();
    }

    private List<Element<Respondent>> prepareConfidentialRespondentsFromRespondents1(
        List<Element<Respondent>> respondents1) {
        return respondents1.stream().map(
            e -> element(e.getId(), Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName(e.getValue().getParty().getFirstName())
                    .addressKnow("Yes")
                    .address(buildHiddenAddress(e.getValue().getParty().getFirstName()))
                    .telephoneNumber(Telephone.builder().telephoneNumber(telephoneNumber).build())
                    .build())
                .build())
        ).collect(toList());
    }

    private List<Element<Other>> prepareConfidentialOthersFromAllOthers(List<Element<Other>> allOthers) {
        return allOthers.stream().map(
            e -> element(
                e.getId(),
                e.getValue()
                    .toBuilder()
                    .detailsHidden(null)
                    .telephone(telephoneNumber)
                    .address(buildHiddenAddress(e.getValue().getName()))
                    .build())
        ).collect(toList());
    }

    private List<Element<Other>> prepareSingleConfidentialOther(int selectedOtherSeq,
                                                                Other firstOther,
                                                                List<Element<Other>> additionalOthers) {
        return List.of(element(
            (selectedOtherSeq == 0 ? UUID.randomUUID() : additionalOthers.get(selectedOtherSeq - 1).getId()),
            (selectedOtherSeq == 0 ? firstOther : additionalOthers.get(selectedOtherSeq - 1).getValue())
                .toBuilder()
                .detailsHidden(null)
                .telephone(telephoneNumber)
                .address(buildHiddenAddress("selected other"))
                .build())
        );
    }

    private static Address buildHiddenAddress(String identifier) {
        return Address.builder()
            .addressLine1(String.format("Secret Address %s", identifier))
            .build();
    }

    public static Stream<Arguments> othersToRespondentParam() {
        Stream.Builder<Arguments> builder = Stream.builder();
        for (int i = 0; i < 10; i++) { // selectedOtherSeq
            for (int j = 0; j <= 9; j++) { // numberOfAdditionalOther
                for (int k = 1; k <= 9; k++) { // numberOfRespondent
                    if (i <= j) {
                        builder.add(Arguments.of(i, j, k));
                    }
                }
            }
        }
        return builder.build();
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam")
    void shouldConvertOthersToRespondent(int selectedOtherSeq, int numberOfAdditionalOther,
                                         int numberOfRespondent) {
        Others others = prepareOthersTestingData(numberOfAdditionalOther, false, false);
        List<Respondent> respondents = prepareRespondentsTestingData(numberOfRespondent);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);

        CaseData caseData = CaseData.builder()
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(wrapElementsWithRandomUUID(respondents))
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, selectedOtherSeq))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(numberOfAdditionalOther);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", selectedOtherSeq).equals(o.getValue().getName()))).isEmpty();
        if (numberOfAdditionalOther > 0) {
            assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
                Other.builder()
                    .detailsHidden("No")
                    .name(String.format("Marco %s", selectedOtherSeq == 0 ? 1 : 0))
                    .build());
        } else {
            assertThat(responseCaseData.getOthers()).isNull();
        }
        assertThat(responseCaseData.getAllRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(transformedRespondent);
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam")
    void shouldConvertOthersWithHiddenDetailsToRespondentWhereNoConfidentialRespondent(
        int selectedOtherSeq, int numberOfAdditionalOther, int numberOfRespondent) {
        Others others = prepareOthersTestingData(numberOfAdditionalOther, false,
            (i) -> (selectedOtherSeq - 1) == i);
        Other firstOther = others.getFirstOther();
        List<Element<Other>> additionalOthers = others.getAdditionalOthers();
        List<Respondent> respondents = prepareRespondentsTestingData(numberOfRespondent);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        CaseData caseData = CaseData.builder()
            .confidentialOthers(prepareSingleConfidentialOther(selectedOtherSeq, firstOther, additionalOthers))
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(wrapElementsWithRandomUUID(respondents))
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, selectedOtherSeq))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(numberOfAdditionalOther);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", selectedOtherSeq).equals(o.getValue().getName()))).isEmpty();
        if (numberOfAdditionalOther > 0) {
            assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
                Other.builder()
                    .name(String.format("Marco %s", selectedOtherSeq == 0 ? 1 : 0))
                    .detailsHidden("No")
                    .build());
        } else {
            assertThat(responseCaseData.getOthers()).isNull();
        }
        assertThat(responseCaseData.getConfidentialOthers()).hasSize(0);
        assertThat(responseCaseData.getConfidentialRespondents()).hasSize(1);
        assertThat(responseCaseData.getAllRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(
            prepareExpectedTransformedRespondent(true));

        UUID targetUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> targetUUID.equals(e.getId())).map(Element::getValue).findFirst())
            .contains(prepareExpectedTransformedConfidentialRespondent());
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam")
    void shouldConvertOthersWithDetailsHiddenToRespondentWithDetailsHiddenAndNoMoreConfidentialOthers(
        int selectedOtherSeq,
        int numberOfAdditionalOther,
        int numberOfRespondent) {
        Others others = prepareOthersTestingData(numberOfAdditionalOther, selectedOtherSeq == 0,
            (i) -> (selectedOtherSeq - 1) == i);
        Other firstOther = others.getFirstOther();
        List<Element<Other>> additionalOthers = others.getAdditionalOthers();

        List<Respondent> respondents = prepareRespondentsTestingData(numberOfRespondent, true);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .confidentialRespondents(prepareConfidentialRespondentsFromRespondents1(respondents1))
            .confidentialOthers(prepareSingleConfidentialOther(selectedOtherSeq, firstOther, additionalOthers))
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, selectedOtherSeq))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(numberOfAdditionalOther);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", selectedOtherSeq).equals(o.getValue().getName()))).isEmpty();
        if (numberOfAdditionalOther > 0) {
            assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
                Other.builder()
                    .name(String.format("Marco %s", selectedOtherSeq == 0 ? 1 : 0))
                    .detailsHidden("No")
                    .build());
        } else {
            assertThat(responseCaseData.getOthers()).isNull();
        }
        assertThat(responseCaseData.getConfidentialOthers()).hasSize(0);
        assertThat(responseCaseData.getConfidentialRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.getAllRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(
            prepareExpectedTransformedRespondent(true));
        // Check converted respondent's confidential address
        UUID targetUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> targetUUID.equals(e.getId()))
            .map(Element::getValue)
            .findFirst())
            .contains(prepareExpectedTransformedConfidentialRespondent());
        // Check existing respondent address
        UUID prevExistingRespondentUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent - 1).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> prevExistingRespondentUUID.equals(e.getId()))
            .map(Element::getValue)
            .findFirst())
            .contains(prepareExpectedExistingConfidentialRespondent(numberOfRespondent - 1));
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam")
    void shouldConvertOthersWithDetailsHiddenToRespondentWithDetailsHiddenAndRetainConfidentialOthers(
        int selectedOtherSeq,
        int numberOfAdditionalOther,
        int numberOfRespondent) {
        Others others = prepareOthersTestingData(numberOfAdditionalOther, true, true);
        List<Element<Other>> allOthers = new ArrayList<>();
        allOthers.add(element(others.getFirstOther()));
        allOthers.addAll(others.getAdditionalOthers());

        List<Respondent> respondents = prepareRespondentsTestingData(numberOfRespondent, true);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(true);

        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .confidentialRespondents(prepareConfidentialRespondentsFromRespondents1(respondents1))
            .confidentialOthers(prepareConfidentialOthersFromAllOthers(allOthers))
            .localAuthorities(localAuthorities())
            .respondents1(respondents1)
            .others(others)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, selectedOtherSeq))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(numberOfAdditionalOther);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", selectedOtherSeq).equals(o.getValue().getName()))).isEmpty();
        if (numberOfAdditionalOther > 0) {
            assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
                Other.builder()
                    .name(String.format("Marco %s", selectedOtherSeq == 0 ? 1 : 0))
                    .detailsHidden("Yes")
                    .build());
        } else {
            assertThat(responseCaseData.getOthers()).isNull();
        }
        assertThat(responseCaseData.getConfidentialOthers()).hasSize(numberOfAdditionalOther);
        assertThat(responseCaseData.getConfidentialRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.getAllRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(
            prepareExpectedTransformedRespondent(true));
        // Check converted respondent's confidential address
        UUID targetUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> targetUUID.equals(e.getId())).map(Element::getValue).findFirst())
            .contains(prepareExpectedTransformedConfidentialRespondent());
        // Check existing respondent address
        UUID prevExistingRespondentUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent - 1).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> prevExistingRespondentUUID.equals(e.getId()))
            .map(Element::getValue)
            .findFirst())
            .contains(prepareExpectedExistingConfidentialRespondent(numberOfRespondent - 1));
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam")
    void shouldConvertOthersWithRepresentativeToRespondent(
        int selectedOtherSeq,
        int numberOfAdditionalOther,
        int numberOfRespondent) {
        Others others = prepareOthersTestingData(numberOfAdditionalOther, false, false);
        Element<Representative> representativeForOther = element(prepareOtherRepresentative(selectedOtherSeq));
        if (selectedOtherSeq == 0) {
            others.getFirstOther().addRepresentative(representativeForOther.getId());
        } else {
            others.getAdditionalOthers().get(selectedOtherSeq - 1).getValue()
                .addRepresentative(representativeForOther.getId());
        }

        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);
        transformedRespondent.addRepresentative(representativeForOther.getId());

        List<Respondent> respondents = prepareRespondentsTestingData(numberOfRespondent);
        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .representatives(List.of(representativeForOther))
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, selectedOtherSeq))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(numberOfAdditionalOther);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", selectedOtherSeq).equals(o.getValue().getName()))).isEmpty();
        if (numberOfAdditionalOther > 0) {
            assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(
                Other.builder()
                    .name(String.format("Marco %s", selectedOtherSeq == 0 ? 1 : 0))
                    .detailsHidden("No")
                    .build());
        } else {
            assertThat(responseCaseData.getOthers()).isNull();
        }
        assertThat(responseCaseData.getAllRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.findRespondent(numberOfRespondent).map(Respondent::getParty))
            .contains(prepareExpectedTransformedRespondent(false).getParty());
        assertThat(responseCaseData.findRespondent(numberOfRespondent).map(Respondent::getRepresentedBy).map(
            ElementUtils::unwrapElements).orElse(List.of())).isEqualTo(List.of(representativeForOther.getId()));
        assertThat(responseCaseData.getRepresentatives()).hasSize(1);
        assertThat(unwrapElements(responseCaseData.getRepresentatives()).stream()
            .map(Representative::getRole).findFirst())
            .contains(resolveRespondentRepresentativeRole(numberOfRespondent + 1));
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam")
    void shouldConvertOthersWithRepresentativesToRespondent(
            int selectedOtherSeq,
            int numberOfAdditionalOther,
            int numberOfRespondent) {
        Others others = prepareOthersTestingData(numberOfAdditionalOther, false, false);
        List<Element<Representative>> representatives = new ArrayList<>();
        List<UUID> representativeIdsInTransformedRespondent = new ArrayList<>();
        for (int i = 0; i < numberOfAdditionalOther + 1; i++) {
            Element<Representative> representativeElement = element(prepareOtherRepresentative(i));
            if (i == 0) {
                others.getFirstOther().addRepresentative(representativeElement.getId());
            } else {
                others.getAdditionalOthers().get(i - 1).getValue().addRepresentative(representativeElement.getId());
            }
            if (i == selectedOtherSeq) {
                representativeIdsInTransformedRespondent.add(representativeElement.getId());
            }
            representatives.add(representativeElement);
        }

        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);
        representativeIdsInTransformedRespondent.forEach(uuid -> transformedRespondent.addRepresentative(uuid));

        List<Respondent> respondents = prepareRespondentsTestingData(numberOfRespondent);
        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .representatives(representatives)
            .localAuthorities(localAuthorities())
            .others(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(otherToRespondentEventData(transformedRespondent, others, selectedOtherSeq))
            .build();

        AboutToStartOrSubmitCallbackResponse callbackResponse = postAboutToSubmitEvent(caseData);
        CaseData responseCaseData = extractCaseData(callbackResponse);

        assertThat(responseCaseData.getAllOthers()).hasSize(numberOfAdditionalOther);
        assertThat(responseCaseData.getAllOthers().stream()
            .filter(o -> String.format("Marco %s", selectedOtherSeq).equals(o.getValue().getName()))).isEmpty();
        if (numberOfAdditionalOther > 0) {
            assertThat(responseCaseData.getOthers().getFirstOther().toParty()).isEqualTo(
                Other.builder()
                    .name(String.format("Marco %s", selectedOtherSeq == 0 ? 1 : 0))
                    .detailsHidden("No")
                    .build().toParty());
        } else {
            assertThat(responseCaseData.getOthers()).isNull();
        }
        // verify the respondent count should be increased by 1
        assertThat(responseCaseData.getAllRespondents()).hasSize(numberOfRespondent + 1);
        // verify respondent content was transferred successfully
        assertThat(responseCaseData.findRespondent(numberOfRespondent).map(Respondent::getParty))
            .contains(prepareExpectedTransformedRespondent(false).getParty());
        // verify if the transformed respondent's representative ids are migrated to respondent's representedBy
        assertThat(responseCaseData.findRespondent(numberOfRespondent).map(Respondent::getRepresentedBy).map(
            ElementUtils::unwrapElements).orElse(List.of())).isEqualTo(representativeIdsInTransformedRespondent);
        // verify if the representative's count = other's count
        assertThat(responseCaseData.getRepresentatives()).hasSize(numberOfAdditionalOther + 1);
        // verify the new respondent's roles
        representativeIdsInTransformedRespondent.forEach(representativeId -> {
            assertThat(
                ElementUtils.findElement(representativeId, responseCaseData.getRepresentatives())
                    .map(Element::getValue)
                    .map(Representative::getRole)
                    .stream().collect(toSet())
            ).isEqualTo(Set.of(resolveRespondentRepresentativeRole(numberOfRespondent + 1)));
        });
        // verify the non-affected other's representatives
        List<Element<Other>> responseAllOthers = responseCaseData.getAllOthers();
        for (int i = 0; i < responseAllOthers.size(); i++) {
            final int finalI = i;
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

}
