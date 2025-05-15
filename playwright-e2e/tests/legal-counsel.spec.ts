import { test } from '../fixtures/create-fixture';
import caseWithResSolicitor  from '../caseData/caseWithRespondentSolicitor.json' assert { type: "json" };
import caseWithResSolCounsel from '../caseData/caseWithRespondentSolicitorAndCounsel.json' assert { type: "json" } ;
import caseWithResSolCounselDemo from '../caseData/caseWithRespondentSolicitorAndCounselDemo.json' assert {type: "json"};
import {
    newSwanseaLocalAuthorityUserOne,
    privateSolicitorOrgUser,
    FPLSolicitorOrgUser,
    CTSCTeamLeadUser
} from '../settings/user-credentials';
import { expect } from '@playwright/test';
import {createCase, giveAccessToCase, updateCase} from "../utils/api-helper";
import {urlConfig} from "../settings/urls";

test.describe('Respondent solicitor counsel ', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let caseName: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('Respondent solicitor add counsel @xbrowser',
        async ({page, signInPage, legalCounsel}) => {
            caseName = 'Respondent Solicitor add Counsel ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseWithResSolicitor);
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
            await signInPage.visit();
            await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await legalCounsel.gotoNextStep('Add or remove counsel');
            await legalCounsel.clickContinue();
            await legalCounsel.toAddLegalCounsel();
            await legalCounsel.enterLegalCounselDetails();
            await legalCounsel.clickContinue();
            await legalCounsel.checkYourAnsAndSubmit();
            await legalCounsel.tabNavigation('People in the case');
            await expect(page.locator('#case-viewer-field-read--respondents1')).toContainText('Counsel 1');
            await expect(page.locator('#case-viewer-field-read--respondents1')).toContainText('FPLSolicitorOrg');
            await legalCounsel.clickSignOut();
            await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await expect(page.getByRole('heading', {name: caseName})).toBeVisible();
            await expect(page.locator('h1')).toContainText(caseName);
        });

    test('Respondent solicitor remove counsel',
        async ({page, signInPage, legalCounsel}) => {
            caseName = 'Respondent solicitor remove counsel ' + dateTime.slice(0, 10);
            await updateCase(caseName, caseNumber, caseWithResSolCounsel);
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
            await giveAccessToCase(caseNumber, FPLSolicitorOrgUser, '[BARRISTER]');
            await signInPage.visit();
            await signInPage.login(privateSolicitorOrgUser.email, privateSolicitorOrgUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await legalCounsel.gotoNextStep('Add or remove counsel');
            await legalCounsel.clickContinue();
            await legalCounsel.toRemoveLegalCounsel();
            await legalCounsel.clickContinue();
            await legalCounsel.checkYourAnsAndSubmit();
            await legalCounsel.tabNavigation('People in the case');
            await expect(page.getByText('Counsel', {exact: true})).toBeHidden;
            await legalCounsel.clickSignOut();
            await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await expect(page.getByRole('heading', {name: caseName})).toBeHidden;
        });

    test('Legal counsel removed when respondent representation removed',
        async ({page, signInPage, legalCounsel}) => {
            caseName = 'Respondent representative removed ' + dateTime.slice(0, 10);
            if (urlConfig.env== 'demo'){
                await updateCase(caseName, caseNumber, caseWithResSolCounselDemo);
            }
            else{
                await updateCase(caseName, caseNumber, caseWithResSolCounsel);
            }
            await giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
            await giveAccessToCase(caseNumber, FPLSolicitorOrgUser, '[BARRISTER]');
            await signInPage.visit();
            await signInPage.login(CTSCTeamLeadUser.email, CTSCTeamLeadUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await legalCounsel.gotoNextStep('Respondents');
            await legalCounsel.removeRepresentative();
            await legalCounsel.clickContinue();
            await legalCounsel.checkYourAnsAndSubmit();
            await legalCounsel.tabNavigation('People in the case');
            await expect(page.getByRole('row', {
                name: 'Do they have legal representation? No',
                exact: true
            })).toBeVisible;
            await legalCounsel.clickSignOut();
            await signInPage.login(FPLSolicitorOrgUser.email, FPLSolicitorOrgUser.password)
            await signInPage.navigateTOCaseDetails(caseNumber);
            await expect(page.getByRole('heading', {name: caseName})).toBeHidden;
        });

});
