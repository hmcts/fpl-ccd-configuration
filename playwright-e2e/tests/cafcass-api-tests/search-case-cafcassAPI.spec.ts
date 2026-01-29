import {expect, test} from "../../fixtures/fixtures";
import {createCase, updateCase} from "../../utils/api-helper";
import {authToken, newSwanseaLocalAuthorityUserOne} from "../../settings/user-credentials";
import cafcassAPISearchSchema from '../../caseData/cafcassAPITest/cafcassAPICaseSchema.json' assert {type: 'json'};
import submitCase from '../../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import cafcassCase from '../../caseData/cafcassAPITest/caseCaffcassAPISearchAllFieldData.json' assert {type: 'json'};
import {cafcassAPICaseSearch, getDateTimePram, validateCaseItemWithSchema} from "../../utils/cafcass-api-test-helper";
import caseData from "../../caseData/mandatorySubmissionFieldsWithoutAdditionalApp.json";
import {testConfig} from "../../settings/test-config";

test.describe('CafcassAPI search cases', () => {
    let startTime = new Date().toISOString();
    let intervalEndTime: string;
    let intervalStartTime: string;
    let currentTime: Date;
    let caseNumber1: string
    let caseNumber2: string;


    test.beforeAll(async () => {
        test.setTimeout(90000);
        currentTime = new Date();

        // set up the test data
        // return case, case with minimum data , case with all data
        caseNumber1 = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber1).toBeDefined();
        expect(await updateCase('Cafcass search case1 ' + startTime.slice(0, 10), caseNumber1, submitCase)).toBeTruthy();
        caseNumber2 = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber2).toBeDefined();
        expect(await updateCase('Cafcass search case2 ' + startTime.slice(0, 10), caseNumber2, cafcassCase)).toBeTruthy();

    });
    test(' Cafcass user search cases for given time frame',
        async ({request, page}) => {

            await page.waitForTimeout(testConfig.TEST_DATA_SETUP_TIMEOUT_MS) // wait for the test data to be set up
            intervalStartTime = getDateTimePram(currentTime, -2); // getting time 2 mins before the current time for start time
            intervalEndTime = getDateTimePram(currentTime, 10); // getting time 10 mins after the current time for end time

            let response = await cafcassAPICaseSearch(request, authToken.cafcassAuth, intervalStartTime, intervalEndTime);

            //assert the response
            expect.soft(response.status()).toBe(200);

            if (response.status() == 200) {

                const body = await response.json();
                expect.soft(await body.total).toBeGreaterThanOrEqual(2);

                //assert case id in the response
                const allCaseIds = body.cases.map((a: { id: number }) => String(a.id));
                expect.soft(allCaseIds).toContain(String(caseNumber1));
                expect.soft(allCaseIds).toContain(String(caseNumber2));

//validate each case item with schema
                const caseItems = body.cases.filter((c: {
                    id: number;
                }) => [Number(caseNumber1), Number(caseNumber2)].includes(c.id));
                expect.soft(caseItems.length).toBe(2);

                for (const item of caseItems) {
                    const caseId: any = item.id
                    const valid = await validateCaseItemWithSchema(cafcassAPISearchSchema, item);
                    expect.soft(valid).toBe(true);
                    if (!valid) {
                        console.log(`Case reference ${caseId} failed schema validation.`);
                    }
                }

            } else {
                console.error(`Unexpected response status: ${response.status()}`);
            }

        })
    test('search case by unauthorised user', async ({request}) => {

        let response = await cafcassAPICaseSearch(request, authToken.systemAuth, intervalStartTime, intervalEndTime);
        //assertion
        expect(response.status()).toBe(403);
        expect(response.statusText()).toBe('Forbidden');
    })
    test('Search cases with invalid search time interval', async ({request}) => {
        let endTime = new Date().setMinutes(new Date().getMinutes() + 30);
        let response = await cafcassAPICaseSearch(request, authToken.cafcassAuth, intervalStartTime, endTime.toString());
        //assertion
        expect(response.status()).toEqual(400);
        expect(response.statusText()).toEqual('Bad Request');

    })

})
