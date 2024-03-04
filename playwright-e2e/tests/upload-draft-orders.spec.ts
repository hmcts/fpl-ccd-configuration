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

    test('LA Upload draft orders',
        async ({ page, signInPage, uploadDraftOrders }) => {
            casename = 'LA Upload draft orders ' + dateTime.slice(0, 10);
            await apiDataSetup.updateCase(casename, caseNumber, caseData);
            await signInPage.visit();
            await signInPage.login(newSwanseaLocalAuthorityUserOne.email, newSwanseaLocalAuthorityUserOne.password);
            await signInPage.navigateTOCaseDetails(caseNumber);
            await uploadDraftOrders.gotoNextStep('Upload draft orders')
            await uploadDraftOrders.uploadDraftOrders();

            await uploadDraftOrders.tabNavigation('Draft orders');
            await expect(page.locator('#case-viewer-field-read--hearingOrdersBundlesDrafts')).toContainText('Case management hearing, 3 November 2012');
            await expect(page.getByLabel('Draft orders').getByRole('link')).toContainText('draftOrder.docx');
        });

});