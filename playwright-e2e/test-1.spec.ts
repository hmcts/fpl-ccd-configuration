import { test, expect } from '@playwright/test';

test('test', async ({ page }) => {
  await page.goto('https://idam-web-public.aat.platform.hmcts.net/login?client_id=xuiwebapp&redirect_uri=https://manage-case.aat.platform.hmcts.net/oauth2/callback&state=Vy8hEsVIUn5YemkuXqCQkU8OWbcU2-MPbzbb06hR9_o&nonce=YGBHKW0P7yisYcdh-C7itSwARm5OEbQ2Zhbbb9RnIrQ&response_type=code&scope=profile%20openid%20roles%20manage-user%20create-user%20search-user&prompt=');
 
  await page.getByLabel('Case type').selectOption('119: Object');
  await page.getByLabel('Apply filter').click();
  await page.getByLabel('go to case with Case reference:1709-5807-9889-').click();
  await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1709580798890193');
  await page.getByLabel('Next step').selectOption('1: Object');
  await page.getByRole('button', { name: 'Go' }).click();
  await page.getByRole('button', { name: 'Go' }).click();
  await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1709580798890193/trigger/uploadCMO/uploadCMOOrderDraftKindSelection');
  await page.getByLabel('Additional order (PDO ETC)').check();
  await page.getByRole('button', { name: 'Continue' }).click();
  await page.goto('https://manage-case.aat.platform.hmcts.net/cases/case-details/1709580798890193/trigger/uploadCMO/uploadCMOSelectDraftOrderHearing');
  await page.getByLabel('Which hearing does the order').selectOption('2: f2be08a2-4daf-4aa3-b7ba-95843b4bcb88');
  await page.getByRole('button', { name: 'Continue' }).click();
  await page.getByLabel('Order title').click();
  await page.getByLabel('Order title').press('CapsLock');
  await page.getByLabel('Order title').fill('Test');
  await page.getByRole('textbox', { name: 'Upload the order' }).click();
  await page.getByRole('textbox', { name: 'Upload the order' }).setInputFiles('Blank page pdf.docx');
  await page.getByRole('checkbox', { name: 'Yes' }).check();
  await page.getByRole('button', { name: 'Add new' }).nth(1).click();
  await page.locator('#currentHearingOrderDrafts_1_title').click();
  await page.locator('#currentHearingOrderDrafts_1_title').press('CapsLock');
  await page.locator('#currentHearingOrderDrafts_1_title').fill('Test2');
  await page.locator('#currentHearingOrderDrafts_1_order').click();
  await page.locator('#currentHearingOrderDrafts_1_order').setInputFiles('Bundle Documents pdf.docx');
  await page.locator('#currentHearingOrderDrafts_1_documentAcknowledge-ACK_RELATED_TO_CASE').check();
  await page.getByRole('button', { name: 'Continue' }).click();
  await page.getByRole('button', { name: 'Continue' }).click();
  await page.getByRole('heading', { name: 'Upload draft orders' }).click();
  await page.getByRole('heading', { name: 'Check your answers' }).click();
  await page.getByRole('link', { name: 'Blank page pdf.docx' }).click();
  await page.getByRole('button', { name: 'Submit' }).click();
  
});