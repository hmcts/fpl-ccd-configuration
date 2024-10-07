import {expect, test} from "../fixtures/fixtures";
import {cafcassAPICaseSearch, createCase, updateCase} from "../utils/api-helper";
import {authToken, newSwanseaLocalAuthorityUserOne} from "../settings/user-credentials";
import Ajv from 'ajv';
import cafcassAPISearchSchema from '../caseData/cafcassAPICaseSchema.json' assert {type: 'json'};
import submitCase from '../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import cafcassCase from '../caseData/caseCaffcassAPISearchAllFieldData.json' assert {type: 'json'};

test.describe('CafcassAPI search cases @cafcassAPI', () => {
    let startTime = new Date().toISOString();
    let intervalEndTime: string;
    let intervalStartTime: string;
    const ajv = new Ajv();
    let caseNumber: string
    let caseNumber2: string;

    test.beforeAll(async () => {
        let currentTime = new Date();
        intervalStartTime = new Date(currentTime.setMinutes(currentTime.getMinutes() - 2)).toISOString();

        // set up the test data
        // return case, case with minimum data , case with all data
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('submit case' + startTime.slice(0, 10), caseNumber, submitCase);
        caseNumber2 = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('submit case' + startTime.slice(0, 10), caseNumber2, cafcassCase);
        let interval = currentTime.setMinutes(currentTime.getMinutes() + 5);
        intervalEndTime = new Date(interval).toISOString();


    });
    test(' Cafcass user search cases for given time frame',
        async ({request, page}) => {
            await page.waitForTimeout(2000) // wait for the test data to be set up
            let response = await cafcassAPICaseSearch(request, authToken.cafcassAuth, intervalStartTime, intervalEndTime);

            //assert the response
            expect(response.status()).toBe(200);
            let body = await response.json();

            const validJson = ajv.validate(cafcassAPISearchSchema, body);
            if (!validJson) console.log(ajv.errors)
            expect(validJson).toBe(true);
            expect(await body.total).toBeGreaterThanOrEqual(2);
            expect(JSON.stringify(await body.cases)).toContain(caseNumber);
            expect(JSON.stringify(await body.cases)).toContain(caseNumber2);
        })
    test('search case by user without cafcass role', async ({request}) => {

        let response = await cafcassAPICaseSearch(request, authToken.systemAuth, intervalStartTime, intervalEndTime);
        //assertion
        expect(response.status()).toBe(403);
        expect(response.statusText()).toBe('Forbidden');
    })
    test('Search cases with invalid parameter', async ({request}) => {
        let endTime = new Date().setMinutes(new Date().getMinutes() + 30);
        let response = await cafcassAPICaseSearch(request, authToken.cafcassAuth, intervalStartTime, endTime.toString());
        //assertion
        expect(response.status()).toEqual(400);
        expect(response.statusText()).toEqual('Bad Request');

    })

})
