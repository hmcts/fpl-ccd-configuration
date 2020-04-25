package uk.gov.hmcts.reform.fpl.service.casesubmission;

import uk.gov.hmcts.reform.fpl.model.Allocation;
import uk.gov.hmcts.reform.fpl.model.DocmosisAnnexDocuments;
import uk.gov.hmcts.reform.fpl.model.DocmosisFactorsParenting;
import uk.gov.hmcts.reform.fpl.model.DocmosisHearing;
import uk.gov.hmcts.reform.fpl.model.DocmosisHearingPreferences;
import uk.gov.hmcts.reform.fpl.model.DocmosisInternationalElement;
import uk.gov.hmcts.reform.fpl.model.DocmosisProceeding;
import uk.gov.hmcts.reform.fpl.model.DocmosisRisks;
import uk.gov.hmcts.reform.fpl.model.common.DocmosisSocialWorkOther;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisApplicant;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisCaseSubmission;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisChild;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisOtherParty;
import uk.gov.hmcts.reform.fpl.model.docmosis.DocmosisRespondent;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static java.time.LocalDate.now;
import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.List.of;
import static uk.gov.hmcts.reform.fpl.service.DocmosisTemplateDataGeneration.generateCourtSealEncodedString;

public class SampleCaseSubmissionTestDataHelper {
    static final String BASE_64 = "image:base64:%1$s";

    private SampleCaseSubmissionTestDataHelper() {
    }

    public static DocmosisCaseSubmission expectedDCaseSubmissionTemplateData() throws IOException {
        return DocmosisCaseSubmission.builder()
            .respondents(expectedDocmosisRespondents())
            .applicants(expectedDocmosisApplicants())
            .children(expectedDocmosisChildren())
            .others(expectedDocmosisOtherParty())
            .applicantOrganisations("London Borough of Southwark")
            .respondentNames("Paul Smith\nJames Smith\nAn Other")
            .ordersNeeded("Emergency protection order")
            .directionsNeeded("Contact with any named personYes\n")
            .hearing(expectedDocmosisHearing())
            .allocation(expectedAllocation())
            .hearingPreferences(expectedDocmosisHearingPreferences())
            .internationalElement(expectedDocmosisInternationalElement())
            .courtseal(format(BASE_64, generateCourtSealEncodedString()))
            .draftWaterMark(null)
            .userFullName("Professor")
            .submittedDate(now().format(ofPattern("d MMMM yyyy")))
            .groundsForEPOReason("There’s reasonable cause to believe the child is likely to suffer"
                + " significant harm if they’re not moved to accommodation provided by you, or on your behalf\n"
                + "There’s reasonable cause to believe the child is likely to suffer significant harm if they don’t "
                + "stay in their current accommodation")
            .groundsThresholdReason("Not receiving care that would be reasonably expected "
                + "from a parent.\nBeyond parental control.\n")
            .thresholdDetails("grounds for application criteria")
            .risks(expectedDocmosisRisks())
            .factorsParenting(expectedDocmosisFactorsParenting())
            .proceeding(expectedDocmosisProceeding())
            .annexDocuments(expectedDocmosisAnnexDocuments())
            .build();
    }

    public static List<DocmosisRespondent> expectedDocmosisRespondents() {
        return of(DocmosisRespondent.builder()
                .name("Paul Smith")
                .age("75 years old")
                .gender("Male")
                .dateOfBirth("2 May 1944")
                .placeOfBirth("Crewe")
                .address("-")
                .contactDetailsHidden("No")
                .contactDetailsHiddenDetails("No")
                .telephoneNumber("07712345678")
                .relationshipToChild("Uncle")
                .litigationIssuesDetails("Yes\nrespondent one test litigation reason")
                .build(),
            DocmosisRespondent.builder()
                .name("James Smith")
                .age("87 years old")
                .gender("Unknown")
                .dateOfBirth("2 April 1933")
                .placeOfBirth("Newry")
                .address("Unit 12\nTransa Way\nHillsborough\nLurgan\nDown\nBT26 6RJ\nUnited Kingdom")
                .contactDetailsHidden("No")
                .contactDetailsHiddenDetails("No")
                .telephoneNumber("02892611016")
                .relationshipToChild("Brother")
                .litigationIssuesDetails("Yes\nrespondent two litigation capacity reason")
                .build(),
            DocmosisRespondent.builder()
                .name("An Other")
                .age("87 years old")
                .gender("Male")
                .dateOfBirth("2 April 1933")
                .placeOfBirth("Reading")
                .address("Flat 90\nSurrey street\nSurrey road\nSurrey\nCroydon\nBT22 2345\nUK")
                .contactDetailsHidden("-")
                .contactDetailsHiddenDetails("-")
                .telephoneNumber("0987654321")
                .relationshipToChild("Cousin")
                .litigationIssuesDetails("No")
                .build());
    }

