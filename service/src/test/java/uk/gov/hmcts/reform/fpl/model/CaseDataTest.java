package uk.gov.hmcts.reform.fpl.model;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import uk.gov.hmcts.reform.fpl.enums.HearingOrderType;
import uk.gov.hmcts.reform.fpl.enums.HearingType;
import uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle;
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
import static java.lang.String.format;
import static java.time.LocalDateTime.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import static uk.gov.hmcts.reform.fpl.enums.HearingType.OTHER;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.DIGITAL_SERVICE;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.EMAIL;
import static uk.gov.hmcts.reform.fpl.enums.RepresentativeServingPreferences.POST;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.buildDynamicList;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChild;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testRepresentative;

class CaseDataTest {

    private static final String EXCLUSION_CLAUSE = "exclusionClause";
    private static final UUID[] HEARING_IDS = {randomUUID(), randomUUID(), randomUUID(), randomUUID()};

    private final Time time = new FixedTimeConfiguration().stoppedTime();
    private final UUID cmoID = randomUUID();
    private final LocalDateTime futureDate = time.now().plusDays(1);
    private final LocalDateTime pastDate = time.now().minusDays(1);

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
        CaseData caseData = CaseData.builder()
            .hearingStartDate(futureDate)
            .hearingEndDate(futureDate)
            .build();

        boolean hearingInPast = caseData.isHearingDateInPast();

        assertThat(hearingInPast).isFalse();
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

        @Test
        void shouldReturnFalseIfC2DocumentBundleIsNullOrEmpty() {
            CaseData caseData = CaseData.builder().build();
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
    class BuildApplicationBundlesDynamicList {
        @Test
        void shouldBuildDynamicApplicationsBundleListFromC2DocumentsAndAdditionalApplications() {
            List<Element<C2DocumentBundle>> c2DocumentBundle = List.of(
                element(buildC2DocumentBundle(futureDate.plusDays(2))),
                element(buildC2DocumentBundle(futureDate.plusDays(1)))
            );

            CaseData caseData = CaseData.builder().c2DocumentBundle(c2DocumentBundle).build();
            DynamicList expectedDynamicList = ElementUtils.asDynamicList(
                c2DocumentBundle, null, bundle -> format("C2, %s", bundle.getUploadedDateTime()));

            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicListWithC2DocumentsInAdditionalApplicationsBundle() {
            C2DocumentBundle c2Bundle1 = buildC2DocumentBundle(randomUUID(), futureDate.plusDays(1))
                .toBuilder().id(randomUUID()).build();

            C2DocumentBundle c2Bundle2 = buildC2DocumentBundle(randomUUID(), futureDate.plusDays(2))
                .toBuilder().id(randomUUID()).build();

            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(
                element(AdditionalApplicationsBundle.builder().c2DocumentBundle(c2Bundle1).build()),
                element(AdditionalApplicationsBundle.builder().c2DocumentBundle(c2Bundle2).build()));

            CaseData caseData = CaseData.builder().additionalApplicationsBundle(additionalBundles).build();

            DynamicList expectedDynamicList = ElementUtils.asDynamicList(
                List.of(element(c2Bundle1.getId(), c2Bundle1), element(c2Bundle2.getId(), c2Bundle2)),
                null, bundle -> format("C2, %s", bundle.getUploadedDateTime()));

            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicListWithC2BundlesFromC2DocumentsAndAdditionalDocumentsBundles() {
            Element<C2DocumentBundle> c2Bundle1 = element(buildC2DocumentBundle(futureDate.plusDays(2)));

            C2DocumentBundle c2Bundle2 = buildC2DocumentBundle(randomUUID(), futureDate.plusDays(1))
                .toBuilder().id(randomUUID()).build();

            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(c2Bundle2)
                    .build()));

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(c2Bundle1))
                .additionalApplicationsBundle(additionalBundles)
                .build();

            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(c2Bundle1.getId(), "C2, " + c2Bundle1.getValue().getUploadedDateTime()),
                Pair.of(c2Bundle2.getId(), "C2, " + c2Bundle2.getUploadedDateTime())
            );
            assertThat(caseData.buildApplicationBundlesDynamicList()).isEqualTo(expectedDynamicList);
        }

