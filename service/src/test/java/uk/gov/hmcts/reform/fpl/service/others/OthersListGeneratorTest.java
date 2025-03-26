package uk.gov.hmcts.reform.fpl.service.others;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

class OthersListGeneratorTest {

    private DynamicListService dynamicListService = new DynamicListService(new ObjectMapper());

    private OthersListGenerator underTest = new OthersListGenerator(dynamicListService);

    private static CaseData caseData;

    @BeforeEach
    void setup() {
        List<Element<Other>> others = wrapElements(
            Other.builder().firstName("Ross").build(),
            Other.builder().firstName("Bob").build(),
            Other.builder().name("Smith").build());

        caseData = CaseData.builder()
            .othersV2(others)
            .build();
    }

    @Test
    void shouldReturnAllApplicantsList() {
        DynamicList actualDynamicList = underTest.buildOthersList(caseData.getOthersV2());
        assertThat(actualDynamicList.getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly("Ross", "Bob", "Smith");
    }

}
