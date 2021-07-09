package uk.gov.hmcts.reform.fpl.service.email.content.respondentsolicitor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.respondentsolicitor.UnregisteredRespondentSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.config.LookupTestConfig;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {UnregisteredRespondentSolicitorContentProvider.class, LookupTestConfig.class,
    EmailNotificationHelper.class})
class UnregisteredRespondentSolicitorContentProviderTest extends AbstractEmailContentProviderTest {

    private static final String LOCAL_AUTHORITY_CODE = "LA_CODE";
    private static final String LOCAL_AUTHORITY_NAME = "LA name";
    private static final long CCD_NUMBER = 1234567890123456L;
    private static final String FORMATTED_CCD_NUMBER = "1234-5678-9012-3456";

    @MockBean
    private EmailNotificationHelper helper;

    @MockBean
    private LocalAuthorityNameLookupConfiguration lookup;

    @Autowired
    private UnregisteredRespondentSolicitorContentProvider underTest;

    private final List<Element<Child>> children = wrapElements(mock(Child.class));

    @BeforeEach
    void setup() {
        when(lookup.getLocalAuthorityName(LOCAL_AUTHORITY_CODE)).thenReturn(LOCAL_AUTHORITY_NAME);
        when(helper.getEldestChildLastName(children)).thenReturn("Tim Jones");
    }

    @Test
    void buildContent() {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().firstName("David").lastName("Jones").build())
            .solicitor(RespondentSolicitor.builder().firstName("John").lastName("Smith").build()).build();

        CaseData caseData = CaseData.builder()
            .id(CCD_NUMBER)
            .caseName("Test case1")
            .children1(children)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(respondent))
            .build();

        NotifyData expectedTemplateData = getExpectedTemplateData("David Jones");

        assertThat(underTest.buildContent(caseData, respondent)).isEqualTo(expectedTemplateData);
    }

    @Test
    void buildContentWithEmptyRespondentName() {
        Respondent respondent = Respondent.builder()
            .party(RespondentParty.builder().build())
            .solicitor(RespondentSolicitor.builder().firstName("John").lastName("Smith").build()).build();

        CaseData caseData = CaseData.builder()
            .id(CCD_NUMBER)
            .caseName("Test case1")
            .children1(children)
            .caseLocalAuthority(LOCAL_AUTHORITY_CODE)
            .respondents1(wrapElements(respondent))
            .build();

        NotifyData expectedTemplateData = getExpectedTemplateData(EMPTY);

        assertThat(underTest.buildContent(caseData, respondent)).isEqualTo(expectedTemplateData);
    }

    private NotifyData getExpectedTemplateData(String expectedName) {
        return UnregisteredRespondentSolicitorTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .ccdNumber(FORMATTED_CCD_NUMBER)
            .clientFullName(expectedName)
            .childLastName("Tim Jones")
            .caseName("Test case1")
            .build();
    }
}
