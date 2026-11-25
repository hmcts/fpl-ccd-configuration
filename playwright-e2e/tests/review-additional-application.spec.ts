import {expect, test} from "../fixtures/fixtures";
import {
    CTSCUser, judgeWalesUser, newSwanseaLocalAuthorityUserOne, privateSolicitorOrgUser
} from "../settings/user-credentials";
import {createCase, updateCase} from "../utils/api-helper";
import caseWithC2App from "../caseData/caseWithC2Application.json" assert {type: "json"}
import caseWithPaperC2App from "../caseData/caseWithPaperC2Application.json" assert {type: "json"}
import {testConfig} from "../settings/test-config";
import caseWithChildrenCafcassSolicitor from "../caseData/caseWithMultipleChildCafcassSolicitor.json";
import config from "../settings/test-docs/config";


test.describe('Review Additional Application', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('Court service', privateSolicitorOrgUser);
        expect(caseNumber).toBeDefined();
        expect(await updateCase("caseName", caseNumber, caseWithC2App)).toBeTruthy();
    });
    test('Judge approve additional application and draft order @xbrowser', async ({
                                                                                           signInPage,
                                                                                           reviewAdditionalApplication,
                                                                                           caseFileView,
                                                                                       }, testInfo) => {
        let caseName = 'Judge approve additional application and draft order ' + dateTime;
        expect(await updateCase(caseName, caseNumber, caseWithC2App)).toBeTruthy();

        await test.step('Login and navigate to case', async () => {
            await signInPage.visit();
            await signInPage.page.pause();
            await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await signInPage.gotoNextStep('Review additional application');
            await signInPage.page.pause()


        });
        await test.step('Judge review the application details', async () => {
            //soft assert the application details are displayed correctly
            await reviewAdditionalApplication.clickContinue();
        });
        await test.step('Judge approve the application and draft order', async () => {
            await reviewAdditionalApplication.makeDecision('Approve the application and the order');
            await reviewAdditionalApplication.clickContinue();
            await reviewAdditionalApplication.openDraftOrder('C2-draft order-01.docx');
            expect(await reviewAdditionalApplication.draftOrderPage.getByText('Order made by District Judge (MC) Taylor in private')).toBeVisible();
            expect(await reviewAdditionalApplication.draftOrderPage.getByText('Order made by District Judge (MC) Taylor in private')).toBeVisible();
            await reviewAdditionalApplication.clickContinue();
            await expect(reviewAdditionalApplication.page.locator('#reviewOrderUrgencyLabel')).toContainText('EPOs, ICOs, recovery orders or port alertsinterpreter booking for hearings within 3 working daysproduction order for hearings within 10 working daysorder against government departments (such as DWP, HMRC, Home Office)any action that needs to be taken within 24 hours');
            await reviewAdditionalApplication.setOrderUrgent(true);
            await reviewAdditionalApplication.clickContinue();
            await reviewAdditionalApplication.checkYourAnsAndSubmit();

        });

        await test.step('Assert the event success ', async () => {
            await reviewAdditionalApplication.page.pause();
            expect(await reviewAdditionalApplication.page.getByText('Response submitted').isVisible());
            await expect(reviewAdditionalApplication.page.locator('#confirmation-body')).toContainText('What happens next The task will be closed and marked as complete. The applicant and the other parties will be notified. A task for the courts and tribunals service centre (CTSC) to review the order will be created. If you need to send any further directions to the CTSC or the local court, for example, to request a listing, you can do this using the \'Send message\' event.');
            await reviewAdditionalApplication.closeConfirmation();
            await reviewAdditionalApplication.tabNavigation('Other applications');
            expect(await reviewAdditionalApplication.getRowData('Application Reviewed by Judge', reviewAdditionalApplication.page)).toContain('Yes');
            await reviewAdditionalApplication.tabNavigation('Orders');
            expect(await reviewAdditionalApplication.getRowData('Order document', reviewAdditionalApplication.page)).toContain('C2-draft order-01.pdf');
            await reviewAdditionalApplication.tabNavigation('Case File View');
            await caseFileView.openFolder('Orders');
            await expect(reviewAdditionalApplication.page.getByText('C2-draft order-01.pdf')).toBeVisible();
        });


        if (testConfig.waEnabled) {
            await test.step('Assert the WA task', async () => {
                await signInPage.logout();
                await signInPage.login(CTSCUser.email, CTSCUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
                await reviewAdditionalApplication.tabNavigation('Tasks');
                await reviewAdditionalApplication.waitForTask('Review Order');
            });

        }
    });
    test('Judge approve additional application but make changes to draft order @xbrowser', async ({
                                                                                                      reviewAdditionalApplication,
                                                                                                      signInPage,caseFileView
                                                                                                  }, testInfo) => {
        let caseName = 'Judge approve additional application but make changes to draft order ' + dateTime;
        expect(await updateCase(caseName, caseNumber, caseWithPaperC2App)).toBeTruthy();
        console.log(`Case created with case number: ${caseNumber} and case name: ${caseName}`);
        await test.step('Login and navigate to case', async () => {
            await signInPage.visit();
            await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await signInPage.gotoNextStep('Review additional application');
        });
        await test.step('Judge review the application details', async () => {
            // await additionalApplicationPage.checkApplicationDetails();
            await reviewAdditionalApplication.clickContinue();


        });
        await test.step('Judge approve the application but make changes to draft order', async () => {
            await reviewAdditionalApplication.makeDecision('Approve the application but make changes to the order');
            await reviewAdditionalApplication.uploadAmendedOrder(config.testPdfFile);
            await reviewAdditionalApplication.clickContinue();
            await reviewAdditionalApplication.clickContinue();
            await reviewAdditionalApplication.setOrderUrgent(false);
            await reviewAdditionalApplication.clickContinue();
            await reviewAdditionalApplication.checkYourAnsAndSubmit();
            await reviewAdditionalApplication.page.pause();
        });

        await test.step('Assert the event success ', async () => {

            await expect(reviewAdditionalApplication.page.getByRole('heading', { name: 'Response submitted' })).toBeVisible();
            await expect(reviewAdditionalApplication.page.getByRole('heading', { name: 'What happens next' })).toBeVisible();
            await expect(reviewAdditionalApplication.page.getByText('The task will be closed and')).toBeVisible();
            await expect(reviewAdditionalApplication.page.getByText('The applicant and the other parties will be notified.')).toBeVisible();
            await expect(reviewAdditionalApplication.page.getByText('A task for the courts and')).toBeVisible();
            await expect(reviewAdditionalApplication.page.getByText('If you need to send any further directions to the CTSC or the local court, for example, to request a listing, you can do this using the \'Send message\' event.')).toBeVisible();


            await reviewAdditionalApplication.closeConfirmation();
            await reviewAdditionalApplication.tabNavigation('Other applications');
            expect(reviewAdditionalApplication.getRowData('Application Reviewed by Judge', reviewAdditionalApplication.page)).toContain('Yes');
            await reviewAdditionalApplication.tabNavigation('Orders');
            expect(reviewAdditionalApplication.getRowData('Order document', reviewAdditionalApplication.page)).toContain('amended_Draft order 2.pdf');
            await reviewAdditionalApplication.tabNavigation('Case File View');
            await caseFileView.openFolder('Orders');
            await expect(reviewAdditionalApplication.page.getByText('amended_Draft order 2.pdf')).toBeVisible();

        });
        if (testConfig.waEnabled) {
            await test.step('Assert the WA task', async () => {
                await signInPage.logout();
                await signInPage.login(CTSCUser.email, CTSCUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
                await reviewAdditionalApplication.tabNavigation('Tasks');
                await reviewAdditionalApplication.waitForTask('Review Order');
                // assert for the urgency of the task based on the order urgency
            });

        }
    });

    test('Judge approve additional application but request applicant to amend draft order @xbrowser', async ({
                                                                                                                 reviewAdditionalApplication,
                                                                                                                 signInPage,
                                                                                                                 courtServices,
                                                                                                                 makeAxeBuilder
                                                                                                             }, testInfo) => {
        let caseName = 'Judge approve additional application but request applicant to amend draft order ' + dateTime;
        expect(await updateCase(caseName, caseNumber, caseWithC2App)).toBeTruthy();
        console.log(`Case created with case number: ${caseNumber} and case name: ${caseName}`);
        await test.step('Login and navigate to case', async () => {
            await signInPage.visit();
            await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await signInPage.gotoNextStep('Review additional application');
        });
        await test.step('Judge review the application details', async () => {
            // await additionalApplicationPage.checkApplicationDetails();

        });
        await test.step('Judge approve the application but request applicant to amend draft order', async () => {
            await reviewAdditionalApplication.makeDecision('Approve the application but the applicant needs to make changes to the order');
            await reviewAdditionalApplication.enterChangesRequested('The order needs to be amended');
            await reviewAdditionalApplication.clickContinue();
            await reviewAdditionalApplication.checkYourAnsAndSubmit();

        });

        test.step('Assert the event success ', async () => {
            expect(await reviewAdditionalApplication.page.getByText('Response submitted').isVisible());
            await reviewAdditionalApplication.closeConfirmation();
            await reviewAdditionalApplication.tabNavigation('Additional application');
            await expect(reviewAdditionalApplication.getRowData('Is Reviewed by Judge', reviewAdditionalApplication.page)).toContain('Yes');
            await reviewAdditionalApplication.tabNavigation('Refused orders');
            await expect(reviewAdditionalApplication.getRowData('Refusal reason', reviewAdditionalApplication.page)).toContain('The order needs to be amended');
            await reviewAdditionalApplication.tabNavigation('Orders');
            await expect(reviewAdditionalApplication.getRowData('Order title', reviewAdditionalApplication.page)).toContain('Amended order');
// CFV assertion
            await reviewAdditionalApplication.tabNavigation('Case file view');
        });
        if (testConfig.waEnabled) {
            test.step('Assert the WA task', async () => {
                await signInPage.logout();
                await signInPage.login(CTSCUser.email, CTSCUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
                await reviewAdditionalApplication.tabNavigation('Tasks');
                await reviewAdditionalApplication.waitForTask('Review Order');
                // assert for the urgency of the task based on the order urgency
            });

        }
    });

    test('Judge reject additional application and draft order @xbrowser', async ({
                                                                                     reviewAdditionalApplication,
                                                                                     signInPage,
                                                                                     courtServices,
                                                                                     makeAxeBuilder
                                                                                 }, testInfo) => {
        let caseName = 'Judge reject additional application and draft order ' + dateTime;
        expect(await updateCase(caseName, caseNumber, caseWithC2App)).toBeTruthy();
        console.log(`Case created with case number: ${caseNumber} and case name: ${caseName}`);
        await test.step('Login and navigate to case', async () => {
            await signInPage.visit();
            await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await signInPage.gotoNextStep('Review additional application');
        });
        await test.step('Judge review the application details', async () => {
            // await additionalApplicationPage.checkApplicationDetails();

        });
        await test.step('Judge refuse the application and the draft order', async () => {
            await reviewAdditionalApplication.makeDecision('Refuse the application');
            await reviewAdditionalApplication.enterRefusalReason('The application is not valid and the order is not valid');
            await reviewAdditionalApplication.clickContinue();
            await reviewAdditionalApplication.checkYourAnsAndSubmit();

        });

        test.step('Assert the event success ', async () => {
            expect(await reviewAdditionalApplication.page.getByText('Response submitted').isVisible());
            await reviewAdditionalApplication.closeConfirmation();
            await reviewAdditionalApplication.tabNavigation('Additional application');
            await expect(reviewAdditionalApplication.getRowData('Is Reviewed by Judge', reviewAdditionalApplication.page)).toContain('Yes');
            await reviewAdditionalApplication.tabNavigation('Refused orders');
            //assert for the refusal order - blank order title and refusal reason
            await expect(reviewAdditionalApplication.getRowData('Refusal reason', reviewAdditionalApplication.page)).toContain('The order needs to be amended');
            await reviewAdditionalApplication.tabNavigation('Orders');
            await expect(reviewAdditionalApplication.getRowData('Order title', reviewAdditionalApplication.page)).toContain('Amended order');
// CFV assertion
            await reviewAdditionalApplication.tabNavigation('Case file view');
        });
        if (testConfig.waEnabled) {
            test.step('Assert the WA task', async () => {
                await signInPage.logout();
                await signInPage.login(CTSCUser.email, CTSCUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
                await reviewAdditionalApplication.tabNavigation('Tasks');
                await reviewAdditionalApplication.waitForTask('Review Order');
                // assert for the urgency of the task based on the order urgency
            });

        }
    });
    test('Judge list at next hearing @xbrowser', async ({
                                                            reviewAdditionalApplication,
                                                            signInPage,
                                                            courtServices,
                                                            makeAxeBuilder
                                                        }, testInfo) => {
        let caseName = 'Judge list the additional application at next hearing ' + dateTime;
        expect(await updateCase(caseName, caseNumber, caseWithC2App)).toBeTruthy();
        console.log(`Case created with case number: ${caseNumber} and case name: ${caseName}`);
        await test.step('Login and navigate to case', async () => {
            await signInPage.visit();
            await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await signInPage.gotoNextStep('Review additional application');
        });
        await test.step('Judge review the application details', async () => {
            // await additionalApplicationPage.checkApplicationDetails();

        });
        await test.step('Judge refuse the application and the draft order', async () => {
            await reviewAdditionalApplication.makeDecision('Refuse the application');
            await reviewAdditionalApplication.enterRefusalReason('The application is not valid and the order is not valid');
            await reviewAdditionalApplication.clickContinue();
            await reviewAdditionalApplication.checkYourAnsAndSubmit();

        });

        test.step('Assert the event success ', async () => {
            expect(await reviewAdditionalApplication.page.getByText('Response submitted').isVisible());
            await reviewAdditionalApplication.closeConfirmation();
            await reviewAdditionalApplication.tabNavigation('Additional application');
            await expect(reviewAdditionalApplication.getRowData('Is Reviewed by Judge', reviewAdditionalApplication.page)).toContain('Yes');
            await reviewAdditionalApplication.tabNavigation('Refused orders');
            //assert for the refusal order - blank order title and refusal reason
            await expect(reviewAdditionalApplication.getRowData('Refusal reason', reviewAdditionalApplication.page)).toContain('The order needs to be amended');
            await reviewAdditionalApplication.tabNavigation('Orders');
            await expect(reviewAdditionalApplication.getRowData('Order title', reviewAdditionalApplication.page)).toContain('Amended order');
// CFV assertion
            await reviewAdditionalApplication.tabNavigation('Case file view');
        });
        if (testConfig.waEnabled) {
            test.step('Assert the WA task', async () => {
                await signInPage.logout();
                await signInPage.login(CTSCUser.email, CTSCUser.password);
                await signInPage.navigateToCaseDetails(caseNumber);
                await reviewAdditionalApplication.tabNavigation('Tasks');
                await reviewAdditionalApplication.waitForTask('Review Order');
                // assert for the urgency of the task based on the order urgency
            });

        }
    });

});
