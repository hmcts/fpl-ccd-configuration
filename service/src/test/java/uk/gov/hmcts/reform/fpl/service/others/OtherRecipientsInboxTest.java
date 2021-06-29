package uk.gov.hmcts.reform.fpl.service.others;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;

import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

class OtherRecipientsInboxTest {

    private final OtherRecipientsInbox underTest = new OtherRecipientsInbox();

    @Test
    void testShouldReturnNonSelectedRecipients() {
        CaseData caseData = CaseData.builder()
            .others(Others.builder().firstOther(buildOther("First other", emptyList()))
            .additionalOthers(List.of())
            .build()
    }

    public static Other buildOther(String name, List<Element<UUID>> representedBy) {
        return Other.builder()
            .name(name)
            .birthPlace(randomAlphanumeric(10))
            .address(testAddress())
            .representedBy(representedBy)
            .build();
    }
}