        @Test
        void shouldBuildDynamicApplicationsBundleListFromC2DocumentsAndAdditionalDocumentsBundle() {
            Element<C2DocumentBundle> c2Bundle1 = element(buildC2DocumentBundle(futureDate.plusDays(2)));

            C2DocumentBundle c2Bundle2 = buildC2DocumentBundle(randomUUID(), futureDate.plusDays(1))
                .toBuilder().id(randomUUID()).build();

            OtherApplicationsBundle otherBundle = OtherApplicationsBundle.builder()
                .applicationType(OtherApplicationType.C1_PARENTAL_RESPONSIBILITY)
                .id(randomUUID()).uploadedDateTime(futureDate.plusDays(1).toString()).build();

            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(c2Bundle2)
                    .otherApplicationsBundle(otherBundle)
                    .build()));

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(c2Bundle1))
                .additionalApplicationsBundle(additionalBundles)
                .build();

            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(c2Bundle1.getId(), "C2, " + c2Bundle1.getValue().getUploadedDateTime()),
                Pair.of(c2Bundle2.getId(), "C2, " + c2Bundle2.getUploadedDateTime()),
                Pair.of(otherBundle.getId(), "C1 - Parental responsibility, " + otherBundle.getUploadedDateTime())
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
            Element<C2DocumentBundle> c2Bundle1 = element(buildC2DocumentBundle(futureDate.plusDays(2)));

            C2DocumentBundle c2Bundle2 = buildC2DocumentBundle(randomUUID(), futureDate.plusDays(1))
                .toBuilder().id(randomUUID()).build();

            OtherApplicationsBundle otherBundle = OtherApplicationsBundle.builder()
                .applicationType(OtherApplicationType.C1_PARENTAL_RESPONSIBILITY)
                .id(randomUUID()).uploadedDateTime(futureDate.plusDays(1).toString()).build();

            List<Element<AdditionalApplicationsBundle>> additionalBundles = List.of(element(
                AdditionalApplicationsBundle.builder()
                    .c2DocumentBundle(c2Bundle2)
                    .otherApplicationsBundle(otherBundle)
                    .build()));

            DynamicList expectedDynamicList = buildDynamicList(
                Pair.of(c2Bundle1.getId(), "C2, " + c2Bundle1.getValue().getUploadedDateTime()),
                Pair.of(c2Bundle2.getId(), "C2, " + c2Bundle2.getUploadedDateTime()),
                Pair.of(otherBundle.getId(), "C1, " + otherBundle.getUploadedDateTime())
            );

            CaseData caseData = CaseData.builder()
                .c2DocumentBundle(List.of(c2Bundle1))
                .additionalApplicationsBundle(additionalBundles)
                .manageDocumentsSupportingC2List(expectedDynamicList)
                .build();

            assertThat(caseData.getApplicationBundleByUUID(c2Bundle1.getId())).isEqualTo(c2Bundle1.getValue());
            assertThat(caseData.getApplicationBundleByUUID(c2Bundle2.getId())).isEqualTo(c2Bundle2);
            assertThat(caseData.getApplicationBundleByUUID(otherBundle.getId())).isEqualTo(otherBundle);
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
            HearingBooking otherHearing = hearingBooking(OTHER, now().plusDays(1));
            HearingBooking caseManagementHearing = hearingBooking(CASE_MANAGEMENT, now());
            HearingBooking laterCaseManagementHearing = hearingBooking(CASE_MANAGEMENT, now().plusDays(3));

            CaseData caseData = CaseData.builder()
                .hearingDetails(wrapElements(otherHearing, laterCaseManagementHearing, caseManagementHearing))
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

            Optional<HearingBooking> foundHearing = caseData.getFirstHearingOfType(OTHER);

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

    private HearingOrder buildHearingOrder(HearingOrderType type) {
        return HearingOrder.builder().type(type).build();
    }

    private C2DocumentBundle buildC2DocumentBundle(LocalDateTime dateTime) {
        return C2DocumentBundle.builder().uploadedDateTime(dateTime.toString()).build();
    }

    private C2DocumentBundle buildC2DocumentBundle(UUID bundleId, LocalDateTime dateTime) {
        return C2DocumentBundle.builder().id(bundleId).uploadedDateTime(dateTime.toString()).build();
    }
}
