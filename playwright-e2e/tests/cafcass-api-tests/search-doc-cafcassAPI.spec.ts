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
    cafcassAPICaseSearch,
    cafcassAPIDocSearch,
    getDateTimePram,
    getTestDocID
} from "../../utils/cafcass-api-test-helper";

test.describe('@new CafcassAPI Document Search', () => {
    let dateTime = new Date().toISOString();
    let currentDateTime = new Date();
    let caseNumber:string;
  //  let startTime = new Date(currentTime.setMinutes(currentTime.getMinutes() - 2)).toISOString();
    let docId:string;
    test.beforeEach(async ({request}) => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('Cafcass search document' + dateTime.slice(0, 10), caseNumber, submitCase);
        docId = await getTestDocID(request);


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
