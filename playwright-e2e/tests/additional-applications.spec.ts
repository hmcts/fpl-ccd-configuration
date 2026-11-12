import { test } from '../fixtures/create-fixture';
import {
    newSwanseaLocalAuthorityUserOne, judgeWalesUser, CTSCUser, HighCourtAdminUser, privateSolicitorOrgUser,
    CTSCTeamLeadUser, FPLSolicitorOrgUser
} from '../settings/user-credentials';
import { expect } from "@playwright/test";
import { testConfig } from '../settings/test-config';
import caseData from '../caseData/mandatorySubmissionFieldsWithoutAdditionalApp.json' assert { type: "json" };
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json' assert { type: "json" };
import caseDataWithHearing from '../caseData/caseWithHearingDetailsNoAdditionalApp.json' assert {type: "json"};
import caseDataWithChildSolicitor from '../caseData/caseWithMultipleChildCafcassSolicitor.json' assert {type: "json"};
import caseDataWithChildSolicitorDemo from '../caseData/caseWithMultipleChildCafcassSolicitorDemo.json' assert {type: "json"};
import { setHighCourt } from '../utils/update-case-details';
import {assignAMJudicialRole, createCase, giveAccessToCase, updateCase} from "../utils/api-helper";
import config from "../settings/test-docs/config";
import {urlConfig} from "../settings/urls";

