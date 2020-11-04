package uk.gov.hmcts.reform.fpl.service.email.content;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalRepresentative;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseUrlService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class LegalRepresentativeAddedContentProviderTest {

    public static final String REPRESENTATIVE_FULL_NAME = "representative full name";
    public static final LegalRepresentative LEGAL_REPRESENTATIVE = LegalRepresentative.builder()
        .fullName(REPRESENTATIVE_FULL_NAME)
        .build();
    public static final String LOCAL_AUTHORITY_CODE = "LocalAuthorityCode";
    public static final String RESPONDENT_LAST_NAME = "RespondentLastName";
    public static final Element<Respondent> ANOTHER_RESPONDENT = element(mock(Respondent.class));
    public static final String FAMILY_MAN_CASE_NUMBER = "1234556";
    public static final long ID = 213432435L;
    public static final CaseData CASE_DATA = CaseData.builder()
        .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
        .respondents1(List.of(
            element(Respondent.builder()
                .party(RespondentParty.builder()
                    .lastName(RESPONDENT_LAST_NAME).build())
                .build()),
            ANOTHER_RESPONDENT))
        .familyManCaseNumber(FAMILY_MAN_CASE_NUMBER)
        .id(ID)
        .build();
    private static final String CASE_URL = "caseUrl";
    private static final String LOCAL_AUTHORITY_NAME = "localAuthorityName";

    @Mock
    private CaseUrlService caseUrlService;

    private final LocalAuthorityNameLookupConfiguration localAuthorityNameLookupConfiguration = mock(
        LocalAuthorityNameLookupConfiguration.class
    );

    @InjectMocks
    private LegalRepresentativeAddedContentProvider underTest = new LegalRepresentativeAddedContentProvider(
        localAuthorityNameLookupConfiguration
    );

    @BeforeEach
    void setUp() {
        when(caseUrlService.getCaseUrl(ID)).thenReturn(CASE_URL);
        when(localAuthorityNameLookupConfiguration.getLocalAuthorityName(LOCAL_AUTHORITY_CODE))
            .thenReturn(LOCAL_AUTHORITY_NAME);
    }

    @Test
    void testGetParameters() {

        Map<String, Object> actual = underTest.getParameters(
            LEGAL_REPRESENTATIVE,
            CASE_DATA
        );

        assertThat(actual).isEqualTo(ImmutableMap.builder()
            .put("repName", REPRESENTATIVE_FULL_NAME)
            .put("localAuthority", LOCAL_AUTHORITY_NAME)
            .put("firstRespondentLastName", RESPONDENT_LAST_NAME)
            .put("familyManCaseNumber", FAMILY_MAN_CASE_NUMBER)
            .put("caseUrl", CASE_URL)
            .build()
        );
    }

    @Test
    void testGetParametersIfNullFamilyManCaseNumber() {

        Map<String, Object> actual = underTest.getParameters(
            LEGAL_REPRESENTATIVE,
            CASE_DATA.toBuilder().familyManCaseNumber(null).build()
        );

        assertThat(actual).isEqualTo(ImmutableMap.builder()
            .put("repName", REPRESENTATIVE_FULL_NAME)
            .put("localAuthority", LOCAL_AUTHORITY_NAME)
            .put("firstRespondentLastName", RESPONDENT_LAST_NAME)
            .put("familyManCaseNumber", "")
            .put("caseUrl", CASE_URL)
            .build()
        );
    }
}
