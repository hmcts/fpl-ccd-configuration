package uk.gov.hmcts.reform.fpl.service.legalcounsel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.fpl.model.CaseData;
import uk.gov.hmcts.reform.fpl.model.LegalCounsellor;
import uk.gov.hmcts.reform.fpl.model.Respondent;
import uk.gov.hmcts.reform.fpl.model.RespondentSolicitor;
import uk.gov.hmcts.reform.fpl.model.common.Element;
import uk.gov.hmcts.reform.fpl.service.CaseConverter;
import uk.gov.hmcts.reform.fpl.service.CaseRoleLookupService;
import uk.gov.hmcts.reform.fpl.service.OrganisationService;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.fpl.Constants.TEST_CASE_ID_AS_LONG;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORA;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.CHILDSOLICITORC;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORB;
import static uk.gov.hmcts.reform.fpl.enums.SolicitorRole.SOLICITORC;
import static uk.gov.hmcts.reform.fpl.utils.CoreCaseDataStoreLoader.getCaseConverterInstance;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.fpl.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondentWithSolicitor;
import static uk.gov.hmcts.reform.fpl.utils.RespondentsTestHelper.respondents;
import static uk.gov.hmcts.reform.fpl.utils.TestDataHelper.testChildren;

public class ManageLegalCounselServiceIndirectUpdateTest {

    private static final Element<LegalCounsellor> TEST_LEGAL_COUNSELLOR = element(LegalCounsellor.builder()
        .firstName("Ted")
        .lastName("Robinson")
        .email("ted.robinson@example.com")
        .organisation(Organisation.organisation("123"))
        .build()
    );

    private final CaseConverter caseConverter = getCaseConverterInstance();
    private final CaseRoleLookupService caseRoleLookupService = mock(CaseRoleLookupService.class);
    private final OrganisationService organisationService = mock(OrganisationService.class);
    private final ManageLegalCounselService manageLegalCounselService =
        new ManageLegalCounselService(caseConverter, caseRoleLookupService, organisationService);

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        when(caseRoleLookupService.getCaseSolicitorRolesForCurrentUser(TEST_CASE_ID))
            .thenReturn(asList(SOLICITORB, SOLICITORC, CHILDSOLICITORA, CHILDSOLICITORC));

