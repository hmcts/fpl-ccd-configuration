package uk.gov.hmcts.reform.fpl.model;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.enums.OtherApplicationType;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.common.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.common.OtherApplicationsBundle;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.judicialmessage.JudicialMessage;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrder;
import uk.gov.hmcts.reform.fpl.model.order.HearingOrdersBundle;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
import uk.gov.hmcts.reform.fpl.model.order.generated.GeneratedOrder;
import uk.gov.hmcts.reform.fpl.model.order.generated.OrderExclusionClause;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.ElementUtils;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;
import uk.gov.hmcts.reform.fpl.utils.IncrementalInteger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SEND_TO_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.AGREED_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.C21;
import static uk.gov.hmcts.reform.fpl.enums.HearingOrderType.DRAFT_CMO;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.ADJOURNED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_AND_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingStatus.VACATED_TO_BE_RE_LISTED;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.PLACEMENT_HEARING;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C100_CHILD_ARRANGEMENTS;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C17A_EXTENSION_OF_ESO;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C19_WARRANT_TO_ASSISTANCE;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_PARENTAL_RESPONSIBILITY;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C1_WITH_SUPPLEMENT;
import static uk.gov.hmcts.reform.fpl.enums.OtherApplicationType.C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.model.document.SealType.WELSH;
import static uk.gov.hmcts.reform.fpl.model.document.SealType.ENGLISH;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElementsWithUUIDs;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentative;

class CaseDataTest {

    private static final String EXCLUSION_CLAUSE = "exclusionClause";
    private static final UUID[] HEARING_IDS = {randomUUID(), randomUUID(), randomUUID(), randomUUID()};
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final UUID cmoID = randomUUID();
    private final LocalDateTime futureDate = NOW.plusDays(1);
    private final LocalDateTime pastDate = NOW.minusDays(1);

    @Test
    void shouldGetAllOthersWhenFirstAndAdditionalOthersExist() {
        Other other1 = otherWithName("John");
        Other other2 = otherWithName("Sam");

        CaseData caseData = caseData(Others.builder().firstOther(other1).additionalOthers(wrapElements(other2)));

        assertThat(caseData.getAllOthers().get(0).getValue()).isEqualTo(other1);
        assertThat(caseData.getAllOthers().get(1).getValue()).isEqualTo(other2);
    }

    @Test
    void shouldGetAllOthersWhenFirstOtherIsEmpty() {
        Other other1 = Other.builder().build();
        Other other2 = otherWithName("Sam");

        CaseData caseData = caseData(Others.builder().firstOther(other1).additionalOthers(wrapElements(other2)));

        assertThat(caseData.getAllOthers()).hasSize(1);
        assertThat(caseData.getAllOthers().get(0).getValue()).isEqualTo(other2);
    }

    @Test
    void shouldGetEmptyListOfOthersWhenOthersIsNull() {
        CaseData caseData = CaseData.builder().build();

        assertThat(caseData.getAllOthers()).isEmpty();
    }

    @Test
    void shouldGetEmptyListOfOthersWhenOthersAreEmpty() {
        CaseData caseData = caseData(Others.builder());

        assertThat(caseData.getAllOthers()).isEmpty();
    }

    @Test
    void shouldGetFirstOtherWhenNoAdditionalOthers() {
        Other other1 = otherWithName("John");
        CaseData caseData = caseData(Others.builder().firstOther(other1));

        assertThat(caseData.getAllOthers().get(0).getValue()).isEqualTo(other1);
    }

    @Test
    void shouldFindFirstOther() {
        Other other1 = otherWithName("John");
        CaseData caseData = caseData(Others.builder().firstOther(other1));

        assertThat(caseData.findOther(0)).contains(other1);
    }

    @Test
    void shouldNotFindNonExistingOther() {
        Other other1 = otherWithName("John");
        CaseData caseData = caseData(Others.builder().firstOther(other1));

        assertThat(caseData.findOther(1)).isEmpty();
    }

    @Test
    void shouldFindExistingOther() {
        Other other1 = otherWithName("John");
        Other other2 = otherWithName("Sam");
        CaseData caseData = CaseData.builder().others(Others.builder()
            .firstOther(other1)
            .additionalOthers(wrapElements(other2))
            .build())
            .build();

        assertThat(caseData.findOther(1)).contains(other2);
    }

    @Test
    void shouldFindExistingRespondent() {
        Respondent respondent = Respondent.builder().build();
        CaseData caseData = CaseData.builder().respondents1(wrapElements(respondent)).build();

        assertThat(caseData.findRespondent(0)).contains(respondent);
    }

    @Test
    void shouldNotFindNonExistingRespondent() {
        Respondent respondent = Respondent.builder().build();
        CaseData caseData = CaseData.builder().respondents1(wrapElements(respondent)).build();

        assertThat(caseData.findRespondent(1)).isEmpty();
    }

    @Test
    void shouldGetOrderAppliesToAllChildrenWithValueAsYesWhenOnlyOneChildOnCase() {
        CaseData caseData = CaseData.builder().children1(List.of(testChild())).build();
        assertThat(caseData.getOrderAppliesToAllChildren()).isEqualTo("Yes");
    }

    @Test
    void shouldGetOrderAppliesToAllChildrenWithCustomValueWhenMultipleChildrenOnCase() {
        CaseData caseData = CaseData.builder().children1(testChildren()).orderAppliesToAllChildren("No").build();
        assertThat(caseData.getOrderAppliesToAllChildren()).isEqualTo("No");
    }

    @Test
    void shouldReturnTrueWhenUsingTemporaryJudge() {
        CaseData caseData = CaseData.builder().judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
            .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
            .judgeEmailAddress("test@test.com")
            .build()).build();

        assertThat(caseData.hasSelectedTemporaryJudge(caseData.getJudgeAndLegalAdvisor())).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUsingAllocatedJudge() {
        CaseData caseData = CaseData.builder().judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
            .build())
            .allocatedJudge(Judge.builder()
                .judgeTitle(JudgeOrMagistrateTitle.HER_HONOUR_JUDGE)
                .judgeEmailAddress("test@test.com")
                .build())
            .build();

