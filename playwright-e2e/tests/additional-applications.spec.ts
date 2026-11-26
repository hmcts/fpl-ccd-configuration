import {test} from '../fixtures/create-fixture';
import {
    newSwanseaLocalAuthorityUserOne, judgeWalesUser, CTSCUser, HighCourtAdminUser, privateSolicitorOrgUser,
    CTSCTeamLeadUser, FPLSolicitorOrgUser
} from '../settings/user-credentials';
import {expect} from "@playwright/test";
import {testConfig} from '../settings/test-config';
import caseData from '../caseData/mandatorySubmissionFieldsWithoutAdditionalApp.json' assert {type: "json"};
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json' assert {type: "json"};
import caseDataWithHearing from '../caseData/caseWithHearingDetailsNoAdditionalApp.json' assert {type: "json"};
import caseDataWithChildSolicitor from '../caseData/caseWithMultipleChildCafcassSolicitor.json' assert {type: "json"};
import caseDataWithChildSolicitorDemo
    from '../caseData/caseWithMultipleChildCafcassSolicitorDemo.json' assert {type: "json"};
import {setHighCourt} from '../utils/update-case-details';
import {assignAMJudicialRole, createCase, giveAccessToCase, updateCase} from "../utils/api-helper";
import config from "../settings/test-docs/config";
import {urlConfig} from "../settings/urls";
import{assertComplexValue} from "../utils/util-helper";

