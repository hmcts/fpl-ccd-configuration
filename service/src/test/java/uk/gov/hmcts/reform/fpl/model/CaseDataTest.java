package uk.gov.hmcts.reform.fpl.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.HearingStatus;
import uk.gov.hmcts.reform.fpl.model.common.C2DocumentBundle;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.order.generated.FurtherDirections;
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

import static com.google.common.collect.Lists.newArrayList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.FINAL;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.ISSUE_RESOLUTION;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

class CaseDataTest {

    private static final String EXCLUSION_CLAUSE = "exclusionClause";
    private static final UUID[] HEARING_IDS = {randomUUID(), randomUUID(), randomUUID(), randomUUID()};

    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final UUID cmoID = randomUUID();
    private final LocalDateTime futureDate = time.now().plusDays(1);

    @Test
    void shouldGetAllOthersWhenFirstAndAdditionalOthersExist() {
        Other other1 = otherWithName("John");
        Other other2 = otherWithName("Sam");

        CaseData caseData = caseData(Others.builder().firstOther(other1).additionalOthers(wrapElements(other2)));

        assertThat(caseData.getAllOthers().get(0).getValue()).isEqualTo(other1);
        assertThat(caseData.getAllOthers().get(1).getValue()).isEqualTo(other2);
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
    void shouldGetEmptyListOfPlacementsWhenPlacementsIsNull() {
        CaseData caseData = CaseData.builder().build();
        assertThat(caseData.getPlacements()).isEmpty();
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
    void shouldFindExistingApplicant() {
        Applicant applicant = Applicant.builder().build();
        CaseData caseData = CaseData.builder().applicants(wrapElements(applicant)).build();

        assertThat(caseData.findApplicant(0)).contains(applicant);
    }

    @Test
    void shouldNotFindNonExistingApplicant() {
        Applicant applicant = Applicant.builder().build();
        CaseData caseData = CaseData.builder().applicants(wrapElements(applicant)).build();

        assertThat(caseData.findApplicant(1)).isEmpty();
    }

    @Test
    void shouldNotFindApplicantWhenNull() {
        CaseData caseData = CaseData.builder().build();

        assertThat(caseData.findApplicant(0)).isEmpty();
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
            List.of(element(createHearingBooking(time.now().plusDays(6),
                time.now().plusDays(6))));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBooking)
            .build();

        boolean hearingBookingInFuture = caseData.hasFutureHearing(hearingBooking);

        assertThat(hearingBookingInFuture).isTrue();
    }

    @Test
    void shouldReturnFalseWhenNoFutureHearingExists() {
        List<Element<HearingBooking>> hearingBooking =
            newArrayList(element(createHearingBooking(time.now().minusDays(6),
                time.now().plusDays(6))));

        CaseData caseData = CaseData.builder()
            .hearingDetails(hearingBooking)
            .build();

        boolean hearingBookingInFuture = caseData.hasFutureHearing(hearingBooking);

        assertThat(hearingBookingInFuture).isFalse();
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
    class DocumentBundleContainsHearingId {
        @Test
        void shouldReturnTrueIfDocumentBundleContainsHearingId() {
            UUID hearingId = randomUUID();
            List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments = List.of(
                element(hearingId, HearingFurtherEvidenceBundle.builder().build()));

            CaseData caseData = CaseData.builder()
                .hearingFurtherEvidenceDocuments(hearingFurtherEvidenceDocuments)
                .build();

            assertThat(caseData.documentBundleContainsHearingId(hearingId)).isTrue();
        }

        @Test
        void shouldReturnFalseIfDocumentBundleDoesNotContainHearingId() {
            UUID hearingId = randomUUID();
            List<Element<HearingFurtherEvidenceBundle>> hearingFurtherEvidenceDocuments = List.of(
                element(randomUUID(), HearingFurtherEvidenceBundle.builder().build()));

            CaseData caseData = CaseData.builder()
                .hearingFurtherEvidenceDocuments(hearingFurtherEvidenceDocuments)
                .build();

            assertThat(caseData.documentBundleContainsHearingId(hearingId)).isFalse();
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
    class GetPastAndTodayHearings {

        @Test
        void shouldReturnPastAndTodayHearingBookings() {
            Element<HearingBooking> todayHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDateTime.now())
                .build());
            Element<HearingBooking> todayLateHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDate.now().plusDays(1).atStartOfDay().minusMinutes(1))
                .build());
            Element<HearingBooking> pastHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .build());
            Element<HearingBooking> futureHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(1))
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
                .startDate(LocalDateTime.now().plusDays(1))
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
    class AddHearingBooking {

        @Test
        void shouldAddFirstHearingBooking() {
            Element<HearingBooking> firstHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(1))
                .build());

            CaseData caseData = CaseData.builder().build();

            caseData.addHearingBooking(firstHearingBooking);

            assertThat(caseData.getHearingDetails()).containsExactly(firstHearingBooking);
        }

        @Test
        void shouldAddNewHearingBooking() {
            Element<HearingBooking> existingHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDateTime.now())
                .build());

            Element<HearingBooking> newHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(1))
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
                .status(HearingStatus.ADJOURNED)
                .build());

            CaseData caseData = CaseData.builder()
                .build();

            caseData.addCancelledHearingBooking(firstAdjournedBooking);

            assertThat(caseData.getCancelledHearingDetails()).containsExactly(firstAdjournedBooking);
        }

        @Test
        void shouldAddNewAdjournedHearingBooking() {
            Element<HearingBooking> existingAdjournedHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDateTime.now().minusDays(1))
                .status(HearingStatus.ADJOURNED)
                .build());

            Element<HearingBooking> newAdjournedHearingBooking = element(HearingBooking.builder()
                .startDate(LocalDateTime.now().plusDays(1))
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

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }
}
