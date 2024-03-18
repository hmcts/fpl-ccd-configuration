import { test } from '../fixtures/create-fixture';
import { newSwanseaLocalAuthorityUserOne } from '../settings/user-credentials';
import { Apihelp } from '../utils/api-helper';
import caseData from '../caseData/caseWithHearingDetails.json';
import { expect } from '@playwright/test';

test.describe('Upload draft orders', () => {
    let apiDataSetup = new Apihelp();
    const dateTime = new Date().toISOString();
    let caseNumber: string;
    let casename: string;
    test.beforeEach(async () => {
        caseNumber = await apiDataSetup.createCase('e2e case', newSwanseaLocalAuthorityUserOne);
    });

    test('LA upload CMO draft orders',
        async ({ page, signInPage, uploadDraftOrders }) => {
            casename = 'LA upload CMO draft orders ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await uploadDraftOrders.gotoNextStep('Upload draft orders')
            await uploadDraftOrders.uploadCMODraftOrders();

            await uploadDraftOrders.tabNavigation('Draft orders');
            await expect(page.locator('#case-viewer-field-read--hearingOrdersBundlesDrafts')).toContainText('Case management hearing, 3 November 2012');
            await expect(page.getByLabel('Draft orders').getByRole('link')).toContainText('draftOrder.docx');
        });

    test('LA upload Additional Draft Order',
        async ({ page, signInPage, uploadDraftOrders }) => {

            casename = 'LA upload Additional Draft Order ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await uploadDraftOrders.gotoNextStep('Upload draft orders')
            await uploadDraftOrders.uploadAdditionalDraftOrders();

            await uploadDraftOrders.tabNavigation('Draft orders');
            await expect(page.locator('#case-viewer-field-read--hearingOrdersBundlesDrafts')).toContainText('Case management hearing, 3 November 2012');
            await expect(page.getByRole('link', { name: 'draftOrder2.docx' })).toBeVisible();
            await expect(page.getByRole('link', { name: 'draftOrder.docx' })).toBeVisible();
        });
});