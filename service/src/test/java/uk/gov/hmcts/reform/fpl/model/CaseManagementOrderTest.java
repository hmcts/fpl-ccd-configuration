package uk.gov.hmcts.reform.fpl.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.fpl.enums.ActionType;
import uk.gov.hmcts.reform.fpl.enums.OrderOwner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.fpl.enums.ActionType.SEND_TO_ALL_PARTIES;
import static uk.gov.hmcts.reform.fpl.enums.OrderOwner.JUDICIARY;
import static uk.gov.hmcts.reform.fpl.enums.OrderOwner.LOCAL_AUTHORITY;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JacksonAutoConfiguration.class})
class CaseManagementOrderTest {

    @Autowired
    private ObjectMapper mapper;

    @Test
    void shouldReturnFalseWhenActionTypeEqualsSendToAllParties() {
        assertFalse(order(SEND_TO_ALL_PARTIES).isDraft());
    }

    @EnumSource(value = ActionType.class, names = {"JUDGE_REQUESTED_CHANGE", "SELF_REVIEW"})
    @ParameterizedTest
    void shouldReturnTrueWhenActionTypeEqualsOtherThanSendToAllParties(ActionType type) {
        assertTrue(order(type).isDraft());
    }

    @Test
    void shouldReturnTrueWhenActionTypeIsNull() {
        assertTrue(CaseManagementOrder.builder().build().isDraft());
    }

    private CaseManagementOrder order(ActionType type) {
        return CaseManagementOrder.builder()
            .action(OrderAction.builder()
                .type(type)
                .build())
            .build();
    }

    @Test
    void shouldSerialiseCaseManagementOrderToCorrectStringValueWhenLocalAuthorityOwner() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().owner(LOCAL_AUTHORITY).build())
            .build());

        JSONAssert.assertEquals("{caseManagementOrder_LOCAL_AUTHORITY:{owner: LOCAL_AUTHORITY}}", serialised, true);
    }

    @Test
    void shouldSerialiseCaseManagementOrderToCorrectStringValueWhenJudiciaryOwner() throws JsonProcessingException {
        String serialised = mapper.writeValueAsString(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().owner(JUDICIARY).build())
            .build());

        JSONAssert.assertEquals("{caseManagementOrder_JUDICIARY:{owner: JUDICIARY}}", serialised, true);
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithLocalAuthorityOwner() throws JsonProcessingException {
        String content = "{\"caseManagementOrder_LOCAL_AUTHORITY\":{\"owner\": \"LOCAL_AUTHORITY\"}}";

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().owner(LOCAL_AUTHORITY).build())
            .build());
    }

    @Test
    void shouldDeserialiseCaseDataWhenCaseManagementOrderWithJudiciaryOwner() throws JsonProcessingException {
        String content = "{\"caseManagementOrder_JUDICIARY\":{\"owner\": \"JUDICIARY\"}}";

        CaseData deserialised = mapper.readValue(content, CaseData.class);

        assertThat(deserialised).isEqualTo(CaseData.builder()
            .caseManagementOrder(CaseManagementOrder.builder().owner(JUDICIARY).build())
            .build());
    }
}