        assertThat(caseData.hasSelectedTemporaryJudge(caseData.getJudgeAndLegalAdvisor())).isFalse();
    }

    private CaseData caseData(Others.OthersBuilder othersBuilder) {
        return CaseData.builder().others(othersBuilder.build()).build();
    }

    private Other otherWithName(String name) {
        return Other.builder().name(name).build();
    }

    @Nested
    class GetFurtherDirectionsText {
        private FurtherDirections furtherDirections;
        private CaseData caseData;

        @Test
        void shouldReturnDirectionTextWhenFurtherDirectionIsPopulated() {
            furtherDirections = FurtherDirections.builder().directions("some text").build();
            caseData = CaseData.builder().orderFurtherDirections(furtherDirections).build();

            assertThat(caseData.getFurtherDirectionsText()).isEqualTo("some text");
        }

        @Test
        void shouldReturnEmptyStringWhenFurtherDirectionIsNotPopulated() {
            furtherDirections = FurtherDirections.builder().build();
            caseData = CaseData.builder().orderFurtherDirections(furtherDirections).build();

            assertThat(caseData.getFurtherDirectionsText()).isEmpty();
        }

        @Test
        void shouldReturnEmptyStringWhenFurtherDirectionIsNull() {
            caseData = CaseData.builder().build();

            assertThat(caseData.getFurtherDirectionsText()).isEmpty();
        }
    }

    @Nested
    class GetLastC2DocumentBundle {
        private CaseData caseData;

        @Test
        void shouldReturnLastC2DocumentBundleWhenC2DocumentBundleIsPopulated() {
            C2DocumentBundle c2DocumentBundle1 = C2DocumentBundle.builder()
                .description("Mock bundle 1")
                .build();

            C2DocumentBundle c2DocumentBundle2 = C2DocumentBundle.builder()
                .description("Mock bundle 2")
                .build();

            List<Element<C2DocumentBundle>> c2DocumentBundle = wrapElements(c2DocumentBundle1, c2DocumentBundle2);

            caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundle).build();
            assertThat(caseData.getLastC2DocumentBundle()).isEqualTo(c2DocumentBundle2);
        }

        @Test
        void shouldReturnNullIfC2DocumentBundleIsNotPopulated() {
            caseData = CaseData.builder().c2DocumentBundle(null).build();
            assertThat(caseData.getLastC2DocumentBundle()).isNull();
        }
    }

    @Test
    void shouldReturnTrueWhenAllocatedJudgeExists() {
        CaseData caseData = CaseData.builder().allocatedJudge(Judge.builder()
            .judgeFullName("Test Judge")
            .build()).build();

        assertThat(caseData.allocatedJudgeExists()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAllocatedJudgeDoesNotExist() {
        CaseData caseData = CaseData.builder().build();

        assertThat(caseData.allocatedJudgeExists()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenAllocatedJudgeEmailHasEmail() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress("test@test.com")
                .build())
            .build();

        assertThat(caseData.hasAllocatedJudgeEmail()).isTrue();
    }

    @Test
    void shouldReturnFalseWhenAllocatedJudgeEmailIsAnEmptyString() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeEmailAddress("")
                .build())
            .build();

        assertThat(caseData.hasAllocatedJudgeEmail()).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAllocatedJudgeEmailDoesNotExist() {
        CaseData caseData = CaseData.builder()
            .allocatedJudge(Judge.builder()
                .judgeLastName("Stevens")
                .build())
            .build();

        assertThat(caseData.hasAllocatedJudgeEmail()).isFalse();
    }

    @Test
    void shouldReturnTrueWhenFutureHearingExists() {
        List<Element<HearingBooking>> hearingBooking =
            List.of(element(createHearingBooking(NOW.plusDays(6), NOW.plusDays(6))));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBooking)
            .build();

        boolean hearingBookingInFuture = caseData.hasFutureHearing(hearingBooking);

        assertThat(hearingBookingInFuture).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoFutureHearingExists() {
        List<Element<HearingBooking>> hearingBooking =
            List.of(element(createHearingBooking(NOW.minusDays(6), NOW.plusDays(6))));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBooking)
            .build();

        boolean hearingBookingInFuture = caseData.hasFutureHearing(hearingBooking);

        assertThat(hearingBookingInFuture).isFalse();
    }

    @Test
    void shouldReturnTrueWhenStartDateIsInThePast() {
        CaseData caseData = CaseData.builder()
            .hearingStartDate(pastDate)
            .hearingEndDate(futureDate)
            .build();

        boolean hearingInPast = caseData.isHearingDateInPast();

        assertThat(hearingInPast).isTrue();
    }

    @Test
    void shouldReturnTrueWhenEndHearingDateIsInThePast() {
        CaseData caseData = CaseData.builder()
            .hearingStartDate(futureDate)
            .hearingEndDate(pastDate)
            .build();

        boolean hearingInPast = caseData.isHearingDateInPast();

        assertThat(hearingInPast).isTrue();
    }

    @Test
    void shouldReturnTrueWhenHearingDateIsInThePast() {
        CaseData caseData = CaseData.builder()
            .hearingStartDate(futureDate)
            .hearingEndDate(pastDate)
            .build();

        boolean hearingInPast = caseData.isHearingDateInPast();

        assertThat(hearingInPast).isTrue();
    }

    @Test
    void shouldReturnFalseWhenHearingDateIsTheFuture() {
        Time today = new FixedTimeConfiguration().stoppedTime();
        CaseData caseData = CaseData.builder()
            .hearingStartDate(today.now().plusDays(1))
            .hearingEndDate(today.now().plusDays(1))
            .build();

        boolean hearingInPast = caseData.isHearingDateInPast();

        assertThat(hearingInPast).isFalse();
    }

    @Test
    void shouldReturnFalseWhenWelshRequestedNotSelected() {
        CaseData caseData = CaseData.builder()
            .languageRequirement(null)
            .build();

        boolean languageRequirement = caseData.isWelshLanguageRequested();

        assertThat(languageRequirement).isFalse();
    }

    @Test
    void shouldReturnFalseWhenWelshRequestedSetToNo() {
        CaseData caseData = CaseData.builder()
            .languageRequirement("No")
            .build();

        boolean languageRequirement = caseData.isWelshLanguageRequested();

        assertThat(languageRequirement).isFalse();
    }

    @Test
    void shouldReturnTrueWhenWelshRequestedSetToYes() {
        CaseData caseData = CaseData.builder()
            .languageRequirement("Yes")
            .build();

        boolean languageRequirement = caseData.isWelshLanguageRequested();

        assertThat(languageRequirement).isTrue();
    }

    @Test
    void testGetExclusionClauseTextIfNull() {
        CaseData underTest = CaseData.builder().build();

        assertThat(underTest.getExclusionClauseText()).isEmpty();
    }

    @Test
    void testGetExclusionClauseTextIfExists() {
        CaseData underTest = CaseData.builder().orderExclusionClause(OrderExclusionClause.builder()
            .exclusionClause(EXCLUSION_CLAUSE)
            .build()).build();

        assertThat(underTest.getExclusionClauseText()).isEqualTo(EXCLUSION_CLAUSE);
    }

    @Nested
    class GetNextHearingAfterCmo {
        @Test
        void shouldGetAllHearings() {
            Element<HearingBooking> hearing = element(HearingBooking.builder()
                .startDate(now())
                .build());
            Element<HearingBooking> adjournedHearing = element(HearingBooking.builder()
                .startDate(now())
                .status(ADJOURNED)
                .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(hearing))
                .cancelledHearingDetails(List.of(adjournedHearing))
                .build();

            List<Element<HearingBooking>> allHearings = caseData.getAllHearings();
            assertThat(allHearings).containsExactly(hearing, adjournedHearing);
        }

        @Test
        void shouldReturnEmptyListWhenNoHearings() {
            CaseData caseData = CaseData.builder().build();

            List<Element<HearingBooking>> allHearings = caseData.getAllHearings();
            assertThat(allHearings).isEmpty();
        }

        @Test
        void shouldReturnExpectedNextHearingBooking() {
            HearingBooking nextHearing = createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7),
                ISSUE_RESOLUTION, randomUUID());

            List<Element<HearingBooking>> hearingBookings = List.of(
                element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT, null)),
                element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, cmoID)),
                element(nextHearing),
                element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION, null)));

            CaseData caseData = CaseData.builder().hearingDetails(hearingBookings).build();
            Optional<HearingBooking> nextHearingBooking = caseData.getNextHearingAfterCmo(cmoID);

            assertThat(nextHearingBooking).isPresent().contains(nextHearing);
        }

        @Test
        void shouldReturnExpectedHearingWhenCMOAssociatedWithAdjournedHearing() {
            HearingBooking nextHearing = createHearingBooking(futureDate.plusDays(6), futureDate.plusDays(7),
                ISSUE_RESOLUTION, randomUUID());

            List<Element<HearingBooking>> hearingBookings = List.of(
                element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT, null)),
                element(nextHearing),
                element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION, null)));

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearingBookings)
                .cancelledHearingDetails(List.of(element(
                    createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, cmoID))))
                .build();
            Optional<HearingBooking> nextHearingBooking = caseData.getNextHearingAfterCmo(cmoID);

            assertThat(nextHearingBooking).isPresent().contains(nextHearing);
        }

        @Test
        void shouldThrowAnExceptionWhenNoHearingsNotMatchCmo() {
            UUID cmoID = randomUUID();
            List<Element<HearingBooking>> hearingBookings = List.of(
                element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL,
                    randomUUID())),
                element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT,
                    randomUUID())),
                element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION,
                    randomUUID())));

            CaseData caseData = CaseData.builder().hearingDetails(hearingBookings).build();

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> caseData.getNextHearingAfterCmo(cmoID));

            assertThat(exception).hasMessageContaining("Failed to find hearing matching cmo id", cmoID);
        }

        @Test
        void shouldReturnEmptyOptionalHearingIfNoUpcomingHearingsAreFound() {
            List<Element<HearingBooking>> hearingBookings = List.of(
                element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6), FINAL, cmoID)),
                element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3), CASE_MANAGEMENT,
                    randomUUID())),
                element(createHearingBooking(futureDate, futureDate.plusDays(1), ISSUE_RESOLUTION, randomUUID())));

            CaseData caseData = CaseData.builder().hearingDetails(hearingBookings).build();
            Optional<HearingBooking> nextHearingBooking = caseData.getNextHearingAfterCmo(cmoID);

            assertThat(nextHearingBooking).isNotPresent();
        }
    }

    @Nested
    class BuildDynamicHearingList {
        @Test
        void shouldBuildDynamicHearingListFromHearingDetails() {
            List<Element<HearingBooking>> hearingBookings = List.of(
                element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
                element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
                element(createHearingBooking(futureDate, futureDate.plusDays(1))));

            CaseData caseData = CaseData.builder().hearingDetails(hearingBookings).build();
            DynamicList expectedDynamicList = ElementUtils
                .asDynamicList(hearingBookings, null, HearingBooking::toLabel);

            assertThat(caseData.buildDynamicHearingList())
                .isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicHearingListWithSelectorPropertyFromHearingDetails() {
            UUID selectedHearingId = randomUUID();

            List<Element<HearingBooking>> hearingBookings = List.of(
                element(createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
                element(createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
                element(createHearingBooking(futureDate, futureDate.plusDays(1))),
                element(selectedHearingId, createHearingBooking(futureDate, futureDate.plusDays(3)))
            );

            CaseData caseData = CaseData.builder().hearingDetails(hearingBookings).build();
            DynamicList expectedDynamicList = ElementUtils
                .asDynamicList(hearingBookings, selectedHearingId, HearingBooking::toLabel);

            assertThat(caseData.buildDynamicHearingList(selectedHearingId))
                .isEqualTo(expectedDynamicList);
        }
    }

    @Nested
    class BuildDynamicC2DocumentBundleList {
        @Test
        void shouldBuildDynamicC2DocumentBundleListFromC2Documents() {
            List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
                element(buildC2DocumentBundle(futureDate.plusDays(2))),
                element(buildC2DocumentBundle(futureDate.plusDays(1)))
            );

            CaseData caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundle).build();
            IncrementalInteger i = new IncrementalInteger(1);
            DynamicList expectedDynamicList = ElementUtils
                .asDynamicList(c2DocumentBundle, null, documentBundle ->
                    documentBundle.toLabel(i.getAndIncrement()));

            assertThat(caseData.buildC2DocumentDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicHearingListWithSelectorPropertyFromHearingDetails() {
            UUID selectedC2Id = randomUUID();

            List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
                element(buildC2DocumentBundle(futureDate.plusDays(2))),
                element(buildC2DocumentBundle(futureDate.plusDays(1))),
                element(selectedC2Id, buildC2DocumentBundle(futureDate.plusDays(5)))
            );

            CaseData caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundle).build();
            IncrementalInteger i = new IncrementalInteger(1);
            DynamicList expectedDynamicList = ElementUtils
                .asDynamicList(c2DocumentBundle, null, documentBundle ->
                    documentBundle.toLabel(i.getAndIncrement()));

            assertThat(caseData.buildC2DocumentDynamicList()).isEqualTo(expectedDynamicList);
        }
    }

    @Nested
    class GetC2DocumentBundleByUUID {
        @Test
        void shouldReturnC2DocumentBundleWhenIdMatches() {
            UUID elementId = randomUUID();
            C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().author("Test").build();
            List<Element<C2DocumentBundle>> c2DocumentBundles = List.of(
                element(elementId, c2DocumentBundle),
                element(C2DocumentBundle.builder().build()));

            CaseData caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundles).build();

            assertThat(caseData.getC2DocumentBundleByUUID(elementId)).isEqualTo(c2DocumentBundle);
        }

        @Test
        void shouldReturnNullWhenIdDoNotMatch() {
            UUID elementId = randomUUID();
            List<Element<C2DocumentBundle>> c2DocumentBundles = List.of(
                element(C2DocumentBundle.builder().build()),
                element(C2DocumentBundle.builder().build()));

            CaseData caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundles).build();

            assertThat(caseData.getC2DocumentBundleByUUID(elementId)).isNull();
        }
    }

    @Nested
    class HasC2DocumentBundle {
        @Test
        void shouldReturnTrueIfC2DocumentBundleIsPresentOnCaseDataAndNotEmpty() {
            List<Element<C2DocumentBundle>> c2DocumentBundles = List.of(
                element(C2DocumentBundle.builder().build()),
                element(C2DocumentBundle.builder().build()));

            CaseData caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundles).build();

            assertThat(caseData.hasC2DocumentBundle()).isTrue();
        }

        @Test
        void shouldReturnFalseIfC2DocumentBundleIsPresentOnCaseDataButIsEmpty() {
            CaseData caseData = CaseData.builder().c2DocumentBundle(List.of()).build();
            assertThat(caseData.hasC2DocumentBundle()).isFalse();
        }

        @Test
        void shouldReturnFalseIfC2DocumentBundleIsNotPresentOnCaseData() {
            CaseData caseData = CaseData.builder().build();
            assertThat(caseData.hasC2DocumentBundle()).isFalse();
        }
    }

    @Nested
    class HasApplicationBundles {

        @Test
        void shouldReturnTrueIfC2DocumentBundleIsPresentOnCaseDataAndNotEmpty() {
            List<Element<C2DocumentBundle>> c2DocumentBundles = List.of(
                element(C2DocumentBundle.builder().build()),
                element(C2DocumentBundle.builder().build()));

            CaseData caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundles).build();

            assertThat(caseData.hasApplicationBundles()).isTrue();
        }

        @Test
        void shouldReturnTrueWhenAdditionalApplicationsBundlesExist() {
            List<Element<AdditionalApplicationsBundle>> additionalApplications = List.of(
                element(AdditionalApplicationsBundle.builder().build()),
                element(AdditionalApplicationsBundle.builder().build()));

            CaseData caseData = CaseData.builder()
                .additionalApplicationsBundle(additionalApplications)
                .build();

            assertThat(caseData.hasApplicationBundles()).isTrue();
        }

        @Test
        void shouldReturnTrueWhenC2DocumentsBundleAndAdditionalApplicationsBundlesExist() {
            CaseData caseData = CaseData.builder()
                .additionalApplicationsBundle(wrapElements(AdditionalApplicationsBundle.builder().build()))
                .c2DocumentBundle(wrapElements(C2DocumentBundle.builder().build()))
                .build();

            assertThat(caseData.hasApplicationBundles()).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnFalseIfC2DocumentBundleIsNullOrEmpty(
            List<Element<C2DocumentBundle>> c2Bundles) {
            CaseData caseData = CaseData.builder().c2DocumentBundle(c2Bundles).build();
            assertThat(caseData.hasApplicationBundles()).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnFalseWhenCaseDataDoesNotHaveAdditionalApplicationsBundle(
            List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundles) {
            CaseData caseData = CaseData.builder()
                .additionalApplicationsBundle(additionalApplicationsBundles)
                .build();

            assertThat(caseData.hasApplicationBundles()).isFalse();
        }
    }

    @Nested
    class GetApplicationBundleByUUID {
        @Test
        void shouldReturnC2DocumentBundleWhenIdMatchesWithTheC2DocumentBundlesCollection() {
            UUID selectedId = randomUUID();
            C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder().author("Test").build();

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(
                    element(selectedId, c2DocumentBundle),
                    element(C2DocumentBundle.builder().build())))
                .additionalApplicationsBundle(List.of(element(
                    AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(C2DocumentBundle.builder().id(randomUUID()).build())
                        .build()
                )))
                .build();

            assertThat(caseData.getApplicationBundleByUUID(selectedId)).isEqualTo(c2DocumentBundle);
        }

        @Test
        void shouldReturnC2DocumentBundleWhenIdMatchesWithTheAdditionalApplicationsBundlesCollection() {
            UUID selectedId = randomUUID();
            String uploadedTime = now().toString();
            C2DocumentBundle c2DocumentBundle = C2DocumentBundle.builder()
                .id(selectedId).author("Test").build();

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder().uploadedDateTime(uploadedTime).build())))
                .additionalApplicationsBundle(List.of(element(
                    AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(c2DocumentBundle)
                        .otherApplicationsBundle(OtherApplicationsBundle.builder()
                            .applicationType(C17A_EXTENSION_OF_ESO).id(randomUUID()).build())
                        .build())))
                .build();

            assertThat(caseData.getApplicationBundleByUUID(selectedId)).isEqualTo(c2DocumentBundle);
        }

        @Test
        void shouldReturnOtherApplicationBundleWhenIdMatchesWithTheAdditionalApplicationsBundlesCollection() {
            UUID selectedId = randomUUID();
            OtherApplicationsBundle otherApplicationsBundle = OtherApplicationsBundle.builder()
                .id(selectedId).author("Test").build();

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(element(C2DocumentBundle.builder().build())))
                .additionalApplicationsBundle(List.of(element(
                    AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(C2DocumentBundle.builder().id(randomUUID()).build())
                        .otherApplicationsBundle(otherApplicationsBundle)
                        .build())))
                .build();

            assertThat(caseData.getApplicationBundleByUUID(selectedId)).isEqualTo(otherApplicationsBundle);
        }

        @Test
        void shouldReturnNullWhenIdDoNotMatchWithC2DocumentsBundlesAndAdditionalApplicationsBundle() {
            UUID elementId = randomUUID();

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(
                    element(C2DocumentBundle.builder().build()), element(C2DocumentBundle.builder().build())))
                .additionalApplicationsBundle(List.of(
                    element(AdditionalApplicationsBundle.builder()
                        .c2DocumentBundle(C2DocumentBundle.builder().id(randomUUID()).build())
                        .otherApplicationsBundle(OtherApplicationsBundle.builder().id(randomUUID()).build())
                        .build())))
                .build();

            assertThat(caseData.getApplicationBundleByUUID(elementId)).isNull();
        }
    }

    @Nested
    class BuildApplicationBundlesDynamicList {
        private final String formattedDate = "5 December 2020, 3:00pm";
        private final String formattedFutureDate = "6 December 2020, 3:00pm";
        private final String formattedPastDate = "4 December 2020, 3:00pm";
        private final String july2020 = "4 July 2020, 3:00pm";
        private final String aug2020 = "4 August 2020, 3:00pm";
        private final String may2021 = "6 May 2021, 3:00pm";

        private final Element<C2DocumentBundle> pastC2Element = buildC2WithFormattedDate(formattedPastDate);
        private final Element<C2DocumentBundle> presentC2Element = buildC2WithFormattedDate(formattedDate);
        private final Element<C2DocumentBundle> futureC2Element = buildC2WithFormattedDate(formattedFutureDate);


        private final C2DocumentBundle pastC2Bundle = buildC2WithFormattedDate(july2020).getValue();
        private final C2DocumentBundle pastC2BundleConf = buildC2WithFormattedDate(aug2020).getValue();
        private final C2DocumentBundle presentC2Bundle = buildC2WithFormattedDate(formattedDate).getValue();
        private final C2DocumentBundle futureC2Bundle = buildC2WithFormattedDate(may2021).getValue();

        @Test
        void shouldBuildDynamicListFromC2Documents() {
            List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(futureC2Element, pastC2Element);

            CaseData caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundle).build();

            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(futureC2Element.getId(), "C2, " + futureC2Element.getValue().getUploadedDateTime()),
                Pair.of(pastC2Element.getId(), "C2, " + pastC2Element.getValue().getUploadedDateTime())
            );

            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicListFromC2DocumentsWithinAdditionalApplicationsBundle() {
            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(
                element(AdditionalApplicationsBundle.builder().c2DocumentBundle(pastC2Bundle).build()),
                element(AdditionalApplicationsBundle.builder().c2DocumentBundleConfidential(pastC2BundleConf).build()),
                element(AdditionalApplicationsBundle.builder().c2DocumentBundle(futureC2Bundle).build()));

            CaseData caseData = CaseData.builder().additionalApplicationsBundle(additionalBundles).build();

            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(futureC2Bundle.getId(), "C2, " + futureC2Bundle.getUploadedDateTime()),
                Pair.of(pastC2BundleConf.getId(), "C2, " + pastC2BundleConf.getUploadedDateTime()),
                Pair.of(pastC2Bundle.getId(), "C2, " + pastC2Bundle.getUploadedDateTime())
            );

            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicListFromC2DocumentsAndC2DocumentsWithinAdditionalDocumentsBundles() {
            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(futureC2Bundle)
                    .build()));

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(pastC2Element))
                .additionalApplicationsBundle(additionalBundles)
                .build();

            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(futureC2Bundle.getId(), "C2, " + futureC2Bundle.getUploadedDateTime()),
                Pair.of(pastC2Element.getId(), "C2, " + pastC2Element.getValue().getUploadedDateTime())
            );

            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicListFromC2DocumentsAndC2DocumentsPlusOtherWithinAdditionalDocumentsBundles() {
            OtherApplicationsBundle otherBundle = buildOtherApplicationBundle(
                C1_PARENTAL_RESPONSIBILITY, formattedFutureDate);

            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(futureC2Bundle)
                    .otherApplicationsBundle(otherBundle)
                    .build()));

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(pastC2Element))
                .additionalApplicationsBundle(additionalBundles)
                .build();

            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(otherBundle.getId(), "C1, " + otherBundle.getUploadedDateTime()),
                Pair.of(futureC2Bundle.getId(), "C2, " + futureC2Bundle.getUploadedDateTime()),
                Pair.of(pastC2Element.getId(), "C2, " + pastC2Element.getValue().getUploadedDateTime())
            );
            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicApplicationsBundlesListAndSortByApplicationNumberAndLabel() {
            OtherApplicationsBundle otherBundle1 = buildOtherApplicationBundle(
                C19_WARRANT_TO_ASSISTANCE, formattedFutureDate);

            OtherApplicationsBundle otherBundle2 = buildOtherApplicationBundle(
                C3_SEARCH_TAKE_CHARGE_AND_DELIVERY_OF_A_CHILD, formattedFutureDate);

            OtherApplicationsBundle otherBundle3 = buildOtherApplicationBundle(
                C100_CHILD_ARRANGEMENTS, formattedFutureDate);

            OtherApplicationsBundle otherBundle4 = buildOtherApplicationBundle(
                C1_WITH_SUPPLEMENT, formattedFutureDate);

            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(element(
                AdditionalApplicationsBundle.builder().c2DocumentBundle(pastC2Bundle)
                    .otherApplicationsBundle(otherBundle1).build()),
                element(AdditionalApplicationsBundle.builder().otherApplicationsBundle(otherBundle2).build()),
                element(AdditionalApplicationsBundle.builder().otherApplicationsBundle(otherBundle3).build()),
                element(AdditionalApplicationsBundle.builder().otherApplicationsBundle(otherBundle4).build()));

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(futureC2Element))
                .additionalApplicationsBundle(additionalBundles)
                .build();

            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(otherBundle4.getId(), "C1, " + otherBundle4.getUploadedDateTime()),
                Pair.of(futureC2Element.getId(), "C2, " + futureC2Element.getValue().getUploadedDateTime()),
                Pair.of(pastC2Bundle.getId(), "C2, " + pastC2Bundle.getUploadedDateTime()),
                Pair.of(otherBundle2.getId(), "C3, " + otherBundle2.getUploadedDateTime()),
                Pair.of(otherBundle1.getId(), "C19, " + otherBundle1.getUploadedDateTime()),
                Pair.of(otherBundle3.getId(), "C100, " + otherBundle3.getUploadedDateTime())
            );

            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildEmptyDynamicListWhenC2DocumentsAndAdditionalApplicationsBundlesDoNotExist() {
            CaseData caseData = CaseData.builder().build();
            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(
                DynamicList.builder().value(DynamicListElement.builder().build()).listItems(List.of()).build());
        }

        @Test
        void shouldGetTheSelectedBundleFromTheC2AndAdditionalApplicationsDynamicList() {
            OtherApplicationsBundle otherBundle = buildOtherApplicationBundle(
                C1_PARENTAL_RESPONSIBILITY, formattedFutureDate);

            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(futureC2Bundle)
                    .otherApplicationsBundle(otherBundle)
                    .build()));

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(pastC2Element))
                .additionalApplicationsBundle(additionalBundles)
                .build();

            assertThat(caseData.getApplicationBundleByUUID(pastC2Element.getId())).isEqualTo(pastC2Element.getValue());
            assertThat(caseData.getApplicationBundleByUUID(futureC2Bundle.getId())).isEqualTo(futureC2Bundle);
            assertThat(caseData.getApplicationBundleByUUID(otherBundle.getId())).isEqualTo(otherBundle);
        }

        @Test
        void shouldSortByDateWithinC2Bundle() {
            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(futureC2Element.getId(), "C2, " + futureC2Element.getValue().getUploadedDateTime()),
                Pair.of(presentC2Element.getId(), "C2, " + presentC2Element.getValue().getUploadedDateTime()),
                Pair.of(pastC2Element.getId(), "C2, " + pastC2Element.getValue().getUploadedDateTime())
            );

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(presentC2Element, pastC2Element, futureC2Element))
                .build();

            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldSortByApplicationTypeThenDateWithinFullyPopulatedApplicationBundle() {
            OtherApplicationsBundle pastOther = buildOtherApplicationBundle(
                C1_PARENTAL_RESPONSIBILITY, "6 September 2020, 3:00pm");

            OtherApplicationsBundle presentOther = buildOtherApplicationBundle(
                C19_WARRANT_TO_ASSISTANCE, formattedFutureDate);

            OtherApplicationsBundle futureOther = buildOtherApplicationBundle(
                C1_PARENTAL_RESPONSIBILITY, "6 March 2021, 3:00pm");

            Element<AdditionalApplicationsBundle> pastBundle = element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(pastC2Bundle)
                    .otherApplicationsBundle(pastOther)
                    .build());

            Element<AdditionalApplicationsBundle> presentBundle = element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(presentC2Bundle)
                    .otherApplicationsBundle(presentOther)
                    .build());

            Element<AdditionalApplicationsBundle> futureBundle = element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(futureC2Bundle)
                    .otherApplicationsBundle(futureOther)
                    .build());

            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(
                futureBundle, pastBundle, presentBundle);

            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(futureOther.getId(), "C1, " + futureOther.getUploadedDateTime()),
                Pair.of(pastOther.getId(), "C1, " + pastOther.getUploadedDateTime()),
                Pair.of(futureC2Bundle.getId(), "C2, " + futureC2Bundle.getUploadedDateTime()),
                Pair.of(futureC2Element.getId(), "C2, " + futureC2Element.getValue().getUploadedDateTime()),
                Pair.of(presentC2Element.getId(), "C2, " + presentC2Element.getValue().getUploadedDateTime()),
                Pair.of(presentC2Bundle.getId(), "C2, " + presentC2Bundle.getUploadedDateTime()),
                Pair.of(pastC2Element.getId(), "C2, " + pastC2Element.getValue().getUploadedDateTime()),
                Pair.of(pastC2Bundle.getId(), "C2, " + pastC2Bundle.getUploadedDateTime()),
                Pair.of(presentOther.getId(), "C19, " + presentOther.getUploadedDateTime())
            );

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(presentC2Element, pastC2Element, futureC2Element))
                .additionalApplicationsBundle(additionalBundles)
                .build();

            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        private OtherApplicationsBundle buildOtherApplicationBundle(OtherApplicationType type,
                                                                    String formattedDateTime) {
            return OtherApplicationsBundle.builder()
                .applicationType(type)
                .id(randomUUID())
                .uploadedDateTime(formattedDateTime)
                .build();
        }
    }

    @Nested
    class GetNonCancelledHearings {

        @Test
        void shouldReturnNonCancelledHearingBookings() {
            Element<HearingBooking> todayHearingBooking = element(HearingBooking.builder()
                .startDate(now())
                .build());
            Element<HearingBooking> todayLateHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDate.now().plusDays(1).atStartOfDay().minusMinutes(1))
                .build());
            Element<HearingBooking> pastHearingBooking = element(HearingBooking.builder()
                .startDate(now().minusDays(1))
                .build());
            Element<HearingBooking> futureHearingBooking = element(HearingBooking.builder()
                .startDate(now().plusDays(1))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(
                    pastHearingBooking,
                    todayHearingBooking,
                    todayLateHearingBooking,
                    futureHearingBooking))
                .build();

            assertThat(caseData.getAllNonCancelledHearings())
                .containsExactly(pastHearingBooking, todayHearingBooking, todayLateHearingBooking,
                    futureHearingBooking);
        }

        @Test
        void shouldReturnEmptyListWhenNoNonCancelledHearingBookings() {
            Element<HearingBooking> hearing1 = element(HearingBooking.builder()
                .startDate(now().plusDays(1))
                .build());

            CaseData caseData = CaseData.builder()
                .cancelledHearingDetails(List.of(hearing1))
                .build();

            assertThat(caseData.getAllNonCancelledHearings()).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenNoHearingBookings() {
            CaseData caseData = CaseData.builder().build();

            assertThat(caseData.getAllNonCancelledHearings()).isEmpty();
        }
    }

    @Nested
    class GetPastAndTodayHearings {

        @Test
        void shouldReturnPastAndTodayHearingBookings() {
            Element<HearingBooking> todayHearingBooking = element(HearingBooking.builder()
                .startDate(now())
                .build());
            Element<HearingBooking> todayLateHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDate.now().plusDays(1).atStartOfDay().minusMinutes(1))
                .build());
            Element<HearingBooking> pastHearingBooking = element(HearingBooking.builder()
                .startDate(now().minusDays(1))
                .build());
            Element<HearingBooking> futureHearingBooking = element(HearingBooking.builder()
                .startDate(now().plusDays(1))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(
                    pastHearingBooking,
                    todayHearingBooking,
                    todayLateHearingBooking,
                    futureHearingBooking))
                .build();

            assertThat(caseData.getPastAndTodayHearings())
                .containsExactly(pastHearingBooking, todayHearingBooking, todayLateHearingBooking);
        }

        @Test
        void shouldReturnEmptyListWhenNoPastOrTodayHearingBookings() {
            Element<HearingBooking> futureHearingBooking = element(HearingBooking.builder()
                .startDate(now().plusDays(1))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(futureHearingBooking))
                .build();

            assertThat(caseData.getPastAndTodayHearings()).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenNoHearingBookings() {
            CaseData caseData = CaseData.builder().build();

            assertThat(caseData.getPastAndTodayHearings()).isEmpty();
        }
    }

    @Nested
    class GetFutureAndTodayHearings {

        @Test
        void shouldReturnFutureAndTodayHearingBookings() {
            Element<HearingBooking> todayHearingBooking = element(HearingBooking.builder()
                .startDate(now())
                .build());
            Element<HearingBooking> todayLateHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDate.now().plusDays(1).atStartOfDay().minusMinutes(1))
                .build());
            Element<HearingBooking> pastHearingBooking = element(HearingBooking.builder()
                .startDate(now().minusDays(1))
                .build());
            Element<HearingBooking> futureHearingBooking = element(HearingBooking.builder()
                .startDate(now().plusDays(1))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(
                    pastHearingBooking,
                    todayHearingBooking,
                    todayLateHearingBooking,
                    futureHearingBooking))
                .build();

            assertThat(caseData.getFutureAndTodayHearings())
                .containsExactly(todayHearingBooking, todayLateHearingBooking, futureHearingBooking);
        }

        @Test
        void shouldReturnEmptyListWhenNoFutureOrTodayHearingBookings() {
            Element<HearingBooking> pastHearingBooking = element(HearingBooking.builder()
                .startDate(now().minusDays(1))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(pastHearingBooking))
                .build();

            assertThat(caseData.getFutureAndTodayHearings()).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenNoHearingBookings() {
            CaseData caseData = CaseData.builder().build();

            assertThat(caseData.getFutureAndTodayHearings()).isEmpty();
        }
    }

    @Nested
    class ToBeReListedHearings {

        @Test
        void shouldReturnOnlyHearingsToBeReListed() {
            Element<HearingBooking> hearing1 = element(HearingBooking.builder()
                .status(ADJOURNED)
                .build());

            Element<HearingBooking> hearing2 = element(HearingBooking.builder()
                .status(ADJOURNED_TO_BE_RE_LISTED)
                .build());

            Element<HearingBooking> hearing3 = element(HearingBooking.builder()
                .status(ADJOURNED_AND_RE_LISTED)
                .build());

            Element<HearingBooking> hearing4 = element(HearingBooking.builder()
                .status(VACATED)
                .build());

            Element<HearingBooking> hearing5 = element(HearingBooking.builder()
                .status(VACATED_TO_BE_RE_LISTED)
                .build());

            Element<HearingBooking> hearing6 = element(HearingBooking.builder()
                .status(VACATED_AND_RE_LISTED)
                .build());

            CaseData caseData = CaseData.builder()
                .cancelledHearingDetails(List.of(hearing1, hearing2, hearing3, hearing4, hearing5, hearing6))
                .build();

            assertThat(caseData.getToBeReListedHearings()).containsExactly(hearing2, hearing5);
        }

        @Test
        void shouldReturnEmptyListWhenNoHearingsToBeReListed() {
            Element<HearingBooking> hearing1 = element(HearingBooking.builder()
                .status(ADJOURNED)
                .build());

            Element<HearingBooking> hearing2 = element(HearingBooking.builder()
                .status(VACATED_AND_RE_LISTED)
                .build());

            CaseData caseData = CaseData.builder()
                .cancelledHearingDetails(List.of(hearing1, hearing2))
                .build();

            assertThat(caseData.getToBeReListedHearings()).isEmpty();
        }

        @Test
        void shouldReturnEmptyListWhenNoCancelledHearings() {
            CaseData caseData = CaseData.builder().build();

            assertThat(caseData.getToBeReListedHearings()).isEmpty();
        }
    }

    @Nested
    class AddHearingBooking {

        @Test
        void shouldAddFirstHearingBooking() {
            Element<HearingBooking> firstHearingBooking = element(HearingBooking.builder()
                .startDate(now().plusDays(1))
                .build());

            CaseData caseData = CaseData.builder().build();

            caseData.addHearingBooking(firstHearingBooking);

            assertThat(caseData.getHearingDetails()).containsExactly(firstHearingBooking);
        }

        @Test
        void shouldAddNewHearingBooking() {
            Element<HearingBooking> existingHearingBooking = element(HearingBooking.builder()
                .startDate(now())
                .build());

            Element<HearingBooking> newHearingBooking = element(HearingBooking.builder()
                .startDate(now().plusDays(1))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(existingHearingBooking))
                .build();

            caseData.addHearingBooking(newHearingBooking);

            assertThat(caseData.getHearingDetails()).containsExactly(existingHearingBooking, newHearingBooking);
        }
    }

    @Nested
    class AddAdjournedOrVacatedHearingBooking {

        @Test
        void shouldAddFirstAdjournedOrVacatedHearingBooking() {
            Element<HearingBooking> firstAdjournedBooking = element(HearingBooking.builder()
                .status(ADJOURNED)
                .build());

            CaseData caseData = CaseData.builder()
                .build();

            caseData.addCancelledHearingBooking(firstAdjournedBooking);

            assertThat(caseData.getCancelledHearingDetails()).containsExactly(firstAdjournedBooking);
        }

        @Test
        void shouldAddNewAdjournedHearingBooking() {
            Element<HearingBooking> existingAdjournedHearingBooking = element(HearingBooking.builder()
                .startDate(now().minusDays(1))
                .status(ADJOURNED)
                .build());

            Element<HearingBooking> newAdjournedHearingBooking = element(HearingBooking.builder()
                .startDate(now().plusDays(1))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingDetails(newArrayList(existingAdjournedHearingBooking))
                .build();

            caseData.addHearingBooking(newAdjournedHearingBooking);

            assertThat(caseData.getHearingDetails())
                .containsExactly(existingAdjournedHearingBooking, newAdjournedHearingBooking);
        }
    }

    @Nested
    class FindHearingBookingElement {
        @Test
        void shouldFindHearingBookingElementWhenKeyMatchesHearingBookingElementUUID() {
            Element<HearingBooking> expectedHearingBookingElement
                = element(HEARING_IDS[2], createHearingBooking(futureDate, futureDate.plusDays(1)));

            List<Element<HearingBooking>> hearingBookings = new ArrayList<>(List.of(
                element(HEARING_IDS[0], createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
                element(HEARING_IDS[1], createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
                expectedHearingBookingElement,
                element(HEARING_IDS[3], createHearingBooking(futureDate.minusDays(2), futureDate.plusDays(3)))
            ));

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearingBookings)
                .build();

            Optional<Element<HearingBooking>> hearingBookingElement
                = caseData.findHearingBookingElement(HEARING_IDS[2]);

            assertThat(hearingBookingElement).contains(expectedHearingBookingElement);
        }

        @Test
        void shouldReturnAnEmptyOptionalWhenKeyDoesNotMatchHearingBookingElementUUID() {
            List<Element<HearingBooking>> hearingBookings = new ArrayList<>(List.of(
                element(HEARING_IDS[0], createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
                element(HEARING_IDS[1], createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
                element(HEARING_IDS[2], createHearingBooking(futureDate, futureDate.plusDays(1))),
                element(HEARING_IDS[3], createHearingBooking(futureDate.minusDays(2), futureDate.plusDays(3)))
            ));

            CaseData caseData = CaseData.builder()
                .hearingDetails(hearingBookings)
                .build();

            Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(randomUUID());

            assertThat(hearingBooking).isNotPresent();
        }

        @Test
        void shouldReturnAnEmptyOptionalWhenHearingDetailsDoNotExistOnCaseData() {
            CaseData caseData = CaseData.builder().build();
            Optional<Element<HearingBooking>> hearingBooking = caseData.findHearingBookingElement(randomUUID());

            assertThat(hearingBooking).isNotPresent();
        }
    }

    @Nested
    class GetHearingLinkedToUUID {
        @Test
        void hearingBookingShouldBePresentWhenHearingBookingCaseManagementOrderMatchesId() {
            UUID hearingId = UUID.randomUUID();

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(
                    element(HearingBooking.builder()
                        .caseManagementOrderId(hearingId)
                        .build())
                )).build();

            assertThat(caseData.getHearingLinkedToCMO(hearingId)).isPresent();
        }

        @Test
        void hearingBookingShouldNotBePresentWhenHearingBookingCaseManagementOrderDoesNotMatchId() {
            UUID hearingId = UUID.randomUUID();

            CaseData caseData = CaseData.builder()
                .hearingDetails(List.of(
                    element(HearingBooking.builder()
                        .caseManagementOrderId(UUID.randomUUID())
                        .build())
                )).build();

            assertThat(caseData.getHearingLinkedToCMO(hearingId)).isNotPresent();
        }
    }

    @Nested
    class GetNextHearingAfter {

        @Test
        void shouldReturnNextHearingAfterGivenTime() {
            HearingBooking pastHearing = HearingBooking.builder().startDate(now().minusDays(1)).build();
            HearingBooking futureHearing = HearingBooking.builder().startDate(now().plusDays(1)).build();

            CaseData caseData = CaseData.builder()
                .hearingDetails(wrapElements(pastHearing, futureHearing))
                .build();

            Optional<HearingBooking> foundHearing = caseData.getNextHearingAfter(now());

            assertThat(foundHearing).contains(futureHearing);
        }

        @Test
        void shouldReturnNothingIfNoHearingsAfterGivenTime() {
            HearingBooking pastHearing = HearingBooking.builder().startDate(now().minusDays(1)).build();

            CaseData caseData = CaseData.builder()
                .hearingDetails(wrapElements(pastHearing))
                .build();

            Optional<HearingBooking> foundHearing = caseData.getNextHearingAfter(now());

            assertThat(foundHearing).isEmpty();
        }
    }

    @Nested
    class GetFirstHearingOfType {

        @Test
        void shouldReturnFirstHearingOfGivenType() {
            HearingBooking placementHearing = hearingBooking(PLACEMENT_HEARING, now().plusDays(1));
            HearingBooking caseManagementHearing = hearingBooking(CASE_MANAGEMENT, now());
            HearingBooking laterCaseManagementHearing = hearingBooking(CASE_MANAGEMENT, now().plusDays(3));

            CaseData caseData = CaseData.builder()
                .hearingDetails(wrapElements(placementHearing, laterCaseManagementHearing, caseManagementHearing))
                .build();

            Optional<HearingBooking> foundHearing = caseData.getFirstHearingOfType(CASE_MANAGEMENT);

            assertThat(foundHearing).contains(caseManagementHearing);
        }

        @Test
        void shouldReturnEmptyOptionalWhenTypeIsNotInPopulatedList() {
            HearingBooking caseManagementHearing = hearingBooking(CASE_MANAGEMENT, now());

            CaseData caseData = CaseData.builder()
                .hearingDetails(wrapElements(caseManagementHearing))
                .build();

            Optional<HearingBooking> foundHearing = caseData.getFirstHearingOfType(PLACEMENT_HEARING);

            assertThat(foundHearing).isNotPresent();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmptyOptional(List<Element<HearingBooking>> hearingBookings) {
            CaseData caseData = CaseData.builder().hearingDetails(hearingBookings).build();

            Optional<HearingBooking> foundHearing = caseData.getFirstHearingOfType(CASE_MANAGEMENT);

            assertThat(foundHearing).isNotPresent();
        }

        private HearingBooking hearingBooking(HearingType type, LocalDateTime startDate) {
            return HearingBooking.builder()
                .type(type)
                .startDate(startDate)
                .build();
        }
    }

    @Nested
    class BuildJudicialMessageList {
        UUID firstId = randomUUID();
        UUID secondId = randomUUID();
        UUID thirdId = randomUUID();

        @Test
        void shouldBuildDynamicJudicialMessageListFromJudicialMessages() {
            List<Element<JudicialMessage>> judicialMessages = List.of(
                element(firstId, buildJudicialMessage("Subject 1", "Low", "11 November 2020", YES)),
                element(secondId, buildJudicialMessage("Subject 2", "Medium", "12 November 2020", NO)),
                element(thirdId, buildJudicialMessage("Subject 3", "High", "13 November 2020", YES))
            );

            CaseData caseData = CaseData.builder().judicialMessages(judicialMessages).build();
            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(firstId, "C2, Subject 1, 11 November 2020, Low"),
                Pair.of(secondId, "Subject 2, 12 November 2020, Medium"),
                Pair.of(thirdId, "C2, Subject 3, 13 November 2020, High")
            );

            assertThat(caseData.buildJudicialMessageDynamicList())
                .isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicJudicialMessageListWithSelectorPropertyFromJudicialMessage() {
            List<Element<JudicialMessage>> judicialMessages = List.of(
                element(firstId, buildJudicialMessage("Subject 1", "Low", "11 November 2020", YES)),
                element(secondId, buildJudicialMessage("Subject 2", "Medium", "12 November 2020", NO)),
                element(thirdId, buildJudicialMessage("Subject 3", "High", "13 November 2020", YES))
            );

            CaseData caseData = CaseData.builder().judicialMessages(judicialMessages).build();
            DynamicList expectedDynamicList = buildDynamicList(2,
                Pair.of(firstId, "C2, Subject 1, 11 November 2020, Low"),
                Pair.of(secondId, "Subject 2, 12 November 2020, Medium"),
                Pair.of(thirdId, "C2, Subject 3, 13 November 2020, High")
            );

            assertThat(caseData.buildJudicialMessageDynamicList(thirdId))
                .isEqualTo(expectedDynamicList);
        }

        private JudicialMessage buildJudicialMessage(String subject, String urgency, String dateSent,
                                                     YesNo isRelatedToC2) {
            return JudicialMessage.builder()
                .subject(subject)
                .urgency(urgency)
                .dateSent(dateSent)
                .isRelatedToC2(isRelatedToC2)
                .build();
        }
    }

    @Nested
    class GetRepresentativesByServedPreference {
        private Representative emailRepOne = testRepresentative(EMAIL);
        private Representative emailRepTwo = testRepresentative(EMAIL);
        private Representative digitalRepOne = testRepresentative(DIGITAL_SERVICE);
        private Representative digitalRepTwo = testRepresentative(DIGITAL_SERVICE);

        @Test
        void shouldReturnListOfEmailRepresentatives() {
            CaseData caseData = CaseData.builder()
                .representatives(getRepresentativesOfMixedServingPreferences())
                .build();

            List<Representative> emailRepresentatives = caseData.getRepresentativesByServedPreference(EMAIL);

            assertThat(emailRepresentatives).containsExactlyInAnyOrder(emailRepOne, emailRepTwo);
        }

        @Test
        void shouldReturnListOfDigitalRepresentatives() {
            CaseData caseData = CaseData.builder()
                .representatives(getRepresentativesOfMixedServingPreferences())
                .build();

            List<Representative> digitalRepresentatives
                = caseData.getRepresentativesByServedPreference(DIGITAL_SERVICE);

            assertThat(digitalRepresentatives).containsExactlyInAnyOrder(digitalRepOne, digitalRepTwo);
        }

        @Test
        void shouldReturnAnEmptyListWhenRepresentativesDoNotMatchServingPreference() {
            CaseData caseData = CaseData.builder()
                .representatives(getRepresentativesOfMixedServingPreferences())
                .build();

            List<Representative> digitalRepresentatives = caseData.getRepresentativesByServedPreference(POST);

            assertThat(digitalRepresentatives).isEmpty();
        }

        @Test
        void shouldReturnAnEmptyListWhenRepresentativesDoNotExist() {
            CaseData caseData = CaseData.builder()
                .build();

            List<Representative> digitalRepresentatives = caseData.getRepresentativesByServedPreference(POST);

            assertThat(digitalRepresentatives).isEmpty();
        }

        @Test
        void shouldReturnRepresentativesElementsByServedPreference() {
            UUID firstID = randomUUID();
            UUID secondID = randomUUID();
            List<Element<Representative>> expectedReps = List.of(element(firstID, emailRepOne),
                element(secondID, emailRepTwo));

            CaseData caseData = CaseData.builder()
                .representatives(List.of(element(firstID, emailRepOne), element(secondID, emailRepTwo),
                    element(UUID.randomUUID(), digitalRepOne)))
                .build();

            List<Element<Representative>> digitalRepresentatives
                = caseData.getRepresentativesElementsByServedPreference(EMAIL);

            assertThat(digitalRepresentatives).isEqualTo(expectedReps);
        }

        @Test
        void shouldReturnEmptyListIfNoRepresentativesByPreference() {
            CaseData caseData = CaseData.builder()
                .representatives(List.of(element(UUID.randomUUID(), emailRepOne),
                    element(UUID.randomUUID(), emailRepTwo)))
                .build();

            List<Element<Representative>> digitalRepresentatives
                = caseData.getRepresentativesElementsByServedPreference(DIGITAL_SERVICE);

            assertThat(digitalRepresentatives).isEmpty();
        }

        @Test
        void shouldReturnHearingOrdersBundlesForApproval() {
            Element<HearingOrdersBundle> bundle1 = element(randomUUID(),
                HearingOrdersBundle.builder()
                    .orders(newArrayList(
                        element(HearingOrder.builder().type(AGREED_CMO).status(SEND_TO_JUDGE).build())
                    )).build());

            Element<HearingOrdersBundle> bundle2 = element(randomUUID(),
                HearingOrdersBundle.builder()
                    .orders(newArrayList(
                        element(HearingOrder.builder().type(DRAFT_CMO).status(DRAFT).build()),
                        element(HearingOrder.builder().type(C21).status(SEND_TO_JUDGE).build())
                    )).build());

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(newArrayList(bundle1, bundle2))
                .build();

            assertThat(caseData.getBundlesForApproval())
                .extracting(Element::getId)
                .containsExactly(bundle1.getId(), bundle2.getId());
        }

        @Test
        void shouldReturnEmptyWhenNoHearingOrdersBundlesExistForApproval() {
            Element<HearingOrdersBundle> bundle1 = element(randomUUID(),
                HearingOrdersBundle.builder()
                    .orders(newArrayList(
                        element(HearingOrder.builder().type(DRAFT_CMO).status(DRAFT).build())
                    )).build());

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(newArrayList(bundle1))
                .build();

            assertThat(caseData.getBundlesForApproval()).isEmpty();
        }

        @Test
        void shouldReturnAllHearingOrdersBundlesForApproval() {
            Element<HearingOrdersBundle> bundle1 = element(randomUUID(),
                HearingOrdersBundle.builder()
                    .ordersCTSC(newArrayList(
                        element(HearingOrder.builder().type(AGREED_CMO).status(SEND_TO_JUDGE).build())))
                    .build());

            Element<HearingOrdersBundle> bundle2 = element(randomUUID(),
                HearingOrdersBundle.builder()
                    .orders(newArrayList(
                        element(HearingOrder.builder().type(DRAFT_CMO).status(DRAFT).build()),
                        element(HearingOrder.builder().type(C21).status(SEND_TO_JUDGE).build())
                    )).build());

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(newArrayList(bundle1, bundle2))
                .build();

            assertThat(caseData.getBundlesForApproval())
                .extracting(Element::getId)
                .containsExactly(bundle1.getId(), bundle2.getId());
        }

        private List<Element<Representative>> getRepresentativesOfMixedServingPreferences() {
            return List.of(
                element(emailRepOne),
                element(emailRepTwo),
                element(digitalRepOne),
                element(digitalRepTwo));
        }
    }

    @Nested
    class GetHearingOrdersBundlesDraftOrders {
        @Test
        void shouldReturnAListOfDraftCaseManagementOrdersWhenExistingWithinHearingOrderBundleDrafts() {
            Element<HearingOrder> draftCMOOne = element(randomUUID(), buildHearingOrder(DRAFT_CMO));
            Element<HearingOrder> draftCMOTwo = element(randomUUID(), buildHearingOrder(AGREED_CMO));
            Element<HearingOrder> draftCMOThree = element(randomUUID(), buildHearingOrder(DRAFT_CMO));
            Element<HearingOrder> draftOrder = element(buildHearingOrder(C21));

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(
                    element(HearingOrdersBundle.builder().orders(newArrayList(draftCMOOne, draftCMOTwo)).build()),
                    element(HearingOrdersBundle.builder().orders(newArrayList(draftCMOThree, draftOrder)).build())))
                .build();

            assertThat(caseData.getOrdersFromHearingOrderDraftsBundles()).isEqualTo(
                List.of(draftCMOOne, draftCMOTwo, draftCMOThree, draftOrder));
        }

        @Test
        void shouldReturnAnEmptyListIfHearingOrderBundlesDoNotExist() {
            CaseData caseData = CaseData.builder().build();

            assertThat(caseData.getOrdersFromHearingOrderDraftsBundles()).isEmpty();
        }
    }

    @Nested
    class GetDraftUploadedCMOWithId {

        @Test
        void shouldReturnEmptyWhenCaseDataDoesNotHaveDraftUploadedCMOs() {
            Element<HearingOrder> draftCMOOne = element(randomUUID(), buildHearingOrder(DRAFT_CMO));
            CaseData caseData = CaseData.builder()
                .draftUploadedCMOs(List.of(draftCMOOne, element(buildHearingOrder(DRAFT_CMO))))
                .build();

            assertThat(caseData.getDraftUploadedCMOWithId(null)).isEmpty();
        }

        @Test
        void shouldReturnDraftCMOWhenDraftUploadedCMOsContainTheExpectedOrder() {
            Element<HearingOrder> draftCMOOne = element(randomUUID(), buildHearingOrder(DRAFT_CMO));

            CaseData caseData = CaseData.builder()
                .draftUploadedCMOs(List.of(draftCMOOne, element(buildHearingOrder(DRAFT_CMO))))
                .build();

            Optional<Element<HearingOrder>> matchingHearingOrder =
                caseData.getDraftUploadedCMOWithId(draftCMOOne.getId());

            assertThat(matchingHearingOrder).isPresent().contains(draftCMOOne);
        }

        @Test
        void shouldReturnAnEmptyOptionalWhenDraftUploadedCMOsDoNotContainExpectedOrder() {
            Element<HearingOrder> draftCMOOne = element(randomUUID(), buildHearingOrder(DRAFT_CMO));

            CaseData caseData = CaseData.builder()
                .draftUploadedCMOs(List.of(element(buildHearingOrder(DRAFT_CMO))))
                .build();

            Optional<Element<HearingOrder>> matchingHearingOrder =
                caseData.getDraftUploadedCMOWithId(draftCMOOne.getId());

            assertThat(matchingHearingOrder).isNotPresent();
        }
    }

    @Nested
    class GetHearingOrderBundleThatContainsOrder {
        @Test
        void shouldReturnHearingOrderBundleWhenBundleContainsExpectedCaseManagementOrder() {
            Element<HearingOrder> draftCMOOne = element(randomUUID(), buildHearingOrder(DRAFT_CMO));

            Element<HearingOrdersBundle> hearingOrdersBundleOne = element(randomUUID(), HearingOrdersBundle.builder()
                .orders(newArrayList(
                    draftCMOOne,
                    element(buildHearingOrder(C21))))
                .build());

            Element<HearingOrdersBundle> hearingOrdersBundleTwo = element(randomUUID(), HearingOrdersBundle.builder()
                .orders(List.of(
                    element(buildHearingOrder(DRAFT_CMO)),
                    element(buildHearingOrder(C21))))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundleOne, hearingOrdersBundleTwo))
                .build();

            Optional<Element<HearingOrdersBundle>> matchingHearingOrderBundle =
                caseData.getHearingOrderBundleThatContainsOrder(draftCMOOne.getId());

            assertThat(matchingHearingOrderBundle).isPresent().contains(hearingOrdersBundleOne);
        }

        @Test
        void shouldReturnHearingOrdersBundleWhenBundleContainsExpectedDraftOrder() {
            Element<HearingOrder> draftOrder = element(buildHearingOrder(C21));

            Element<HearingOrdersBundle> hearingOrdersBundleOne = element(randomUUID(), HearingOrdersBundle.builder()
                .orders(newArrayList(element(buildHearingOrder(DRAFT_CMO)), element(buildHearingOrder(C21))))
                .build());

            Element<HearingOrdersBundle> hearingOrdersBundleTwo = element(randomUUID(), HearingOrdersBundle.builder()
                .orders(newArrayList(element(buildHearingOrder(DRAFT_CMO)), draftOrder))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundleOne, hearingOrdersBundleTwo))
                .build();

            Optional<Element<HearingOrdersBundle>> matchingHearingOrderBundle =
                caseData.getHearingOrderBundleThatContainsOrder(draftOrder.getId());

            assertThat(matchingHearingOrderBundle).isPresent().contains(hearingOrdersBundleTwo);
        }

        @Test
        void shouldReturnEmptyWhenHearingOrdersBundlesDraftsIsNull() {
            CaseData caseData = CaseData.builder().build();

            Optional<Element<HearingOrdersBundle>> matchingHearingOrderBundle =
                caseData.getHearingOrderBundleThatContainsOrder(randomUUID());

            assertThat(matchingHearingOrderBundle).isEmpty();
        }

        @Test
        void shouldReturnAnEmptyOptionalWhenExpectedOrderIsNotContainedWithinHearingOrderBundle() {
            Element<HearingOrder> draftCMOOne = element(randomUUID(), buildHearingOrder(DRAFT_CMO));

            Element<HearingOrdersBundle> hearingOrdersBundleTwo = element(randomUUID(), HearingOrdersBundle.builder()
                .orders(newArrayList(
                    element(HearingOrder.builder().type(DRAFT_CMO).build()),
                    element(HearingOrder.builder().type(C21).build())))
                .build());

            CaseData caseData = CaseData.builder()
                .hearingOrdersBundlesDrafts(List.of(hearingOrdersBundleTwo))
                .build();

            Optional<Element<HearingOrdersBundle>> matchingHearingOrderBundle =
                caseData.getHearingOrderBundleThatContainsOrder(draftCMOOne.getId());

            assertThat(matchingHearingOrderBundle).isEmpty();
        }
    }

    @Nested
    class HasRespondentsOrOthers {
        Element<Respondent> respondent = element(Respondent.builder()
            .party(RespondentParty.builder().firstName("David").lastName("Jones").build()).build());
        Other firstOther = Other.builder().name("John Smith").build();

        @Test
        void shouldReturnTrueWhenRespondentsAndOthersExist() {
            CaseData caseData = CaseData.builder()
                .respondents1(List.of(respondent))
                .others(Others.builder().firstOther(firstOther).build())
                .build();

            assertTrue(caseData.hasRespondentsOrOthers());
        }

        @Test
        void shouldReturnTrueWhenRespondentsExist() {
            CaseData caseData = CaseData.builder().respondents1(List.of(respondent)).build();

            assertTrue(caseData.hasRespondentsOrOthers());
        }

        @Test
        void shouldReturnTrueWhenOthersExist() {
            CaseData caseData = CaseData.builder()
                .others(Others.builder().firstOther(firstOther).build())
                .build();

            assertTrue(caseData.hasRespondentsOrOthers());
        }

        @Test
        void shouldReturnFalseWhenRespondentsAndOthersDoNotExist() {
            CaseData caseData = CaseData.builder().build();

            assertFalse(caseData.hasRespondentsOrOthers());
        }

        @Test
        void shouldReturnFalseWhenFirstOtherDoesNotExist() {
            CaseData caseData = CaseData.builder().others(Others.builder().build()).build();

            assertFalse(caseData.hasRespondentsOrOthers());
        }
    }

    @Nested
    class FindRespondent {
        UUID elementId = randomUUID();

        @Test
        void shouldReturnRespondentWhenIdMatches() {
            Element<Respondent> respondentOneElement = element(elementId, Respondent.builder().build());

            List<Element<Respondent>> respondents = List.of(
                respondentOneElement,
                element(Respondent.builder().build()));

            CaseData caseData = CaseData.builder().respondents1(respondents).build();
            Optional<Element<Respondent>> optionalRespondentElement = caseData.findRespondent(elementId);

            assertThat(optionalRespondentElement).isPresent().contains(respondentOneElement);
        }

        @Test
        void shouldReturnNullWhenIdDidNotMatch() {
            List<Element<Respondent>> respondents = List.of(
                element(Respondent.builder().build()),
                element(Respondent.builder().build()));

            CaseData caseData = CaseData.builder().respondents1(respondents).build();
            Optional<Element<Respondent>> optionalRespondentElement = caseData.findRespondent(elementId);

            assertThat(optionalRespondentElement).isNotPresent();
        }
    }

    @Test
    void shouldReturnTrueIfCaseHasProperOutsourcingPolicy() {
        CaseData caseData = CaseData.builder()
            .outsourcingPolicy(OrganisationPolicy.builder()
                .organisation(Organisation.builder().organisationID("1").build())
                .build())
            .build();

        assertThat(caseData.isOutsourced()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("incompleteOutsourcingPolicy")
    @NullSource
    void shouldReturnFalseIfCaseHasIncompleteOutsourcingPolicy(OrganisationPolicy outsourcingPolicy) {
        CaseData caseData = CaseData.builder()
            .outsourcingPolicy(outsourcingPolicy)
            .build();

        assertThat(caseData.isOutsourced()).isFalse();
    }

    @Nested
    class DesignatedLocalAuthority {

        @Test
        void shouldReturnDesignatedLocalAuthority() {

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .name("LA1")
                .designated("Yes")
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .name("LA1")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            assertThat(caseData.getDesignatedLocalAuthority()).isEqualTo(localAuthority1);
        }

        @Test
        void shouldReturnNullWhenNoneOfLocalAuthoritiesIsDesignated() {

            final LocalAuthority localAuthority1 = LocalAuthority.builder()
                .name("LA1")
                .build();

            final LocalAuthority localAuthority2 = LocalAuthority.builder()
                .name("LA1")
                .build();

            final CaseData caseData = CaseData.builder()
                .localAuthorities(wrapElements(localAuthority1, localAuthority2))
                .build();

            assertThat(caseData.getDesignatedLocalAuthority()).isEqualTo(null);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnNullWhenNoLocalAuthorities(List<Element<LocalAuthority>> localAuthorities) {

            final CaseData caseData = CaseData.builder()
                .localAuthorities(localAuthorities)
                .build();

            assertThat(caseData.getDesignatedLocalAuthority()).isNull();
        }

    }

    @Nested
    class GetSealType {

        @Test
        void testIfEmpty() {
            assertThat(CaseData.builder().build().getSealType()).isEqualTo(ENGLISH);
        }

        @Test
        void testIfLanguageRequirementNo() {
            assertThat(CaseData.builder().languageRequirement("No").build().getSealType()).isEqualTo(ENGLISH);
        }

        @Test
        void testIfLanguageRequirementYes() {
            assertThat(CaseData.builder().languageRequirement("Yes").build().getSealType()).isEqualTo(WELSH);
        }
    }

    @Nested
    class DischargeOfCareApplication {

        @Test
        void shouldReturnFalseWhenNoOrders() {
            CaseData underTest = CaseData.builder()
                .build();

            assertThat(underTest.isDischargeOfCareApplication()).isFalse();
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnFalseWhenNoOrdersTypesSpecified(List<OrderType> orderTypes) {
            CaseData underTest = CaseData.builder()
                .orders(Orders.builder()
                    .orderType(orderTypes)
                    .build())
                .build();

            assertThat(underTest.isDischargeOfCareApplication()).isFalse();
        }

        @Test
        void shouldReturnFalseWhenMultipleOrdersSpecified() {
            CaseData underTest = CaseData.builder()
                .orders(Orders.builder()
                    .orderType(List.of(OrderType.OTHER, OrderType.SUPERVISION_ORDER))
                    .build())
                .build();

            assertThat(underTest.isDischargeOfCareApplication()).isFalse();
        }

        @Test
        void shouldReturnTrueWhenOnlyDischargeOrdersSpecified() {
            CaseData underTest = CaseData.builder()
                .orders(Orders.builder()
                    .orderType(List.of(OrderType.OTHER))
                    .build())
                .build();

            assertThat(underTest.isDischargeOfCareApplication()).isTrue();
        }

    }

    @Test
    void shouldReturnAllOrderCollection() {
        List<Element<GeneratedOrder>> orders = wrapElementsWithUUIDs(GeneratedOrder.builder().title("order").build());
        List<Element<GeneratedOrder>> ordersCTSC =
            wrapElementsWithUUIDs(GeneratedOrder.builder().title("orderCTSC").build());
        CaseData caseData = CaseData.builder()
            .orderCollection(orders)
            .confidentialOrders(ConfidentialGeneratedOrders.builder().orderCollectionCTSC(ordersCTSC).build())
            .build();

        List<Element<GeneratedOrder>> expected = Stream.of(orders, ordersCTSC).flatMap(List::stream).toList();

        assertThat(caseData.getAllOrderCollections()).isEqualTo(expected);
    }

    private HearingOrder buildHearingOrder(HearingOrderType type) {
        return HearingOrder.builder().type(type).build();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }

    private Element<C2DocumentBundle> buildC2WithFormattedDate(String formattedDateTime) {
        return element(C2DocumentBundle.builder()
            .id(randomUUID())
            .uploadedDateTime(formattedDateTime)
            .build());
    }

    private static Stream<OrganisationPolicy> incompleteOutsourcingPolicy() {
        return Stream.of(
            OrganisationPolicy.builder().build(),
            OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().build())
                .build(),
            OrganisationPolicy.builder()
                .organisation(uk.gov.hmcts.reform.ccd.model.Organisation.builder().organisationID("").build())
                .build()
        );
    }
}
