package uk.gov.hmcts.reform.fpl.service.email.content.cmo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.HearingBooking;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.cmo.ChaseMissingCMOsTemplate;
import uk.gov.hmcts.reform.fpl.service.cmo.SendOrderReminderService;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.enums.HearingType.CASE_MANAGEMENT;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.DATE;
import static uk.gov.hmcts.reform.fpl.utils.DateFormatterHelper.formatLocalDateTimeBaseUsingFormat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ContextConfiguration(classes = {ChaseMissingCMOEmailContentProvider.class})
class ChaseMissingCMOEmailContentProviderTest extends AbstractEmailContentProviderTest {

    private static final Long CASE_NUMBER = 12345L;

    @MockBean
    private SendOrderReminderService service;

    @Autowired
    private ChaseMissingCMOEmailContentProvider contentProvider;

    LocalDateTime hearingDate = now().minusDays(10);

    private final CaseData caseData = CaseData.builder()
        .id(12345L)
        .caseName("Test v Smith")
        .familyManCaseNumber("AB12C45000")
        .respondents1(List.of(element(Respondent.builder().party(RespondentParty.builder()
                .lastName("Smith")
                .firstName("John")
                .build())
            .build())))
        .hearingDetails(List.of(element(HearingBooking.builder()
            .type(CASE_MANAGEMENT)
            .startDate(hearingDate)
            .endDate(hearingDate.plusHours(1))
            .build())))
        .build();

    @BeforeEach
    void beforeEach() {
        when(service.getPastHearingBookingsWithoutCMOs(caseData)).thenReturn(caseData.getAllHearings().stream()
            .map(Element::getValue)
            .collect(Collectors.toList())
        );
    }

    @Test
    void shouldCreateTemplateWithExpectedParameters() {
        ChaseMissingCMOsTemplate template = contentProvider.buildTemplate(caseData);

        ChaseMissingCMOsTemplate expected = ChaseMissingCMOsTemplate.builder()
            .respondentLastName("Smith")
            .caseUrl(caseUrl(CASE_NUMBER.toString()))
            .subjectLine("Test v Smith, 12345, AB12C45000")
            .listOfHearingsMissingOrders("Case management hearing, "
                + formatLocalDateTimeBaseUsingFormat(hearingDate, DATE))
            .build();

        assertThat(template).isEqualTo(expected);
    }
}
