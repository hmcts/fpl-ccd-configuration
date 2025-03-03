import { test } from '../fixtures/create-fixture';
import { newSwanseaLocalAuthorityUserOne } from '../settings/user-credentials';
import caseData from '../caseData/caseWithHearingDetails.json' assert { type: 'json' };
import { expect } from '@playwright/test';
import {createCase, updateCase} from "../utils/api-helper";

test.describe('Upload draft orders', () => {
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
        caseNumber = await createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('LA upload CMO draft orders',
        async ({ page, signInPage, uploadDraftOrders }) => {
            casename = 'LA upload CMO draft orders ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await uploadDraftOrders.gotoNextStep('Upload draft orders')
            await uploadDraftOrders.uploadCMODraftOrders();

            await uploadDraftOrders.tabNavigation('Draft orders');
            await expect(page.locator('#case-viewer-field-read--hearingOrdersBundlesDrafts')).toContainText('Case management hearing, 3 November 2012');
            await expect(page.getByLabel('Draft orders').getByRole('link')).toContainText('draftOrder1.docx');
        });

    test('LA upload Additional Draft Order',
        async ({ page, signInPage, uploadDraftOrders }) => {

            casename = 'LA upload Additional Draft Order ' + dateTime.slice(0, 10);
            await updateCase(casename, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await uploadDraftOrders.gotoNextStep('Upload draft orders')
            await uploadDraftOrders.uploadAdditionalDraftOrders();

            await uploadDraftOrders.tabNavigation('Draft orders');
            //await expect(page.locator('#case-viewer-field-read--hearingOrdersBundlesDrafts')).toContainText('Case management hearing, 3 November 2012');
            await expect(page.getByRole('link', { name: 'draftOrder2.docx' })).toBeVisible();
            await expect(page.getByRole('link', { name: 'draftOrder1.docx' })).toBeVisible();
        });
});
