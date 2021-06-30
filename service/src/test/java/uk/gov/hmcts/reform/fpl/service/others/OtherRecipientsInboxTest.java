package uk.gov.hmcts.reform.fpl.service.others;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testAddress;

class OtherRecipientsInboxTest {

    private final OtherRecipientsInbox underTest = new OtherRecipientsInbox();

    @Test
    void testShouldReturnNonSelectedRecipients() {
        CaseData caseData = CaseData.builder()
            .others(Others.builder().firstOther(buildOther("First other", emptyList()))
            .additionalOthers(List.of()).build())
            .build();
    }

    public static Other buildOther(String name, List<Element<UUID>> representedBy) {
        Other other = Other.builder()
            .name(name)
            .birthPlace(randomAlphanumeric(10))
            .address(testAddress())
            .build();
        representedBy.forEach(x -> other.addRepresentative(x.getId()));
        return other;
    }
}
