import {expect, test} from "../fixtures/fixtures";
import {cafcassAPICaseSearch, createCase, getAccessToken, updateCase} from "../utils/api-helper";
import {cafcassAPIUser, newSwanseaLocalAuthorityUserOne, systemUpdateUser} from "../settings/user-credentials";
import Ajv from 'ajv';
import cafcassAPISearchSchema from '../caseData/cafcassAPICaseSchema.json' assert {type: 'json'};
import submitCase from '../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import cafcassCase from '../caseData/caseCaffcassAPISearchAllFieldData.json' assert {type: 'json'};

test.describe('@new CafcassAPI @cafcassAPI', () => {
    let startTime = new Date().toISOString();
    let intervalEndTime: string;
    let intervalStartTime: string;
    const ajv = new Ajv();
    let caseNumber: string
    let caseNumber2: string;

    test.beforeAll(async () => {

        let accessToken = await getAccessToken({user: cafcassAPIUser});
        let currentTime = new Date();
        intervalStartTime = new Date().toISOString();

        // set up the test data
        // return case, case with minimum data , case with all data
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('submit case' + startTime.slice(0, 10), caseNumber, submitCase);
        caseNumber2 = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('submit case' + startTime.slice(0, 10), caseNumber2, cafcassCase);
        let interval = currentTime.setMinutes(currentTime.getMinutes() + 14);
        intervalEndTime = new Date(interval).toISOString();

    });
    test(' Cafcass user search cases for given time frame',
        async ({request}) => {
            let response = await cafcassAPICaseSearch(request, cafcassAPIUser, intervalStartTime, intervalEndTime);

            //assert the response
            expect(response.status()).toBe(200);
            let body = await response.json();
            const validated = ajv.validate(cafcassAPISearchSchema, body);
            expect(validated).toBe(true);
            expect(body.total).toBeGreaterThanOrEqual(2);
            expect(JSON.stringify(body.cases)).toContain(caseNumber);
            expect(JSON.stringify(body.cases)).toContain(caseNumber2);
        })
    test('search case by user without cafcass role', async ({request}) => {

        let response = await cafcassAPICaseSearch(request, systemUpdateUser, intervalStartTime, intervalEndTime);
        //assertion
        expect(response.status()).toBe(403);
        expect(response.statusText()).toBe('Forbidden');
    })
    test('Search cases with invalid parameter', async ({request}) => {
        let endTime = new Date().setMinutes(new Date().getMinutes() + 30);
        let response = await cafcassAPICaseSearch(request, cafcassAPIUser, intervalStartTime, endTime.toString());
        //assertion
        expect(response.status()).toEqual(400);
        expect(response.statusText()).toEqual('Bad Request');

    })

})
