import { expect, type Locator, type Page } from "@playwright/test";
import { BasePage } from "./base-page";
import config from "../settings/test-docs/config";

export class Extend26WeekTimeline extends BasePage {



    public constructor(page: Page) {
        super(page);

    }


    async isExtensionApprovedAtHearing(YesNo='Yes'){
        await this.page.getByRole('group', { name: 'Was this timeline extension approved at a hearing?' }).getByLabel(`${YesNo}`).click();
    }
    async selectHearing(hearingDetails:string){
        await this.page.getByLabel('Which hearing was this extension approved at?').selectOption(hearingDetails);
    }
async isAboutAllChildren(YesNo='Yes'){
    await this.page.getByRole('group', { name: 'Is the timeline extending for all the children?' }).getByLabel(`${YesNo}`).click();
}
async sameExtensionDateForAllChildren(YesNo='Yes'){
    await this.page.getByRole('group', { name: 'Are all the selected children’s timelines being extended by the same amount of time, and for the same reason?' }).getByLabel(`${YesNo}`).click();
}

async enterExtendsionDetails(){
    // await this.page.getByRole('group', { name: 'Child 1' }).getByLabel('Extend by 8 Weeks').check();
    // await this.page.getByRole('group', { name: 'Child 1' }).getByLabel('Timetable for child').check();
    // await this.page.getByRole('group', { name: 'Child 2' }).getByLabel('Extend by 8 Weeks').check();
    // await page.getByRole('group', { name: 'Child 2' }).getByLabel('Timetable for child').check();
    // await page.getByRole('group', { name: 'Child 2' }).getByLabel('Delay in case/impact on child').check();
    // await page.getByRole('group', { name: 'Child 3' }).getByLabel('Extend by 8 Weeks').check();
    // await page.getByRole('group', { name: 'Child 3' }).getByLabel('Enter a different date').check();
    // await page.getByRole('textbox', { name: 'Day' }).click();
    // await page.getByRole('textbox', { name: 'Day' }).fill('3');
    // await page.getByRole('textbox', { name: 'Day' }).press('Tab');
    // await page.getByRole('textbox', { name: 'Month' }).fill('5');
    // await page.getByRole('textbox', { name: 'Month' }).press('Tab');
    // await page.getByRole('textbox', { name: 'Year' }).fill('2025');
    // await page.getByText('Extend 26-week timelineCTSC').click();
    // await page.locator('body').press('Tab');
    // await page.getByRole('group', { name: 'Child 3' }).getByLabel('Delay in case/impact on child').check();
    // await page.getByRole('group', { name: 'Child 4' }).getByLabel('Enter a different date').check();
    // await page.getByRole('group', { name: 'Child 4' }).getByLabel('Day').click();
    // await page.getByRole('group', { name: 'Child 4' }).getByLabel('Day').fill('2');
    // await page.getByRole('group', { name: 'Child 4' }).getByLabel('Day').press('Tab');
    // await page.getByRole('group', { name: 'Child 4' }).getByLabel('Month').fill('5');
    // await page.getByRole('group', { name: 'Child 4' }).getByLabel('Month').press('Tab');
    // await page.getByRole('group', { name: 'Child 4' }).getByLabel('Year').fill('2025');
    // await page.getByRole('group', { name: 'Child 4' }).getByLabel('International Aspect').check();
    await this.page.getByRole('radio', { name: 'Extend by 8 Weeks' }).check();
    await this.page.getByRole('radio', { name: 'Timetable for proceedings' }).check();
}
   // asyn tempc extend26WeekTimeline(){
   //
   // // await this.page.getByLabel('Next step').selectOption('20: Object');
   //  await page.getByRole('button', { name: 'Go' }).click();
   //  await page.getByRole('radio', { name: 'Yes' }).check();
   //  await page.getByRole('button', { name: 'Continue' }).click();
   //  await page.getByRole('radio', { name: 'No' }).check();
   //  await page.getByRole('textbox', { name: 'Day' }).click();
   //  await page.getByRole('textbox', { name: 'Day' }).fill('5');
   //  await page.getByRole('textbox', { name: 'Month' }).click();
   //  await page.getByRole('textbox', { name: 'Month' }).fill('4');
   //  await page.getByRole('textbox', { name: 'Month' }).press('Tab');
   //  await page.getByRole('textbox', { name: 'Year' }).fill('2025');
   //  await page.getByRole('button', { name: 'Continue' }).click();
   //  await page.getByRole('radio', { name: 'Yes' }).check();
   //  await page.getByRole('button', { name: 'Continue' }).click();
   //  // await expect(page.getByText('Are all the selected children')).toBeVisible();
   //  await page.getByRole('radio', { name: 'Yes' }).check();
   //  await page.getByRole('radio', { name: 'Extend by 8 Weeks' }).check();
   //  // await expect(page.getByRole('group', { name: 'Select reason for extension' }).locator('span')).toBeVisible();
   //  // await expect(page.locator('#childExtensionAll_caseExtensionReasonList')).toContainText('Select reason for extension for:Timetable for proceedingsTimetable for childDelay in case/impact on childInternational Aspect');
   //  await page.getByRole('radio', { name: 'Timetable for child' }).check();
   //  await page.getByRole('button', { name: 'Continue' }).click();
   //  await page.getByRole('button', { name: 'Save and continue' }).click();
   //  await page.locator('button').nth(3).click();
   //  await page.locator('button').nth(2).click();
   //  // await expect(page.getByRole('columnheader', { name: 'Extend 26-week timeline' })).toBeVisible();
   //  await page.getByRole('link', { name: 'Extend 26-week timeline' }).click();
   //  await page.getByRole('link', { name: 'Cancel' }).click();
   //  await page.locator('button').nth(2).click();
   //  // await expect(page.getByText('31 May 2025', { exact: true })).toBeVisible();
   //  await page.getByRole('cell', { name: 'Child katie - 31 May 2025 -' }).click();
   //  await page.locator('ccd-field-read-label').filter({ hasText: 'Child katie - 31 May 2025 -' }).locator('div').click();



}