test.describe('Upload additional applications', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;

  test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    expect(caseNumber).toBeDefined();
  });
  //mark test as slow to give extra timeout
  test.slow();

  test('LA uploads a C1 application @test',
    async ({ page, signInPage, additionalApplications,envDataConfig }) => {
      caseName = 'LA uploads an other application ' + dateTime.slice(0, 10);
        expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();

      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateToCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseOtherApplicationType();
      await additionalApplications.fillOtherApplicationDetails();

      // Payment details
      await expect(page.getByText(testConfig.C1)).toBeVisible();
      await additionalApplications.payForApplication(envDataConfig.swanseaOrgPBA);
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText(envDataConfig.swanseaOrgPBA)).toBeVisible();
      await expect(page.getByText('C1 - Change surname or remove from jurisdiction')).toBeVisible();
      await expect(page.getByText('On the same day')).toBeVisible();

      // If WA is enabled
      if (testConfig.waEnabled) {
        console.log('WA testing');
        await additionalApplications.clickSignOut();
        await signInPage.visit();
        await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
        await signInPage.navigateToCaseDetails(caseNumber);

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

    test.skip('LA uploads a C2 application with draft order ',
        async ({page, signInPage, additionalApplications, envDataConfig}) => {
            caseName = 'LA uploads a C2 application with draft order ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateToCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText(testConfig.C2_WITH_SUPPLEMENT)).toBeVisible();
      await additionalApplications.payForApplication(envDataConfig.swanseaOrgPBA);
      await additionalApplications.checkYourAnsAndSubmit();
    });

  test.skip('LA uploads combined Other and C2 applications @xbrowser',
    async ({ page, signInPage, additionalApplications,envDataConfig }) => {
      caseName = 'LA uploads additional application with both Other and C2 ' + dateTime.slice(0, 10);
      expect(   await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateToCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseBothApplicationTypes();
      await additionalApplications.fillC2ApplicationDetails();
      await additionalApplications.fillOtherApplicationDetails();

      await expect(page.getByText(testConfig.COMBINE_C1_C2)).toBeVisible();
      await additionalApplications.payForApplication(envDataConfig.swanseaOrgPBA);
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText(envDataConfig.swanseaOrgPBA)).toBeVisible();
      await expect(page.getByText('Change surname or remove from jurisdiction.')).toBeVisible();
      await expect(page.getByText('On the same day')).toBeVisible(); // Other application
      await expect(page.getByText('Within 2 days')).toBeVisible(); // C2 application

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();
    });

  test.skip('LA uploads a confidential C2 application with draft order @xbrowser',
    async ({ page, signInPage, additionalApplications ,envDataConfig}) => {
      caseName = 'LA uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
      expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);

      await signInPage.navigateToCaseDetails(caseNumber);
      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseConfidentialC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText(testConfig.C2_WITH_SUPPLEMENT)).toBeVisible();
      await additionalApplications.payForApplication(envDataConfig.swanseaOrgPBA);
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText(envDataConfig.swanseaOrgPBA)).toBeVisible(); //PBA0076191
      await expect(page.getByText('Within 2 days')).toBeVisible();

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();

      await additionalApplications.clickSignOut();
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password);
      await signInPage.navigateToCaseDetails(caseNumber);

      // CTSC can see some basic properties of the application
      await additionalApplications.tabNavigation('Other applications');
      await expect(page.getByText(envDataConfig.swanseaOrgPBA)).toBeVisible();
      await expect(page.getByText('Within 2 days')).toBeVisible();

      // CTSC can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();
    });

  test.skip('CTSC uploads a confidential C2 application with draft order @test',
    async ({ page, signInPage, additionalApplications }) => {
      caseName = 'CTSC uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
      expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
      await signInPage.visit();
      await signInPage.login(CTSCUser.email, CTSCUser.password);
      await signInPage.navigateToCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseConfidentialC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText(testConfig.C2_WITH_SUPPLEMENT)).toBeVisible();
      await additionalApplications.ctscPayForApplication();
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      // can see some basic properties of the application
      await expect(page.getByText('PBA0096471')).toBeVisible();
      await expect(page.getByText('Within 2 days')).toBeVisible();

      // can see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('Draft order title')).toBeVisible();

      await additionalApplications.clickSignOut();
      await signInPage.visit();
      await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
      await signInPage.navigateToCaseDetails(caseNumber);

      // LA cannot see some basic properties of the application
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('This is a confidential draft order and restricted viewing applies')).toBeVisible();

      // LA cannot see the draft order to be approved
      await additionalApplications.tabNavigation('Draft orders');
      await expect(page.getByText('This is a confidential draft order and restricted viewing applies')).toBeVisible();
    });

    test.skip('CTSC uploads standard C2 application with no PBA', async ({ page,
                                                                        signInPage,
                                                                        additionalApplications,
                                                                        uploadAdditionalApplications,
                                                                        uploadAdditionalApplicationsApplicationFee,
                                                                        uploadAdditionalApplicationsSuppliedDocuments,
                                                                        submit    }) => {
        caseName = 'CTSC standard C2 application ' + dateTime.slice(0, 10);
        expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();

        await test.step('Login and Navigate to Case', async () => {
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
        });

        await test.step('Complete C2 Application', async () => {
            await additionalApplications.gotoNextStep('Upload additional applications');
            await additionalApplications.chooseC2ApplicationType();
            await uploadAdditionalApplications.checkC2Order();
            await uploadAdditionalApplications.checkConfidentialApplicationYes();
            await uploadAdditionalApplications.selectApplicantValue(1);
            await uploadAdditionalApplications.clickContinue();
            await additionalApplications.fillC2ApplicationDetails();
        });

        await test.step('Upload C2 Document', async () => {
            uploadAdditionalApplicationsSuppliedDocuments.uploadC2Document(config.testPdfFile);
            await expect(uploadAdditionalApplicationsSuppliedDocuments.cancelUploadButton).toBeDisabled({timeout: 10000});
            await uploadAdditionalApplicationsSuppliedDocuments.page.waitForTimeout(6000);//wait for upload to complete restriction by EXUI for users
            await uploadAdditionalApplicationsSuppliedDocuments.checkDocumentRelatedToCaseYes();
            await uploadAdditionalApplicationsSuppliedDocuments.clickContinue();
        });


        await test.step('Handle Application Fee', async () => {
            await uploadAdditionalApplicationsApplicationFee.checkPaidWithPBANo()
            await expect(uploadAdditionalApplicationsApplicationFee.paymentByPbaTextbox).toBeHidden({timeout: 200});

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
                additionalApplications.tabNavigation('Other applications')
            ]);
            expect([200, 304]).toContain(response.status());
            await expect.soft(page.getByText('C2 application').first()).toBeVisible();
            await expect.soft(page.getByRole('cell', {
                name: 'testPdf.pdf',
                exact: true
            }).locator('div').nth(1)).toBeVisible();
        });
    });

  test.skip('Respondent Solicitor Uploads additional applications',
    async ({ page, signInPage, additionalApplications,envDataConfig }) => {
      caseName = 'Respondent solicitor Uploads additional application ' + dateTime.slice(0, 10);
      expect(await updateCase(caseName, caseNumber, caseWithResSolicitor)).toBeTruthy();
      expect(await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]')).toBeTruthy();
      await signInPage.visit();
      await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
      await signInPage.navigateToCaseDetails(caseNumber);

      await additionalApplications.gotoNextStep('Upload additional applications');
      await additionalApplications.chooseConfidentialC2ApplicationType();
      await additionalApplications.fillC2ApplicationDetails();

      // Payment details
      await expect(page.getByText(testConfig.C2_WITH_SUPPLEMENT)).toBeVisible();
      await additionalApplications.payForApplication(envDataConfig.privateSolicitorOrgPBA);
      await additionalApplications.checkYourAnsAndSubmit();
      await additionalApplications.tabNavigation('Other applications');

      await additionalApplications.clickSignOut();
      await signInPage.visit();
      await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
      await signInPage.navigateToCaseDetails(caseNumber);

      // Assertion
      await additionalApplications.tabNavigation('Other applications');
      await expect(page.getByText('This is a confidential application and restricted viewing applies')).toBeVisible();
    });

    test.skip('Failed Payment High Court WA task', async ({
                                                         page,
                                                         signInPage,
                                                         additionalApplications,
                                                         caseFileView,
                                                         envDataConfig
                                                     }) => {
        caseName = 'Failed Payment High Court WA task ' + dateTime.slice(0, 10);
        setHighCourt(caseData);
        expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateToCaseDetails(caseNumber);
        await signInPage.gotoNextStep('Upload additional applications');
        await additionalApplications.uploadBasicC2Application(false, envDataConfig.swanseaOrgPBA);

        // Check CFV
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Applications');
        await caseFileView.openFolder('C2 applications');
        await expect(page.getByRole('tree')).toContainText('testPdf.pdf');

        // If WA is enabled
        if (testConfig.waEnabled) {
            console.log('WA testing');
            await additionalApplications.clickSignOut();
            await signInPage.visit();
            await signInPage.login(HighCourtAdminUser.email, HighCourtAdminUser.password);

            await signInPage.navigateToCaseDetails(caseNumber);

            // Judge in Wales should see this Welsh case task + be able to assign it to themselves
            await additionalApplications.tabNavigation('Tasks');
            await additionalApplications.waitForTask('Failed Payment (High Court)');

            // Assign and complete the task
            await page.getByText('Assign to me').click();
            await page.getByText('Mark as done').click();
            await page.getByRole('button', {name: "Mark as done"}).click();

            // Should be no more tasks on the page
            await expect(page.getByText('Failed Payment (High Court)')).toHaveCount(0);
        }
    });
    test('CTSC user submit  C2 paper application', async ({
                                                                               page,
                                                                               signInPage,
                                                                               additionalApplications,
                                                                               caseFileView,
                                                                               envDataConfig
                                                                           }) => {
        caseName = 'CTSC submit C2 paper application ' + dateTime.slice(0, 10);
        expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateToCaseDetails(caseNumber);
        await additionalApplications.gotoNextStep('Upload additional applications');
        await additionalApplications.selectApplicationType('C2 Application');
        await additionalApplications.selectC2FormType('Upload a paper form');
        await additionalApplications.giveC2AppConsent('Yes');
        await additionalApplications.isC2AppConfidential('No');
        await additionalApplications.selectWhoMakeApplication('Someone else');
        await additionalApplications.clickContinue();
        await additionalApplications.uploadC2ApplicationForm();
        await additionalApplications.isC2ApplicationHasSafeguardRisk('Yes', 'Urgent');
        await additionalApplications.IsC2ToAdjournHearing('No');
        await additionalApplications.canC2AppWaitUntilNextHearing('Yes');

        await additionalApplications.uploadSupplementDocument('0', 'C18 - Recovery order', 'test notes');
        await additionalApplications.uploadC2DraftOrder('0', 'Draft order one');
        await additionalApplications.uploadSupportingDocument('0', 'birth certificate', 'birth certificate of child one');


        await additionalApplications.clickContinue();

        await additionalApplications.ctscPayForApplication();
        await additionalApplications.clickContinue();
        await additionalApplications.checkYourAnsAndSubmit();

        await expect.soft(page.getByText('updated with event: Upload additional applications')).toBeVisible();
        //assertion
        await additionalApplications.tabNavigation('Other applications');
        expect(await additionalApplications.getRowData('Application with consent?',page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Safeguarding risk?',page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Consider at next hearing?',page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Payment by account (PBA) number',page)).toContain('PBA0096471');
        expect(await additionalApplications.getRowData('Customer reference',page)).toContain('payments');
        expect(await additionalApplications.getRowData('Application Reviewed by Judge',page)).toContain('No');
        await expect(additionalApplications.page.getByText('C2 application')).toBeVisible();
        await expect(additionalApplications.page.getByText('Draft Orders 1')).toBeVisible();
        await expect(additionalApplications.page.getByText('Supporting documents 1')).toBeVisible();
        await expect(additionalApplications.page.getByText('birth certificate',{exact:true})).toBeVisible();
        await expect(additionalApplications.page.getByText('Supporting documents 2')).toBeVisible();
        await expect(additionalApplications.page.getByText('Evidence of consent')).toBeVisible();
        await expect(additionalApplications.page.getByText('Supplements 1')).toBeVisible();
        await expect(additionalApplications.page.getByText('C18 - Recovery order')).toBeVisible();

    });
    test('Child Solicitor submit C2 confidential paper application', async ({
                                                      page,
                                                      signInPage,
                                                      additionalApplications,
                                                      caseFileView,
                                                      envDataConfig
                                                  }) => {


        caseName = 'Child Solicitor submit C2 confidential paper application ' + dateTime.slice(0, 10);
       if(urlConfig.env == 'aat'){
        expect(await updateCase(caseName, caseNumber, caseDataWithChildSolicitor)).toBeTruthy();
         }
       if(urlConfig.env == 'demo'){
           expect(await updateCase(caseName, caseNumber, caseDataWithChildSolicitorDemo)).toBeTruthy();
       }
        expect(await giveAccessToCase(caseNumber,FPLSolicitorOrgUser, '[CHILDSOLICITORA]')).toBeTruthy();
        expect(await assignAMJudicialRole(caseNumber,judgeWalesUser)).toBeTruthy();

        await signInPage.visit();
        await signInPage.page.pause();

        await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password);
        await signInPage.navigateToCaseDetails(caseNumber);

        await additionalApplications.gotoNextStep('Upload additional applications');
        await additionalApplications.selectApplicationType('C2 Application');
        await additionalApplications.selectC2FormType('Upload a paper form');
        await additionalApplications.giveC2AppConsent('Yes');
        await additionalApplications.isC2AppConfidential('Yes');
        await additionalApplications.selectWhoMakeApplication('Susan Brown, Child 1');
        await additionalApplications.clickContinue();
        await additionalApplications.uploadC2ApplicationForm();
        await additionalApplications.isC2ApplicationHasSafeguardRisk('No');
        await additionalApplications.IsC2ToAdjournHearing('No');
        await additionalApplications.canC2AppWaitUntilNextHearing('No');

        await additionalApplications.uploadSupplementDocument('0', 'C20 - Secure accommodation', 'test notes');
        await additionalApplications.uploadSupplementDocument('1', 'C18 - Recovery order', 'Recovery order notes');
        await additionalApplications.uploadC2DraftOrder('0', 'Draft order one');
        await additionalApplications.clickContinue();
        await expect.soft(additionalApplications.page.getByText(testConfig.C2_WITH_SUPPLEMENT)).toBeVisible();
        await additionalApplications.payForApplication(envDataConfig.fplSolicitorOrgPBA);
        await additionalApplications.checkYourAnsAndSubmit();


        await expect.soft(page.getByText('updated with event: Upload additional applications')).toBeVisible();
        await page.pause();
        //assertion
        await additionalApplications.tabNavigation('Other applications');

        expect(await additionalApplications.getRowData('Applicant',page)).toContain('Susan Brown, Child 1');

        expect(await additionalApplications.getRowData('Application with consent?',page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Adjournment requested?',page)).toContain('No');
        expect(await additionalApplications.getRowData('Safeguarding risk?',page)).toContain('No');
        expect(await additionalApplications.getRowData('Consider at next hearing?',page)).toContain('No');
        expect(await additionalApplications.getRowData('Consider at next hearing?',page)).toContain('No');
        expect(await additionalApplications.getRowData('Payment by account (PBA) number',page)).toContain(envDataConfig.fplSolicitorOrgPBA);
        expect(await additionalApplications.getRowData('Customer reference',page)).toContain('Test');
        expect(await additionalApplications.getRowData('Application Reviewed by Judge',page)).toContain('No');
        await expect(additionalApplications.page.getByText('C2 application')).toBeVisible();
        await expect(additionalApplications.page.getByText('Supplements 1')).toBeVisible();
        await expect(additionalApplications.page.getByText('Supplements 2')).toBeVisible();
        await expect(additionalApplications.page.getByText('C20 - Secure accommodation')).toBeVisible();
        await expect(additionalApplications.page.getByText('Draft order one')).toBeVisible();

        //Login in as Local Authority to check  confidential application is not visible to them
        await signInPage.clickSignOut();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateToCaseDetails(caseNumber);
        await additionalApplications.tabNavigation('Other applications');
        await expect(additionalApplications.page.getByText('This is a confidential application and restricted viewing applies')).toBeVisible();


        if (testConfig.waEnabled) {
            await signInPage.clickSignOut();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await additionalApplications.tabNavigation('Tasks');
            await additionalApplications.waitForTask('View Additional Applications');
        }



    });
    test('Respondent solicitor submit C2 online application',async ({
                                                                        page,
                                                                        signInPage,
                                                                        additionalApplications,
                                                                        caseFileView,
                                                                        envDataConfig
                                                                    }) => {

        caseName = 'C2 Online application by Respondent solicitor ' + dateTime.slice(0, 10);
        expect(await updateCase(caseName, caseNumber, caseWithResSolicitor)).toBeTruthy();
        expect(await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]')).toBeTruthy();
        expect(await assignAMJudicialRole(caseNumber,judgeWalesUser)).toBeTruthy();
        await signInPage.visit();
        await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
        await signInPage.navigateToCaseDetails(caseNumber);
        await additionalApplications.gotoNextStep('Upload additional applications');
        await additionalApplications.page.pause();
        await additionalApplications.selectApplicationType('C2 Application');
        await additionalApplications.selectC2FormType('Apply online');
        await additionalApplications.giveC2AppConsent('No');
        await additionalApplications.isC2AppConfidential('No');
        await additionalApplications.selectWhoMakeApplication('Charlie Respondent, Respondent 1');
        await additionalApplications.clickContinue();
        await additionalApplications.selectPermissionC2PermissionType('Permission already granted');
        await additionalApplications.selectC2RelatedToAllChildren('No',['Child katie']);
        await additionalApplications.enterC2ApplicationDetails();
        await additionalApplications.isC2ApplicationHasSafeguardRisk('Yes', 'Urgent');
        await additionalApplications.IsC2ToAdjournHearing('No');
        await additionalApplications.canC2AppWaitUntilNextHearing('Yes');
        await additionalApplications.uploadC2DraftOrder('0', 'Draft order one');
        await additionalApplications.checkStatementOfTruth();
        await additionalApplications.clickContinue();
        await additionalApplications.payForApplication(envDataConfig.privateSolicitorOrgPBA);
        await additionalApplications.checkYourAnsAndSubmit();
        await expect(additionalApplications.page.getByText('with event: Upload additional applications')).toBeVisible();

        await additionalApplications.tabNavigation('Other applications');

        await expect(additionalApplications.page.getByText('Charlie Respondent, Respondent 1')).toBeVisible();
        await expect(additionalApplications.page.getByRole('button', { name: 'C2_APPLICATION.pdf' })).toBeVisible();
        await additionalApplications.openC2Application('C2_APPLICATION.pdf');
        await expect(additionalApplications.c2applicationPage.getByText('For permission to start proceedings')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('For an order or directions in existing proceedings')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('To be joined as, or ceased to be, a party in existing family proceedings')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('under the Children Act 1989')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('I, Charlie Respondent believe that the facts stated in this application are true.')).toBeVisible();
        expect(await additionalApplications.getRowData('Applicant', additionalApplications.page)).toContain('Charlie Respondent, Respondent 1');
        expect(await additionalApplications.getRowData('Application with consent?',additionalApplications.page)).toContain('No');
        expect(await additionalApplications.getRowData('Online or paper form?',additionalApplications.page)).toContain('Apply online');
        expect(await additionalApplications.getRowData('Adjournment requested?',additionalApplications.page)).toContain('No');
        expect(await additionalApplications.getRowData('Permission needed?',additionalApplications.page)).toContain('Permission already granted');
        expect(await additionalApplications.getRowData('Relates to all children?',additionalApplications.page)).toContain('No');
        expect(await additionalApplications.getRowData('Which children does the application relate to?', additionalApplications.page)).toContain('Child katie (Child 1)');
        expect(await additionalApplications.getRowData('Application Summary', additionalApplications.page)).toContain('The C2 application for the taking the gaurdian ship of the chidren');
        expect(await additionalApplications.getRowData('Safeguarding risk?',additionalApplications.page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Safeguarding Reason', additionalApplications.page)).toContain('Urgent');
        expect(await additionalApplications.getRowData('Consider at next hearing?',additionalApplications.page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Payment by account (PBA) number',additionalApplications.page)).toContain(envDataConfig.privateSolicitorOrgPBA);
        expect(await additionalApplications.getRowData('Customer reference',additionalApplications.page)).toContain('Test');
        expect(await additionalApplications.getRowData('Application Reviewed by Judge',additionalApplications.page)).toContain('No');

    });
    test.only('Local Authority submit Confidential C2 online application and C1 application',async ({
                                                                        page,
                                                                        signInPage,
                                                                        additionalApplications,
                                                                        caseFileView,
                                                                        envDataConfig
                                                                    }) => {

        caseName = 'C1 and C2 Online application by Local Authority ' + dateTime.slice(0, 10);
        expect(await updateCase(caseName, caseNumber, caseWithResSolicitor)).toBeTruthy();
        expect(await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]')).toBeTruthy();
        expect(await assignAMJudicialRole(caseNumber,judgeWalesUser)).toBeTruthy();
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateToCaseDetails(caseNumber);
        await additionalApplications.gotoNextStep('Upload additional applications');
        await additionalApplications.clickContinue();
        await expect.soft(page.getByRole('heading', { name: 'There is a problem' })).toBeVisible();
        await expect.soft(page.getByLabel('There is a problem')).toContainText('What application are you making? is required');
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Who is making this application? is required');
        await additionalApplications.selectApplicationType('C2 Application');
        await additionalApplications.selectApplicationType('Other specific application - including C1, C100 and supplements');
        await additionalApplications.selectC2FormType('Apply online');
        await additionalApplications.giveC2AppConsent('Yes');
        await additionalApplications.isC2AppConfidential('Yes');
        await additionalApplications.selectWhoMakeApplication('Swansea City Council, Applicant');
        await additionalApplications.clickContinue();
        await expect.soft(additionalApplications.page.getByText('C2 application')).toBeVisible();
        await additionalApplications.clickContinue();
        await expect(page.getByRole('heading', { name: 'There is a problem' })).toBeVisible();
        await expect(page.getByLabel('There is a problem')).toContainText('Do all other parties consent to this application? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Is this a confidential application? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Who is making this application? is required');







        await page.getByRole('radio', { name: 'Yes', exact: true }).check();
        await page.getByRole('button', { name: 'Evidence of consent' }).click();
        await page.getByRole('button', { name: 'Evidence of consent' }).setInputFiles('C4 - Whereabouts of a missing child.pdf');
        await page.getByRole('radio', { name: 'Yes - only the judge or HMCTS' }).check();
        await page.getByLabel('Who is making this').selectOption('1: applicant');
        await page.getByRole('button', { name: 'Continue' }).click();
        await page.getByRole('button', { name: 'Continue' }).click();
        await expect(page.getByLabel('There is a problem')).toContainText('Do you need permission to make this application? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Does this application relate to all children in the case? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('What is your application about? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Is there any reason, such as a safeguarding risk or other urgent issue, that requires your application to be considered by a judge on the same day? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Are you requesting an adjournment for a scheduled hearing? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Draft Orders is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Field is required');
        await page.getByRole('group', { name: 'Do you need permission to' }).getByLabel('Yes').check();
        await page.getByRole('group', { name: 'Does this application relate' }).getByLabel('No').check();
        await page.getByRole('group', { name: 'Does this application relate' }).getByLabel('Yes').check();
        await page.getByRole('textbox', { name: 'What is your application' }).click();
        await page.getByRole('textbox', { name: 'What is your application' }).fill('application');
        await page.locator('#temporaryC2Document_hasSafeguardingRisk_radio div').filter({ hasText: 'Yes' }).click();
        await page.getByRole('textbox', { name: 'Reason' }).click();
        await page.getByRole('textbox', { name: 'Reason' }).fill('Test');
        await page.getByRole('group', { name: 'Are you requesting an' }).getByLabel('Yes').check();
        await page.getByRole('group', { name: 'Are you requesting an' }).getByLabel('No').check();
        await page.getByRole('group', { name: 'Can your application wait to' }).getByLabel('Yes').check();
        await page.getByRole('group', { name: 'Can your application wait to' }).getByLabel('No').check();
        await page.locator('#temporaryC2Document_draftOrdersBundle').getByRole('button', { name: 'Add new' }).click();
        await page.getByRole('textbox', { name: 'File name' }).click();
        await page.getByRole('textbox', { name: 'File name' }).fill('Draft Order');
        await page.getByRole('button', { name: 'Upload document' }).click();
        await page.getByRole('button', { name: 'Upload document' }).setInputFiles('Draft order 2.pdf');
        await page.getByRole('checkbox', { name: 'Yes' }).click();
        await page.locator('#temporaryC2Document_supplementsBundle').getByRole('button', { name: 'Add new' }).click();
        await page.getByLabel('Select supplement').selectOption('2: C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD');
        await page.getByRole('textbox', { name: 'Notes (Optional)' }).click();
        await page.getByRole('textbox', { name: 'Notes (Optional)' }).fill('note');
        await page.locator('#temporaryC2Document_supplementsBundle_0_document').click();
        await page.locator('#temporaryC2Document_supplementsBundle_0_document').setInputFiles('AdditionalDoc1.pdf');
        await page.locator('#temporaryC2Document_supplementsBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.locator('#temporaryC2Document_supportingEvidenceBundle').getByRole('button', { name: 'Add new' }).click();
        await page.locator('#temporaryC2Document_supportingEvidenceBundle_0_name').fill('supporting document');
        await page.locator('#temporaryC2Document_supportingEvidenceBundle_0_notes').dblclick();
        await page.locator('#temporaryC2Document_supportingEvidenceBundle_0_notes').fill('Note');
        await page.locator('#temporaryC2Document_supportingEvidenceBundle_0_document').click();
        await page.locator('#temporaryC2Document_supportingEvidenceBundle_0_document').setInputFiles('Birth Certificate.docx');
        await page.locator('#temporaryC2Document_supportingEvidenceBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.getByRole('checkbox', { name: 'I believe that the facts' }).check();
        await expect(page.locator('#statementOfTruthLabel')).toContainText('Statement of Truth I understand that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth');
        await page.getByRole('button', { name: 'Continue' }).click();
        await page.getByLabel('Select application').selectOption('1: C1_CHANGE_SURNAME_OR_REMOVE_FROM_JURISDICTION');
        await page.getByRole('button', { name: 'Upload application' }).click();
        await page.getByRole('button', { name: 'Upload application' }).setInputFiles('C12 - Warrant to assist person authorised by an emergency protection order.pdf');
        await page.getByRole('checkbox', { name: 'Yes' }).check();
        await page.getByRole('radio', { name: 'On the same day' }).check();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle').getByRole('button', { name: 'Add new' }).click();
        await page.getByLabel('Document name').selectOption('5: C18_RECOVERY_ORDER');
        await page.getByRole('textbox', { name: 'Notes (Optional)' }).click();
        await page.getByRole('textbox', { name: 'Notes (Optional)' }).fill('Note for recovery order');
        await page.getByRole('button', { name: 'Upload document' }).click();
        await page.getByRole('button', { name: 'Upload document' }).setInputFiles('Consent 2.pdf');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.getByRole('button', { name: 'Add new' }).nth(1).click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_1_name').selectOption('1: C13A_SPECIAL_GUARDIANSHIP');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_1_document').click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_1_document').setInputFiles('C17 - Education supervision order (ESO).pdf');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_1_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.getByRole('button', { name: 'Add new' }).nth(1).click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_2_name').selectOption('2: C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_2_document').click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_2_document').setInputFiles('C4 - Whereabouts of a missing child.pdf');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_2_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.getByRole('button', { name: 'Add new' }).nth(1).click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_3_name').selectOption('3: C15_CONTACT_WITH_CHILD_IN_CARE');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_3_document').click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_3_document').setInputFiles('C3 - Search, take charge and delivery of a child.docx');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_3_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.getByRole('button', { name: 'Add new' }).nth(1).click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_4_name').selectOption('4: C16_CHILD_ASSESSMENT');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_4_document').click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_4_document').setInputFiles('C17 - Education supervision order (ESO).pdf');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_4_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.getByRole('button', { name: 'Add new' }).nth(1).click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_5_name').selectOption('5: C18_RECOVERY_ORDER');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_5_name').selectOption('6: C20_SECURE_ACCOMMODATION');
        await page.getByRole('radio', { name: 'England' }).check();
        await page.getByRole('radio', { name: 'Wales' }).check();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_5_notes').click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_5_notes').fill('fds');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_5_document').click();
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_5_document').setInputFiles('C3 - Search, take charge and delivery of a child.docx');
        await page.locator('#temporaryOtherApplicationsBundle_supplementsBundle_5_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle').getByRole('button', { name: 'Add new' }).click();
        await page.getByRole('textbox', { name: 'File name' }).fill('fdsgds');
        await page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_notes').click();
        await page.getByRole('textbox', { name: 'File name' }).fill('fdsgdsgf');
        await page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_notes').fill('dsfg');
        await page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_document').click();
        await page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_document').setInputFiles('C17 - Education supervision order (ESO).pdf');
        await page.locator('#temporaryOtherApplicationsBundle_supportingEvidenceBundle_0_documentAcknowledge-ACK_RELATED_TO_CASE').check();
        await page.getByRole('button', { name: 'Continue' }).click();
        await expect(page.getByText('£')).toBeVisible();
        await expect(page.getByRole('definition')).toContainText('£270.00');
        await page.getByLabel('Payment by account (PBA)').selectOption('1: PBA0076191');
        await page.getByRole('textbox', { name: 'Customer reference' }).click();
        await page.getByRole('textbox', { name: 'Customer reference' }).fill('res');
        await page.getByRole('button', { name: 'Continue' }).click();
        await page.getByRole('button', { name: 'Previous' }).click();
        await page.getByRole('textbox', { name: 'Client code (Optional)' }).click();
        await page.getByRole('textbox', { name: 'Client code (Optional)' }).fill('cliendt');
        await page.getByRole('button', { name: 'Continue' }).click();
        await page.getByRole('button', { name: 'Save and continue' }).click();
        await page.goto('https://xui-fpl-case-service-pr-6281.preview.platform.hmcts.net/cases/case-details/PUBLICLAW/CARE_SUPERVISION_EPO/1794414033636359#Case%20File%20View');
        await page.getByText('Other applications', { exact: true }).click();
        await expect(page.getByText('C1 - Change surname or remove')).toBeVisible();
        await page.getByText('C18 - Recovery order').click();
        await expect(page.getByText('C18 - Recovery order')).toBeVisible();
        await expect(page.getByText('C13A - Special guardianship')).toBeVisible();
        await expect(page.locator('div').filter({ hasText: /^C14 - Authority to refuse contact with a child in care$/ }).nth(3)).toBeVisible();
        await expect(page.getByText('C15 - Contact with a child in')).toBeVisible();
        await expect(page.getByText('C16 - Child assessment')).toBeVisible();
        await expect(page.getByText('C20 - Secure accommodation')).toBeVisible();
        await page.getByText('Sign out').click();
        await page.getByRole('textbox', { name: 'Enter your email address' }).click();
        await page.getByRole('textbox', { name: 'Enter your email address' }).fill('private.solicitors@mailinator.com');
        await page.getByRole('button', { name: 'Continue' }).click();
        await page.getByRole('button', { name: 'Continue' }).click();
        await page.goto('https://xui-fpl-case-service-pr-6281.preview.platform.hmcts.net/cases');
        await page.getByRole('link', { name: 'Case list' }).click();
        await page.goto('https://xui-fpl-case-service-pr-6281.preview.platform.hmcts.net/cases');






        await additionalApplications.page.pause();
        await additionalApplications.selectApplicationType('C2 Application');
        await additionalApplications.selectC2FormType('Apply online');
        await additionalApplications.giveC2AppConsent('No');
        await additionalApplications.isC2AppConfidential('No');
        await additionalApplications.selectWhoMakeApplication('Charlie Respondent, Respondent 1');
        await additionalApplications.clickContinue();
        await additionalApplications.selectPermissionC2PermissionType('Permission already granted');
        await additionalApplications.selectC2RelatedToAllChildren('No',['Child katie']);
        await additionalApplications.enterC2ApplicationDetails();
        await additionalApplications.isC2ApplicationHasSafeguardRisk('Yes', 'Urgent');
        await additionalApplications.IsC2ToAdjournHearing('No');
        await additionalApplications.canC2AppWaitUntilNextHearing('Yes');
        await additionalApplications.uploadC2DraftOrder('0', 'Draft order one');
        await additionalApplications.checkStatementOfTruth();
        await additionalApplications.clickContinue();
        await additionalApplications.payForApplication(envDataConfig.privateSolicitorOrgPBA);
        await additionalApplications.checkYourAnsAndSubmit();
        await expect(additionalApplications.page.getByText('with event: Upload additional applications')).toBeVisible();

        await additionalApplications.tabNavigation('Other applications');

        await expect(additionalApplications.page.getByText('Charlie Respondent, Respondent 1')).toBeVisible();
        await expect(additionalApplications.page.getByRole('button', { name: 'C2_APPLICATION.pdf' })).toBeVisible();
        await additionalApplications.openC2Application('C2_APPLICATION.pdf');
        await expect(additionalApplications.c2applicationPage.getByText('For permission to start proceedings')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('For an order or directions in existing proceedings')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('To be joined as, or ceased to be, a party in existing family proceedings')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('under the Children Act 1989')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('I, Charlie Respondent believe that the facts stated in this application are true.')).toBeVisible();
        expect(await additionalApplications.getRowData('Applicant', additionalApplications.page)).toContain('Charlie Respondent, Respondent 1');
        expect(await additionalApplications.getRowData('Application with consent?',additionalApplications.page)).toContain('No');
        expect(await additionalApplications.getRowData('Online or paper form?',additionalApplications.page)).toContain('Apply online');
        expect(await additionalApplications.getRowData('Adjournment requested?',additionalApplications.page)).toContain('No');
        expect(await additionalApplications.getRowData('Permission needed?',additionalApplications.page)).toContain('Permission already granted');
        expect(await additionalApplications.getRowData('Relates to all children?',additionalApplications.page)).toContain('No');
        expect(await additionalApplications.getRowData('Which children does the application relate to?', additionalApplications.page)).toContain('Child katie (Child 1)');
        expect(await additionalApplications.getRowData('Application Summary', additionalApplications.page)).toContain('The C2 application for the taking the gaurdian ship of the chidren');
        expect(await additionalApplications.getRowData('Safeguarding risk?',additionalApplications.page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Safeguarding Reason', additionalApplications.page)).toContain('Urgent');
        expect(await additionalApplications.getRowData('Consider at next hearing?',additionalApplications.page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Payment by account (PBA) number',additionalApplications.page)).toContain(envDataConfig.privateSolicitorOrgPBA);
        expect(await additionalApplications.getRowData('Customer reference',additionalApplications.page)).toContain('Test');
        expect(await additionalApplications.getRowData('Application Reviewed by Judge',additionalApplications.page)).toContain('No');

    });
    })
