import { test} from '@playwright/test';
import {Apihelp} from '../utils/apiFixture';
import fs from 'fs';
import caseData from '../caseData/mandatorySubmissionFields.json';
import axios from 'axios';
import qs from 'qs';


test.describe('send and reply message',()=>{
  let apiDataSetup = new Apihelp();
  test.beforeAll(()  => {

  });

  test('CTSC admin send message to Judge',
    async ({request,page}) => {
    // await  apiDataSetup.createCase('CTSCSendMessageJudge');
   // await apiDataSetup.updateCase(caseData);
   // console.log (await  apiDataSetup.gettestDocDetails());
   let caseNumber : string;
   caseNumber =  await apiDataSetup.createCase('e2e case');
   console.log(caseNumber);
   await apiDataSetup.updateCase('judge test',caseNumber,caseData)
   
    
     

    });

});
