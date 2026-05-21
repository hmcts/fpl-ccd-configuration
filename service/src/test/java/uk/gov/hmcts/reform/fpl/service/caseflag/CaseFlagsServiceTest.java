package uk.gov.hmcts.reform.fpl.service.caseflag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.caseflag.CaseFlagsType;
import uk.gov.hmcts.reform.fpl.model.caseflag.FlagDetailType;
import uk.gov.hmcts.reform.fpl.model.caseflag.GenericTypeItem;
import uk.gov.hmcts.reform.fpl.model.caseflag.ListTypeItem;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static uk.gov.hmcts.reform.fpl.service.caseflag.CaseFlagsService.APPLICANT;
import static uk.gov.hmcts.reform.fpl.service.caseflag.CaseFlagsService.EXTERNAL;
import static uk.gov.hmcts.reform.fpl.service.caseflag.CaseFlagsService.INTERNAL;
import static uk.gov.hmcts.reform.fpl.service.caseflag.CaseFlagsService.RESPONDENT;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.ACTIVE;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.DISRUPTIVE_CUSTOMER;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.NO;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.VEXATIOUS_LITIGANT;
import static uk.gov.hmcts.reform.fpl.utils.CaseFlagConstants.YES;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class CaseFlagsServiceTest {
    private static final String APPLICANT_NAME = "Swansea City Council";
    private static final String UPDATED_APPLICANT_NAME = "Cardiff City Council";

    private CaseFlagsService caseFlagsService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseFlagsService = new CaseFlagsService();
        caseData = buildCaseData(10);
    }

    @Test
    void shouldReturnThatCaseFlagsSetupIsRequiredWhenFieldsHaveNotBeenInitialised() {
        assertThat(caseFlagsService.caseFlagsSetupRequired(caseData)).isTrue();

        caseFlagsService.setupCaseFlags(caseData);

        assertThat(caseFlagsService.caseFlagsSetupRequired(caseData)).isFalse();
    }

    @Test
    void shouldReturnThatCaseFlagsSetupIsRequiredWhenCaseFlagsAreMissingAfterPartyFlagsAreInitialised() {
        caseFlagsService.setupCaseFlags(caseData);

        caseData.setCaseFlags(null);

        assertThat(caseFlagsService.caseFlagsSetupRequired(caseData)).isTrue();
    }

    @Test
    void shouldReturnThatCaseFlagsSetupIsRequiredWhenApplicantFlagsAreMissingOrHaveNoRole() {
        caseFlagsService.setupCaseFlags(caseData);

        caseData.setApplicantFlags(null);

        assertThat(caseFlagsService.caseFlagsSetupRequired(caseData)).isTrue();

        caseFlagsService.setupCaseFlags(caseData);
        caseData.setApplicantExternalFlags(CaseFlagsType.builder().roleOnCase("").build());

        assertThat(caseFlagsService.caseFlagsSetupRequired(caseData)).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("respondentFlagSetters")
    void shouldReturnThatCaseFlagsSetupIsRequiredWhenUsedRespondentFlagsAreMissingOrHaveNoRole(
        String description, BiConsumer<CaseData, CaseFlagsType> setter) {

        caseFlagsService.setupCaseFlags(caseData);

        setter.accept(caseData, null);

        assertThat(caseFlagsService.caseFlagsSetupRequired(caseData))
            .as(description + " missing")
            .isTrue();

        caseFlagsService.setupCaseFlags(caseData);
        setter.accept(caseData, CaseFlagsType.builder().roleOnCase("").build());

        assertThat(caseFlagsService.caseFlagsSetupRequired(caseData))
            .as(description + " role missing")
            .isTrue();
    }

    @Test
    void shouldIgnoreUnusedRespondentFlagSlotsWhenCheckingIfSetupIsRequired() {
        CaseData caseDataWithTwoRespondents = buildCaseData(2);
        caseFlagsService.setupCaseFlags(caseDataWithTwoRespondents);

        caseDataWithTwoRespondents.setRespondent3Flags(CaseFlagsType.builder().build());
        caseDataWithTwoRespondents.setRespondent3ExternalFlags(CaseFlagsType.builder().build());
        caseDataWithTwoRespondents.setRespondent10Flags(CaseFlagsType.builder().build());
        caseDataWithTwoRespondents.setRespondent10ExternalFlags(CaseFlagsType.builder().build());

        assertThat(caseFlagsService.caseFlagsSetupRequired(caseDataWithTwoRespondents)).isFalse();
    }

    @Test
    void shouldStillRequireSetupWhenThereIsNoLocalAuthorityToPopulateApplicantFlags() {
        CaseData caseDataWithoutLocalAuthority = CaseData.builder()
            .respondents1(respondents(1))
            .build();

        caseFlagsService.setupCaseFlags(caseDataWithoutLocalAuthority);

        assertThat(caseDataWithoutLocalAuthority.getCaseFlags()).isNotNull();
        assertThat(caseDataWithoutLocalAuthority.getApplicantFlags()).isNull();
        assertThat(caseFlagsService.caseFlagsSetupRequired(caseDataWithoutLocalAuthority)).isTrue();
    }

    @Test
    void shouldNotRequireRespondentFlagsWhenThereAreNoRespondents() {
        CaseData caseDataWithoutRespondents = buildCaseData(0);

        caseFlagsService.setupCaseFlags(caseDataWithoutRespondents);

        assertThat(caseFlagsService.caseFlagsSetupRequired(caseDataWithoutRespondents)).isFalse();
        assertThat(caseFlagsService.generate(caseDataWithoutRespondents)).containsOnlyKeys(
            "caseFlags",
            "applicantFlags",
            "applicantExternalFlags"
        );
    }

    @Test
    void shouldSetupCaseApplicantAndRespondentFlags() {
        caseFlagsService.setupCaseFlags(caseData);

        assertFlag(caseData.getCaseFlags(), null, null, null, null);
        assertFlag(caseData.getApplicantFlags(), APPLICANT_NAME, APPLICANT, APPLICANT, INTERNAL);
        assertFlag(caseData.getApplicantExternalFlags(), APPLICANT_NAME, APPLICANT, APPLICANT, EXTERNAL);

        List<CaseFlagsType> internalRespondentFlags = internalRespondentFlags(caseData);
        List<CaseFlagsType> externalRespondentFlags = externalRespondentFlags(caseData);

        for (int respondentNumber = 1; respondentNumber <= 10; respondentNumber++) {
            String respondentName = respondentName(respondentNumber);
            String groupId = "respondent" + respondentNumber;

            assertFlag(internalRespondentFlags.get(respondentNumber - 1),
                respondentName, RESPONDENT, groupId, INTERNAL);
            assertFlag(externalRespondentFlags.get(respondentNumber - 1),
                respondentName, RESPONDENT, groupId, EXTERNAL);
        }
    }

    @Test
    void shouldUpdateApplicantFlagNamesWhenLocalAuthorityNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);

        caseData.getLocalAuthorities().getFirst().getValue().setName(UPDATED_APPLICANT_NAME);

        caseFlagsService.setupCaseFlags(caseData);

        assertFlag(caseData.getApplicantFlags(), UPDATED_APPLICANT_NAME, APPLICANT, APPLICANT, INTERNAL);
        assertFlag(caseData.getApplicantExternalFlags(), UPDATED_APPLICANT_NAME, APPLICANT, APPLICANT, EXTERNAL);
    }

    @Test
    void shouldUpdateRespondentFlagNamesWhenRespondentNameChanges() {
        caseFlagsService.setupCaseFlags(caseData);

        caseData.getAllRespondents().getFirst().setValue(respondent("Updated", "Respondent"));

        caseFlagsService.setupCaseFlags(caseData);

        assertFlag(caseData.getRespondent1Flags(), "Updated Respondent", RESPONDENT, "respondent1", INTERNAL);
        assertFlag(caseData.getRespondent1ExternalFlags(), "Updated Respondent", RESPONDENT, "respondent1", EXTERNAL);
    }

    @Test
    void shouldPreserveExistingFlagDetailsWhenUpdatingPartyNames() {
        ListTypeItem<FlagDetailType> applicantDetails = ListTypeItem.from(flagDetail(LANGUAGE_INTERPRETER, ACTIVE));
        ListTypeItem<FlagDetailType> respondentDetails = ListTypeItem.from(flagDetail(VEXATIOUS_LITIGANT, ACTIVE));
        caseData.setApplicantFlags(CaseFlagsType.builder()
            .partyName("Old applicant")
            .roleOnCase(APPLICANT)
            .groupId(APPLICANT)
            .visibility(INTERNAL)
            .details(applicantDetails)
            .build());
        caseData.setRespondent1Flags(CaseFlagsType.builder()
            .partyName("Old respondent")
            .roleOnCase(RESPONDENT)
            .groupId("respondent1")
            .visibility(INTERNAL)
            .details(respondentDetails)
            .build());

        caseFlagsService.setupCaseFlags(caseData);

        assertThat(caseData.getApplicantFlags().getPartyName()).isEqualTo(APPLICANT_NAME);
        assertThat(caseData.getApplicantFlags().getDetails()).isSameAs(applicantDetails);
        assertThat(caseData.getRespondent1Flags().getPartyName()).isEqualTo("Respondent 1");
        assertThat(caseData.getRespondent1Flags().getDetails()).isSameAs(respondentDetails);
    }

    @Test
    void shouldSetupNoMoreThanTenRespondentFlagPairs() {
        CaseData caseDataWithElevenRespondents = buildCaseData(11);

        caseFlagsService.setupCaseFlags(caseDataWithElevenRespondents);

        assertThat(caseDataWithElevenRespondents.getRespondent10Flags().getPartyName()).isEqualTo("Respondent 10");
        assertThat(caseFlagsService.caseFlagsSetupRequired(caseDataWithElevenRespondents)).isFalse();
        assertThat(caseFlagsService.generate(caseDataWithElevenRespondents))
            .doesNotContainKeys("respondent11Flags", "respondent11ExternalFlags");
    }

    @Test
    void shouldSetInterpreterRequiredFlagWhenActiveLanguageFlagExists() {
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(emptyFlags())
            .applicantExternalFlags(emptyFlags())
            .respondent1Flags(flagsWith(LANGUAGE_INTERPRETER))
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(YES);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(NO);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("interpreterFlagNames")
    void shouldSetInterpreterRequiredFlagForAllInterpreterFlagCodes(String flagName) {
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(flagsWith(flagName))
            .applicantExternalFlags(emptyFlags())
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(YES);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(NO);
    }

    @Test
    void shouldSetInterpreterRequiredFlagWhenActiveSignLanguageFlagExists() {
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(flagsWith(SIGN_LANGUAGE_INTERPRETER))
            .applicantExternalFlags(emptyFlags())
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(YES);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(NO);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("additionalSecurityFlagNames")
    void shouldSetAdditionalSecurityFlagForAllSecurityFlagCodes(String flagName) {
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(emptyFlags())
            .applicantExternalFlags(flagsWith(flagName))
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(NO);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(YES);
    }

    @Test
    void shouldSetAdditionalSecurityFlagWhenActiveSecurityFlagExists() {
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(emptyFlags())
            .applicantExternalFlags(flagsWith(VEXATIOUS_LITIGANT))
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(NO);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(YES);
    }

    @Test
    void shouldSetAdditionalSecurityFlagWhenActiveDisruptiveCustomerFlagExists() {
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(emptyFlags())
            .applicantExternalFlags(emptyFlags())
            .respondent1ExternalFlags(flagsWith(DISRUPTIVE_CUSTOMER))
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(NO);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(YES);
    }

    @Test
    void shouldSetDependentFlagsToNoWhenThereAreNoActiveRelevantFlags() {
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(flagsWithStatus(SIGN_LANGUAGE_INTERPRETER, "Inactive"))
            .applicantExternalFlags(flagsWithStatus(VEXATIOUS_LITIGANT, "Inactive"))
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(NO);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(NO);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("respondentFlagSetters")
    void shouldProcessActiveFlagsFromEveryRespondentFlagField(
        String description, BiConsumer<CaseData, CaseFlagsType> setter) {

        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(emptyFlags())
            .applicantExternalFlags(emptyFlags())
            .build();
        setter.accept(flaggedCaseData, flagsWith(VEXATIOUS_LITIGANT));

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag())
            .as(description)
            .isEqualTo(YES);
    }

    @Test
    void shouldIgnoreNullFlagsNullDetailsAndNullFlagItemsWhenProcessingNewlySetCaseFlags() {
        ListTypeItem<FlagDetailType> details = ListTypeItem.from(
            GenericTypeItem.from((FlagDetailType) null),
            GenericTypeItem.from(flagDetail("Not relevant", ACTIVE))
        );
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(CaseFlagsType.builder().details(details).build())
            .applicantExternalFlags(null)
            .respondent1Flags(CaseFlagsType.builder().details(null).build())
            .respondent1ExternalFlags(null)
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(NO);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(NO);
    }

    @Test
    void shouldSetDependentFlagsWhenRelevantActiveFlagAppearsAfterInactiveFlagWithSameName() {
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(flagsWith(
                flagDetail(SIGN_LANGUAGE_INTERPRETER, "Inactive"),
                flagDetail(SIGN_LANGUAGE_INTERPRETER, ACTIVE)
            ))
            .applicantExternalFlags(emptyFlags())
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(YES);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(NO);
    }

    @Test
    void shouldSetBothDependentFlagsWhenInterpreterAndSecurityFlagsAreActive() {
        CaseData flaggedCaseData = CaseData.builder()
            .applicantFlags(flagsWith(SIGN_LANGUAGE_INTERPRETER))
            .applicantExternalFlags(flagsWith(DISRUPTIVE_CUSTOMER))
            .build();

        caseFlagsService.processNewlySetCaseFlags(flaggedCaseData);

        assertThat(flaggedCaseData.getCaseInterpreterRequiredFlag()).isEqualTo(YES);
        assertThat(flaggedCaseData.getCaseAdditionalSecurityFlag()).isEqualTo(YES);
    }

    @Test
    void shouldGenerateDataForInitialisedFlagsOnly() {
        CaseData caseDataWithOneRespondent = buildCaseData(1);

        caseFlagsService.setupCaseFlags(caseDataWithOneRespondent);

        Map<String, Object> generatedData = caseFlagsService.generate(caseDataWithOneRespondent);

        assertThat(generatedData).containsOnlyKeys(
            "caseFlags",
            "applicantFlags",
            "applicantExternalFlags",
            "respondent1Flags",
            "respondent1ExternalFlags"
        );
        assertThat(generatedData.get("caseFlags")).isSameAs(caseDataWithOneRespondent.getCaseFlags());
        assertThat(generatedData.get("applicantFlags")).isSameAs(caseDataWithOneRespondent.getApplicantFlags());
        assertThat(generatedData.get("respondent1Flags")).isSameAs(caseDataWithOneRespondent.getRespondent1Flags());
    }

    @Test
    void shouldGenerateEmptyDataWhenNoFlagsAreInitialised() {
        CaseData emptyCaseData = CaseData.builder().build();

        assertThat(caseFlagsService.generate(emptyCaseData)).isEmpty();
    }

    @Test
    void shouldGenerateAllInitialisedRespondentFlagFields() {
        caseFlagsService.setupCaseFlags(caseData);

        Map<String, Object> generatedData = caseFlagsService.generate(caseData);

        assertThat(generatedData).containsKeys(
            "caseFlags",
            "applicantFlags",
            "applicantExternalFlags",
            "respondent1Flags",
            "respondent1ExternalFlags",
            "respondent2Flags",
            "respondent2ExternalFlags",
            "respondent3Flags",
            "respondent3ExternalFlags",
            "respondent4Flags",
            "respondent4ExternalFlags",
            "respondent5Flags",
            "respondent5ExternalFlags",
            "respondent6Flags",
            "respondent6ExternalFlags",
            "respondent7Flags",
            "respondent7ExternalFlags",
            "respondent8Flags",
            "respondent8ExternalFlags",
            "respondent9Flags",
            "respondent9ExternalFlags",
            "respondent10Flags",
            "respondent10ExternalFlags"
        );
        assertThat(generatedData).hasSize(23);
    }

    private static Stream<Arguments> interpreterFlagNames() {
        return Stream.of(
            arguments(SIGN_LANGUAGE_INTERPRETER),
            arguments(LANGUAGE_INTERPRETER)
        );
    }

    private static Stream<Arguments> additionalSecurityFlagNames() {
        return Stream.of(
            arguments(VEXATIOUS_LITIGANT),
            arguments(DISRUPTIVE_CUSTOMER)
        );
    }

    private static Stream<Arguments> respondentFlagSetters() {
        return Stream.of(
            respondentFlagSetter("respondent1Flags", CaseData::setRespondent1Flags),
            respondentFlagSetter("respondent1ExternalFlags", CaseData::setRespondent1ExternalFlags),
            respondentFlagSetter("respondent2Flags", CaseData::setRespondent2Flags),
            respondentFlagSetter("respondent2ExternalFlags", CaseData::setRespondent2ExternalFlags),
            respondentFlagSetter("respondent3Flags", CaseData::setRespondent3Flags),
            respondentFlagSetter("respondent3ExternalFlags", CaseData::setRespondent3ExternalFlags),
            respondentFlagSetter("respondent4Flags", CaseData::setRespondent4Flags),
            respondentFlagSetter("respondent4ExternalFlags", CaseData::setRespondent4ExternalFlags),
            respondentFlagSetter("respondent5Flags", CaseData::setRespondent5Flags),
            respondentFlagSetter("respondent5ExternalFlags", CaseData::setRespondent5ExternalFlags),
            respondentFlagSetter("respondent6Flags", CaseData::setRespondent6Flags),
            respondentFlagSetter("respondent6ExternalFlags", CaseData::setRespondent6ExternalFlags),
            respondentFlagSetter("respondent7Flags", CaseData::setRespondent7Flags),
            respondentFlagSetter("respondent7ExternalFlags", CaseData::setRespondent7ExternalFlags),
            respondentFlagSetter("respondent8Flags", CaseData::setRespondent8Flags),
            respondentFlagSetter("respondent8ExternalFlags", CaseData::setRespondent8ExternalFlags),
            respondentFlagSetter("respondent9Flags", CaseData::setRespondent9Flags),
            respondentFlagSetter("respondent9ExternalFlags", CaseData::setRespondent9ExternalFlags),
            respondentFlagSetter("respondent10Flags", CaseData::setRespondent10Flags),
            respondentFlagSetter("respondent10ExternalFlags", CaseData::setRespondent10ExternalFlags)
        );
    }

    private static Arguments respondentFlagSetter(String description, BiConsumer<CaseData, CaseFlagsType> setter) {
        return arguments(description, setter);
    }

    private static CaseData buildCaseData(int respondentCount) {
        return CaseData.builder()
            .localAuthorities(List.of(element(LocalAuthority.builder()
                .name(APPLICANT_NAME)
                .build())))
            .respondents1(respondents(respondentCount))
            .build();
    }

    private static List<Element<Respondent>> respondents(int respondentCount) {
        return IntStream.rangeClosed(1, respondentCount)
            .mapToObj(respondentNumber -> element(respondent("Respondent", String.valueOf(respondentNumber))))
            .toList();
    }

    private static Respondent respondent(String firstName, String lastName) {
        return Respondent.builder()
            .party(RespondentParty.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build())
            .build();
    }

    private static String respondentName(int respondentNumber) {
        return "Respondent " + respondentNumber;
    }

    private static CaseFlagsType emptyFlags() {
        return CaseFlagsType.builder().build();
    }

    private static CaseFlagsType flagsWith(String name) {
        return flagsWithStatus(name, ACTIVE);
    }

    private static CaseFlagsType flagsWith(FlagDetailType... flagDetails) {
        return CaseFlagsType.builder()
            .details(ListTypeItem.from(flagDetails))
            .build();
    }

    private static CaseFlagsType flagsWithStatus(String name, String status) {
        return flagsWith(flagDetail(name, status));
    }

    private static FlagDetailType flagDetail(String name, String status) {
        return FlagDetailType.builder()
            .name(name)
            .status(status)
            .build();
    }

    private static List<CaseFlagsType> internalRespondentFlags(CaseData caseData) {
        return List.of(
            caseData.getRespondent1Flags(),
            caseData.getRespondent2Flags(),
            caseData.getRespondent3Flags(),
            caseData.getRespondent4Flags(),
            caseData.getRespondent5Flags(),
            caseData.getRespondent6Flags(),
            caseData.getRespondent7Flags(),
            caseData.getRespondent8Flags(),
            caseData.getRespondent9Flags(),
            caseData.getRespondent10Flags()
        );
    }

    private static List<CaseFlagsType> externalRespondentFlags(CaseData caseData) {
        return List.of(
            caseData.getRespondent1ExternalFlags(),
            caseData.getRespondent2ExternalFlags(),
            caseData.getRespondent3ExternalFlags(),
            caseData.getRespondent4ExternalFlags(),
            caseData.getRespondent5ExternalFlags(),
            caseData.getRespondent6ExternalFlags(),
            caseData.getRespondent7ExternalFlags(),
            caseData.getRespondent8ExternalFlags(),
            caseData.getRespondent9ExternalFlags(),
            caseData.getRespondent10ExternalFlags()
        );
    }

    private static void assertFlag(CaseFlagsType actual, String partyName, String roleOnCase,
                                   String groupId, String visibility) {
        assertThat(actual.getPartyName()).isEqualTo(partyName);
        assertThat(actual.getRoleOnCase()).isEqualTo(roleOnCase);
        assertThat(actual.getGroupId()).isEqualTo(groupId);
        assertThat(actual.getVisibility()).isEqualTo(visibility);
    }
}
