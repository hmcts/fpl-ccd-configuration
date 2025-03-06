package uk.gov.hmcts.reform.fpl.service.cafcass.api;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.enums.YesNo;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.InternationalElement;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiCaseData;
import uk.gov.hmcts.reform.fpl.model.cafcass.api.CafcassApiInternationalElement;

import java.util.List;

public class CafcassApiInternationalElementConverterTest extends CafcassApiConverterTestBase {
    CafcassApiInternationalElementConverterTest() {
        super(new CafcassApiInternationalElementConverter());
    }

    @Test
    void shouldReturnSource() {
        testSource(List.of("data.internationalElement"));
    }

    @Test
    void shouldConvertInternationalElement() {
        CaseData caseData = CaseData.builder()
            .internationalElement(InternationalElement.builder()
                .possibleCarer(YesNo.YES.getValue())
                .possibleCarerReason("possibleCarerReason")
                .significantEvents(YesNo.YES.getValue())
                .significantEventsReason("significantEventsReason")
                .issues(YesNo.YES.getValue())
                .issuesReason("issuesReason")
                .proceedings(YesNo.YES.getValue())
                .proceedingsReason("proceedingsReason")
                .internationalAuthorityInvolvement(YesNo.YES.getValue())
                .internationalAuthorityInvolvementDetails("internationalAuthorityInvolvementDetails")
                .build())
            .build();

        CafcassApiCaseData expected = CafcassApiCaseData.builder()
            .internationalElement(CafcassApiInternationalElement.builder()
                .possibleCarer(true)
                .possibleCarerReason("possibleCarerReason")
                .significantEvents(true)
                .significantEventsReason("significantEventsReason")
                .issues(true)
                .issuesReason("issuesReason")
                .proceedings(true)
                .proceedingsReason("proceedingsReason")
                .internationalAuthorityInvolvement(true)
                .internationalAuthorityInvolvementDetails("internationalAuthorityInvolvementDetails")
                .build())
            .build();

        testConvert(caseData, expected);
    }

    @Test
    void shouldReturnEmptyObjectIfNull() {
        CafcassApiCaseData expected = CafcassApiCaseData.builder()
            .internationalElement(null)
            .build();

        testConvert(CaseData.builder().internationalElement(null).build(), expected);
        testConvert(CaseData.builder().internationalElement(InternationalElement.builder().build()).build(), expected);
    }
}