        caseData = CaseData.builder()
            .id(TEST_CASE_ID_AS_LONG)
            .children1(testChildren())
            .respondents1(respondents())
            .build();
    }//TODO - reconsider everything above this point - do it last

    @Test
    void shouldRemoveLegalCounselWhenSolicitorIsRemovedFromRepresentedParty() {
        List<Element<LegalCounsellor>> legalCounsel = asList(TEST_LEGAL_COUNSELLOR);
        Respondent respondent1 = respondentWithSolicitor().toBuilder()
            .legalCounsellors(legalCounsel)
            .build();
        Respondent respondent2 = respondentWithSolicitor().toBuilder()
            .legalCounsellors(legalCounsel)
            .build();

        List<Element<Respondent>> previousRespondents = wrapElements(respondent1, respondent2);
        List<Element<Respondent>> currentRespondents = wrapElements(
            respondent1.toBuilder().solicitor(null).build(),
            respondent2
        );

        List<Element<Respondent>> modifiedRespondentsForCurrentCaseData =
            manageLegalCounselService.updateLegalCounselForRemovedSolicitors(previousRespondents, currentRespondents);

        assertThat(modifiedRespondentsForCurrentCaseData).hasSize(2);
        assertThat(modifiedRespondentsForCurrentCaseData.get(0).getValue().getLegalCounsellors()).isEmpty();
        assertThat(modifiedRespondentsForCurrentCaseData.get(1).getValue().getLegalCounsellors()).isEqualTo(legalCounsel);
    }

    @Test
    void shouldNotRemoveLegalCounselWhenSolicitorOrganisationIdDoesNotChange() {
        List<Element<LegalCounsellor>> legalCounsel = asList(TEST_LEGAL_COUNSELLOR);
        Respondent respondentBefore = Respondent.builder()
            .solicitor(RespondentSolicitor.builder().firstName("Ted").organisation(Organisation.organisation("123")).build())
            .legalCounsellors(legalCounsel)
            .build();
        Respondent respondentAfter = Respondent.builder()
            .solicitor(RespondentSolicitor.builder().firstName("John").organisation(Organisation.organisation("123")).build())
            .legalCounsellors(legalCounsel)
            .build();

        List<Element<Respondent>> modifiedRespondentsForCurrentCaseData =
            manageLegalCounselService.updateLegalCounselForRemovedSolicitors(
                wrapElements(respondentBefore),
                wrapElements(respondentAfter)
            );//TODO - this should look into children and respondents

        assertThat(modifiedRespondentsForCurrentCaseData).hasSize(1);
        assertThat(modifiedRespondentsForCurrentCaseData.get(0).getValue().getLegalCounsellors()).isEqualTo(legalCounsel);
    }

    @Test
    void shouldRemoveLegalCounselIfNewSolicitorOrganisationDoesNotHaveLegalCounselInCaseData() {
        List<Element<LegalCounsellor>> legalCounsel = asList(TEST_LEGAL_COUNSELLOR);
        Respondent respondentBefore = Respondent.builder()
            .solicitor(RespondentSolicitor.builder().firstName("Ted").organisation(Organisation.organisation("123")).build())
            .legalCounsellors(legalCounsel)
            .build();
        Respondent respondentAfter = Respondent.builder()
            .solicitor(RespondentSolicitor.builder().firstName("Billy").organisation(Organisation.organisation("456")).build())
            .legalCounsellors(legalCounsel)
            .build();

        List<Element<Respondent>> modifiedRespondentsForCurrentCaseData =
            manageLegalCounselService.updateLegalCounselForRemovedSolicitors(
                wrapElements(respondentBefore),
                wrapElements(respondentAfter)
            );//TODO - this should look into children and respondents

        assertThat(modifiedRespondentsForCurrentCaseData).hasSize(1);
        assertThat(modifiedRespondentsForCurrentCaseData.get(0).getValue().getLegalCounsellors()).isEmpty();
    }

    @Test
    void shouldCopyLegalCounselOverIfSameOrganisationAlreadyHasLegalCounselInCaseData() {
        Organisation solicitorOrganisationA = Organisation.organisation("123");
        RespondentSolicitor solicitorA = RespondentSolicitor.builder()
            .firstName("Bob")
            .organisation(solicitorOrganisationA)
            .build();
        List<Element<LegalCounsellor>> legalCounselA = asList(TEST_LEGAL_COUNSELLOR);
        Respondent firstRespondentBefore = Respondent.builder().build();//No representation
        Respondent secondRespondentBefore = Respondent.builder()
            .solicitor(solicitorA)
            .legalCounsellors(legalCounselA)
            .build();
        Respondent firstRespondentAfter = Respondent.builder()
            .solicitor(solicitorA)//Now represented by someone who already has a legal counsel
            .build();
        Respondent secondRespondentAfter = Respondent.builder()//No change on this respondent
            .solicitor(solicitorA)
            .legalCounsellors(legalCounselA)
            .build();
        //TODO - apply this logic for a child later, instead of respondent

        List<Element<Respondent>> modifiedRespondentsForCurrentCaseData =
            manageLegalCounselService.updateLegalCounselForRemovedSolicitors(
                wrapElements(firstRespondentBefore, secondRespondentBefore),
                wrapElements(firstRespondentAfter, secondRespondentAfter)
            );

        assertThat(modifiedRespondentsForCurrentCaseData).hasSize(2);
        assertThat(modifiedRespondentsForCurrentCaseData.get(0).getValue().getLegalCounsellors()).isEqualTo(legalCounselA);//Legal counsel from the other organisation was copied over
        assertThat(modifiedRespondentsForCurrentCaseData.get(1).getValue().getLegalCounsellors()).isEqualTo(legalCounselA);//Legal counsel kept intact
    }

    @Test
    void shouldCopyLegalCounselOverIfNewSolicitorOrganisationAlreadyHasLegalCounselInCaseData() {
        Organisation solicitorOrganisationA = Organisation.organisation("123");
        Organisation solicitorOrganisationB = Organisation.organisation("456");
        RespondentSolicitor solicitorA = RespondentSolicitor.builder()
            .firstName("Bob")
            .organisation(solicitorOrganisationA)
            .build();
        RespondentSolicitor firstSolicitorFromOrganisationB = RespondentSolicitor.builder()
            .firstName("Charles")
            .organisation(solicitorOrganisationB)
            .build();
        RespondentSolicitor secondSolicitorFromOrganisationB = RespondentSolicitor.builder()
            .firstName("Duncan")
            .organisation(solicitorOrganisationB)
            .build();
        List<Element<LegalCounsellor>> legalCounselA = asList(TEST_LEGAL_COUNSELLOR);
        List<Element<LegalCounsellor>> legalCounselB = wrapElements(LegalCounsellor.builder()
            .firstName("Aaron")
            .build());
        Respondent firstRespondentBefore = Respondent.builder()
            .solicitor(solicitorA)
            .legalCounsellors(legalCounselA)
            .build();
        Respondent secondRespondentBefore = Respondent.builder()
            .solicitor(firstSolicitorFromOrganisationB)//Solicitor changed to an existing organisation
            .legalCounsellors(legalCounselB)
            .build();
        Respondent firstRespondentAfter = Respondent.builder()
            .solicitor(secondSolicitorFromOrganisationB)
            .legalCounsellors(legalCounselA)//Counsel is not changed on screen
            .build();
        Respondent secondRespondentAfter = Respondent.builder()
            .solicitor(firstSolicitorFromOrganisationB)//No change on this solicitor
            .legalCounsellors(legalCounselB)
            .build();
        //TODO - apply this logic for a child later, instead of respondent

        List<Element<Respondent>> modifiedRespondentsForCurrentCaseData =
            manageLegalCounselService.updateLegalCounselForRemovedSolicitors(//TODO - consider this name - it doesn't only remove legal counsel, it copies as well
                wrapElements(firstRespondentBefore, secondRespondentBefore),
                wrapElements(firstRespondentAfter, secondRespondentAfter)
            );//TODO - this should look into children and respondents

        assertThat(modifiedRespondentsForCurrentCaseData).hasSize(2);
        assertThat(modifiedRespondentsForCurrentCaseData.get(0).getValue().getLegalCounsellors()).isEqualTo(legalCounselB);//Legal counsel from the other organisation was copied over
        assertThat(modifiedRespondentsForCurrentCaseData.get(1).getValue().getLegalCounsellors()).isEqualTo(legalCounselB);//Legal counsel kept intact
    }

}