test.describe('Upload additional applications', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
    });
    //mark test as slow to give extra timeout
    // test.slow();

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
                await page.getByRole('button', {name: "Mark as done"}).click();

                // Should be no more tasks on the page
                await expect(page.getByText('View Additional Applications')).toHaveCount(0);
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
        setHighCourt(caseData);
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
        expect(await additionalApplications.getRowData('Application with consent?', page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Safeguarding risk?', page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Consider at next hearing?', page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Payment by account (PBA) number', page)).toContain('PBA0096471');
        expect(await additionalApplications.getRowData('Customer reference', page)).toContain('payments');
        expect(await additionalApplications.getRowData('Application Reviewed by Judge', page)).toContain('No');
        await expect(additionalApplications.page.getByText('C2 application')).toBeVisible();
        await expect(additionalApplications.page.getByText('Draft Orders 1')).toBeVisible();
        await expect(additionalApplications.page.getByText('Supporting documents 1')).toBeVisible();
        await expect(additionalApplications.page.getByText('birth certificate', {exact: true})).toBeVisible();
        await expect(additionalApplications.page.getByText('Supporting documents 2')).toBeVisible();
        await expect(additionalApplications.page.getByText('Evidence of consent')).toBeVisible();
        await expect(additionalApplications.page.getByText('Supplements 1')).toBeVisible();
        await expect(additionalApplications.page.getByText('C18 - Recovery order')).toBeVisible();
        if (testConfig.waEnabled) {

            await additionalApplications.tabNavigation('Tasks');
            await additionalApplications.waitForTask('View Additional Applications');
        }

    });
    test('Child Solicitor submit C2 confidential paper application', async ({
                                                                                page,
                                                                                signInPage,
                                                                                additionalApplications,
                                                                                caseFileView,
                                                                                envDataConfig
                                                                            }) => {


        caseName = 'Child Solicitor submit C2 confidential paper application ' + dateTime.slice(0, 10);
        if (urlConfig.env == 'aat') {
            expect(await updateCase(caseName, caseNumber, caseDataWithChildSolicitor)).toBeTruthy();
        }
        if (urlConfig.env == 'demo') {
            expect(await updateCase(caseName, caseNumber, caseDataWithChildSolicitorDemo)).toBeTruthy();
        }
        expect(await giveAccessToCase(caseNumber, FPLSolicitorOrgUser, '[CHILDSOLICITORA]')).toBeTruthy();
        expect(await assignAMJudicialRole(caseNumber, judgeWalesUser)).toBeTruthy();

        await signInPage.visit();

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

        //assertion
        await additionalApplications.tabNavigation('Other applications');

        expect(await additionalApplications.getRowData('Applicant', page)).toContain('Susan Brown, Child 1');

        expect(await additionalApplications.getRowData('Application with consent?', page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Adjournment requested?', page)).toContain('No');
        expect(await additionalApplications.getRowData('Safeguarding risk?', page)).toContain('No');
        expect(await additionalApplications.getRowData('Consider at next hearing?', page)).toContain('No');
        expect(await additionalApplications.getRowData('Consider at next hearing?', page)).toContain('No');
        expect(await additionalApplications.getRowData('Payment by account (PBA) number', page)).toContain(envDataConfig.fplSolicitorOrgPBA);
        expect(await additionalApplications.getRowData('Customer reference', page)).toContain('Test');
        expect(await additionalApplications.getRowData('Application Reviewed by Judge', page)).toContain('No');
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
    test('Respondent solicitor submit C2 online application', async ({
                                                                         page,
                                                                         signInPage,
                                                                         additionalApplications,
                                                                         caseFileView,
                                                                         envDataConfig
                                                                     }) => {

        caseName = 'C2 Online application by Respondent solicitor ' + dateTime.slice(0, 10);
        expect(await updateCase(caseName, caseNumber, caseWithResSolicitor)).toBeTruthy();
        expect(await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]')).toBeTruthy();
        expect(await assignAMJudicialRole(caseNumber, judgeWalesUser)).toBeTruthy();
        await signInPage.visit();
        await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password);
        await signInPage.navigateToCaseDetails(caseNumber);
        await additionalApplications.gotoNextStep('Upload additional applications');
        await additionalApplications.selectApplicationType('C2 Application');
        await additionalApplications.selectC2FormType('Apply online');
        await additionalApplications.giveC2AppConsent('No');
        await additionalApplications.isC2AppConfidential('No');
        await additionalApplications.selectWhoMakeApplication('Charlie Respondent, Respondent 1');
        await additionalApplications.clickContinue();
        await additionalApplications.selectPermissionC2PermissionType('Permission already granted');
        await additionalApplications.selectC2RelatedToAllChildren('No', ['Child katie']);
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
        await expect(additionalApplications.page.getByRole('button', {name: 'C2_APPLICATION.pdf'})).toBeVisible();
        await additionalApplications.openC2Application('C2_APPLICATION.pdf');
        await expect(additionalApplications.c2applicationPage.getByText('For permission to start proceedings')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('For an order or directions in existing proceedings')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('To be joined as, or ceased to be, a party in existing family proceedings')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('under the Children Act 1989')).toBeVisible();
        await expect(additionalApplications.c2applicationPage.getByText('I, Charlie Respondent believe that the facts stated in this application are true.')).toBeVisible();
        expect(await additionalApplications.getRowData('Applicant', additionalApplications.page)).toContain('Charlie Respondent, Respondent 1');
        expect(await additionalApplications.getRowData('Application with consent?', additionalApplications.page)).toContain('No');
        expect(await additionalApplications.getRowData('Online or paper form?', additionalApplications.page)).toContain('Apply online');
        expect(await additionalApplications.getRowData('Adjournment requested?', additionalApplications.page)).toContain('No');
        expect(await additionalApplications.getRowData('Permission needed?', additionalApplications.page)).toContain('Permission already granted');
        expect(await additionalApplications.getRowData('Relates to all children?', additionalApplications.page)).toContain('No');
        expect(await additionalApplications.getRowData('Which children does the application relate to?', additionalApplications.page)).toContain('Child katie (Child 1)');
        expect(await additionalApplications.getRowData('Application Summary', additionalApplications.page)).toContain('The C2 application for the taking the gaurdian ship of the chidren');
        expect(await additionalApplications.getRowData('Safeguarding risk?', additionalApplications.page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Safeguarding Reason', additionalApplications.page)).toContain('Urgent');
        expect(await additionalApplications.getRowData('Consider at next hearing?', additionalApplications.page)).toContain('Yes');
        expect(await additionalApplications.getRowData('Payment by account (PBA) number', additionalApplications.page)).toContain(envDataConfig.privateSolicitorOrgPBA);
        expect(await additionalApplications.getRowData('Customer reference', additionalApplications.page)).toContain('Test');
        expect(await additionalApplications.getRowData('Application Reviewed by Judge', additionalApplications.page)).toContain('No');
        if (testConfig.waEnabled) {
            await signInPage.clickSignOut();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await additionalApplications.tabNavigation('Tasks');
            await additionalApplications.waitForTask('View Additional Applications');
        }

    });
    test('Local Authority submit Confidential C2 online application and C1 application', async ({
                                                                                                         page,
                                                                                                         signInPage,
                                                                                                         additionalApplications,
                                                                                                         caseFileView,
                                                                                                         envDataConfig
                                                                                                     }) => {

        caseName = 'C1 and C2 Online application by Local Authority ' + dateTime.slice(0, 10);
        expect(await updateCase(caseName, caseNumber, caseWithResSolicitor)).toBeTruthy();
        expect(await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]')).toBeTruthy();
        expect(await assignAMJudicialRole(caseNumber, judgeWalesUser)).toBeTruthy();
        await signInPage.visit();
        await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
        await signInPage.navigateToCaseDetails(caseNumber);


        await additionalApplications.gotoNextStep('Upload additional applications');
        await additionalApplications.selectApplicationType('C2 Application');
        await additionalApplications.selectApplicationType('Other specific application - including C1, C100 and supplements');
        await additionalApplications.clickContinue();
        await expect(page.getByRole('heading', {name: 'There is a problem'})).toBeVisible();
        await expect(page.getByLabel('There is a problem')).toContainText('Do you want to make the C2 application online or upload a paper form? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Do all other parties consent to this application? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Is this a confidential application? is required');
        await expect(page.getByLabel('There is a problem')).toContainText('Who is making this application? is required');

        await additionalApplications.selectC2FormType('Apply online');
        await additionalApplications.giveC2AppConsent('Yes');
        await additionalApplications.isC2AppConfidential('Yes');
        await additionalApplications.selectWhoMakeApplication('Swansea City Council, Applicant');
        await additionalApplications.clickContinue();
        await expect.soft(additionalApplications.page.getByText('C2 application', {exact: true})).toBeVisible();
        await additionalApplications.clickContinue();
        await expect.soft(page.getByRole('heading', {name: 'There is a problem'})).toBeVisible();
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Do you need permission to make this application? is required');
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Does this application relate to all children in the case? is required');
        await expect.soft(page.getByLabel('There is a problem')).toContainText('What is your application about? is required');
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Is there any reason, such as a safeguarding risk or other urgent issue, that requires your application to be considered by a judge on the same day? is required');
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Are you requesting an adjournment for a scheduled hearing? is required');
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Draft Orders is required');
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Field is required');

        await additionalApplications.selectPermissionC2PermissionType('Permission not required');
        await additionalApplications.selectC2RelatedToAllChildren('Yes');
        await additionalApplications.enterC2ApplicationDetails();
        await additionalApplications.isC2ApplicationHasSafeguardRisk('No');
        await additionalApplications.IsC2ToAdjournHearing('No');
        await additionalApplications.canC2AppWaitUntilNextHearing('No');
        await additionalApplications.uploadC2DraftOrder('0', 'Draft order one');
        await additionalApplications.uploadSupplementDocument('0', 'C13A - Special guardianship order', 'Guardianship notes');
        await additionalApplications.uploadSupplementDocument('1', 'C14 - Authority to refuse contact with a child in care', 'Refuse contact notes');
        await additionalApplications.uploadSupplementDocument('2', 'C15 - Contact with a child in care', 'Contact with a child a care notes');
        await additionalApplications.uploadSupplementDocument('3', 'C16 - Child assessment', 'Child assessment notes');
        await additionalApplications.uploadSupplementDocument('4', 'C18 - Recovery order', 'Recovery order notes');
        await additionalApplications.uploadSupportingDocument('0', 'birth certificate', 'birth certificate of child one');
        await additionalApplications.checkStatementOfTruth();
        await additionalApplications.clickContinue();
        expect(await page.getByRole('heading', {name: 'Other applications', exact: true})).toBeVisible();

        await additionalApplications.clickContinue();

        await expect.soft(page.getByRole('heading', {name: 'There is a problem'})).toBeVisible();
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Select application is required');
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Select or fill the required Upload application field');
        await expect.soft(page.getByLabel('There is a problem')).toContainText('Tick to confirm this document is related to this case is required');

        await additionalApplications.fillOtherApplicationDetails();
        await additionalApplications.payForApplication(envDataConfig.swanseaOrgPBA);
        await additionalApplications.checkYourAnsAndSubmit();
        await expect(additionalApplications.page.getByText('with event: Upload additional applications')).toBeVisible();

        await additionalApplications.tabNavigation('Other applications');


        const c2Data = await additionalApplications.extractComplexTable(additionalApplications.c2Panel);
        const otherAppData = await additionalApplications.extractComplexTable(additionalApplications.otherAppPanel);

        console.log(JSON.stringify(c2Data, null, 2));
        console.log(JSON.stringify(otherAppData, null, 2));

        //Assertions for C2 application
        assertComplexValue(c2Data, ["Applicant"], "Swansea City Council, Applicant");
        assertComplexValue(c2Data, ["Application with consent?"], "Yes");
        assertComplexValue(c2Data, ["Online or paper form?"], "Apply online");
        assertComplexValue(c2Data, ["Adjournment requested?"], "No");
        assertComplexValue(c2Data, ["Do you need permission to make this application?"], "Permission not required");
        assertComplexValue(c2Data, ["Relates to all children?"], "Yes");
        assertComplexValue(c2Data, ["Application Summary"], "The C2 application for the taking the gaurdian ship of the chidren");
        assertComplexValue(c2Data, ["Safeguarding risk?"], "No");
        assertComplexValue(c2Data, ["Consider at next hearing?"], "No");
        assertComplexValue(c2Data, ["Draft Orders", "0", "Document name"], "Draft order one");
        assertComplexValue(c2Data, ["Supplements", "0", "Document name"], "C13A - Special guardianship order");
        assertComplexValue(c2Data, ["Supplements", "0", "Notes"], "Guardianship notes");
        assertComplexValue(c2Data, ["Supplements", "1", "Document name"], "C14 - Authority to refuse contact with a child in care");
        assertComplexValue(c2Data, ["Supplements", "1", "Notes"], "Refuse contact notes");
        assertComplexValue(c2Data, ["Supplements", "2", "Document name"], "C15 - Contact with a child in care");
        assertComplexValue(c2Data, ["Supplements", "2", "Notes"], "Contact with a child a care notes");
        assertComplexValue(c2Data, ["Supplements", "3", "Document name"], "C16 - Child assessment");
        assertComplexValue(c2Data, ["Supplements", "3", "Notes"], "Child assessment notes");
        assertComplexValue(c2Data, ["Supplements", "4", "Document name"], "C18 - Recovery order");
        assertComplexValue(c2Data, ["Supplements", "4", "Notes"], "Recovery order notes");
        assertComplexValue(c2Data, ["Supporting documents", "0", "Document name"], "birth certificate");
        assertComplexValue(c2Data, ["Supporting documents", "1", "Document name"], "Evidence of consent");

        //assertions for Other application
        assertComplexValue(otherAppData, ["Applicant"], "Swansea City Council, Applicant");
        assertComplexValue(otherAppData, ["Application type"], "C1 - Change surname or remove from jurisdiction");
        assertComplexValue(otherAppData, ["Please state how soon you want the judge to consider your application?"], "On the same day");
        assertComplexValue(otherAppData, ["Supporting documents", "0", "Document name"], "supporting document");
        assertComplexValue(otherAppData, ["Supplements", "0", "Document name"], "C13A - Special guardianship order");


    });
})
