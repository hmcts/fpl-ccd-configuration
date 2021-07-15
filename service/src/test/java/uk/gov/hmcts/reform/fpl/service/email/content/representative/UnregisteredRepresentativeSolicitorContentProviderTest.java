package uk.gov.hmcts.reform.fpl.service.email.content.representative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.NotifyData;
import uk.gov.hmcts.reform.fpl.model.notify.representative.UnregisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {UnregisteredRepresentativeSolicitorContentProvider.class})
class UnregisteredRepresentativeSolicitorContentProviderTest extends AbstractEmailContentProviderTest {

    private static final String LOCAL_AUTHORITY_CODE = "LA_CODE";
    private static final String LOCAL_AUTHORITY_NAME = "LA name";
    private static final long CCD_NUMBER = 1234567890123456L;
    private static final String FORMATTED_CCD_NUMBER = "1234-5678-9012-3456";
    private static final String CASE_NAME = "Test case1";

    private final WithSolicitor representable = mock(WithSolicitor.class);
    private final Party party = mock(Party.class);
    private final CaseData caseData = mock(CaseData.class);


    @MockBean
    private EmailNotificationHelper helper;

    @MockBean
    private LocalAuthorityNameLookupConfiguration lookup;

    @Autowired
    private UnregisteredRepresentativeSolicitorContentProvider underTest;

    @BeforeEach
    void setup() {
        List<Element<Child>> children = wrapElements(mock(Child.class));

        when(caseData.getId()).thenReturn(CCD_NUMBER);
        when(caseData.getCaseName()).thenReturn(CASE_NAME);
        when(caseData.getCaseLocalAuthority()).thenReturn(LOCAL_AUTHORITY_CODE);
        when(caseData.getAllChildren()).thenReturn(children);

        when(lookup.getLocalAuthorityName(LOCAL_AUTHORITY_CODE)).thenReturn(LOCAL_AUTHORITY_NAME);
        when(helper.getEldestChildLastName(children)).thenReturn("Tim Jones");
    }

    @Test
    void buildContent() {
        when(representable.toParty()).thenReturn(party);
        when(party.getFullName()).thenReturn("David Jones");

        NotifyData expectedTemplateData = getExpectedTemplateData("David Jones");

        assertThat(underTest.buildContent(caseData, representable)).isEqualTo(expectedTemplateData);
    }

    @Test
    void buildContentWithEmptyRespondentName() {
        when(representable.toParty()).thenReturn(null);

        NotifyData expectedTemplateData = getExpectedTemplateData(EMPTY);

        assertThat(underTest.buildContent(caseData, representable)).isEqualTo(expectedTemplateData);
    }

    private NotifyData getExpectedTemplateData(String expectedName) {
        return UnregisteredRepresentativeSolicitorTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .ccdNumber(FORMATTED_CCD_NUMBER)
            .clientFullName(expectedName)
            .childLastName("Tim Jones")
            .caseName("Test case1")
            .build();
    }
}
