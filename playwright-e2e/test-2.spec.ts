import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  
  await page.getByLabel('Email address').click();
  await page.getByLabel('Email address').fill('local-authority-swansea-0001@maildrop.cc');
  await page.getByLabel('Password').click();
  await page.getByLabel('Password').press('CapsLock');
  await page.getByLabel('Password').fill('Password1234');
  await page.getByRole('button', { name: 'Sign in' }).click();
  await page.locator('div').filter({ hasText: 'Loading' }).nth(1).click();
  await page.getByRole('link', { name: 'Create case' }).click();
  await page.getByLabel('Jurisdiction').selectOption('PUBLICLAW');
  await page.getByLabel('Case type').selectOption('CARE_SUPERVISION_EPO');
  await page.getByRole('button', { name: 'Start' }).click();
  await page.getByLabel('Case name').click();
  await page.getByLabel('Case name').press('CapsLock');
  await page.getByLabel('Case name').fill('Me2');
  await page.getByRole('button', { name: 'Submit' }).click();
  await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1706878896239450');
  await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1706878896239450#Summary');
  await page.getByRole('heading', { name: 'Add application details' }).click();
  await page.getByRole('link', { name: 'Allocation proposal' }).click();
  await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1706878896239450/trigger/otherProposal/otherProposal1');
  await page.getByRole('group', { name: 'Allocation proposal' }).getByRole('heading').click();
  await page.getByLabel('Circuit Judge', { exact: true }).check();
  await page.getByText('*Give reason (Optional)').click();
  await page.getByLabel('*Give reason (Optional)').click();
  await page.getByLabel('*Give reason (Optional)').fill('test');
  await page.getByRole('button', { name: 'Continue' }).click();
  await page.getByRole('button', { name: 'Save and continue' }).click();
  await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1706878896239450#Summary');
});