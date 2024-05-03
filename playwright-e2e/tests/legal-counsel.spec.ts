import { test } from '../fixtures/create-fixture';
import { Apihelp } from '../utils/api-helper';
import caseWithResSolicitor from '../caseData/caseWithRespondentSolicitor.json';
import caseWithResSolCounsel from '../caseData/caseWithRespondentSolicitorAndCounsel.json';
import {
    newSwanseaLocalAuthorityUserOne,
    privateSolicitorOrgUser,
    FPLSolicitorOrgUser,
    CTSCTeamLeadUser
} from '../settings/user-credentials';
import { expect } from '@playwright/test';

test.describe('Respondent solicitor counsel ', () => {
    let apiDataSetup = new Apihelp();
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
        caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('Respondent solicitor add counsel',
        async ({page, signInPage, legalCounsel}) => {
            casename = 'Respondent Solicitor add Counsel ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithResSolicitor);
            await apiDataSetup.giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
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
            await expect(page.getByRole('heading', {name: casename})).toBeVisible();
            await expect(page.locator('h1')).toContainText(casename);
        });

    test('Respondent solicitor remove counsel',
        async ({page, signInPage, legalCounsel}) => {
            casename = 'Respondent solicitor remove counsel ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithResSolCounsel);
            await apiDataSetup.giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
            await apiDataSetup.giveAccessToCase(caseNumber, FPLSolicitorOrgUser, '[BARRISTER]');
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
            await expect(page.getByRole('heading', {name: casename})).toBeHidden;
        });

    test('Legal counsel removed when respondent representation removed',
        async ({page, signInPage, legalCounsel}) => {
            casename = 'Respondent representative removed ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseWithResSolCounsel);
            await apiDataSetup.giveAccessToCase(caseNumber, privateSolicitorOrgUser, '[SOLICITORA]');
            await apiDataSetup.giveAccessToCase(caseNumber, FPLSolicitorOrgUser, '[BARRISTER]');
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
            await expect(page.getByRole('heading', {name: casename})).toBeHidden;
        });

});
