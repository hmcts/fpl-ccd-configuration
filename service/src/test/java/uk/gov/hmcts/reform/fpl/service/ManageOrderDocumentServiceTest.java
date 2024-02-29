package uk.gov.hmcts.reform.fpl.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.selectors.ChildrenSmartSelector;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.COURT_1;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_CODE;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_COURT_NAME;
import static uk.gov.hmcts.reform.fpl.Constants.LOCAL_AUTHORITY_1_NAME;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ExtendWith(SpringExtension.class)
public class ManageOrderDocumentServiceTest {
    private static final CaseData CASE_DATA = CaseData.builder()
        .caseLocalAuthority(LOCAL_AUTHORITY_1_CODE)
        .court(COURT_1)
        .build();

    private static final CaseData CASE_DATA_2 = CaseData.builder()
        .localAuthorities(wrapElements(LocalAuthority.builder()
            .name(LOCAL_AUTHORITY_1_NAME)
            .build()))
        .court(COURT_1)
        .build();

    private final ChildrenSmartSelector childrenSmartSelector = mock(ChildrenSmartSelector.class);
    private final LocalAuthorityNameLookupConfiguration laNameLookup =
        mock(LocalAuthorityNameLookupConfiguration.class);
    private final ManageOrderDocumentService manageOrderDocumentService =
        new ManageOrderDocumentService(childrenSmartSelector,
            laNameLookup);

    @Test
    void shouldReturnExpectedSingularGrammar() {
        when(laNameLookup.getLocalAuthorityName(LOCAL_AUTHORITY_1_CODE)).thenReturn(LOCAL_AUTHORITY_1_NAME);
        when(childrenSmartSelector.getSelectedChildren(CASE_DATA)).thenReturn(wrapElements(mock(Child.class)));

        Map<String, String> expectedGrammar = Map.of(
            "childOrChildren", "child",
            "childIsOrAre", "is",
            "childWasOrWere", "was",
            "localAuthorityName", LOCAL_AUTHORITY_1_NAME,
            "courtName", LOCAL_AUTHORITY_1_COURT_NAME
        );

        assertThat(manageOrderDocumentService.commonContextElements(CASE_DATA)).isEqualTo(expectedGrammar);
    }

    @Test
    void shouldReturnExpectedPluralGrammar() {
        when(laNameLookup.getLocalAuthorityName(LOCAL_AUTHORITY_1_CODE)).thenReturn(LOCAL_AUTHORITY_1_NAME);
        when(childrenSmartSelector.getSelectedChildren(CASE_DATA)).thenReturn(wrapElements(mock(Child.class),
            mock(Child.class)));

        Map<String, String> expectedGrammar = Map.of(
            "childOrChildren", "children",
            "childIsOrAre", "are",
            "childWasOrWere", "were",
            "localAuthorityName", LOCAL_AUTHORITY_1_NAME,
            "courtName", LOCAL_AUTHORITY_1_COURT_NAME
        );

        assertThat(manageOrderDocumentService.commonContextElements(CASE_DATA)).isEqualTo(expectedGrammar);
    }

    @Test
    void shouldReturnExpectedSingularGrammarEvenWhenCaseLocalAuthorityDoesntExist() {
        when(childrenSmartSelector.getSelectedChildren(CASE_DATA_2)).thenReturn(wrapElements(mock(Child.class)));

        Map<String, String> expectedGrammar = Map.of(
            "childOrChildren", "child",
            "childIsOrAre", "is",
            "childWasOrWere", "was",
            "localAuthorityName", LOCAL_AUTHORITY_1_NAME,
            "courtName", LOCAL_AUTHORITY_1_COURT_NAME
        );

        assertThat(manageOrderDocumentService.commonContextElements(CASE_DATA_2)).isEqualTo(expectedGrammar);
    }
}
