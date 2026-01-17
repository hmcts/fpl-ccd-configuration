import { expect, test } from "../fixtures/fixtures";
import { newSwanseaLocalAuthorityUserOne, privateSolicitorOrgUser } from "../settings/user-credentials";
import { CreateCaseName } from "../utils/create-case-name";
import { CaseFileView } from "../pages/case-file-view";


test.describe('Smoke Test @xbrowser @smoke-test', () => {
test.setTimeout(7 * 60 * 1000);
    test('Local Authority C110A application submission  @accessibility ', async ({
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

           await startApplication.applicantDetails()

            await applicantDetails.applicantDetailsNeeded(envDataConfig.swanseaOrgPBA);
        });

        await test.step('Child details', async() => {

            await   startApplication.childDetails()
            await childDetails.addChildDetailsForC110AApplication();
        });

        await test.step('Add respondent details', async() => {
            await startApplication.respondentDetails()
            await respondentDetails.respondentDetailsNeeded();
        });

        await test.step('Allocation proposal', async() => {
            await startApplication.allocationProposal()
           await allocationProposal.allocationProposalSmokeTest();
        });

        await test.step('Submit case', async() => {

           await  startApplication.submitCase()
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

    test('Private solicitor applies C110a application', async ({
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
    }) => {

        await test.step('Login as private solicitor', async () => {
            await signInPage.visit();

            await Promise.all([
                page.waitForResponse(response => {
                    const url = response.url();
                    return (
                        url.includes('/auth/isAuthenticated') &&
                        response.request().method() === 'GET' &&
                        response.status() === 200
                    );
                }),
                signInPage.login(
                    privateSolicitorOrgUser.email,
                    privateSolicitorOrgUser.password
                )
            ]);
            await signInPage.isSignedIn();
        });

        await test.step('Create solicitor Case', async () => {
            await createCase.caseName()
            await createCase.createCase();
            await createCase.respondentSolicitorCreatCase()
        });

        await test.step('Submit solicitor Case', async () => {
            await createCase.submitCase('Private Solicitor -C110 a Application ' + CreateCaseName.getFormattedDate());
        });

        await test.step('Order and Direction Sorted', async () => {
            await ordersAndDirectionSought.SoliciotrC110AAppOrderAndDirectionNeeded();
            await expect(startApplication.ordersAndDirectionsSoughtFinishedStatus).toBeVisible();
        });
            await test.step('Hearing Urgency', async () => {
                await startApplication.hearingUrgency();
                await expect(hearingUrgency.hearingUrgencyHeading).toBeVisible();
                await hearingUrgency.hearingUrgencySmokeTest();
            });

            await test.step('Applicant Details', async() => {
                await startApplication.applicantDetails();
                await applicantDetails.solicitorC110AApplicationApplicantDetails(envDataConfig.privateSolicitorOrgPBA);
                await startApplication.applicantDetailsHasBeenUpdated();
            });


            await test.step('Child Details', async () => {
                 await   startApplication.childDetails()
                await childDetails.addChildDetailsForC110AApplication();
                await startApplication.childDetailsHasBeenUpdated();
            });

            await test.step('Add Respondent Details', async () => {
                await    startApplication.respondentDetails()
                await respondentDetails.respondentDetailsPrivateSolicitor();
            });


            await test.step('Allocation Proposal', async () => {
                await startApplication.allocationProposal()
                await allocationProposal.allocationProposalSmokeTest();
                await startApplication.allocationProposalHasBeenUpdated();
            });

            await test.step('Submit the Case', async () => {
                await   startApplication.submitCase()
                await submitCase.submitCaseSmokeTest('£263.00');
            });


        await test.step('CFV Application check', async () => {
            await caseFileView.goToCFVTab();
            await caseFileView.openFolder('Applications');
            await caseFileView.openFolder('Original Applications');
            await expect(caseFileView.page.getByLabel('Original Applications folder')).toBeVisible();
            await expect(caseFileView.page.getByRole('treeitem', { name: 'Somuy__Swansea_City_Council_Asa_Yaks.pdf', exact: true })).toBeVisible();

            await caseFileView.openDocInNewTab();
            await expect(caseFileView.docNewTab.getByText('Application from Private')).toBeVisible();
        });

    })
})
