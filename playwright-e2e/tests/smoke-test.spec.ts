import {expect, test} from "../fixtures/fixtures";
import {newSwanseaLocalAuthorityUserOne, privateSolicitorOrgUser} from "../settings/user-credentials";
import {CreateCaseName} from "../utils/create-case-name";
import {CaseFileView} from "../pages/case-file-view";

test.describe('', () => {
    test.slow();
    test.only("Local Authority submit C110A application @smoke-test @accessibility", async ({
                                                                                           signInPage,
                                                                                           createCase,
                                                                                           ordersAndDirectionSought,
                                                                                           startApplication,
                                                                                           hearingUrgency,
                                                                                           groundsForTheApplication,
                                                                                           applicantDetails,
                                                                                           allocationProposal,
                                                                                           addApplicationDocuments,
                                                                                           childDetails,
                                                                                           respondentDetails,
                                                                                           submitCase,
                                                                                           page,
                                                                                           makeAxeBuilder
                                                                                       }, testInfo) => {
        // Marking this test slow to increase the time for 3 times of other test

        // 1. Sign in as local-authority user
        await signInPage.visit();
        await signInPage.login(
            newSwanseaLocalAuthorityUserOne.email,
            newSwanseaLocalAuthorityUserOne.password,
        );
        //sign in page
        await signInPage.isSignedIn();

        // Add application details
        // Start new case, get case id and assert case id is created
        await createCase.caseName();
        await createCase.createCase();
        await createCase.submitCase(createCase.generatedCaseName);
        //this has to be refracted to new test as the test execution time exceed 8m
        //await createCase.checkCaseIsCreated(createCase.generatedCaseName);

        // Orders and directions sought
        await ordersAndDirectionSought.ordersAndDirectionsNeeded();
        await startApplication.addApplicationDetailsHeading.isVisible();

        // Hearing urgency
        await startApplication.hearingUrgency();
        await expect(hearingUrgency.hearingUrgencyHeading).toBeVisible();
        await hearingUrgency.hearingUrgencySmokeTest();

        // Grounds for the application
        await startApplication.groundsForTheApplication();
        await groundsForTheApplication.groundsForTheApplicationSmokeTest();
        await startApplication.groundsForTheApplicationHasBeenUpdated();

        //Add application documents
        await startApplication.addApplicationDetailsHeading.isVisible();
        await startApplication.addApplicationDocuments();
        await addApplicationDocuments.uploadDocumentSmokeTest();
        await startApplication.addApplicationDocumentsInProgress();

// Applicant Details
        await startApplication.applicantDetails();
        await applicantDetails.applicantDetailsNeeded();
        await startApplication.applicantDetailsHasBeenUpdated();

        // Child details
        await startApplication.childDetails();
        await childDetails.childDetailsNeeded();
        await startApplication.childDetailsHasBeenUpdated();

        // // Add respondents' details
        await startApplication.respondentDetails();
        await respondentDetails.respondentDetailsNeeded();

        // Allocation Proposal
        await startApplication.allocationProposal();
        await allocationProposal.allocationProposalSmokeTest();
        await startApplication.allocationProposalHasBeenUpdated();

        // Submit the case
        await startApplication.submitCase();
        await submitCase.submitCaseSmokeTest('2,515.00');

        const accessibilityScanResults = await makeAxeBuilder()
            // Automatically uses the shared AxeBuilder configuration,
            // but supports additional test-specific configuration too
            .analyze();

        await testInfo.attach('accessibility-scan-results', {
            body: JSON.stringify(accessibilityScanResults, null, 2),
            contentType: 'application/json'
        });

        expect(accessibilityScanResults.violations).toEqual([]);
    });
    test('Private solicitor applies C110a application', async ({
                             signInPage,
                             createCase,
                             ordersAndDirectionSought,
                             startApplication,
                             hearingUrgency,
                             groundsForTheApplication,
                             applicantDetails,
                             allocationProposal,
                             addApplicationDocuments,
                             childDetails,
                             respondentDetails,
                             submitCase,
                             page,
        caseFileView,
                             makeAxeBuilder
                         }, testInfo) => {

        // 1. Sign in as local-authority user
        await signInPage.visit();
        await signInPage.login(
            privateSolicitorOrgUser.email,
            privateSolicitorOrgUser.password,
        );
        //sign in page
        await signInPage.isSignedIn();

        // Add application details
        // Start new case, get case id and assert case id is created
        await createCase.caseName();
        await createCase.createCase();
        await createCase.respondentSolicitorCreatCase();
        await createCase.submitCase('Private Solicitor -C110 a Application ' + CreateCaseName.getFormattedDate());


        // Orders and directions sought
        await ordersAndDirectionSought.SoliciotrC110AAppOrderAndDirectionNeeded();
        await startApplication.ordersAndDirectionsSoughtFinishedStatus.isVisible();


        // Hearing urgency
        await startApplication.hearingUrgency();
        await expect(hearingUrgency.hearingUrgencyHeading).toBeVisible();
        await hearingUrgency.hearingUrgencySmokeTest();


        // Applicant Details
        await startApplication.applicantDetails();
        await applicantDetails.solicitorC110AApplicationApplicantDetails();
        await startApplication.applicantDetailsHasBeenUpdated();

        // Child details
        await startApplication.childDetails();
        await childDetails.childDetailsNeeded();
        await startApplication.childDetailsHasBeenUpdated();

        // // Add respondents' details
        await startApplication.respondentDetails();
        await respondentDetails.respondentDetailsNeeded();

        // Allocation Proposal
        await startApplication.allocationProposal();
        await allocationProposal.allocationProposalSmokeTest();
        await startApplication.allocationProposalHasBeenUpdated();

        // Submit the case
        await startApplication.submitCase();
        await submitCase.submitCaseSmokeTest('£255.00');
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Applications');
        await caseFileView.openFolder('Original Applications');
        await expect(page.getByRole('tree')).toContainText('Private_Solicitor_-C110_a_Application');
        await  caseFileView.openDocInNewTab();
        await expect(caseFileView.docNewTab.getByText('Application from Private')).toBeVisible();

    })
});