    public static List<DocmosisApplicant> expectedDocmosisApplicants() {
        return of(DocmosisApplicant.builder()
            .organisationName("London Borough of Southwark")
            .contactName("Jane Smith")
            .jobTitle("Legal adviser")
            .address("160 Tooley St\nTooley road\nTooley\nLimerick\nGalway\nSE1 2QH\nIreland")
            .email("jane@smith.com")
            .mobileNumber("2020202020")
            .telephoneNumber("02120202020")
            .pbaNumber("PBA1234567")
            .solicitorName("Brian Banks")
            .solicitorMobile("7665545327")
            .solicitorTelephone("020 2772 5772")
            .solicitorEmail("brian@banks.com")
            .solicitorDx("106 Southwark &")
            .solicitorReference("12345")
            .build());
    }

    public static List<DocmosisChild> expectedDocmosisChildren() {
        return of(DocmosisChild.builder()
                .name("Tom Reeves")
                .age("1 year old")
                .gender("Boy")
                .dateOfBirth("15 June 2018")
                .livingSituation("Living with respondents\nConfidentialDate this began: 8 November 2018")
                .keyDates("child starting primary school or taking GCSEs")
                .careAndContactPlan("Place baby in local authority foster care")
                .adoption("Yes")
                .placementOrderApplication("Yes")
                .placementCourt("Central London County Court")
                .mothersName("Isbella Reeves")
                .fathersName("Rob Reeves")
                .fathersResponsibility("Yes")
                .socialWorkerName("Helen Green")
                .socialWorkerTelephoneNumber("-")
                .additionalNeeds("Yes\nAutism")
                .litigationIssues("-")
                .detailsHiddenReason("Yes\nHistory of domestic violence with relatives")
                .build(),
            DocmosisChild.builder()
                .name("Sarah Reeves")
                .age("18 years old")
                .gender("Girl")
                .dateOfBirth("2 February 2002")
                .livingSituation("Living with respondentsCarnegie House\nCentral Milton Keynes\nMilton Keynes"
                    + "\nMK\nLondon\nMK10 1SA\nBT66 7RRDate this began: 2 February 2002")
                .keyDates("test child two key date")
                .careAndContactPlan("test child two care and contact plan")
                .adoption("Yes")
                .placementOrderApplication("Yes")
                .placementCourt("test child two court")
                .mothersName("Sarah Simpson")
                .fathersName("Scott Simpson")
                .fathersResponsibility("Yes")
                .socialWorkerName("Paul Wilsdon")
                .socialWorkerTelephoneNumber("-")
                .additionalNeeds("Yes\nlearning disabilities")
                .litigationIssues("Yes\ntest child two ability to take part in proceedings")
                .detailsHiddenReason("No\ntest child two contact details hidden reason")
                .build());
    }

    public static List<DocmosisOtherParty> expectedDocmosisOtherParty() {
        return of(DocmosisOtherParty.builder()
                .name("Jason Lavery")
                .gender("Unknown")
                .dateOfBirth("2 February 1999")
                .placeOfBirth("Armagh")
                .address("Flat 13\nPortadown street\nPortadown road\nPortadown\nArmagh\nBT43 343\nN.Ire")
                .detailsHidden("-")
                .detailsHiddenReason("-")
                .telephoneNumber("07749972245")
                .relationshipToChild("Cousin")
                .litigationIssuesDetails("No")
                .build(),
            DocmosisOtherParty.builder()
                .name("Peter Lavery")
                .gender("Male")
                .dateOfBirth("2 February 2000")
                .placeOfBirth("Lisburn")
                .address("Confidential")
                .detailsHidden("Yes")
                .detailsHiddenReason("Yes\nOther two hide from parties")
                .telephoneNumber("Confidential")
                .relationshipToChild("Friend of family")
                .litigationIssuesDetails("Yes\nOther two inability to take part in proceedings")
                .build());
    }

