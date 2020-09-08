package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;
import uk.gov.hmcts.reform.fpl.model.order.selector.Selector;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.docmosis.DocmosisDocumentGeneratorService;
import uk.gov.hmcts.reform.fpl.service.docmosis.NoticeOfHearingGenerationService;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HER_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.JudgeOrMagistrateTitle.HIS_HONOUR_JUDGE;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.NO;
import static uk.gov.hmcts.reform.fpl.enums.YesNo.YES;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBooking;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {HearingBookingService.class, FixedTimeConfiguration.class,
    JacksonAutoConfiguration.class, LookupTestConfig.class, EmailNotificationHelper.class,
    CaseDataExtractionService.class, NoticeOfHearingGenerationService.class, HearingVenueLookUpService.class
})
class HearingBookingServiceTest {
    private static final UUID[] HEARING_IDS = {randomUUID(), randomUUID(), randomUUID(), randomUUID()};

    @Autowired
    private Time time;

    @Autowired
    private HearingBookingService service;

    private LocalDateTime futureDate;
    private LocalDateTime pastDate;

    @MockBean
    private DocmosisDocumentGeneratorService docmosisDocumentGeneratorService;

    @MockBean
    private UploadDocumentService uploadDocumentService;

    @BeforeEach
    void setUp() {
        futureDate = time.now().plusDays(1);
        pastDate = time.now().minusDays(1);
    }

    @Test
    void shouldReturnAnEmptyHearingBookingIfHearingDetailsIsNull() {
        CaseData caseData = CaseData.builder().build();

        List<Element<HearingBooking>> alteredHearingList = service.expandHearingBookingCollection(caseData);

        assertThat(alteredHearingList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnHearingBookingIfHearingBookingIsPrePopulated() {
        CaseData caseData = CaseData.builder()
            .hearingDetails(wrapElements(HearingBooking.builder().startDate(futureDate).build()))
            .build();

        List<Element<HearingBooking>> hearingList = service.expandHearingBookingCollection(caseData);

        assertThat(hearingList.get(0).getValue().getStartDate()).isEqualTo(futureDate);
    }

    @Test
    void shouldGetMostUrgentHearingBookingFromACollectionOfHearingBookings() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();

        HearingBooking sortedHearingBooking = service.getMostUrgentHearingBooking(hearingBookings);

        assertThat(sortedHearingBooking.getStartDate()).isEqualTo(futureDate);
    }

    @Test
    void shouldGetHearingBookingWhenKeyMatchesHearingBookingElementUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();
        HearingBooking hearingBooking = service.getHearingBookingByUUID(hearingBookings, HEARING_IDS[2]);
        assertThat(hearingBooking.getStartDate()).isEqualTo(futureDate);
    }

    @Test
    void shouldReturnNullWhenKeyDoesNotMatchHearingBookingElementUUID() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();
        HearingBooking hearingBooking = service.getHearingBookingByUUID(hearingBookings, randomUUID());

