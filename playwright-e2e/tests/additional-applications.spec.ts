import {test} from '../fixtures/create-fixture';
import {
    newSwanseaLocalAuthorityUserOne,
    judgeWalesUser,
    CTSCUser,
    HighCourtAdminUser,
    privateSolicitorOrgUser
} from '../settings/user-credentials';
import {expect} from "@playwright/test";
import {testConfig} from '../settings/test-config';
import caseData from '../caseData/mandatorySubmissionFieldsWithoutAdditionalApp.json' assert {type: "json"};
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json' assert {type: "json"};
import {setHighCourt} from '../utils/update-case-details';
import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";
import config from "../settings/test-docs/config";
import {urlConfig} from "../settings/urls";
import {TableUtils} from "@hmcts/playwright-common";
import {json} from "node:stream/consumers";

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
        async ({page, signInPage, additionalApplications, envDataConfig}) => {
            caseName = 'LA uploads an other application ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();

            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateToCaseDetails(caseNumber);

            await additionalApplications.gotoNextStep('Upload additional applications');
            await additionalApplications.chooseOtherApplicationType();
            await additionalApplications.fillOtherApplicationDetails();

            // Payment details
            await expect(page.getByText('£263.00')).toBeVisible();
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
                await page.getByRole('button', {name: "Mark as done"}).click();

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
            await expect(page.getByText('£263.00')).toBeVisible();
            await additionalApplications.payForApplication(envDataConfig.swanseaOrgPBA);
            await additionalApplications.checkYourAnsAndSubmit();
        });

    test.skip('LA uploads combined Other and C2 applications @xbrowser',
        async ({page, signInPage, additionalApplications, envDataConfig}) => {
            caseName = 'LA uploads additional application with both Other and C2 ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateToCaseDetails(caseNumber);

            await additionalApplications.gotoNextStep('Upload additional applications');
            await additionalApplications.chooseBothApplicationTypes();
            await additionalApplications.fillC2ApplicationDetails();
            await additionalApplications.fillOtherApplicationDetails();

            await expect(page.getByText('£263.00')).toBeVisible();
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
        async ({page, signInPage, additionalApplications, envDataConfig}) => {
            caseName = 'LA uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);

            await signInPage.navigateToCaseDetails(caseNumber);
            await additionalApplications.gotoNextStep('Upload additional applications');
            await additionalApplications.chooseConfidentialC2ApplicationType();
            await additionalApplications.fillC2ApplicationDetails();

            // Payment details
            await expect(page.getByText('£263.00')).toBeVisible();
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
        async ({page, signInPage, additionalApplications}) => {
            caseName = 'CTSC uploads a confidential C2 application with draft order ' + dateTime.slice(0, 10);
            expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);

            await additionalApplications.gotoNextStep('Upload additional applications');
            await additionalApplications.chooseConfidentialC2ApplicationType();
            await additionalApplications.fillC2ApplicationDetails();

            // Payment details
            await expect(page.getByText('£263.00')).toBeVisible();
            await additionalApplications.ctscPayForApplication();
            await additionalApplications.clickContinue();
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

    test.skip('CTSC uploads standard C2 application with no PBA', async ({
                                                                        page,
                                                                        signInPage,
                                                                        additionalApplications,
                                                                        uploadAdditionalApplications,
                                                                        uploadAdditionalApplicationsApplicationFee,
                                                                        uploadAdditionalApplicationsSuppliedDocuments,
                                                                        submit
                                                                    }) => {
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
        async ({page, signInPage, additionalApplications, envDataConfig}) => {
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
            await expect(page.getByText('£263.00')).toBeVisible();
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
    test.only('CTSC user submit confidential C2 paper application', async ({
                                                                               page,
                                                                               signInPage,
                                                                               additionalApplications,
                                                                               caseFileView,
                                                                               envDataConfig
                                                                           }) => {
        caseName = 'CTSC submit confidential C2 paper application ' + dateTime.slice(0, 10);
        expect(await updateCase(caseName, caseNumber, caseData)).toBeTruthy();
        await signInPage.visit();
        await signInPage.login(CTSCUser.email, CTSCUser.password);
        await signInPage.navigateToCaseDetails(caseNumber);
        await additionalApplications.gotoNextStep('Upload additional applications');
        await additionalApplications.chooseApplication('C2 Application');
        await additionalApplications.selectC2FormType('Upload a paper form');
        await additionalApplications.giveC2AppConsent('Yes');
        await additionalApplications.isC2AppConfidential('No');
        await additionalApplications.selectWhoMakeApplication('Someone else');
        await additionalApplications.clickContinue();
        await additionalApplications.uploadC2ApplicationForm();
        await additionalApplications.isC2ApplicationUrgent('No', 'Urgent');
        await additionalApplications.C2ToadjournHearing('No');
        await additionalApplications.C2WaitUntilNextHearing('Yes');

        await additionalApplications.uploadSupplementDocument('0', 'C18 - Recovery order', 'test notes');
        await additionalApplications.uploadC2DraftOrder('0', 'Draft order one');
        await additionalApplications.uploadC2DraftOrder('1', 'Draft order two');
        await additionalApplications.uploadSupportingDocument('0', 'birth certificate', 'birth certificate of child one');


        await additionalApplications.clickContinue();
        await additionalApplications.ctscPayForApplication();
        await additionalApplications.clickContinue();
        await additionalApplications.checkYourAnsAndSubmit();
        await expect.soft(page.getByText('updated with event: Upload additional applications')).toBeVisible();
        await additionalApplications.tabNavigation('Other applications');

        await expect(page.getByRole('term').filter({hasText: 'Additional applications 1'})).toBeVisible();
        await expect(page.getByRole('cell', {
            name: 'Draft Orders 1 Document name Draft order one File testWordDoc.docx Document Uploader Type HMCTS',
            exact: true
        })).toBeVisible();
        await expect(page.getByRole('cell', {
            name: 'Draft Orders 2 Document name Draft order two File testWordDoc.docx Document Uploader Type HMCTS',
            exact: true
        })).toBeVisible();

        await expect(page.getByText('Supplements 1')).toBeVisible();
        await expect(page.getByRole('cell', {name: 'test notes', exact: true})).toBeVisible();
        await expect(page.getByText('Supporting documents 1')).toBeVisible();
        await expect(page.getByText('birth certificate of child one')).toBeVisible();
        await expect(page.getByText('Supporting documents 2')).toBeVisible();
        await expect(page.getByText('Evidence of consent')).toBeVisible();
        await expect(page.getByRole('row', {name: 'Safeguarding risk? No', exact: true}).locator('td')).toBeVisible();
        await expect(page.getByRole('row', {
            name: 'Consider at next hearing? Yes',
            exact: true
        }).locator('div').nth(1)).toBeVisible();
        await expect(page.getByRole('cell', {
            name: 'PBA Payment Do you want to enter PBA details? Yes Payment by account (PBA) number PBA0096471 Customer reference payments',
            exact: true
        }).getByRole('term')).toBeVisible();


    });
    test('LA submit C2 paper application', async ({
                                                      page,
                                                      signInPage,
                                                      additionalApplications,
                                                      caseFileView,
                                                      envDataConfig
                                                  }) => {
    });
    test('Child solicitor submit paper C2 and other application together', async ({
                                                                                      page,
                                                                                      signInPage,
                                                                                      additionalApplications,
                                                                                      caseFileView,
                                                                                      envDataConfig
                                                                                  }) => {
    });
    test('LA user submit other application', async ({
                                                        page,
                                                        signInPage,
                                                        additionalApplications,
                                                        caseFileView,
                                                        envDataConfig
                                                    }) => {
    });


    test('CTSC user submit  C2 online application', async ({
                                                               page,
                                                               signInPage,
                                                               additionalApplications,
                                                               caseFileView,
                                                               envDataConfig
                                                           }) => {
    });
    test('CTSC user submit C2 and other application together', async ({
                                                                          page,
                                                                          signInPage,
                                                                          additionalApplications,
                                                                          caseFileView,
                                                                          envDataConfig
                                                                      }) => {
    });

    test('Respondent solcitor submit online confidential C2 application', async ({
                                                                                     page,
                                                                                     signInPage,
                                                                                     additionalApplications,
                                                                                     caseFileView,
                                                                                     envDataConfig
                                                                                 }) => {
    });
    test('Respondent solicitor submit other application', async ({
                                                                     page,
                                                                     signInPage,
                                                                     additionalApplications,
                                                                     caseFileView,
                                                                     envDataConfig
                                                                 }) => {
    });


})