    public static DocmosisHearing expectedDocmosisHearing() {
        return DocmosisHearing.builder()
            .typeAndReason("Contested interim care order\nStandard case management hearing")
            .timeFrame("Same day\nBaby will be discharged from hospital on 21 June 2018")
            .withoutNoticeDetails("Yes\nNotice without hearing needed")
            .reducedNoticeDetails("Yes\nBaby needs to be discharged from the hospital")
            .respondentsAware("Yes")
            .respondentsAwareReason("They seek to care for baby in mother and baby unit")
            .build();
    }

    public static Allocation expectedAllocation() {
        return Allocation.builder()
            .proposal("Section 9 circuit judge")
            .proposalReason("allocation proposal reason")
            .build();
    }

    public static DocmosisHearingPreferences expectedDocmosisHearingPreferences() {
        return DocmosisHearingPreferences.builder()
            .interpreter("Yes\ninterpreter required")
            .welshDetails("Yes\nwelsh proceedings")
            .intermediary("Yes\nintermediary hearing required")
            .disabilityAssistance("Yes\nlearning disability")
            .extraSecurityMeasures("Yes\nsecurity needed")
            .somethingElse("Yes\nI need this from someone")
            .build();
    }

    public static DocmosisInternationalElement expectedDocmosisInternationalElement() {
        return DocmosisInternationalElement.builder()
            .possibleCarer("Yes\nAunt outside UK")
            .significantEvents("Yes\nSomething happened in France")
            .proceedings("Yes\nOutside of the UK")
            .internationalAuthorityInvolvement("Yes\nFrench authorities were involved")
            .issues("Yes\nBrussels 2 regulation")
            .build();
    }

    public static DocmosisRisks expectedDocmosisRisks() {
        return DocmosisRisks.builder()
            .neglectDetails("Yes\nPast harm\nFuture risk of harm")
            .sexualAbuseDetails("Yes\nPast harm\nFuture risk of harm")
            .physicalHarmDetails("Yes\nPast harm\nFuture risk of harm")
            .emotionalHarmDetails("Yes\nPast harm\nFuture risk of harm")
            .build();
    }

    private static DocmosisFactorsParenting expectedDocmosisFactorsParenting() {
        return DocmosisFactorsParenting.builder()
            .anythingElse("No")
            .alcoholDrugAbuseDetails("Yes\nhistory of drug abuse")
            .domesticViolenceDetails("Yes\nhistory of domestic violence")
            .build();
    }

    public static List<DocmosisProceeding> expectedDocmosisProceeding() {
        return List.of(DocmosisProceeding.builder()
                .onGoingProceeding("Yes")
                .proceedingStatus("Ongoing")
                .caseNumber("12345")
                .started("02-02-2002")
                .ended("02-02-2006")
                .ordersMade("Supervision order")
                .judge("William Peters")
                .children("children subject to proceedings")
                .guardian("Mark Watson")
                .sameGuardianDetails("No")
                .build(),
            DocmosisProceeding.builder()
                .onGoingProceeding("Yes")
                .proceedingStatus("Ongoing")
                .caseNumber("12345")
                .started("02-02-2008")
                .ended("02-02-2009")
                .ordersMade("Supervision order")
                .judge("Peters Williams")
                .children("children subject to proceedings")
                .guardian("John Watson")
                .sameGuardianDetails("No")
                .build());
    }

    public static DocmosisAnnexDocuments expectedDocmosisAnnexDocuments() {
        return DocmosisAnnexDocuments.builder()
            .socialWorkChronology("To follow\nSocial work chronology text")
            .socialWorkStatement("To follow\nSocial work statement and genogram text")
            .socialWorkAssessment("To follow\nSocial work assessment text")
            .socialWorkCarePlan("To follow\nsome text")
            .socialWorkEvidenceTemplate("Attached")
            .thresholdDocument("Attached")
            .checklistDocument("Attached")
            .others(of(DocmosisSocialWorkOther.builder()
                    .documentTitle("document_one")
                    .build(),
                DocmosisSocialWorkOther.builder()
                    .documentTitle("document_two")
                    .build()))
            .build();
    }
}
