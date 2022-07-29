package uk.gov.hmcts.reform.fpl.service.others;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Other;
import uk.gov.hmcts.reform.fpl.model.Others;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.DynamicListService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class OthersListGeneratorTest {

    private DynamicListService dynamicListService = new DynamicListService(new ObjectMapper());

    private OthersListGenerator underTest = new OthersListGenerator(dynamicListService);

    private static CaseData caseData;

    @BeforeEach
    void setup() {
        List<Element<Other>> others = List.of(
            element(Other.builder().name("Bob").build()),
            element(Other.builder().name("Smith").build()));

        caseData = CaseData.builder()
            .others(Others.builder()
                .firstOther(Other.builder().name("Ross").build())
                .additionalOthers(others)
                .build())
            .build();
    }

    @Test
    void shouldReturnAllApplicantsList() {
        DynamicList actualDynamicList = underTest.buildOthersList(caseData.getAllOthers());
        assertThat(actualDynamicList.getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly("Ross", "Bob", "Smith");
    }

}
