package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.fpl.enums.DirectionAssignee;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.CaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.Direction;
import uk.gov.hmcts.reform.fpl.model.Directions;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.common.DocumentReference;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Recital;
import uk.gov.hmcts.reform.fpl.model.common.Schedule;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseManagementOrder;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisOrder;
import uk.gov.hmcts.reform.fpl.service.time.Time;
import uk.gov.hmcts.reform.fpl.utils.FixedTimeConfiguration;

import java.time.LocalDateTime;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.fpl.enums.CMOStatus.SELF_REVIEW;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.HEARING_DATE_LIST;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.RECITALS;
import static uk.gov.hmcts.reform.fpl.enums.CaseManagementOrderKeys.SCHEDULE;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.CAFCASS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.COURT;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.OTHERS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.PARENTS_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.fpl.enums.DirectionAssignee.values;
import static uk.gov.hmcts.reform.fpl.enums.DocmosisTemplates.CMO;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.buildCaseDataForCMODocmosisGeneration;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createCmoDirections;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookingsFromInitialDate;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createUnassignedDirection;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateToString;
import static uk.gov.hmcts.reform.fpl.utils.DocumentManagementStoreLoader.document;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    JacksonAutoConfiguration.class, CaseManagementOrderService.class, FixedTimeConfiguration.class
})
class CaseManagementOrderServiceTest {
    private static final Document DOCUMENT = document();

    @MockBean
    private DocumentService documentService;

    @MockBean
    private CaseManagementOrderGenerationService templateDataService;

    @Autowired
    private CaseManagementOrderService service;

    @Autowired
    private Time time;

    @Autowired
    private ObjectMapper mapper;

