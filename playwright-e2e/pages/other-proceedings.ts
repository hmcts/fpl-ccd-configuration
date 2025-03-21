import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class OtherProceedings extends BasePage {

  readonly otherProceedingsHeading: Locator;
 // readonly areThereAnyPastOrOngoingProccedingsReleventToCase: Locator;
    addProceeding: Locator;
    proceedingTwo: Locator;
    proceedingOne: Locator;

  public constructor(page: Page) {
    super(page);
    this.otherProceedingsHeading = page.locator('#proceedings').getByRole('heading', { name: 'Other proceedings' })
   // this.areThereAnyPastOrOngoingProccedingsReleventToCase = page.getByRole('radio', { name: 'No', exact: true });
    this.addProceeding= page.getByRole('button', { name: 'Add new' });
    this.proceedingOne =  page.locator('#proceedings_0_0');
    this.proceedingTwo = page.locator('#proceedings_0_1');
  }

  // async otherProceedingsSmokeTest() {
  //     await this.page.pause();
  //   await this.otherProceedingsHeading.isVisible();
  //   await this.areThereAnyPastOrOngoingProccedingsReleventToCase.check();
  //   await this.clickContinue();
  //   await expect(this.checkYourAnswersHeader).toBeVisible();
  //   await this.checkYourAnsAndSubmit();
  // }

  async otherProceedings(){
       await expect(this.otherProceedingsHeading).toBeVisible();
      // await expect(page.locator('h1').filter({ hasText: /^Other proceedings$/ })).toBeVisible();
     // await page.getByRole('button', { name: 'Add new' }).click();
      await this.addProceeding.nth(0).click();
      await this.proceedingOne.getByLabel('Ongoing').check();
      await this.proceedingOne.getByRole('textbox', { name: 'Day' }).fill('12');
      await this.proceedingOne.getByRole('textbox', { name: 'Month' }).fill('4');
      await this.proceedingOne.getByRole('textbox', { name: 'Year' }).fill('2024');
      await this.proceedingOne.getByLabel('Case number').fill('125756805769');
      await this.proceedingOne.getByLabel('Orders made').fill('EPO');
      await this.proceedingOne.getByLabel('Judge').fill('District judge Susan');
      await this.proceedingOne.getByLabel('Names of children involved').fill('Baby srah\nMilont ');
      await this.proceedingOne.getByLabel('Name of guardian').fill('Joby marian');
      await this.proceedingOne.getByRole('radio', { name: 'Yes' }).check();
      await this.addProceeding.nth(1).click();

      await this.proceedingTwo.getByRole('group', { name: 'Select status of proceeding Select status of proceeding is required' }).getByLabel('Previous').check();
      await this.proceedingTwo.filter({ hasText: 'Application submission date' }).locator('#started-day').fill('3');
      await this.proceedingTwo.filter({ hasText: 'Application submission date' }).locator('#started-month').fill('4');
      await this.proceedingTwo.filter({ hasText: 'Application submission date' }).locator('#started-year').fill('2010');
      await this.proceedingTwo.getByRole('group', { name: 'Final order date' }).locator('#ended-day').fill('4');
      await  this.proceedingTwo.getByRole('group', { name: 'Final order date' }).locator('#ended-month').fill('6');
      await this.proceedingTwo.getByRole('group', { name: 'Final order date' }).locator('#ended-year').fill('2019');
      await this.proceedingTwo.getByLabel('Case number').fill('45756867989');
      await this.proceedingTwo.getByLabel('Orders made').fill('Care order c12');
      await this.proceedingTwo.getByLabel('Judge').fill('District judge Joe Bloggs');
      await this.proceedingTwo.getByLabel('Names of children involved').fill('Baby Julie\nMilont ');
      await this.proceedingTwo.getByLabel('Name of guardian').fill('Joby Susam');
      await this.proceedingTwo.getByRole('radio', { name: 'No' }).check();
      await this.proceedingTwo.getByRole('textbox', { name: 'Give reason' }).fill('The guardian was moved to aboard');
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();



      // await page.getByRole('textbox', { name: 'Day' }).click();
      // await page
      // await page.getByRole('textbox', { name: 'Day' }).press('Tab');
      // await page.
      // await page.getByRole('textbox', { name: 'Month' }).press('Tab');
      // await page.
      // await page.getByRole('textbox', { name: 'Year' }).press('Tab');
      // await page.
      // await page.getByLabel('Case number').press('Tab');
      // await
      // await page.getByLabel('Orders made').press('Tab');
      // await page.
      // await page.getByLabel('Judge').press('Tab');
      // await page.
      // await page.getByLabel('Names of children involved').press('Tab');
      // await page.
      // await page.getByLabel('Name of guardian').press('Tab');
      // await page.


    //   await page.getByRole('button', { name: 'Add new' }).nth(1).click();
    //   //await page.locator('#proceedings_1_proceedingStatus-Previous').check();
    // //  await page.getByRole('group', { name: 'Select status of proceeding Select status of proceeding is required' }).getByLabel('Previous').check();
    //   await page.locator('#started-day').nth(1).click();
    //   await page.locator('#started-day').nth(1).fill('34');
    //   await page.locator('ccd-write-date-field').filter({ hasText: 'Application submission dateFor example, 31 3 2016 The data entered is not valid' }).locator('#started-month').click();
    //   await page.locator('ccd-write-date-field').filter({ hasText: 'Application submission dateFor example, 31 3 2016 The data entered is not valid' }).locator('#started-day').click();
    //   await page.locator('ccd-write-date-field').filter({ hasText: 'Application submission dateFor example, 31 3 2016 The data entered is not valid' }).locator('#started-day').fill('3');
    //   await page.locator('ccd-write-date-field').filter({ hasText: 'Application submission dateFor example, 31 3 2016 The data entered is not valid' }).locator('#started-day').press('Tab');
    //   await page.locator('ccd-write-date-field').filter({ hasText: 'Application submission dateFor example, 31 3 2016 The data entered is not valid' })
    //   await page.locator('ccd-write-date-field').filter({ hasText: 'Application submission dateFor example, 31 3 2016 The data entered is not valid' }).locator('#started-month').press('Tab');
    //   await page.locator('ccd-write-date-field')
    //   await page.locator('ccd-write-date-field').filter({ hasText: 'Application submission dateFor example, 31 3 2016 The data entered is not valid' }).locator('#started-year').press('Tab');
    //   await page
    //   await page.
    //   await page.getByRole('group', { name: 'Final order date' }).click();
    //   await page.locator('body').press('Tab');
    //   await page.getByRole('group', { name: 'Final order date' }).locator('#ended-year').click();
    //   await page.
    //   await page.locator('#proceedings_1_caseNumber').click();
    //   await page.locator('#proceedings_1_caseNumber')
    //   await page.locator('#proceedings_1_ordersMade').click();
    //   await page.locator('#proceedings_1_ordersMade').fill('Final orders');
    //   await page.locator('#proceedings_1_judge').click();
    //   await page.locator('#proceedings_1_ordersMade').click();
    //   await page.locator('#proceedings_1_ordersMade').fill('Care order');
    //   await page.locator('#proceedings_1_judge').click();
    //   await page.locator('#proceedings_1_judge').fill('Legal adviser Joe bloggs');
    //   await page.locator('#proceedings_1_children').click();
    //   await page.locator('#proceedings_1_children').fill('Baby sharmy');
    //   await page.locator('#proceedings_1_guardian').click();
    //   await page.locator('#proceedings_1_guardian').fill('Marian');
    //   await page.locator('#proceedings_1_sameGuardianNeeded_No').check();
    //   await page.getByRole('textbox', { name: 'Give reason' }).click();
    //   await page.
    //   await page.getByRole('button', { name: 'Continue' }).click();
    //   await page.getByRole('button', { name: 'Save and continue' }).click();
    //   await page.getByText('View application').click();


  }
}
