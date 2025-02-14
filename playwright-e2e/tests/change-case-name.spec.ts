import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: "json" };
import { CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from "../settings/user-credentials";
import { expect } from "@playwright/test";

test.describe('Change case name', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });
    test('Change case name',
        async ({ page, signInPage, changeCaseName }) => {
            caseName = 'CTSC Change case name ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password,);
            await signInPage.navigateTOCaseDetails(caseNumber);

            await changeCaseName.gotoNextStep('Change case name');
            await changeCaseName.updateCaseName();
            await expect(page.getByText('Change case name')).toBeVisible();
        })
});
