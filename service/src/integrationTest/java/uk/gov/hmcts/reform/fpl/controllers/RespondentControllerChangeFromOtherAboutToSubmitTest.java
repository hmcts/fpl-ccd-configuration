package uk.gov.hmcts.reform.fpl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.fpl.enums.UserRole;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.Address;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Telephone;
import uk.gov.hmcts.reform.fpl.model.event.OtherToRespondentEventData;
import uk.gov.hmcts.reform.fpl.request.RequestData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_INBOX;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildDynamicListFromOthers;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
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

    private Respondent prepareTransformedRespondentTestingData(boolean contactDeatilsHidden) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(Telephone.builder().telephoneNumber(telephoneNumber).build())
                .address(buildHiddenAddress("Converting"))
                .addressKnow("Yes")
                .contactDetailsHidden(YesNo.from(contactDeatilsHidden).getValue())
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

    private static Others prepareOthersTestingData(int numberOfAdditionalOther,
                                                   boolean firstOtherDetailsHidden,
                                                   boolean additionalOtherDetailsHidden) {
        return prepareOthersTestingData(numberOfAdditionalOther, firstOtherDetailsHidden,
            (i) -> additionalOtherDetailsHidden);
    }

    private static Others prepareOthersTestingData(int numberOfAdditionalOther,
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
        ).collect(Collectors.toList());
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
        ).collect(Collectors.toList());
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
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_1_NAME)
                .email(LOCAL_AUTHORITY_1_INBOX)
                .designated(YesNo.YES.getValue())
                .build()))
            .others(others)
            .respondents1(wrapElementsWithRandomUUID(respondents))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(transformedRespondent)
                .othersList(buildDynamicListFromOthers(others, selectedOtherSeq))
                .build())
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
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_1_NAME)
                .email(LOCAL_AUTHORITY_1_INBOX)
                .designated(YesNo.YES.getValue())
                .build()))
            .others(others)
            .respondents1(wrapElementsWithRandomUUID(respondents))
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(transformedRespondent)
                .othersList(buildDynamicListFromOthers(others, selectedOtherSeq))
                .build())
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
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_1_NAME)
                .email(LOCAL_AUTHORITY_1_INBOX)
                .designated(YesNo.YES.getValue())
                .build()))
            .others(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(transformedRespondent)
                .othersList(buildDynamicListFromOthers(others, selectedOtherSeq))
                .build())
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
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_1_NAME)
                .email(LOCAL_AUTHORITY_1_INBOX)
                .designated(YesNo.YES.getValue())
                .build()))
            .respondents1(respondents1)
            .others(others)
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(transformedRespondent)
                .othersList(buildDynamicListFromOthers(others, selectedOtherSeq))
                .build())
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


    public static Stream<Arguments> othersToRespondentParam2() {
        return Stream.of(Arguments.of(1, 1, 1));
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam2")
    void shouldConvertOthersWithRepresentativeToRespondent(
        int selectedOtherSeq,
        int numberOfAdditionalOther,
        int numberOfRespondent) {
        Others others = prepareOthersTestingData(numberOfAdditionalOther, false, false);
        List<Respondent> respondents = prepareRespondentsTestingData(numberOfRespondent);
        Respondent transformedRespondent = prepareTransformedRespondentTestingData(false);

        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .localAuthorities(wrapElements(LocalAuthority.builder()
                .name(LOCAL_AUTHORITY_1_NAME)
                .email(LOCAL_AUTHORITY_1_INBOX)
                .designated(YesNo.YES.getValue())
                .build()))
            .others(others)
            .respondents1(respondents1)
            .otherToRespondentEventData(OtherToRespondentEventData.builder()
                .transformedRespondent(transformedRespondent)
                .othersList(buildDynamicListFromOthers(others, selectedOtherSeq))
                .build())
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
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(
            prepareExpectedTransformedRespondent(false)
        );
    }

}
