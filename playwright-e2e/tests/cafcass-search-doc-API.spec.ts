import {expect, test} from "../fixtures/fixtures";
import {
    cafcassAPICaseDocSearch,
    cafcassAPICaseSearch, cafcassAPIDocSearch,
    createCase,
    getAccessToken,
    updateCase
} from "../utils/api-helper";
import {
    authToken,
    cafcassAPIUser,
    newSwanseaLocalAuthorityUserOne,
    systemUpdateUser
} from "../settings/user-credentials";
import Ajv from 'ajv';
import cafcassAPISearchSchema from '../caseData/cafcassAPICaseSchema.json' assert {type: 'json'};
import submitCase from '../caseData/mandatorySubmissionFields.json' assert {type: 'json'};
import cafcassCase from '../caseData/caseCaffcassAPISearchAllFieldData.json' assert {type: 'json'};
import * as fs from "fs";

test.describe('@new CafcassAPI Document Search @cafcassAPI', () => {
    let startTime = new Date().toISOString();
    let intervalEndTime: string;
    let intervalStartTime: string;
    const ajv = new Ajv();
    let caseNumber: string
    let caseNumber2: string;

    test.beforeAll(async () => {

    });
    test(' @doc Cafcass user search a valid case document',
        async ({request}) => {
            let response = await cafcassAPIDocSearch(request, authToken.cafcassAuth);

            //assert the response
            expect(response.status()).toBe(200);
            expect(response.headers()["content-type"]).toContain('application/octet-stream');
            let body = await response.body();
            // save the file and assert the content

        })

    test('cafcass user search case document not authorised to cafcass', async ({request}) => {
        let endTime = new Date().setMinutes(new Date().getMinutes() + 30);
        let response = await cafcassAPIDocSearch(request, authToken.cafcassAuth);
        //assertion
        expect(response.status()).toEqual(403);
        expect(response.statusText()).toEqual('Bad Request');

    })
    test('cafcass user search case document not exist', async ({request}) => {
        let endTime = new Date().setMinutes(new Date().getMinutes() + 30);
        let response = await cafcassAPICaseSearch(request, cafcassAPIUser, intervalStartTime, endTime.toString());
        //assertion
        expect(response.status()).toEqual(404);
        expect(response.statusText()).toEqual('Bad Request');

    })

})
