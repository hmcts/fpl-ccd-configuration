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

test.describe('Add a case flag @sessionreuse', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test.only('Add and remove a case flag as admin user',
        async ({ addAdminCaseFlag,ctscUser}) => {
            caseName = 'Add and remove a case flag' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
           await addAdminCaseFlag.switchUser(ctscUser.page);
            await runTest( addAdminCaseFlag);
        });

    test.only('Add and remove a case flag as judicial user',
        async ({ addAdminCaseFlag,legalUser}) => {
            caseName = 'Add and remove a case flag' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await addAdminCaseFlag.switchUser(legalUser.page);
            // await signInPage.visit();
            // await signInPage.login(judgeWalesUser.email, judgeWalesUser.password);
            await runTest(addAdminCaseFlag);
        });

    async function runTest( addAdminCaseFlag: AddAndRemoveAdminCaseFlag) {
        await addAdminCaseFlag.navigateTOCaseDetails(caseNumber);
        await addAdminCaseFlag.runAddCaseFlagTest();
        await expect(addAdminCaseFlag.page.getByText('Potentially violent person',{exact: true})).toBeVisible();
        await expect(addAdminCaseFlag.page.getByText('Case Flag Added')).toBeVisible();
        await  addAdminCaseFlag.runRemoveCaseFlagTest();
        await expect(addAdminCaseFlag.page.getByText('Potentially violent person',{exact: true})).toBeHidden();
    }


});

