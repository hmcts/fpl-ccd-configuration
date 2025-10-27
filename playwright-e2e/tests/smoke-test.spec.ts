import { expect, test } from "../fixtures/fixtures";
import { newSwanseaLocalAuthorityUserOne, privateSolicitorOrgUser } from "../settings/user-credentials";
import { CreateCaseName } from "../utils/create-case-name";
import { CaseFileView } from "../pages/case-file-view";


test.describe('Smoke Test @xbrowser @test', () => {

    test('Local Authority C110A application submission @smoke-test @accessibility', async ({
                                                                    signInPage,
        page,
        createCase,
        startApplication,
        ordersAndDirectionSought,
        groundsForTheApplication,
        hearingUrgency,
        addApplicationDocuments,
        applicantDetails,
        childDetails,
        respondentDetails,
        allocationProposal,
        submitCase,
        envDataConfig,
        makeAxeBuilder
                                                                }, testInfo) => {
        test.info().annotations.push({ type: 'tag', description: 'xbrowser' });
        test.info().annotations.push({ type: 'tag', description: 'smoke-test' });
        test.info().annotations.push({ type: 'tag', description: 'accessibility' });

        await test.step('Login and create Case', async () => {
            await signInPage.visit();
            await signInPage.login(
                newSwanseaLocalAuthorityUserOne.email,
                newSwanseaLocalAuthorityUserOne.password,
            );
            await expect(signInPage.signoutButton).toBeVisible();
            await createCase.createCase();
        });

        await test.step('Submit Case', async () => {
            await createCase.submitCase()
        });

        await test.step('Fill orders and direction', async () => {
            await ordersAndDirectionSought.ordersAndDirectionsNeeded();
        });

        await test.step('Hearing urgency', async() => {

            await startApplication.hearingUrgency();
            await hearingUrgency.hearingUrgency();
        });

        await test.step('Grounds for the application', async() => {
            await startApplication.groundsForTheApplication();
            await groundsForTheApplication.checkChildBeyondParentalControl();
            await groundsForTheApplication.checkNoProvideSummaryRadioButton();
            await groundsForTheApplication.fillSummary('test');
            await Promise.all([
                page.waitForResponse(response =>
                    response.url().includes('CARE_SUPERVISION_EPO/validate?pageId=enterGrounds1') &&
                    response.request().method() === 'POST' &&
                    response.status() === 200
                ),
                groundsForTheApplication.continueButton.click()
            ]);
            await Promise.all([
                page.waitForResponse(response =>
                    response.url().includes('/api/wa-supported-jurisdiction/get') &&
                    response.request().method() === 'GET' &&
                    response.status() === 200
                ),
                groundsForTheApplication.saveAndContinueButton.click()
            ]);
        });

        await test.step('Upload documents', async() => {
            await startApplication.addApplicationDocuments();
            await addApplicationDocuments.uploadDocumentSmokeTest();
        });

        await test.step('Applicant details', async() => {
            await Promise.all([
                page.waitForResponse(response =>
                    !!response.url().match(/\/event-triggers\/enterApplicantDetailsLA/) &&
                    response.request().method() === 'GET' &&
                    response.status() === 200
                ),
                startApplication.applicantDetails()
            ]);
            await applicantDetails.applicantDetailsNeeded(envDataConfig.swanseaOrgPBA);
        });

        await test.step('Child details', async() => {
            await Promise.all([
                page.waitForResponse(response =>
                    response.url().includes('enterChildren') &&
                    response.request().method() === 'GET' &&
                    response.status() === 200
                ),
                startApplication.childDetails()
            ]);

            await childDetails.addChildDetailsForC110AApplication();
        });

        await test.step('Add respondent details', async() => {
            await Promise.all([
                page.waitForResponse(response =>
                    response.url().includes('enterRespondents') &&
                    response.request().method() === 'GET' &&
                    response.status() === 200
                ),
                startApplication.respondentDetails()
            ]);
            await respondentDetails.respondentDetailsNeeded();
        });

        await test.step('Allocation proposal', async() => {
           await Promise.all([
               page.waitForResponse(response =>
                   response.url().includes('otherProposal') &&
                   response.request().method() === 'GET' &&
                   response.status() === 200
               ),
               startApplication.allocationProposal()
           ]);
           await allocationProposal.allocationProposalSmokeTest();
        });

        await test.step('Submit case', async() => {
            await Promise.all([
                page.waitForResponse(response =>
                    response.url().includes('submitApplication') &&
                    response.request().method() === 'GET' &&
                    response.status() === 200
                ),
                startApplication.submitCase()
            ]);
            await submitCase.submitCaseSmokeTest('2,515.00');
        });

        const accessibilityScanResults = await makeAxeBuilder()
            // Automatically uses the shared AxeBuilder configuration,
            // but supports additional test-specific configuration too
            .analyze();

        await testInfo.attach('accessibility-scan-results', {
            body: JSON.stringify(accessibilityScanResults, null, 2),
            contentType: 'application/json'
        });
    });

    test.only('Private solicitor applies C110a application', async ({
        signInPage,
        createCase,
        ordersAndDirectionSought,
        startApplication,
        hearingUrgency,
        applicantDetails,
        allocationProposal,
        childDetails,
        respondentDetails,
        submitCase,
        page,
        caseFileView,
        envDataConfig,
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
        //Start new case, get case id and assert case id is created
        await createCase.caseName();
        await createCase.createCase();
        await createCase.respondentSolicitorCreatCase();
        await createCase.submitCase('Private Solicitor -C110 a Application ' + CreateCaseName.getFormattedDate());
        await startApplication.tabNavigation('View application');


        // //Orders and directions sought
        await startApplication.tabNavigation('Start application');
        await ordersAndDirectionSought.SoliciotrC110AAppOrderAndDirectionNeeded();
        await expect(startApplication.ordersAndDirectionsSoughtFinishedStatus).toBeVisible();
        await startApplication.tabNavigation('View application');


        // Hearing urgency
        await startApplication.tabNavigation('Start application');
        await startApplication.hearingUrgency();
        await expect(hearingUrgency.hearingUrgencyHeading).toBeVisible();
        await hearingUrgency.hearingUrgencySmokeTest();
        await startApplication.tabNavigation('View application');


        // Applicant Details
        await startApplication.tabNavigation('Start application');
        await startApplication.applicantDetails();
        await applicantDetails.solicitorC110AApplicationApplicantDetails(envDataConfig.privateSolicitorOrgPBA);
        await startApplication.applicantDetailsHasBeenUpdated();
        await startApplication.tabNavigation('View application');

        // Child details
        await startApplication.tabNavigation('Start application');
        await startApplication.childDetails();
        await childDetails.addChildDetailsForC110AApplication();
        await startApplication.tabNavigation('Start application');
        await startApplication.childDetailsHasBeenUpdated();
        await startApplication.tabNavigation('View application');

        // // Add respondents' details
        await startApplication.tabNavigation('Start application');
        await startApplication.respondentDetails();
        await respondentDetails.respondentDetailsPrivateSolicitor();
        await startApplication.tabNavigation('View application');

        // Allocation Proposal
        await startApplication.tabNavigation('Start application');
        await startApplication.allocationProposal();
        await allocationProposal.allocationProposalSmokeTest();
        await startApplication.allocationProposalHasBeenUpdated();
        await startApplication.tabNavigation('View application');


       // Submit the case
        await startApplication.tabNavigation('Start application');
        await startApplication.submitCase();
        await submitCase.submitCaseSmokeTest('£263.00');
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Applications');
        await caseFileView.openFolder('Original Applications');
        await expect(page.getByRole('button', { name: 'Document icon' })).toBeVisible();
        await caseFileView.openDocInNewTab();
        await expect(caseFileView.docNewTab.getByText('Application from Private')).toBeVisible();
    })
})
