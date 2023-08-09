package uk.gov.hmcts.reform.fpl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.Child;
import uk.gov.hmcts.reform.fpl.model.ChildParty;
import uk.gov.hmcts.reform.fpl.model.LocalAuthority;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentParty;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.fpl.model.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.fpl.service.document.PartyListGenerator;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;

class PartyListGeneratorTest {

    private static final UUID CHILD_1_ID = UUID.randomUUID();
    private static final UUID CHILD_2_ID = UUID.randomUUID();
    private static final UUID RESPONDENT_1_ID = UUID.randomUUID();
    private static final UUID RESPONDENT_2_ID = UUID.randomUUID();
    private static final UUID LOCAL_AUTHORITY_1_ID = UUID.randomUUID();
    private static final UUID LOCAL_AUTHORITY_2_ID = UUID.randomUUID();
    private static final Child CHILD_1 = Child.builder().party(ChildParty.builder()
                                                                .firstName("Fred")
                                                                .lastName("Roberts")
                                                                .build())
                                                        .build();
    private static final Child CHILD_2 = Child.builder().party(ChildParty.builder()
                                                        .firstName("Robert")
                                                        .lastName("Frederson")
                                                        .build())
                                                    .build();
    private static final Respondent RESPONDENT_1 = Respondent.builder().party(RespondentParty.builder()
                                                                                .firstName("Jim")
                                                                                .lastName("Jimerson")
                                                                                .build())
                                                                        .build();
    private static final Respondent RESPONDENT_2 = Respondent.builder().party(RespondentParty.builder()
                                                                                .firstName("Tom")
                                                                                .lastName("Tomlinson")
                                                                                .build())
                                                                            .build();
    private static final LocalAuthority LOCAL_AUTHORITY_1 = LocalAuthority.builder()
                                                                            .name("An LA or Solicitor")
                                                                            .build();
    private static final LocalAuthority LOCAL_AUTHORITY_2 = LocalAuthority.builder()
                                                                            .name("Another LA or Solicitor")
                                                                            .build();

    private DynamicListService dynamicListService = new DynamicListService(new ObjectMapper());

    private PartyListGenerator underTest = new PartyListGenerator(dynamicListService);

    private static CaseData caseData;

    @BeforeEach
    void setup() {
        List<Element<Child>> children = List.of(
            element(CHILD_1_ID, CHILD_1),
            element(CHILD_2_ID, CHILD_2));

        List<Element<Respondent>> respondents = List.of(
            element(RESPONDENT_1_ID, RESPONDENT_1),
            element(RESPONDENT_2_ID, RESPONDENT_2));

        List<Element<LocalAuthority>> localAuthorities = List.of(
            element(LOCAL_AUTHORITY_1_ID, LOCAL_AUTHORITY_1),
            element(LOCAL_AUTHORITY_2_ID, LOCAL_AUTHORITY_2));


        caseData = CaseData.builder()
            .respondents1(respondents)
            .children1(children)
            .localAuthorities(localAuthorities)
            .build();
    }

    @Test
    void shouldReturnAllApplicantsList() {
        DynamicList actualDynamicList = underTest.buildPartyList(caseData);

        assertThat(actualDynamicList.getListItems())
            .extracting(DynamicListElement::getLabel)
            .containsExactly(
                "Respondent - " + RESPONDENT_1.getParty().getFullName(),
                "Respondent - " + RESPONDENT_2.getParty().getFullName(),
                "Child - " + CHILD_1.getParty().getFullName(),
                "Child - " + CHILD_2.getParty().getFullName(),
                "Applicant - " + LOCAL_AUTHORITY_1.getName(),
                "Applicant - " + LOCAL_AUTHORITY_2.getName());
    }
}
