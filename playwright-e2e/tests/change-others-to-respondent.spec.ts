import { test } from '../fixtures/create-fixture';
import { createCase, updateCase } from "../utils/api-helper";
import { CTSCUser, newSwanseaLocalAuthorityUserOne, HighCourtAdminUser } from "../settings/user-credentials";
import caseData from '../caseData/mandatorySubmissionFields.json' assert { type: "json" };
import { expect } from '@playwright/test';

test.describe('Change others to respondent ', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('change others to respondent event',
        async ({ page, signInPage, changeOthersToRespondent }) => {
            caseName = 'CTSC changes others to respondent event' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCUser.email, CTSCUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);

            await changeOthersToRespondent.gotoNextStep('Change other to respondent');
            await changeOthersToRespondent.changeOthersToRespondent();
            await changeOthersToRespondent.tabNavigation('Change other to respondent')
            await expect(page.getByText('Respondent event')).toBeVisible();

        });
});
