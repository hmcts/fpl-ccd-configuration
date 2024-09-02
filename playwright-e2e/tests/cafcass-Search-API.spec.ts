import {expect, test} from "../fixtures/fixtures";
import {createCase, getAccessToken, updateCase,cafcassAPICaseSearch} from "../utils/api-helper";
import {cafcassAPIUser, newSwanseaLocalAuthorityUserOne, systemUpdateUser} from "../settings/user-credentials";
import Ajv from 'ajv';
import cafcassAPISearchSchema from '../caseData/cafcassAPICaseSchema.json' assert { type: 'json' };
import returnedCase from '../caseData/caseInReturnState.json' assert { type: 'json' };
import submitCase from '../caseData/mandatorySubmissionFields.json' assert { type: 'json' };
import cafcassCase from '../caseData/caseCaffcassAPISearchAllFieldData.json' assert { type:'json'};
import returnCase from '../caseData/caseInReturnState.json' assert {type:'json'};
import {urlConfig} from "../settings/urls";
import {parseJsonNumber} from "ajv/dist/runtime/parseJson";

test.describe('CafcassAPI @cafcassAPI', () => {
    const startTime = new Date().toISOString();
    const ajv = new Ajv();
    let accessToken ;
    test.beforeAll(async()=>{
        accessToken = await getAccessToken({user: cafcassAPIUser});
       // accessToken.data.access_token
        // set up the test data
        // return case, case with minimum data , case with all data
        let caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('submit case' + startTime.slice(0, 10), caseNumber, submitCase);
        let caseNumber2 = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('submit case' + startTime.slice(0, 10), caseNumber2, submitCase);


    });
    test('@new Cafcass user search cases for given time frame',
        async ({request}) => {

          let endTime = new Date().toISOString();
          let response= await cafcassAPICaseSearch(request,accessToken.data.access_token,startTime,endTime);
          //assertion
            await expect(await response.ok()).toBeTruthy();
            await expect(await response.status()).toBe(200);
            let body = await response.json();
            console.log("\ntotal:" + body.total);
            const validate= ajv.validate(cafcassAPISearchSchema,body);
           expect( body.total).toBeGreaterThan(2);
         // validate the case id
           // expect(body.cases).toHaveProperty({},{})
        })

test('Auth Error- user without cafcass role' ,async()=>{

    accessToken = await getAccessToken({user: systemUpdateUser});
    let startTime= new Date().toISOString();
    let endTime = new Date().setMinutes(new Date().getMinutes()+10);
    let response= await cafcassAPICaseSearch(request,accessToken.data.access_token,startTime,endTime);
    //assertion
    await expect(await response.ok()).toBeTruthy();
    await expect(await response.status()).tobe(403);

    })
    test('Search cases with invalid parameter',async()=>{
        let endTime = new Date().setMinutes(new Date().getMinutes()+30);
        let response= await cafcassAPICaseSearch(request,accessToken.data.access_token,startTime,endTime);
        //assertion
        await expect(await response.ok()).toBeTruthy();
        await expect(await response.status()).tobe(400);

    })

})
