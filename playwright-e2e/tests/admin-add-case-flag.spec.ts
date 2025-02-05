import { test } from '../fixtures/create-fixture';
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: "json" };
import {
    newSwanseaLocalAuthorityUserOne,
    CTSCUser,
    judgeWalesUser
} from '../settings/user-credentials';
import { Page, expect} from "@playwright/test";
import {AddAndRemoveAdminCaseFlag} from '../pages/add-and-remove-admin-case-flag';
import {SignInPage} from '../pages/sign-in';
import {createCase, updateCase} from "../utils/api-helper";

test.describe('Add a case flag', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('Add and remove a case flag as admin user',
        async ({page, signInPage, addAdminCaseFlag}) => {
            caseName = 'Add and remove a case flag' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password);
            await runTest(signInPage, addAdminCaseFlag, page);
        });

    test('Add and remove a case flag as judicial user',
        async ({page, signInPage, addAdminCaseFlag}) => {
            caseName = 'Add and remove a case flag' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
            await runTest(signInPage, addAdminCaseFlag, page);
        });

    async function runTest(signInPage: SignInPage, addAdminCaseFlag: AddAndRemoveAdminCaseFlag, page: Page) {
        await signInPage.navigateTOCaseDetails(caseNumber);
        await addAdminCaseFlag.runAddCaseFlagTest();
        await expect(page.getByText('Potentially violent person',{exact: true})).toBeVisible();
        await expect(page.getByText('Case Flag Added')).toBeVisible();
        await  addAdminCaseFlag.runRemoveCaseFlagTest();
        await expect(page.getByText('Potentially violent person',{exact: true})).toBeHidden();
    }   
});
