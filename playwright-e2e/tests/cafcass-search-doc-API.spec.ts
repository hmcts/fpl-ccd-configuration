import {expect, test} from "../fixtures/fixtures";
import {
    cafcassAPICaseSearch, cafcassAPIDocSearch,
    createCase,
    updateCase
} from "../utils/api-helper";
import {
    authToken,
    newSwanseaLocalAuthorityUserOne,
} from "../settings/user-credentials";
import submitCase from '../caseData/mandatorySubmissionFields.json' assert {type: 'json'};

test.describe('@new CafcassAPI Document Search @cafcassAPI', () => {
    let startTime = new Date().toISOString();
    let currentTime = new Date();
    let caseNumber:string;
    let StartTime = new Date(currentTime.setMinutes(currentTime.getMinutes() - 2)).toISOString();
    let docId:string;
    test.beforeEach(async ({request}) => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('Cafcass search document' + startTime.slice(0, 10), caseNumber, submitCase);
        let interval = currentTime.setMinutes(currentTime.getMinutes() + 11);
       let   endTime = new Date(interval).toISOString();
        let caseResponse =await cafcassAPICaseSearch(request,authToken.cafcassAuth,StartTime,endTime)
        let body = await caseResponse.json();
         docId= await body.cases[0].caseData.caseDocuments[0].documentId;

    });
    test('  Cafcass user search a valid case document',
        async ({request}) => {

             let response = await cafcassAPIDocSearch(request, authToken.cafcassAuth,docId);

            //assert the response
            expect(response.status()).toBe(200);
            expect(response.headers()["content-type"]).toContain('application/octet-stream');

        })

    test('user without cafcass role access the end point', async ({request}) => {
        let endTime = new Date().setMinutes(new Date().getMinutes() + 30);
        let response = await cafcassAPIDocSearch(request, authToken.systemAuth,docId);
        //assertion
        expect(response.status()).toEqual(403);
        expect(response.statusText()).toEqual('Forbidden');

    })

})
