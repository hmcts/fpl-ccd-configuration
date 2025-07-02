import {test} from '../fixtures/create-fixture';
import caseData from '../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import {CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne} from '../settings/user-credentials';
import {createCase, updateCase} from "../utils/api-helper";

test.describe('Manage the Retain and Dispose Config', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;

    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('CTSC suspend case disposal',
        async ({page, signInPage, manageTTL}) => {
            caseName = 'Suspend system case disposal ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await manageTTL.gotoNextStep('Manage Case TTL');
            await manageTTL.suspendTTL();
            await manageTTL.clickContinue();
            await manageTTL.clickSaveAndContinue();
        });

    test('CTSC leader override the system disposal date',
        async ({page, signInPage, manageTTL}) => {
            caseName = 'Override system case disposal date ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await manageTTL.gotoNextStep('Manage Case TTL')
            await manageTTL.overrideSystemTTL();
            await manageTTL.clickContinue();
            await manageTTL.clickSaveAndContinue();
        });
});