    private CaseManagementOrder caseManagementOrder;
    private List<Element<HearingBooking>> hearingDetails;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now());
        caseManagementOrder = CaseManagementOrder.builder().build();
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCaseDetailsHasHearingDate() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now().plusDays(5));
        caseData = CaseData.builder().hearingDetails(hearingDetails).build();

        DynamicList data = service.getHearingDateDynamicList(caseData, caseManagementOrder);

        assertThat(data.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(formatLocalDateToMediumStyle(10))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToMediumStyle(7))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToMediumStyle(5))
                    .build()));
    }

    @Test
    void shouldReturnHearingDateWhenHearingDateIsInThePast() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now().minusDays(10));
        caseData = CaseData.builder().hearingDetails(hearingDetails).build();

        DynamicList data = service.getHearingDateDynamicList(caseData, caseManagementOrder);

        assertThat(data.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(formatLocalDateToString(time.now().minusDays(5).toLocalDate(), FormatStyle.MEDIUM))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToString(time.now().minusDays(8).toLocalDate(), FormatStyle.MEDIUM))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToString(time.now().minusDays(10).toLocalDate(), FormatStyle.MEDIUM))
                    .build()));
    }

    @Test
    void shouldNotReturnHearingDateWhenHearingIsInSealedCmo() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now().plusDays(5));
        caseData = CaseData.builder()
            .hearingDetails(hearingDetails)
            .servedCaseManagementOrders(List.of(element(CaseManagementOrder.builder()
                .id(hearingDetails.get(0).getId())
                .build())))
            .build();

        DynamicList data = service.getHearingDateDynamicList(caseData, caseManagementOrder);

        assertThat(data.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToMediumStyle(7))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToMediumStyle(5))
                    .build()));
    }

    @Test
    void shouldNotReturnNextHearingDateWhenHearingDateIsInThePast() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now().minusDays(4));
        caseData = CaseData.builder().hearingDetails(hearingDetails).build();

        DynamicList data = service.getNextHearingDateDynamicList(caseData);

        assertThat(data.getListItems())
            .containsOnly(
                DynamicListElement.builder()
                    .code(fromString("b15eb00f-e151-47f2-8e5f-374cc6fc2657"))
                    .label(formatLocalDateToMediumStyle(1))
                    .build());
    }

    @Test
    void shouldNotReturnNextHearingDateWhenHearingIsInSealedCmo() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now().plusDays(5));
        caseData = CaseData.builder()
            .hearingDetails(hearingDetails)
            .servedCaseManagementOrders(List.of(element(CaseManagementOrder.builder()
                .id(hearingDetails.get(0).getId())
                .build())))
            .build();

        DynamicList data = service.getNextHearingDateDynamicList(caseData);

        assertThat(data.getListItems())
            .containsAll(Arrays.asList(
                DynamicListElement.builder()
                    .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                    .label(formatLocalDateToMediumStyle(7))
                    .build(),
                DynamicListElement.builder()
                    .code(fromString("ecac3668-8fa6-4ba0-8894-2114601a3e31"))
                    .label(formatLocalDateToMediumStyle(5))
                .build()));
    }

    @Test
    void shouldReturnHearingDateDynamicListWhenCmoHasPreviousSelectedValue() {
        hearingDetails = createHearingBookingsFromInitialDate(time.now());
        caseData = CaseData.builder().hearingDetails(hearingDetails).build();
        caseManagementOrder = createCaseManagementOrder();

        DynamicList data = service.getHearingDateDynamicList(caseData, caseManagementOrder);

        assertThat(data.getValue())
            .isEqualTo(DynamicListElement.builder()
                .code(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
                .label(formatLocalDateToMediumStyle(2))
                .build());
    }

    @Test
    void shouldMoveDirectionsToCaseDetailsWhenCMOExistsWithDirections() {
        Map<String, Object> data = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().data(data).build();
        CaseManagementOrder order = CaseManagementOrder.builder().directions(createCmoDirections()).build();

        service.prepareCustomDirections(caseDetails, order);
        CaseData caseData = mapper.convertValue(data, CaseData.class);

        Map<DirectionAssignee, List<Element<Direction>>> map = Directions.getMapping(createCmoDirections());

        assertThat(data).containsKeys("allPartiesCustomCMO", "localAuthorityDirectionsCustomCMO",
            "cafcassDirectionsCustomCMO", "courtDirectionsCustomCMO", "otherPartiesDirectionsCustomCMO",
            "respondentDirectionsCustomCMO");

        assertThat(caseData.getDirectionsForCaseManagementOrder())
            .isEqualTo(Directions.builder()
                .allPartiesCustomCMO(map.get(ALL_PARTIES))
                .localAuthorityDirectionsCustomCMO(map.get(LOCAL_AUTHORITY))
                .respondentDirectionsCustomCMO(map.get(PARENTS_AND_RESPONDENTS))
                .cafcassDirectionsCustomCMO(map.get(CAFCASS))
                .otherPartiesDirectionsCustomCMO(map.get(OTHERS))
                .courtDirectionsCustomCMO(map.get(COURT))
                .build());
    }

    @Test
    void shouldRemoveCustomDirectionsWhenCMODoesNotExistOnCaseDetails() {
        Map<String, Object> caseData = new HashMap<>();

        Stream.of(values()).forEach(assignee ->
            caseData.put(assignee.toCaseManagementOrderDirectionField(), wrapElements(createUnassignedDirection()))
        );

        service.prepareCustomDirections(CaseDetails.builder().data(caseData).build(), null);

        assertThat(caseData).doesNotContainKeys("allPartiesCustomCMO", "localAuthorityDirectionsCustomCMO",
            "cafcassDirectionsCustomCMO", "courtDirectionsCustomCMO", "otherPartiesDirectionsCustomCMO",
            "respondentDirectionsCustomCMO");
    }

    private CaseManagementOrder createCaseManagementOrder() {
        return CaseManagementOrder.builder()
            .hearingDate(formatLocalDateToMediumStyle(2))
            .id(fromString("6b3ee98f-acff-4b64-bb00-cc3db02a24b2"))
            .recitals(wrapElements(Recital.builder().build()))
            .schedule(Schedule.builder().build())
            .status(SELF_REVIEW)
            .orderDoc(DocumentReference.builder().build())
            .build();
    }

    private String formatLocalDateToMediumStyle(int i) {
        return formatLocalDateToString(time.now().plusDays(i).toLocalDate(), FormatStyle.MEDIUM);
    }

    @Nested
    class PrepareCaseDetailsTest {
        private final String[] keys = {HEARING_DATE_LIST.getKey(), RECITALS.getKey(), SCHEDULE.getKey()};

        private HashMap<String, Object> data; // Tries to use an ImmutableMap unless specified

        @Test
        void shouldRemoveScheduleAndRecitalsAndHearingDateListFromCaseData() {
            data = new HashMap<>();

            Arrays.stream(keys).forEach(key -> data.put(key, ""));

            service.removeTransientObjectsFromCaseData(data);

            assertThat(data).doesNotContainKeys(keys);
        }
    }

    @Nested
    class GetOrder {

        @BeforeEach
        void setup() {
            DocmosisOrder order = any(DocmosisOrder.class);
            given(documentService.getDocumentFromDocmosisOrderTemplate(order, eq(CMO))).willReturn(DOCUMENT);
            given(templateDataService.getTemplateData(any())).willReturn(DocmosisCaseManagementOrder.builder().build());
        }

        @Test
        void shouldGetCaseManagementOrderWithDocumentReference() {
            LocalDateTime dateTime = LocalDateTime.of(2099, 1, 1, 10, 0, 0);
            Document document = service.getOrderDocument(buildCaseDataForCMODocmosisGeneration(dateTime));

            assertThat(document).isEqualTo(DOCUMENT);
        }
    }
}
