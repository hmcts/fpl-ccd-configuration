import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class QueryManagement extends BasePage {
    newQuery :Locator;
    private querySubjectText: Locator;
    queryDetailText: Locator;
    relatedToHearingRadio: Locator;
    newDocument: Locator;



  public constructor(page: Page) {
    super(page);
     this.newQuery = page.getByLabel('Raise a new query');
     this.querySubjectText = page.getByLabel('Query subject');
     this.queryDetailText = page.getByLabel('Query detail');
     this.relatedToHearingRadio = page.getByRole('group', {name:'Is the query hearing related?'});
     this.newDocument = page.getByRole('button', { name: 'Add new' });


  }

  async selectNewQuery(){
      await this.newQuery.click();
  }
async enterQueryDetails(){
    await this.querySubjectText.fill('Birth certificate format');

     await expect.soft(this.page.getByText('The subject should be a')).toBeVisible();
     await expect.soft(this.page.getByLabel('Query detail')).toBeVisible();
    await this.queryDetailText.fill('Have birth certificate issued in aboard');
    await this.relatedToHearingRadio.getByLabel('Yes').click();
    await this.enterDate(new Date(new Date().setFullYear(new Date().getFullYear() + 1)));
    await this.newDocument.click();
   // await page.getByLabel('Yes').check();
   //  await page.getByLabel('Day').click();
   //  await page.getByLabel('Day').fill('12');
   //  await page.getByLabel('Month').click();
   //  await page.getByLabel('Month').fill('4');
   //  await page.locator('[id="formControlName\\ \\+\\ \\\'-year\\\'"]').click();
   //  await page.locator('[id="formControlName\\ \\+\\ \\\'-year\\\'"]').fill('2025');
   //await page.getByRole('button', { name: 'Add new' }).click();
    //await page.getByLabel('', { exact: true }).click();
    await this.page.getByLabel('', { exact: true }).setInputFiles(config.testPdfFile2);
    await this.waitForAllUploadsToBeCompleted();
}
}
