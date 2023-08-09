package uk.gov.hmcts.reform.fpl.service.email.content;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.notify.LegalRepresentativeAddedTemplate;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {LegalRepresentativeAddedContentProvider.class})
class LegalRepresentativeAddedContentProviderTest extends AbstractEmailContentProviderTest {
    private static final String REPRESENTATIVE_FULL_NAME = "representative full name";
    private static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .fullName(REPRESENTATIVE_FULL_NAME)
        .build();
    private static final String LOCAL_AUTHORITY_CODE = "LocalAuthorityCode";
    private static final String RESPONDENT_LAST_NAME = "RespondentLastName";
    private static final String FAMILY_MAN_CASE_NUMBER = "1234556";
    private static final long ID = 213432435L;
    private static final CaseData CASE_DATA = CaseData.builder()
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .respondents1(wrapElements(
            Respondent.builder().party(RespondentParty.builder().lastName(RESPONDENT_LAST_NAME).build()).build()
        ))
        .children1(wrapElements(mock(Child.class)))
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .id(ID)
        .build();
    private static final String LOCAL_AUTHORITY_NAME = "localAuthorityName";
    private static final String CHILD_NAME = "Horus";

    @MockBean
    private LocalAuthorityNameLookupConfiguration laNameLookup;
    @MockBean
    private EmailNotificationHelper helper;

    @Autowired
    private LegalRepresentativeAddedContentProvider underTest;

    @BeforeEach
    void setUp() {
        when(laNameLookup.getLocalAuthorityName(LOCAL_AUTHORITY_CODE)).thenReturn(LOCAL_AUTHORITY_NAME);
        when(helper.getEldestChildLastName(anyList())).thenReturn(CHILD_NAME);
    }

    @Test
    void testGetParameters() {
        LegalRepresentativeAddedTemplate actual = underTest.getNotifyData(LEGAL_REPRESENTATIVE, CASE_DATA);

        assertThat(actual).isEqualTo(expectedNotificationData(FAMILY_MAN_CASE_NUMBER));
    }

    @Test
    void testGetParametersIfNullFamilyManCaseNumber() {
        LegalRepresentativeAddedTemplate actual = underTest.getNotifyData(
            LEGAL_REPRESENTATIVE,
            CASE_DATA.toBuilder().familyManCaseNumber(null).build()
        );

        assertThat(actual).isEqualTo(expectedNotificationData(""));
    }

    private LegalRepresentativeAddedTemplate expectedNotificationData(String familyManId) {
        return LegalRepresentativeAddedTemplate.builder()
            .repName(REPRESENTATIVE_FULL_NAME)
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .firstRespondentLastName(RESPONDENT_LAST_NAME)
            .familyManCaseNumber(familyManId)
            .caseUrl(caseUrl(String.valueOf(ID)))
            .childLastName(CHILD_NAME)
            .build();
    }
}
