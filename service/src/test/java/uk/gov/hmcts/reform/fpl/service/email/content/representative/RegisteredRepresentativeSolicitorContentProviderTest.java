package uk.gov.hmcts.reform.fpl.service.email.content.representative;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.fpl.config.LocalAuthorityNameLookupConfiguration;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.Party;
import uk.gov.hmcts.reform.fpl.model.interfaces.WithSolicitor;
import uk.gov.hmcts.reform.fpl.model.notify.representative.RegisteredRepresentativeSolicitorTemplate;
import uk.gov.hmcts.reform.fpl.service.email.content.AbstractEmailContentProviderTest;
import uk.gov.hmcts.reform.fpl.utils.EmailNotificationHelper;

import java.util.List;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.String.join;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;

@ContextConfiguration(classes = {RegisteredRepresentativeSolicitorContentProvider.class})
class RegisteredRepresentativeSolicitorContentProviderTest extends AbstractEmailContentProviderTest {
    private static final String REPRESENTATIVE_FIRST_NAME = "John";
    private static final String REPRESENTATIVE_LAST_NAME = "Smith";
    private static final String FIRST_NAME = "Emma";
    private static final String LAST_NAME = "Williams";
    private static final String MANAGE_ORG_LINK = "https://manage-org.platform.hmcts.net";
    private static final Long CASE_REFERENCE = 12345L;
    private static final String CASE_NAME = "Test case1";

    private final WithSolicitor representable = mock(WithSolicitor.class);
    private final WithSolicitor representable2 = mock(WithSolicitor.class);
    private final WithSolicitor representable3 = mock(WithSolicitor.class);
    private final Party party = mock(Party.class);
    private final Party party2 = mock(Party.class);
    private final CaseData caseData = mock(CaseData.class);
    private final RespondentSolicitor solicitor = mock(RespondentSolicitor.class);
    private final RespondentSolicitor solicitor2 = mock(RespondentSolicitor.class);


    @MockBean
    private EmailNotificationHelper helper;
    @MockBean
    private LocalAuthorityNameLookupConfiguration lookup;

    @Autowired
    private RegisteredRepresentativeSolicitorContentProvider underTest;

    @BeforeEach
    void setUp() {
        List<Element<Child>> children = wrapElements(mock(Child.class));

        when(caseData.getId()).thenReturn(CASE_REFERENCE);
        when(caseData.getCaseName()).thenReturn(CASE_NAME);
        when(caseData.getCaseLocalAuthority()).thenReturn(LOCAL_AUTHORITY_CODE);
        when(caseData.getAllChildren()).thenReturn(children);

        when(helper.getEldestChildLastName(children)).thenReturn("Tim Jones");
        when(lookup.getLocalAuthorityName(LOCAL_AUTHORITY_CODE)).thenReturn(LOCAL_AUTHORITY_NAME);
    }

    @ParameterizedTest
    @MethodSource("representativeNameSource")
    void shouldReturnExpectedMapWithRepresentativeNameAndLocalAuthorityName(String firstName, String lastName,
                                                                            String expectedSalutation) {

        when(representable.toParty()).thenReturn(party);
        when(party.getFullName()).thenReturn(FIRST_NAME + " " + LAST_NAME);
        when(representable.getSolicitor()).thenReturn(solicitor);
        when(solicitor.getFullName())
            .thenReturn(join(" ", defaultString(firstName), defaultString(lastName)).trim());

        assertThat(underTest.buildContent(caseData, representable))
            .isEqualTo(buildRegisteredSolicitorTemplate(expectedSalutation, format("%s %s", FIRST_NAME, LAST_NAME)));
    }

    @Test
    void shouldReturnMultipleRepresentatives() {
        when(representable.getSolicitor()).thenReturn(solicitor);
        when(representable2.getSolicitor()).thenReturn(solicitor2);
        when(representable3.getSolicitor()).thenReturn(solicitor);

        when(solicitor.getFullName()).thenReturn("John Smith");

        when(representable.toParty()).thenReturn(party);
        when(representable3.toParty()).thenReturn(party2);

        when(party.getFullName()).thenReturn("Dave Davidson");
        when(party2.getFullName()).thenReturn("Daisy Davidson");

        assertThat(underTest.buildContent(caseData, solicitor, List.of(representable, representable3)))
            .isEqualTo(buildRegisteredSolicitorTemplate("Dear John Smith", "Dave Davidson, Daisy Davidson"));
    }

    @Test
    void shouldReturnExpectedMapWithEmptyRepresentableName() {
        when(representable.toParty()).thenReturn(null);
        when(representable2.toParty()).thenReturn(party2);
        when(party2.getFullName()).thenReturn("");

        when(solicitor.getFullName()).thenReturn("John Smith");

        assertThat(underTest.buildContent(caseData, solicitor, List.of(representable, representable2)))
            .isEqualTo(buildRegisteredSolicitorTemplate("Dear John Smith", EMPTY));
    }

    private static Stream<Arguments> representativeNameSource() {
        final String salutationPrefix = "Dear ";
        final String expectedSalutationLastName = salutationPrefix + REPRESENTATIVE_LAST_NAME;
        final String expectedSalutationFirstName = salutationPrefix + REPRESENTATIVE_FIRST_NAME;
        final String expectedSalutationFullName = format(
            "%s%s %s", salutationPrefix, REPRESENTATIVE_FIRST_NAME, REPRESENTATIVE_LAST_NAME
        );

        return Stream.of(
            Arguments.of(null, REPRESENTATIVE_LAST_NAME, expectedSalutationLastName),
            Arguments.of(EMPTY, REPRESENTATIVE_LAST_NAME, expectedSalutationLastName),
            Arguments.of(REPRESENTATIVE_FIRST_NAME, null, expectedSalutationFirstName),
            Arguments.of(REPRESENTATIVE_FIRST_NAME, EMPTY, expectedSalutationFirstName),
            Arguments.of(null, null, EMPTY),
            Arguments.of(EMPTY, EMPTY, EMPTY),
            Arguments.of(REPRESENTATIVE_FIRST_NAME, REPRESENTATIVE_LAST_NAME, expectedSalutationFullName)
        );
    }

    private RegisteredRepresentativeSolicitorTemplate buildRegisteredSolicitorTemplate(String salutation,
                                                                                       String clientName) {
        return RegisteredRepresentativeSolicitorTemplate.builder()
            .localAuthority(LOCAL_AUTHORITY_NAME)
            .salutation(salutation)
            .clientFullName(clientName)
            .manageOrgLink(MANAGE_ORG_LINK)
            .ccdNumber(CASE_REFERENCE.toString())
            .caseName(CASE_NAME)
            .childLastName("Tim Jones")
            .build();
    }
}
