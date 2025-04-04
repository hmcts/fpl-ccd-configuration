import {expect, test} from "../fixtures/fixtures";
import {newSwanseaLocalAuthorityUserOne, privateSolicitorOrgUser} from "../settings/user-credentials";
import {CreateCaseName} from "../utils/create-case-name";
import {CaseFileView} from "../pages/case-file-view";

test.describe('', () => {
    test.slow();
    test("Local Authority submit C110A application @smoke-test @accessibility", async ({
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
        await startApplication.caseNameUpdated();



        // Orders and directions sought
        await startApplication.tabNavigation('Start application');
        await ordersAndDirectionSought.ordersAndDirectionsNeeded();
        await startApplication.addApplicationDetailsHeading.isVisible();
        await startApplication.orderAndDirectionUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByRole('link',{name:'Make changes to orders and directions sought',exact:true})).toBeVisible();


        // Hearing urgency
        await startApplication.tabNavigation('Start application');
        await startApplication.hearingUrgency();
        await expect(hearingUrgency.hearingUrgencyHeading).toBeVisible();
        await hearingUrgency.hearingUrgencySmokeTest();
        await startApplication.hearingurgencyHasBeenUpdate();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByRole('link',{name:'Make changes to hearing urgency',exact:true})).toBeVisible();


        // Grounds for the application
        await startApplication.tabNavigation('Start application');
        await startApplication.groundsForTheApplication();
        await groundsForTheApplication.groundsForTheApplicationSmokeTest();
        await startApplication.groundsForTheApplicationHasBeenUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByText('How does this case meet the threshold criteria?',{exact:true})).toBeVisible();

        //Add application documents
        await startApplication.tabNavigation('Start application');
        await startApplication.addApplicationDocuments();
        await addApplicationDocuments.uploadDocumentSmokeTest();
        await startApplication.addApplicationDocumentsInProgress();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByText('Documents 1',{exact:true})).toBeVisible();

// Applicant Details
        await startApplication.tabNavigation('Start application');
        await startApplication.applicantDetails();
        await applicantDetails.applicantDetailsNeeded();
        await startApplication.applicantDetailsHasBeenUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByText('Applicant 1',{exact:true})).toBeVisible();

        // Child details
        await startApplication.tabNavigation('Start application');
        await startApplication.childDetails();
        await childDetails.childDetailsNeeded();
        await startApplication.childDetailsHasBeenUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByText('Child 1',{exact:true})).toBeVisible();

        // // Add respondents' details
        await startApplication.tabNavigation('Start application');
        await startApplication.respondentDetails();
        await respondentDetails.respondentDetailsNeeded();
        await startApplication.respondentDetailsHasBeenUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByText('Respondents 1',{exact:true})).toBeVisible();

        // Allocation Proposal
        await startApplication.tabNavigation('Start application');
        await startApplication.allocationProposal();
        await allocationProposal.allocationProposalSmokeTest();
        await startApplication.allocationProposalHasBeenUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByRole('link',{name:'Make changes to allocation proposal',exact:true})).toBeVisible();



        // Submit the case
        await startApplication.tabNavigation('Start application');
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
        await startApplication.caseNameUpdated();

        // Orders and directions sought
        await startApplication.tabNavigation('Start application');
        await ordersAndDirectionSought.SoliciotrC110AAppOrderAndDirectionNeeded();
        await startApplication.orderAndDirectionUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByRole('link',{name:'Make changes to orders and directions sought',exact:true})).toBeVisible();



        // Hearing urgency
        await startApplication.tabNavigation('Start application');
        await startApplication.hearingUrgency();
        await expect(hearingUrgency.hearingUrgencyHeading).toBeVisible();
        await hearingUrgency.hearingUrgencySmokeTest();
        await startApplication.hearingurgencyHasBeenUpdate();
        await startApplication.tabNavigation('View application');

        // Applicant Details
        await startApplication.tabNavigation('Start application');
        await startApplication.applicantDetails();
        await applicantDetails.solicitorC110AApplicationApplicantDetails();
        await startApplication.applicantDetailsHasBeenUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByText('Applicant 1',{exact:true})).toBeVisible();


        // Child details
        await startApplication.tabNavigation('Start application');
        await startApplication.childDetails();
        await childDetails.childDetailsNeeded();
        await startApplication.childDetailsHasBeenUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByText('Child 1',{exact:true})).toBeVisible();


        // // Add respondents' details
        await startApplication.tabNavigation('Start application');
        await startApplication.respondentDetails();
        await respondentDetails.respondentDetailsNeeded();
        await startApplication.respondentDetailsHasBeenUpdated();
        await startApplication.tabNavigation('View application');
        await expect(startApplication.page.getByText('Respondents 1',{exact:true})).toBeVisible();



        // Allocation Proposal
        await startApplication.tabNavigation('Start application');
        await startApplication.allocationProposal();
        await allocationProposal.allocationProposalSmokeTest();
        await startApplication.allocationProposalHasBeenUpdated();
        await startApplication.tabNavigation('View application');

        // Submit the case
        await startApplication.tabNavigation('Start application');
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
