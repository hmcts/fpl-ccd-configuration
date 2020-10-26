package uk.gov.hmcts.reform.fpl.service;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Recipients;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class StatementOfServiceServiceTest {

    private final StatementOfServiceService service = new StatementOfServiceService();

    @Test
    void shouldReturnAnEmptyRecipientIfRecipientIsNull() {
        CaseData caseData = CaseData.builder().build();

        List<Element<Recipients>> alteredHearingList = service.expandRecipientCollection(caseData);

        assertThat(alteredHearingList.size()).isEqualTo(1);
    }

    @Test
    void shouldReturnRecipientsIfRecipientsIsPrePopulated() {
        CaseData caseData = CaseData.builder()
            .statementOfService(
                ImmutableList.of(Element.<Recipients>builder()
                    .value(Recipients.builder().name("Recipient name").build())
                    .build()))
            .build();

        List<Element<Recipients>> recipientList = service.expandRecipientCollection(caseData);

        assertThat(recipientList.get(0).getValue().getName()).isEqualTo("Recipient name");
    }
}
