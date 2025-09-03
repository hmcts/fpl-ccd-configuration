import { test } from '../fixtures/create-fixture';
import { newSwanseaLocalAuthorityUserOne, judgeWalesUser, CTSCUser, HighCourtAdminUser, privateSolicitorOrgUser } from '../settings/user-credentials';
import { expect } from "@playwright/test";
import { testConfig } from '../settings/test-config';
import caseData from '../caseData/mandatorySubmissionFieldsWithoutAdditionalApp.json' assert { type: "json" };
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json' assert { type: "json" };
import { setHighCourt } from '../utils/update-case-details';
import { createCase, giveAccessToCase, updateCase } from "../utils/api-helper";
import config from "../settings/test-docs/config";
import {urlConfig} from "../settings/urls";

test.describe('Upload additional applications', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;

  test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });
  //mark test as slow to give extra timeout
  test.slow();

  test('LA uploads a C1 application',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'LA uploads an other application ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseOtherApplicationType();
      await additionalApplications.fillOtherApplicationDetails();

      // Payment details
      await expect(page.getByText('£263.00')).toBeVisible();
      await additionalApplications.payForApplication();
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('C1 - Change surname or remove from jurisdiction')).toBeVisible();
      await expect(page.getByText('On the same day')).toBeVisible();

      // If WA is enabled
      if (testConfig.waEnabled) {
        console.log('WA testing');
        await additionalApplications.clickSignOut();
        await signInPage.visit();
        await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);

        // Judge in Wales should see this Welsh case task + be able to assign it to themselves
        await additionalApplications.tabNavigation('Tasks');
        await additionalApplications.waitForTask('View Additional Applications');

        // Assign and complete the task
        await page.getByText('Assign to me').click();
        await page.getByText('Mark as done').click();
        await page.getByRole('button', { name: "Mark as done" }).click();

        // Should be no more tasks on the page
        await expect(page.getByText('View Additional Applications')).toHaveCount(0);
      }
    });

  test('LA uploads a C2 application with draft order ',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'LA uploads a C2 application with draft order ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText('£263.00')).toBeVisible();
      await additionalApplications.payForApplication();
      await additionalApplications.checkYourAnsAndSubmit();
    });

  test('LA uploads combined Other and C2 applications @xbrowser ',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'LA uploads additional application with both Other and C2 ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseBothApplicationTypes();
      await additionalApplications.fillC2ApplicationDetails();
      await additionalApplications.fillOtherApplicationDetails();

      await expect(page.getByText('£263.00')).toBeVisible();
      await additionalApplications.payForApplication();
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
      await expect(page.getByText('On the same day')).toBeVisible(); // Other application
      await expect(page.getByText('Within 2 days')).toBeVisible(); // C2 application

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();
    });

  test('LA uploads a confidential C2 application with draft order @xbrowser',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'LA uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);

      await signInPage.navigateTOCaseDetails(caseNumber);
      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseConfidentialC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText('£263.00')).toBeVisible();
      await additionalApplications.payForApplication();
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
      await expect(page.getByText('Within 2 days')).toBeVisible();

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();

      await additionalApplications.clickSignOut();
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      // CTSC can see some basic properties of the application
      await additionalApplications.tabNavigation('Other applications');
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
      await expect(page.getByText('Within 2 days')).toBeVisible();

      // CTSC can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();
    });

  test('CTSC uploads a confidential C2 application with draft order',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'CTSC uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseData);
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseConfidentialC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText('£263.00')).toBeVisible();
      await additionalApplications.payForApplication();
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA1234567')).toBeVisible();
      await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
      await expect(page.getByText('Within 2 days')).toBeVisible();

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();

      await additionalApplications.clickSignOut();
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      // LA cannot see some basic properties of the application
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('This is a confidential draft order and restricted viewing applies')).toBeVisible();

      // LA cannot see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('This is a confidential draft order and restricted viewing applies')).toBeVisible();
    });

    test('CTSC uploads standard C2 application with no PBA', async ({ page,
                                                                        signInPage,
                                                                        additionalApplications,
                                                                        uploadAdditionalApplications,
                                                                        uploadAdditionalApplicationsApplicationFee,
                                                                        uploadAdditionalApplicationsSuppliedDocuments,
                                                                        submit    }) => {
        caseName = 'CTSC standard C2 application ' + dateTime.slice(0, 10);
        await updateCase(caseName, caseNumber, caseData);

        await test.step('Login and Navigate to Case', async () => {
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
        });

        await test.step('Complete C2 Application', async () => {
            await additionalApplications.gotoNextStep('Upload additional applications');
            await uploadAdditionalApplications.checkC2Order();
            await uploadAdditionalApplications.checkApplicationWithNotice();
            await uploadAdditionalApplications.checkConfidentialApplicationYes();
            await uploadAdditionalApplications.selectApplicantValue(1);
            await uploadAdditionalApplications.clickContinue();
        });

        await test.step('Upload C2 Document', async () => {
           await Promise.all([
                page.waitForResponse(response =>
                    response.url().includes(`${urlConfig.frontEndBaseURL}/documents`) &&
                    response.request().method() === 'POST'
                ),
                uploadAdditionalApplicationsSuppliedDocuments.uploadC2Document(config.testPdfFile)
            ]);
            await expect(uploadAdditionalApplicationsSuppliedDocuments.cancelUploadButton).toBeDisabled({ timeout: 10000 });
            await uploadAdditionalApplicationsSuppliedDocuments.checkDocumentRelatedToCaseYes();
            await uploadAdditionalApplicationsSuppliedDocuments.clickContinue();
        });

        await test.step('Handle Application Fee', async () => {
            await uploadAdditionalApplicationsApplicationFee.checkPaidWithPBANo()
            await expect(uploadAdditionalApplicationsApplicationFee.paymentByPbaTextbox).toBeHidden({ timeout: 200 });

            const [response] = await Promise.all([
                page.waitForResponse(response =>
                    response.url().includes('/data/case-types/CARE_SUPERVISION_EPO/validate') &&
                    response.request().method() === 'POST'
                ),
                uploadAdditionalApplicationsApplicationFee.clickContinue()
            ]);

            expect(response.status()).toBe(200);

        });

        await test.step('Submit Application', async () => {
            await submit.clickSaveAndContinue();
        });

        await test.step("Verify C2 Application in 'Other applications' Tab", async () => {
            const [response] = await Promise.all([
                page.waitForResponse(response =>
                    response.url().includes('/api/wa-supported-jurisdiction/get') &&
                    response.request().method() === 'GET'
                ),
                await additionalApplications.tabNavigation('Other applications')
            ]);
            expect([200, 304]).toContain(response.status());
            await expect.soft(page.getByText('C2 application').first()).toBeVisible();
            await expect.soft(page.getByRole('cell', { name: 'testPdf.pdf', exact: true }).locator('div').nth(1)).toBeVisible();
        });
    });

  test('Respondent Solicitor Uploads additional applications',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'Respondent solicitor Uploads additional application ' + dateTime.slice(0, 10);
      await updateCase(caseName, caseNumber, caseWithResSolicitor);
      await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
      await signInPage.visit();
      await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseConfidentialC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText('£263.00')).toBeVisible();
      await additionalApplications.payForApplication();
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      await additionalApplications.clickSignOut();
      await signInPage.visit();
      await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
      await signInPage.navigateTOCaseDetails(caseNumber);

      // Assertion
      await additionalApplications.tabNavigation('Other applications');
      await expect(page.getByText('This is a confidential application and restricted viewing applies')).toBeVisible();
    });

  test('Failed Payment High Court WA task', async ({ page, signInPage, additionalApplications, caseFileView }) => {
    caseName = 'Failed Payment High Court WA task ' + dateTime.slice(0, 10);
    setHighCourt(caseData);
    await updateCase(caseName, caseNumber, caseData);
    await signInPage.visit();
    await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
    await signInPage.navigateTOCaseDetails(caseNumber);
    await additionalApplications.uploadBasicC2Application(false);

    // Check CFV
    await caseFileView.goToCFVTab();
    await caseFileView.openFolder('Application');
    await caseFileView.openFolder('C2 applications');
    await expect(page.getByRole('tree')).toContainText('testPdf.pdf');

    // If WA is enabled
    if (testConfig.waEnabled) {
      console.log('WA testing');
      await additionalApplications.clickSignOut();
      await signInPage.visit();
      await signInPage.login(HighCourtAdminUser.email, HighCourtAdminUser.password);

      await signInPage.navigateTOCaseDetails(caseNumber);

      // Judge in Wales should see this Welsh case task + be able to assign it to themselves
      await additionalApplications.tabNavigation('Tasks');
      await additionalApplications.waitForTask('Failed Payment (High Court)');

      // Assign and complete the task
      await page.getByText('Assign to me').click();
      await page.getByText('Mark as done').click();
      await page.getByRole('button', { name: "Mark as done" }).click();

      // Should be no more tasks on the page
      await expect(page.getByText('Failed Payment (High Court)')).toHaveCount(0);
    }
  });
})
