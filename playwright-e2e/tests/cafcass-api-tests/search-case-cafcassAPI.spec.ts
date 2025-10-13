import {expect, test} from "../../fixtures/fixtures";
import {createCase, updateCase} from "../../utils/api-helper";
import {authToken, newSwanseaLocalAuthorityUserOne} from "../../settings/user-credentials";
import Ajv from 'ajv';
import cafcassAPISearchSchema from '../../caseData/cafcassAPITest/cafcassAPICaseSchema.json' assert {type: 'json'};
import submitCase from '../../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import cafcassCase from '../../caseData/cafcassAPITest/caseCaffcassAPISearchAllFieldData.json' assert {type: 'json'};
import {cafcassAPICaseSearch, getDateTimePram} from "../../utils/cafcass-api-test-helper";

test.describe('CafcassAPI search cases', () => {
    let startTime = new Date().toISOString();
    let intervalEndTime: string;
    let intervalStartTime: string;
    let currentTime : Date;
    const ajv = new Ajv({allErrors: true, verbose: true});
    let caseNumber1: string
    let caseNumber2: string;
    const TEST_DATA_SETUP_TIMEOUT_MS = 2000;

    test.beforeAll(async () => {
        test.setTimeout(90000);
         currentTime = new Date();

        // set up the test data
        // return case, case with minimum data , case with all data
        caseNumber1 = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('Cafcass search case1 ' + startTime.slice(0, 10), caseNumber1, submitCase);
        caseNumber2 = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('Cafcass search case2 ' + startTime.slice(0, 10), caseNumber2, cafcassCase);

    });
    test.only(' Cafcass user search cases for given time frame',
        async ({request, page}) => {

            await page.waitForTimeout(TEST_DATA_SETUP_TIMEOUT_MS) // wait for the test data to be set up
            intervalStartTime = getDateTimePram(currentTime, -2); // getting time 2 mins before the current time for start time
            intervalEndTime = getDateTimePram(currentTime, 10); // getting time 10 mins after the current time for end time
            console.log('start time ' + intervalStartTime);
            console.log('end time ' + intervalEndTime);

            let response = await cafcassAPICaseSearch(request, authToken.cafcassAuth, intervalStartTime, intervalEndTime);

            //assert the response
            expect(response.status()).toBe(200);
            let body = await response.json();
            expect(await body.total).toBeGreaterThanOrEqual(2);

           console.log("body"+JSON.stringify(body));
            const validJson = ajv.validate(cafcassAPISearchSchema, body);
           if (!validJson) console.log("Json Validation error \n" + ajv.errorsText());
            expect.soft(validJson).toBe(true);

            const allCaseIds = await body.cases.map((a: { id: string; }) => a.id);
            expect.soft(body.cases).toContainEqual({id: caseNumber1});
            expect.soft(body).toContain(caseNumber2);
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
