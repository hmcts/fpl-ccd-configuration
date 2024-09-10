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
import { AxiosResponse } from "axios";
import {ifError} from "node:assert";

test.describe('CafcassAPI @cafcassAPI', () => {
    const startTime = new Date().toISOString();
    const ajv = new Ajv();
    let response: AxiosResponse<any, any> | undefined ;
    test.beforeAll(async()=>{

        let accessToken = await getAccessToken({user: cafcassAPIUser});


        // set up the test data
        // return case, case with minimum data , case with all data
        let caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('submit case' + startTime.slice(0, 10), caseNumber, submitCase);
        let caseNumber2 = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        await updateCase('submit case' + startTime.slice(0, 10), caseNumber2, cafcassCase);


    });
    test('@new Cafcass user search cases for given time frame',
        async ({request}) => {
        let s= new Date();
          let addTime = s.setMinutes(s.getMinutes()+14);
          let endTime = new Date(addTime).toISOString();
            let response= await cafcassAPICaseSearch(request, cafcassAPIUser,startTime,endTime);
          //assertion
            await expect(await response.ok()).toBeTruthy();
            await expect(await response.status()).toBe(200);
            let body = await response.json();
            console.log("\ntotal:" + body.total);
            const validated= ajv.validate(cafcassAPISearchSchema,body);
          expect(validated).toBe(true);
            // expect(body).toEqual(
            //     expect.objectContaining({
            //         type: 'cases',
            //         options: expect.arrayContaining([{ key:'cases', value:''  }]),
            //     })
            // )
            // expect( body.total).toEqual(2);
           console.log('dd' + body.toString());
         // validate the case id
          //  expect(body.cases).toHaveProperty({},{})
        })

test('Auth Error- user without cafcass role' ,async({request})=>{

    let startTime= new Date();
    let endTime = startTime.setMinutes(startTime.getMinutes()+10);
    let response= await cafcassAPICaseSearch(request,systemUpdateUser,startTime.toString(),endTime.toString());
    //assertion
    await expect(await response.ok()).toBeTruthy();
    await expect(await response.status()).toEqual(403);

    })
    test('Search cases with invalid parameter',async({request})=>{
        let endTime = new Date().setMinutes(new Date().getMinutes()+30);
        let response= await cafcassAPICaseSearch(request,cafcassAPIUser,startTime,endTime.toString());
        //assertion
        await expect(await response.ok()).toBeTruthy();
        await expect(await response.status()).toEqual(400);

    })

})
