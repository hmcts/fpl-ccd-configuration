import { test } from '../fixtures/create-fixture';
import {judgeWalesUser, newSwanseaLocalAuthorityUserOne} from '../settings/user-credentials';
import caseData from '../caseData/caseWithHearingDetails.json' assert { type: 'json' };
import { expect } from '@playwright/test';
import {assignAMJudicialRole, createCase, getIdamUserId, updateCase} from "../utils/api-helper";

test.describe('Upload draft orders', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
        expect(caseNumber).toBeDefined();
    });

    test('LA upload CMO draft orders @xbrowser',
        async ({ page, signInPage, uploadDraftOrders }) => {
            casename = 'LA upload CMO draft orders ' + dateTime.slice(0, 10);
            expect(await updateCase(casename, caseNumber, caseData)).toBeTruthy();
            expect(await assignAMJudicialRole(caseNumber,judgeWalesUser)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await uploadDraftOrders.gotoNextStep('Upload draft orders')
            await uploadDraftOrders.uploadCMODraftOrders();

            await uploadDraftOrders.tabNavigation('Draft orders');
            await expect(page.locator('#case-viewer-field-read--hearingOrdersBundlesDrafts')).toContainText('Case management hearing, 3 November 2012');
            await expect(page.getByLabel('Draft orders').getByRole('button')).toContainText('draftOrder.docx');
        });

    test('LA upload Additional Draft Order @xbrowser',
        async ({ page, signInPage, uploadDraftOrders }) => {

            casename = 'LA upload Additional Draft Order ' + dateTime.slice(0, 10);
            expect(await updateCase(casename, caseNumber, caseData)).toBeTruthy();
            expect(await assignAMJudicialRole(caseNumber,judgeWalesUser)).toBeTruthy();
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateToCaseDetails(caseNumber);
            await uploadDraftOrders.gotoNextStep('Upload draft orders')
            await uploadDraftOrders.uploadAdditionalDraftOrders();

            await uploadDraftOrders.tabNavigation('Draft orders');
            //await expect(page.locator('#case-viewer-field-read--hearingOrdersBundlesDrafts')).toContainText('Case management hearing, 3 November 2012');
            await expect(page.getByRole('button', { name: 'draftOrder2.docx' })).toBeVisible();
            await expect(page.getByRole('button', { name: 'draftOrder.docx' })).toBeVisible();
        });
});
