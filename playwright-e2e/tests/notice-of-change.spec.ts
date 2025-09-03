import { test } from '../fixtures/create-fixture';
import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
import {
    newSwanseaLocalAuthorityUserOne,
    FPLSolicitorOrgUser, privateSolicitorOrgUser
} from '../settings/user-credentials';
import { expect } from "@playwright/test";
import thirdPartyCaseData from '../caseData/thirdPartyApplication.json' assert { type: "json" };
import LAc110WithRespondent from '../caseData/LAC110AApplication.json' assert { type: "json" };

import { createCase, giveAccessToCase, updateCase } from "../utils/api-helper";

test.describe('Notice Of Change', () => {
  const dateTime = new Date().toISOString();
  let caseNumber: string;
  let caseName: string;

  test.beforeEach(async () => {
    caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
  });


    test.only('Notice of Change - LA c110A application',
        async ({ page, signInPage, noticeOfChange }) => {
            let hypenCase :string;

            caseName = 'NoC of LA C110a Application ' + dateTime.slice(0, 10);
            console.log('caseName:  ' + caseName);
            await updateCase(caseName, caseNumber, LAc110WithRespondent);
            await console.log('caseNumber:  ' + caseNumber);
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORB]');

            await signInPage.visit();
            await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password);
            await noticeOfChange.page.pause()

            await noticeOfChange.noticeOfChange(caseNumber,'Thierry','Jordan');
            await noticeOfChange.accessTheCase();
            hypenCase = noticeOfChange.hypenateCaseNumber(caseNumber);

            //assert
            await expect(page.getByText(hypenCase)).toBeVisible();
        });

    test('Notice of Change - third party c110A application',
        async ({ page, signInPage, noticeOfChange }) => {
            let hypenCase :string;

            caseName = 'Noc Of 3rd Party C110a Application ' + dateTime.slice(0, 10);
            console.log('caseName:  ' + caseName);
            await updateCase(caseName, caseNumber, thirdPartyCaseData);
            await console.log('caseNumber:  ' + caseNumber);


            await page.pause();
            await signInPage.visit();
            await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password);

            await noticeOfChange.noticeOfChange(caseNumber,'John','Somuy');
            await noticeOfChange.accessTheCase();
            hypenCase = noticeOfChange.hypenateCaseNumber(caseNumber);

            //assert
            await expect(page.getByText(hypenCase)).toBeVisible();
        });

})
