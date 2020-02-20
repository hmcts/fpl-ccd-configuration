package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.OrderStatus;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Judge;
import uk.gov.hmcts.reform.fpl.model.Order;
import uk.gov.hmcts.reform.fpl.model.common.JudgeAndLegalAdvisor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.DRAFT;
import static uk.gov.hmcts.reform.fpl.enums.OrderStatus.SEALED;
import static uk.gov.hmcts.reform.fpl.service.HearingBookingService.HEARING_DETAILS_KEY;
import static uk.gov.hmcts.reform.fpl.utils.CaseDataGeneratorHelper.createHearingBookings;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OrderValidationService.class, JacksonAutoConfiguration.class})
public class OrderValidationServiceTest {

    @Autowired
    private OrderValidationService validationService;

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldReturnErrorsWhenNoHearingDetailsExistsForSealedOrder() {
        CaseDetails caseDetails = buildCaseDetails(SEALED);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> returnedErrors = validationService.validate(caseData);

        assertThat(returnedErrors)
            .contains("You need to enter a hearing date.");
    }

    @Test
    void shouldReturnErrorsWhenNoAllocatedJudgeExistsForSealedOrder() {
        CaseDetails caseDetails = buildCaseDetails(SEALED);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> returnedErrors = validationService.validate(caseData);

        assertThat(returnedErrors)
            .contains("You need to enter the allocated judge.");
    }

    @Test
    void shouldNotReturnErrorsWhenHearingDetailsExistsForSealedOrder() {
        CaseDetails caseDetails = buildCaseDetailsWithMandatoryFields(SEALED);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> returnedErrors = validationService.validate(caseData);

        assertThat(returnedErrors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenAllocatedJudgeExistsForSealedOrder() {
        CaseDetails caseDetails = buildCaseDetailsWithMandatoryFields(SEALED);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> returnedErrors = validationService.validate(caseData);

        assertThat(returnedErrors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenNoHearingDetailsExistsForDraftOrder() {
        CaseDetails caseDetails = buildCaseDetails(DRAFT);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> returnedErrors = validationService.validate(caseData);

        assertThat(returnedErrors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenNoAllocatedJudgeExistsForDraftOrder() {
        CaseDetails caseDetails = buildCaseDetails(DRAFT);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> returnedErrors = validationService.validate(caseData);

        assertThat(returnedErrors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenHearingDetailsExistsForDraftOrder() {
        CaseDetails caseDetails = buildCaseDetailsWithMandatoryFields(DRAFT);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> returnedErrors = validationService.validate(caseData);

        assertThat(returnedErrors).isEmpty();
    }

    @Test
    void shouldNotReturnErrorsWhenAllocatedJudgeExistsForDraftOrder() {
        CaseDetails caseDetails = buildCaseDetailsWithMandatoryFields(DRAFT);
        CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);

        List<String> returnedErrors = validationService.validate(caseData);

        assertThat(returnedErrors).isEmpty();
    }

    private CaseDetails buildCaseDetailsWithMandatoryFields(final OrderStatus orderStatus) {
        CaseDetails caseDetails = buildCaseDetails(orderStatus);

        Map<String, Object> caseDataMap = caseDetails.getData();

        return caseDetails.toBuilder()
            .data(ImmutableMap.<String, Object>builder()
                .putAll(caseDataMap)
                .putAll(Map.of(HEARING_DETAILS_KEY, createHearingBookings(LocalDateTime.now())))
                .put("allocatedJudge", Judge.builder().build())
                .build())
            .build();
    }

    private CaseDetails buildCaseDetails(final OrderStatus orderStatus) {
        return CaseDetails.builder()
            .data(ImmutableMap.<String, Object>builder()
                .put("standardDirectionOrder", buildOrderWithStatus(orderStatus))
                .put("judgeAndLegalAdvisor", JudgeAndLegalAdvisor.builder().build())
                .build())
            .build();
    }

    private Order buildOrderWithStatus(final OrderStatus orderStatus) {
        return Order.builder()
            .orderStatus(orderStatus)
            .build();
    }
}
