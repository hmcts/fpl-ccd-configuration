package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
public class ManageOrderDocumentServiceTest {
    private static final CaseData CASE_DATA = CaseData.builder().caseLocalAuthority(LOCAL_AUTHORITY_1_CODE).build();

    private final ChildrenService childrenService = mock(ChildrenService.class);
    private final LocalAuthorityNameLookupConfiguration laNameLookup =
        mock(LocalAuthorityNameLookupConfiguration.class);
    private final ManageOrderDocumentService manageOrderDocumentService =
        new ManageOrderDocumentService(childrenService,
            laNameLookup);

    @BeforeEach
    void setUp() {
        when(laNameLookup.getLocalAuthorityName(LOCAL_AUTHORITY_1_CODE)).thenReturn(LOCAL_AUTHORITY_1_NAME);
    }

    @Test
    void shouldReturnExpectedSingularGrammar() {
        when(childrenService.getSelectedChildren(CASE_DATA)).thenReturn(wrapElements(mock(Child.class)));

        Map<String, String> expectedGrammar = Map.of(
            "childOrChildren", "child",
            "childIsOrAre", "is",
            "localAuthorityName", LOCAL_AUTHORITY_1_NAME
        );

        assertThat(manageOrderDocumentService.commonContextElements(CASE_DATA)).isEqualTo(expectedGrammar);
    }

    @Test
    void shouldReturnExpectedPluralGrammar() {
        when(childrenService.getSelectedChildren(CASE_DATA)).thenReturn(wrapElements(mock(Child.class),
            mock(Child.class)));

        Map<String, String> expectedGrammar = Map.of(
            "childOrChildren", "children",
            "childIsOrAre", "are",
            "localAuthorityName", LOCAL_AUTHORITY_1_NAME
        );

        assertThat(manageOrderDocumentService.commonContextElements(CASE_DATA)).isEqualTo(expectedGrammar);
    }
}
