import {expect, test} from "../fixtures/fixtures";
import {cafcassAPIUploadDoc, createCase, updateCase} from "../utils/api-helper";
import {authToken, CTSCTeamLeadUser, newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import submitCase from '../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import {CreateCaseName} from "../utils/create-case-name";

test.describe('Cafcass upload guardian report @cafcassAPI', () => {
    let startTime = new Date().toISOString();
    let caseNumber: string
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });


    test('Cafcass upload gaurdian report', async ({request, page, signInPage, caseFileView, manageDocuments}) => {
        await updateCase('cafcass upload guardian report' + startTime.slice(0, 10), caseNumber, submitCase);
        let docName = CreateCaseName.generateFileName('GUARDIAN_REPORT');
        let response = await cafcassAPIUploadDoc(request, authToken.cafcassAuth, caseNumber, 'GUARDIAN_REPORT');
        //assert the response
        expect(response.status()).toBe(200);
        expect(await response.text()).toContain('GUARDIAN_REPORT uploaded successfully');
        //test to see the doc in MC application
        await signInPage.visit();
        await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Guardian\'s reports');

        await expect(page.getByRole('tree')).toContainText(`${docName}`);

        await signInPage.gotoNextStep('Manage documents');
        await manageDocuments.removeDocuments('Guardian report', docName);
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Guardian\'s reports');

        await expect(page.getByRole('tree')).not.toContainText(`${docName}`);


    })
    test('Cafcass upload position statement', async ({request, page, signInPage, caseFileView, manageDocuments}) => {
        await updateCase('Cafcass upload position statement' + startTime.slice(0, 10), caseNumber, submitCase);
        let docName = CreateCaseName.generateFileName('POSITION_STATEMENT');
        let response = await cafcassAPIUploadDoc(request, authToken.cafcassAuth, caseNumber, 'POSITION_STATEMENT');
        expect(response.status()).toBe(200);
        expect(await response.text()).toContain('POSITION_STATEMENT uploaded successfully');

        //test to see the doc in MC application
        await signInPage.visit();
        await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password);
        await signInPage.navigateTOCaseDetails(caseNumber);
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Position Statements');

        await expect(page.getByRole('tree')).toContainText(`${docName}`);

        await signInPage.gotoNextStep('Manage documents');
        await manageDocuments.removeDocuments('Position Statements', docName);
        await caseFileView.goToCFVTab();
        await caseFileView.openFolder('Position Statements');

        await expect(page.getByRole('tree')).not.toContainText(`${docName}`);

    })
    test(' Upload report  for cases in invalid state', async ({request}) => {
        let response = await cafcassAPIUploadDoc(request, authToken.cafcassAuth, caseNumber, 'POSITION_STATEMENT');

        //assertion
        expect(response.status()).toBe(404);
        expect(response.statusText()).toBe('Not Found');
    })
    test('Upload reports by user without cafcass role', async ({request}) => {

        await updateCase('Upload reports by user without cafcass role' + startTime.slice(0, 10), caseNumber, submitCase);
        let response = await cafcassAPIUploadDoc(request, authToken.systemAuth, caseNumber, 'POSITION_STATEMENT');

        //assertion
        expect(response.status()).toEqual(403);
        expect(response.statusText()).toEqual('Forbidden');

    })
    test('Upload wrong file type', async ({request}) => {

        await updateCase('Upload wrong file type' + startTime.slice(0, 10), caseNumber, submitCase);
        let response = await cafcassAPIUploadDoc(request, authToken.cafcassAuth, caseNumber, 'POSITION_STATEMENT', 'txt');

        //assertion
        expect(response.status()).toEqual(400);
        expect(response.statusText()).toEqual('Bad Request');

    })

})
