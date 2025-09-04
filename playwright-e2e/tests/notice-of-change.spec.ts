import {test} from '../fixtures/create-fixture';
import {createRequire} from 'node:module';

const require = createRequire(import.meta.url);
import {
    newSwanseaLocalAuthorityUserOne,
    FPLSolicitorOrgUser, privateSolicitorOrgUser
} from '../settings/user-credentials';
import {expect} from "@playwright/test";
import thirdPartyCaseData from '../caseData/thirdPartyApplication.json' assert {type: "json"};
import LAc110WithRespondent from '../caseData/LAC110AApplication.json' assert {type: "json"};

import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";

test.describe.only('Notice Of Change', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;


    test('Notice of Change - LA c110A application',
        async ({page, signInPage, noticeOfChange}) => {
            let hypenCase: string;
            caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
            caseName = 'NoC of LA C110a Application ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, LAc110WithRespondent);
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORB]');

            await signInPage.visit();
            await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password);

            await noticeOfChange.clickNoticeOfChange();
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterCaseNumber(caseNumber);
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterClientDetails('Thierry', 'Jordan');
            await noticeOfChange.clickContinue();
            await noticeOfChange.confirmDetails();
            await noticeOfChange.clickSubmit();
            await noticeOfChange.accessTheCase();
            hypenCase = noticeOfChange.hypenateCaseNumber(caseNumber);

            //assert
            await expect(page.getByText(hypenCase)).toBeVisible();
        });

    test('Notice of Change - third party c110A application',
        async ({page, signInPage, noticeOfChange}) => {
            let hypenCase: string;
            caseNumber = await createCase('e2e case', privateSolicitorOrgUser);
            caseName = 'Noc Of 3rd Party C110a Application ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, thirdPartyCaseData);

            await signInPage.visit();
            await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password);

            await noticeOfChange.clickNoticeOfChange();
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterCaseNumber(caseNumber);
            await noticeOfChange.clickContinue();
            await noticeOfChange.enterClientDetails('John', 'Somuy');
            await noticeOfChange.clickContinue();
            await noticeOfChange.confirmDetails();
            await noticeOfChange.clickSubmit();
            await noticeOfChange.accessTheCase();
            hypenCase = noticeOfChange.hypenateCaseNumber(caseNumber);

            //assert
            await expect(page.getByText(hypenCase)).toBeVisible();
        })

})
