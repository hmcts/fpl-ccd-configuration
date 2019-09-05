package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.enums.OrderType;
import uk.gov.hmcts.reform.fpl.model.Applicant;
import uk.gov.hmcts.reform.fpl.model.ApplicantParty;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Orders;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static uk.gov.hmcts.reform.fpl.enums.OrderType.CARE_ORDER;
import static uk.gov.hmcts.reform.fpl.enums.OrderType.EDUCATION_SUPERVISION_ORDER;

@ExtendWith(SpringExtension.class)
class CaseDataExtractionServiceTest {

    private String JURISDICTION = "PUBLICLAW";
    private CaseDataExtractionService caseDataExtractionService = new CaseDataExtractionService();

    @Test
    void shouldReturnAMapOfEmptyStringsIfCaseDataIsNotPopulated() {

        CaseData caseData = CaseData.builder().build();
        Map<String, String> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData,
            JURISDICTION);

        assertThat(templateData.get("jurisdiction")).isEqualTo("PUBLICLAW");
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("");
        assertThat(templateData.get("applicantName")).isEqualTo("");
        assertThat(templateData.get("orderTypes")).isEqualTo("");
        assertThat(templateData.get("childrenNames")).isEqualTo("");
        assertThat(templateData.get("hearingDate")).isEqualTo("");
        assertThat(templateData.get("hearingVenue")).isEqualTo("");
        assertThat(templateData.get("preHearingAttendance")).isEqualTo("");
        assertThat(templateData.get("hearingTime")).isEqualTo("");
    }

    @Test
    void shouldConcatenateAllChildrenNames() {
        CaseData caseData = CaseData.builder()
            .children1(createPopulatedChildren())
            .build();

        Map<String, String> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData,
            JURISDICTION);
        assertThat(templateData.get("childrenNames")).isEqualTo("Bran Stark, Sansa Stark");
    }

    @Test
    void shouldReturnFirstApplicantName() {
        CaseData caseData = CaseData.builder()
            .applicants(createPopulatedApplicants())
                .build();

        Map<String, String> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData,
            JURISDICTION);
        assertThat(templateData.get("applicantName")).isEqualTo("Bran Stark");
    }

    @Test
    void shouldMapCaseDataPropertiesToTemplatePlaceholderData() {
        CaseData caseData = CaseData.builder()
            .familyManCaseNumber("123")
            .children1(createPopulatedChildren())
            .applicants(createPopulatedApplicants())
            .hearingDetails(createHearingBookings())
            .orders(Orders.builder()
                .orderType(ImmutableList.<OrderType>of(
                    CARE_ORDER,
                    EDUCATION_SUPERVISION_ORDER
                )).build())
            .build();

        Map<String, String> templateData = caseDataExtractionService.getNoticeOfProceedingTemplateData(caseData, JURISDICTION);
        assertThat(templateData.get("jurisdiction")).isEqualTo("PUBLICLAW");
        assertThat(templateData.get("familyManCaseNumber")).isEqualTo("123");
        assertThat(templateData.get("applicantName")).isEqualTo("Bran Stark");
        assertThat(templateData.get("orderTypes")).isEqualTo("Care order, Education supervision order");
        assertThat(templateData.get("childrenNames")).isEqualTo("Bran Stark, Sansa Stark");
        assertThat(templateData.get("hearingDate")).isEqualTo(LocalDate.now().plusDays(1).toString());
        assertThat(templateData.get("hearingVenue")).isEqualTo("Venue 3");
        assertThat(templateData.get("preHearingAttendance")).isEqualTo("This is usually one hour before the hearing");
        assertThat(templateData.get("hearingTime")).isEqualTo("09.15");
    }

    @Test
    void shouldGetMostUrgentHearingBookingFromACollectionOfHearingBookings() {
        List<Element<HearingBooking>> hearingBookings = createHearingBookings();

        CaseData caseData = CaseData.builder().hearingDetails(hearingBookings).build();
        HearingBooking sortedHearingBooking = caseDataExtractionService.getMostUrgentHearingBooking(caseData);

        assertThat(sortedHearingBooking.getVenue()).isEqualTo("Venue 3");
    }

    private List<Element<HearingBooking>> createHearingBookings() {
        return ImmutableList.of(
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(HearingBooking.builder()
                    .date(LocalDate.now().plusDays(5))
                    .venue("Venue 1")
                    .preHearingAttendance("This is usually one hour before the hearing")
                    .time("09.15")
                    .build())
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(HearingBooking.builder()
                    .date(LocalDate.now().plusDays(2))
                    .venue("Venue 2")
                    .preHearingAttendance("This is usually one hour before the hearing")
                    .time("09.15")
                    .build())
                .build(),
            Element.<HearingBooking>builder()
                .id(UUID.randomUUID())
                .value(HearingBooking.builder()
                    .date(LocalDate.now().plusDays(1))
                    .venue("Venue 3")
                    .preHearingAttendance("This is usually one hour before the hearing")
                    .time("09.15")
                    .build())
                .build()
        );
    }

    private List<Element<Applicant>> createPopulatedApplicants() {
        return ImmutableList.of(
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .leadApplicantIndicator("No")
                    .party(ApplicantParty.builder()
                        .organisationName("Bran Stark")
                        .build())
                    .build())
                .build(),
            Element.<Applicant>builder()
                .id(UUID.randomUUID())
                .value(Applicant.builder()
                    .leadApplicantIndicator("No")
                    .party(ApplicantParty.builder()
                        .organisationName("Sansa Stark")
                        .build())
                    .build())
                .build());
    }

    private List<Element<Child>> createPopulatedChildren() {
        return ImmutableList.of(
            Element.<Child>builder()
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Bran")
                        .lastName("Stark")
                        .build())
                    .build())
                .build(),
            Element.<Child>builder()
                .id(UUID.randomUUID())
                .value(Child.builder()
                    .party(ChildParty.builder()
                        .firstName("Sansa")
                        .lastName("Stark")
                        .build())
                    .build())
                .build());
    }
}
