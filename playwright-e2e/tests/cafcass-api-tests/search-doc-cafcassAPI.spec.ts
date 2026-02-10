import {expect, test} from "../../fixtures/fixtures";
import {
    createCase,
    updateCase
} from "../../utils/api-helper";
import {
    authToken,
    newSwanseaLocalAuthorityUserOne,
} from "../../settings/user-credentials";
import submitCase from '../../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import {
    cafcassAPIDocSearch,
    getTestDocID
} from "../../utils/cafcass-api-test-helper";
import {testConfig} from "../../settings/test-config";

test.describe('@new CafcassAPI Document Search', () => {
    let dateTime = new Date().toISOString();
    let caseNumber:string;
    let docId:string;
    test.beforeEach(async ({request}) => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
        expect(await updateCase('Cafcass search document' + dateTime.slice(0, 10), caseNumber, submitCase)).toBeTruthy();
        await new Promise(resolve => setTimeout(resolve,testConfig.TEST_DATA_SETUP_TIMEOUT_MS ));
        docId = await getTestDocID(request);
        expect(docId).toBeDefined();

    });
    test('  Cafcass user search a valid case document',
        async ({request}) => {
             let response = await cafcassAPIDocSearch(request, authToken.cafcassAuth,docId);
            //assert the response
            expect(response.status()).toBe(200);
            expect(response.headers()["content-type"]).toContain('application/octet-stream');

        })

    test('Unauthorised user search for the document with valid Doc id', async ({request}) => {
        let endTime = new Date().setMinutes(new Date().getMinutes() + 30);
        let response = await cafcassAPIDocSearch(request, authToken.systemAuth,docId);
        //assertion
        expect(response.status()).toEqual(403);
        expect(response.statusText()).toEqual('Forbidden');

    })

})