        assertThat(hearingBooking).isNull();
    }

    @Nested
    class PastHearings {

        @Test
        void shouldReturnEmptyListWhenNoHearingsHaveBeenCreated() {
            assertThat(service.getPastHearings(emptyList())).isEmpty();
        }

        @Test
        void shouldReturnEmptyHearingListWhenNoPastHearingsExist() {
            List<Element<HearingBooking>> hearingBooking = newArrayList(hearingElementWithStartDate(+5));

            assertThat(service.getPastHearings(hearingBooking)).isEmpty();
        }

        @Test
        void shouldReturnEmptyHearingListWhenDateIsToday() {
            List<Element<HearingBooking>> hearingBooking =
                newArrayList(element(HEARING_IDS[0], createHearingBooking(time.now(),
                    time.now().plusDays(6))));

            assertThat(service.getPastHearings(hearingBooking)).isEmpty();
        }

        @Test
        void shouldReturnPopulatedHearingListWhenOnlyPastHearingsExist() {
            List<Element<HearingBooking>> hearingBooking = newArrayList(hearingElementWithStartDate(-5));

            assertThat(service.getPastHearings(hearingBooking)).isEqualTo(hearingBooking);
        }

        @Test
        void shouldReturnOnlyPastHearingsWhenPastAndFutureHearingsExist() {
            Element<HearingBooking> futureHearingBooking = hearingElementWithStartDate(+5);
            Element<HearingBooking> pastHearingBooking = hearingElementWithStartDate(-5);

            List<Element<HearingBooking>> hearingBookings = newArrayList(futureHearingBooking, pastHearingBooking);

            assertThat(service.getPastHearings(hearingBookings)).isEqualTo(List.of(pastHearingBooking));
        }

        @Test
        void shouldRemovePastHearingsWhenPastHearingsExist() {
            List<Element<HearingBooking>> hearingBooking = newArrayList(hearingElementWithStartDate(-5));
            service.removePastHearings(hearingBooking);
            assertThat(hearingBooking).isEmpty();
        }

        @Test
        void shouldNotRemovePastHearingsWhenNoPastHearingsExist() {
            Element<HearingBooking> hearingBookingElement = hearingElementWithStartDate(5);
            List<Element<HearingBooking>> hearingBooking = newArrayList(hearingBookingElement);
            List<Element<HearingBooking>> expectedHearing = newArrayList(hearingBookingElement);

            service.removePastHearings(hearingBooking);
            assertThat(hearingBooking).isEqualTo(expectedHearing);
        }

        @Test
        void shouldNotRemovePastHearingsWhenNoHearingsExist() {
            List<Element<HearingBooking>> hearingBooking = emptyList();
            service.removePastHearings(hearingBooking);
            assertThat(hearingBooking).isEmpty();
        }
    }

    @Nested
    class RebuildHearingDetailsObject {

        @Test
        void shouldReturnListWhenOnlyFutureHearings() {
            List<Element<HearingBooking>> futureHearingBooking = List.of(hearingElementWithStartDate(+5));

            assertThat(service.combineHearingDetails(futureHearingBooking, emptyList()))
                .isEqualTo(futureHearingBooking);
        }

        @Test
        void shouldNotAddElementWithSameIdToList() {
            List<Element<HearingBooking>> hearingBooking = List.of(hearingElementWithStartDate(0));

            assertThat(service.combineHearingDetails(hearingBooking, hearingBooking)).isEqualTo(hearingBooking);
        }

        @Test
        void shouldReturnOrderedListWhenPastAndFutureHearings() {
            List<Element<HearingBooking>> futureHearingBooking = List.of(hearingElementWithStartDate(+5));
            List<Element<HearingBooking>> pastHearingBooking = List.of(hearingElementWithStartDate(-5));

            List<Element<HearingBooking>> expectedHearingBooking = newArrayList();
            expectedHearingBooking.addAll(pastHearingBooking);
            expectedHearingBooking.addAll(futureHearingBooking);

            assertThat(service.combineHearingDetails(futureHearingBooking, pastHearingBooking))
                .isEqualTo(expectedHearingBooking);
        }
    }

    @Test
    void shouldReturnFirstHearingWhenHearingExists() {
        assertThat(service.getFirstHearing(createHearingBookings()))
            .isEqualTo(Optional.of(createHearingBooking(pastDate, pastDate.plusDays(1))));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnEmptyWhenEmptyListOfHearings(List<Element<HearingBooking>> hearings) {
        assertThat(service.getFirstHearing(hearings)).isEmpty();
    }

    @Test
    void shouldUpdateHearingBookingJudgeWhenHearingIsToUseAllocatedJudge() {
        Judge allocatedJudge = buildAllocatedJudge();

        List<Element<HearingBooking>> hearingBookings = wrapElements(
            buildHearingBooking(YES),
            buildHearingBooking(YES));

        List<Element<HearingBooking>> updatedHearingBookings = service.setHearingJudge(hearingBookings, allocatedJudge);

        JudgeAndLegalAdvisor hearingJudgeAndLegalAdvisor
            = updatedHearingBookings.get(0).getValue().getJudgeAndLegalAdvisor();

        assertThat(hearingJudgeAndLegalAdvisor.getJudgeTitle()).isEqualTo(HER_HONOUR_JUDGE);
        assertThat(hearingJudgeAndLegalAdvisor.getJudgeLastName()).isEqualTo("Jones");
    }

    @Test
    void shouldNotUpdateHearingBookingJudgeWhenHearingIsUsingAlternateJudge() {
        Judge allocatedJudge = buildAllocatedJudge();

        List<Element<HearingBooking>> hearingBookings = wrapElements(buildHearingBooking(NO));

        List<Element<HearingBooking>> updatedHearingBookings =
            service.setHearingJudge(hearingBookings, allocatedJudge);

        JudgeAndLegalAdvisor hearingJudgeAndLegalAdvisor
            = updatedHearingBookings.get(0).getValue().getJudgeAndLegalAdvisor();

        assertThat(hearingJudgeAndLegalAdvisor.getJudgeTitle()).isEqualTo(HIS_HONOUR_JUDGE);
        assertThat(hearingJudgeAndLegalAdvisor.getJudgeLastName()).isEqualTo("Richards");
    }

    @Test
    void shouldReturnEmptyHearingBookingsWhenHearingBookingsAreEmpty() {
        List<Element<HearingBooking>> emptyHearingBookings = wrapElements(HearingBooking.builder().build());
        Judge allocatedJudge = buildAllocatedJudge();
        List<Element<HearingBooking>> hearingBookings
            = service.resetHearingJudge(emptyHearingBookings, allocatedJudge);

        assertThat(hearingBookings).isEqualTo(emptyHearingBookings);
    }

    @Test
    void shouldResetJudgeWhenHearingIsUsingAllocatedJudge() {
        List<Element<HearingBooking>> hearingBookings = wrapElements(buildHearingBooking(YES));
        Judge allocatedJudge = Judge.builder()
            .judgeTitle(HIS_HONOUR_JUDGE)
            .judgeLastName("Richards")
            .build();

        List<Element<HearingBooking>> updatedHearingBookings
            = service.resetHearingJudge(hearingBookings, allocatedJudge);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = updatedHearingBookings.get(0).getValue().getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isEqualTo(YES.getValue());
        assertThat(judgeAndLegalAdvisor.getJudgeTitle()).isEqualTo(null);
        assertThat(judgeAndLegalAdvisor.getJudgeLastName()).isEqualTo(null);
        assertThat(judgeAndLegalAdvisor.getLegalAdvisorName()).isEqualTo("Joe Bloggs");
    }

    @Test
    void shouldPersistJudgeWhenHearingIsUsingAlternateJudge() {
        List<Element<HearingBooking>> hearingBookings = wrapElements(buildHearingBooking(NO));
        Judge allocatedJudge = buildAllocatedJudge();

        List<Element<HearingBooking>> updatedHearingBookings
            = service.resetHearingJudge(hearingBookings, allocatedJudge);

        JudgeAndLegalAdvisor judgeAndLegalAdvisor = updatedHearingBookings.get(0).getValue().getJudgeAndLegalAdvisor();

        assertThat(judgeAndLegalAdvisor.getUseAllocatedJudge()).isEqualTo(NO.getValue());
        assertThat(judgeAndLegalAdvisor.getJudgeTitle()).isEqualTo(HIS_HONOUR_JUDGE);
        assertThat(judgeAndLegalAdvisor.getJudgeLastName()).isEqualTo("Richards");
        assertThat(judgeAndLegalAdvisor.getLegalAdvisorName()).isEqualTo("Joe Bloggs");
    }

    @Nested
    class GetNewHearings {
        private List<Element<HearingBooking>> oldHearingBookings;
        private List<Element<HearingBooking>> newHearingBookings;

        @BeforeEach
        void setUp() {
            oldHearingBookings = createHearingBookings();
            newHearingBookings = addNewHearingToExistingHearingBookings();
        }

        @Test
        void shouldReturnListWithMoreThanOneHearingBookingsWhenThereIsNewHearing() {
            assertThat(service.getNewHearings(newHearingBookings, oldHearingBookings).size()).isNotZero();
        }

        @Test
        void shouldReturnListWithZeroHearingBookingsWhenThereIsNoNewHearing() {

            assertThat(service.getNewHearings(oldHearingBookings, oldHearingBookings).size()).isZero();
        }
    }

    @Nested
    class GetSelectedHearings {

        @Test
        void shouldReturnSelectedHearings() {
            List<Element<HearingBooking>> hearingBookings = createHearingBookings();
            Selector selector = Selector.builder().selected(List.of(1)).build();

            assertThat(service.getSelectedHearings(selector, hearingBookings).size()).isEqualTo(1);
            assertThat(service.getSelectedHearings(selector, hearingBookings).get(0).getValue().getType())
                .isEqualTo(CASE_MANAGEMENT);
        }

        @Test
        void shouldReturnEmptyListWhenNoHearings() {
            Selector selector = Selector.builder().selected(List.of(1)).build();

            assertThat(service.getSelectedHearings(selector, emptyList())).isEmpty();
        }
    }

    private Judge buildAllocatedJudge() {
        return Judge.builder()
            .judgeTitle(HER_HONOUR_JUDGE)
            .judgeLastName("Jones")
            .build();
    }

    private HearingBooking buildHearingBooking(YesNo useAllocatedJudge) {
        return HearingBooking.builder()
            .judgeAndLegalAdvisor(JudgeAndLegalAdvisor.builder()
                .useAllocatedJudge(useAllocatedJudge.getValue())
                .judgeTitle(HIS_HONOUR_JUDGE)
                .judgeLastName("Richards")
                .legalAdvisorName("Joe Bloggs")
                .build())
            .build();
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return new ArrayList<>(List.of(
            element(HEARING_IDS[0], createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))),
            element(HEARING_IDS[1], createHearingBooking(futureDate.plusDays(2), futureDate.plusDays(3))),
            element(HEARING_IDS[2], createHearingBooking(futureDate, futureDate.plusDays(1))),
            element(HEARING_IDS[3], createHearingBooking(pastDate, pastDate.plusDays(1)))
        ));
    }

    private List<Element<HearingBooking>> addNewHearingToExistingHearingBookings() {
        List<Element<HearingBooking>> listOfHearingBookings = createHearingBookings();
        listOfHearingBookings.add(
            element(randomUUID(), createHearingBooking(futureDate.plusDays(5), futureDate.plusDays(6))));
        return listOfHearingBookings;
    }

    private Element<HearingBooking> hearingElementWithStartDate(int daysFromToday) {
        return element(HearingBooking.builder()
            .startDate(futureDate.plusDays(daysFromToday))
            .build());
    }
}
