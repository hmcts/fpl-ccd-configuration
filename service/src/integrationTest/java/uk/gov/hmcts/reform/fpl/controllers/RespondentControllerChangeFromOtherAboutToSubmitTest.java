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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
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

    private String hiddenTelephoneNumber = "123456789";

    @MockBean
    private RequestData requestData;

    RespondentControllerChangeFromOtherAboutToSubmitTest() {
        super("enter-respondents/change-from-other");
    }

    @BeforeEach
    void before() {
        given(requestData.userRoles()).willReturn(Set.of(UserRole.HMCTS_ADMIN.getRoleName()));
    }

    private static Respondent respondent(LocalDate dateOfBirth) {
        return respondent(dateOfBirth, true);
    }

    private static Respondent respondent(LocalDate dateOfBirth, boolean isLegalRepAnswered) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .dateOfBirth(dateOfBirth)
                .build())
            .legalRepresentation(isLegalRepAnswered ? "No" : null)
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
        List<Element<Other>> additionalOthers = new ArrayList<>();
        for (int i = 0; i < numberOfAdditionalOther; i++) {
            additionalOthers.add(element(Other.builder().name(String.format("Marco %s", i + 1)).build()));
        }

        Other firstOther = Other.builder().name("Marco 0").build();
        Others others = Others.builder()
            .firstOther(firstOther)
            .additionalOthers(additionalOthers)
            .build();

        List<Respondent> respondents = new ArrayList<>();
        for (int j = 0; j < numberOfRespondent; j++) {
            respondents.add(respondent(dateNow()));
        }

        Respondent transformedRespondent = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .build())
            .legalRepresentation("No")
            .build();

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
            assertThat(responseCaseData.getOthers().getFirstOther()).isEqualTo(Other.builder()
                .name(String.format("Marco %s", selectedOtherSeq == 0 ? 1 : 0)).build());
        } else {
            assertThat(responseCaseData.getOthers()).isNull();
        }
        assertThat(responseCaseData.getAllRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(transformedRespondent);
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam")
    void shouldConvertOthersWithHiddenDetailsToRespondent(int selectedOtherSeq, int numberOfAdditionalOther,
                                                          int numberOfRespondent) {
        List<Element<Other>> additionalOthers = new ArrayList<>();
        for (int i = 0; i < numberOfAdditionalOther; i++) {
            additionalOthers.add(element(Other.builder()
                .name(String.format("Marco %s", i + 1))
                .detailsHidden((selectedOtherSeq - 1) == i ? "Yes" : "No")
                .build()));
        }
        Other firstOther = Other.builder()
            .name("Marco 0")
            .detailsHidden("No")
            .build();
        Others others = Others.builder()
            .firstOther(firstOther)
            .additionalOthers(additionalOthers)
            .build();

        List<Respondent> respondents = new ArrayList<>();
        for (int j = 0; j < numberOfRespondent; j++) {
            respondents.add(respondent(dateNow()));
        }
        Respondent transformedRespondent = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                .address(buildHiddenAddress("Converting"))
                .contactDetailsHidden("Yes")
                .build())
            .legalRepresentation("No")
            .build();

        CaseData caseData = CaseData.builder()
            .confidentialOthers(List.of(
                element(
                    (selectedOtherSeq == 0 ? UUID.randomUUID() : additionalOthers.get(selectedOtherSeq - 1).getId()),
                    (selectedOtherSeq == 0 ? firstOther : additionalOthers.get(selectedOtherSeq - 1).getValue())
                        .toBuilder()
                        .detailsHidden(null)
                        .telephone(hiddenTelephoneNumber)
                        .address(buildHiddenAddress("selected other"))
                        .build())
            ))
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
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .contactDetailsHidden("Yes")
                .addressKnow("Yes")
                .build())
            .legalRepresentation("No")
            .build());

        UUID targetUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> targetUUID.equals(e.getId())).map(Element::getValue).findFirst())
            .contains(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Johnny")
                    .addressKnow("Yes")
                    .address(buildHiddenAddress("Converting"))
                    .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                    .build())
                .build());
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam")
    void shouldConvertOthersWithDetailsHiddenToRespondentWithDetailsHiddenAndNoMoreConfidentialOthers(
        int selectedOtherSeq,
        int numberOfAdditionalOther,
        int numberOfRespondent) {
        List<Element<Other>> additionalOthers = new ArrayList<>();
        for (int i = 0; i < numberOfAdditionalOther; i++) {
            additionalOthers.add(element(Other.builder()
                .name(String.format("Marco %s", i + 1))
                .detailsHidden((selectedOtherSeq - 1) == i ? "Yes" : "No")
                .build()));
        }
        Other firstOther = Other.builder()
            .name("Marco 0")
            .detailsHidden(selectedOtherSeq == 0 ? "Yes" : "No")
            .build();
        Others others = Others.builder()
            .firstOther(firstOther)
            .additionalOthers(additionalOthers)
            .build();

        List<Respondent> respondents = new ArrayList<>();
        for (int j = 0; j < numberOfRespondent; j++) {
            respondents.add(
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName(String.format("existing respondent %s", j))
                        .dateOfBirth(dateNow())
                        .contactDetailsHidden("Yes")
                        .build())
                    .legalRepresentation("No")
                    .build()
            );
        }
        Respondent transformedRespondent = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                .address(buildHiddenAddress("Converting"))
                .contactDetailsHidden("Yes")
                .build())
            .legalRepresentation("No")
            .build();

        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .confidentialRespondents(respondents1.stream().map(
                e -> element(e.getId(), Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName(e.getValue().getParty().getFirstName())
                        .addressKnow("Yes")
                        .address(buildHiddenAddress(e.getValue().getParty().getFirstName()))
                        .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                        .build())
                    .build())
            ).collect(Collectors.toList()))
            .confidentialOthers(List.of(
                element(
                    (selectedOtherSeq == 0 ? UUID.randomUUID() : additionalOthers.get(selectedOtherSeq - 1).getId()),
                    (selectedOtherSeq == 0 ? firstOther : additionalOthers.get(selectedOtherSeq - 1).getValue())
                        .toBuilder()
                        .detailsHidden(null)
                        .telephone(hiddenTelephoneNumber)
                        .address(buildHiddenAddress("SHOULDN'T EXIST"))
                        .build())
            ))
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
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .contactDetailsHidden("Yes")
                .addressKnow("Yes")
                .build())
            .legalRepresentation("No")
            .build());

        // Check converted respondent's confidential address
        UUID targetUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> targetUUID.equals(e.getId())).map(Element::getValue).findFirst())
            .contains(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Johnny")
                    .addressKnow("Yes")
                    .address(buildHiddenAddress("Converting"))
                    .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                    .build())
                .build());

        // Check existing respondent address
        UUID existingRespondentUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent - 1).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> existingRespondentUUID.equals(e.getId())).map(Element::getValue).findFirst())
            .contains(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName(String.format("existing respondent %s", numberOfRespondent - 1))
                    .addressKnow("Yes")
                    .address(buildHiddenAddress(String.format("existing respondent %s", numberOfRespondent - 1)))
                    .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                    .build())
                .build());
    }

    @ParameterizedTest
    @MethodSource("othersToRespondentParam")
    void shouldConvertOthersWithDetailsHiddenToRespondentWithDetailsHiddenAndRetainConfidentialOthers(
        int selectedOtherSeq,
        int numberOfAdditionalOther,
        int numberOfRespondent) {
        List<Element<Other>> additionalOthers = new ArrayList<>();
        for (int i = 0; i < numberOfAdditionalOther; i++) {
            additionalOthers.add(element(Other.builder()
                .name(String.format("Marco %s", i + 1))
                .detailsHidden("Yes")
                .build()));
        }
        Other firstOther = Other.builder()
            .name("Marco 0")
            .detailsHidden("Yes")
            .build();
        Others others = Others.builder()
            .firstOther(firstOther)
            .additionalOthers(additionalOthers)
            .build();
        List<Element<Other>> allOthers = new ArrayList<>();
        allOthers.add(element(others.getFirstOther()));
        allOthers.addAll(others.getAdditionalOthers());

        List<Respondent> respondents = new ArrayList<>();
        for (int j = 0; j < numberOfRespondent; j++) {
            respondents.add(
                Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName(String.format("existing respondent %s", j))
                        .dateOfBirth(dateNow())
                        .contactDetailsHidden("Yes")
                        .build())
                    .legalRepresentation("No")
                    .build()
            );
        }
        Respondent transformedRespondent = Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                .address(buildHiddenAddress("Converting"))
                .contactDetailsHidden("Yes")
                .build())
            .legalRepresentation("No")
            .build();

        List<Element<Respondent>> respondents1 = wrapElementsWithRandomUUID(respondents);

        CaseData caseData = CaseData.builder()
            .confidentialRespondents(respondents1.stream().map(
                e -> element(e.getId(), Respondent.builder()
                    .party(RespondentParty.builder()
                        .firstName(e.getValue().getParty().getFirstName())
                        .addressKnow("Yes")
                        .address(buildHiddenAddress(e.getValue().getParty().getFirstName()))
                        .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                        .build())
                    .build())
            ).collect(Collectors.toList()))
            .confidentialOthers(allOthers.stream().map(
                e -> element(
                    e.getId(),
                    e.getValue()
                        .toBuilder()
                        .detailsHidden(null)
                        .telephone(hiddenTelephoneNumber)
                        .address(buildHiddenAddress(e.getValue().getName()))
                        .build())
            ).collect(Collectors.toList()))
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
                    .detailsHidden("Yes")
                    .build());
        } else {
            assertThat(responseCaseData.getOthers()).isNull();
        }
        assertThat(responseCaseData.getConfidentialOthers()).hasSize(numberOfAdditionalOther);
        assertThat(responseCaseData.getConfidentialRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.getAllRespondents()).hasSize(numberOfRespondent + 1);
        assertThat(responseCaseData.findRespondent(numberOfRespondent)).contains(Respondent.builder()
            .party(RespondentParty.builder()
                .firstName("Johnny")
                .contactDetailsHidden("Yes")
                .addressKnow("Yes")
                .build())
            .legalRepresentation("No")
            .build());

        // Check converted respondent's confidential address
        UUID targetUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> targetUUID.equals(e.getId())).map(Element::getValue).findFirst())
            .contains(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName("Johnny")
                    .addressKnow("Yes")
                    .address(buildHiddenAddress("Converting"))
                    .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                    .build())
                .build());

        // Check existing respondent address
        UUID existingRespondentUUID = responseCaseData.getAllRespondents().stream().filter(
            e -> Objects.equals(e.getValue(), responseCaseData.findRespondent(numberOfRespondent - 1).get())
        ).map(Element::getId).findFirst().orElse(UUID.randomUUID());
        assertThat(responseCaseData.getConfidentialRespondents().stream()
            .filter(e -> existingRespondentUUID.equals(e.getId())).map(Element::getValue).findFirst())
            .contains(Respondent.builder()
                .party(RespondentParty.builder()
                    .firstName(String.format("existing respondent %s", numberOfRespondent - 1))
                    .addressKnow("Yes")
                    .address(buildHiddenAddress(String.format("existing respondent %s", numberOfRespondent - 1)))
                    .telephoneNumber(Telephone.builder().telephoneNumber(hiddenTelephoneNumber).build())
                    .build())
                .build());
    }

    private Address buildHiddenAddress(String identifier) {
        return Address.builder()
            .addressLine1(String.format("Secret Address %s", identifier))
            .build();
    }

    public static Stream<Arguments> othersToRespondentParam2() {
        return Stream.of(Arguments.of(1, 1, 1));
    }
}
