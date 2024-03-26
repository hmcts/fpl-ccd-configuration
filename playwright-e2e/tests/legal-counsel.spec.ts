import { test} from '../fixtures/create-fixture';
import {Apihelp} from '../utils/api-helper';
//import {urlConfig} from "../settings/urls";
import caseData from '../caseData/mandatorySubmissionFields.json';
import caseDataJudgeMessage from '../caseData/caseWithJudgeMessage.json';
import caseDataCloseMessage from '../caseData/caseWithJudicialMessageReply.json';
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json';
import { newSwanseaLocalAuthorityUserOne,CTSCUser,privateSolicitorOrgUser} from '../settings/user-credentials';
import { expect } from '@playwright/test';


test.describe('Respondent solicitor legal counsel ',
    () => {
        let apiDataSetup = new Apihelp();
        const dateTime = new Date().toISOString();
        let caseNumber: string;
        let casename: string;
        test.beforeEach(async () => {
            caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        });

        test('Respondent solicitor add legal council',
            async ({page, signInPage, legalCounsel}) => {
                casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
                 await apiDataSetup.updateCase(casename, caseNumber, caseWithResSolicitor);
                 await apiDataSetup.giveAccessToCase(caseNumber);
                 console.log ("case" + caseNumber);
                 await signInPage.visit();
                 await signInPage.login(privateSolicitorOrgUser.email,privateSolicitorOrgUser.password)
                 await signInPage.navigateTOCaseDetails('1711405300614392');
                 await legalCounsel.gotoNextStep('Add or remove counsel');
                 await legalCounsel.clickContinue();
                 await legalCounsel.addLegalCounsel();
                 await legalCounsel.enterLegalCounselDetails();
                 await legalCounsel.clickContinue();
                 await legalCounsel.checkYourAnsAndSubmit();
                 await legalCounsel.tabNavigation('People in the case');
                 await expect(page.locator('#case-viewer-field-read--respondents1')).toContainText('Counsel 1');
                 await expect(page.locator('#case-viewer-field-read--respondents1')).toContainText('FPLSolicitorOrg');
        
           
                });
           test('Respondent solicitor remove legal council',
            async ({page, signInPage, legalCounsel}) => {
                casename = 'CTSC message Judge ' + dateTime.slice(0, 10);
                // await apiDataSetup.updateCase(casename, caseNumber, caseWithResSolicitor);
                 await apiDataSetup.giveAccessToCase('1711405300614392');
                 console.log ("case" + caseNumber);
                 await signInPage.visit();
                 await signInPage.login(privateSolicitorOrgUser.email,privateSolicitorOrgUser.password)
                 await signInPage.navigateTOCaseDetails('1711405300614392');
                 await legalCounsel.gotoNextStep('Add or remove counsel');
                 await legalCounsel.clickContinue();
                 await legalCounsel.addLegalCounsel();
                 await legalCounsel.enterLegalCounselDetails();
                 await legalCounsel.clickContinue();
                 await legalCounsel.checkYourAnsAndSubmit();
                 await legalCounsel.tabNavigation('People in the case');
                 await expect(page.locator('#case-viewer-field-read--respondents1')).toContainText('Counsel 1');
                 await expect(page.locator('#case-viewer-field-read--respondents1')).toContainText('FPLSolicitorOrg');
        
           });
    });
