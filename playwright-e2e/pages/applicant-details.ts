import {type Page, type Locator, expect} from "@playwright/test";
import { BasePage } from "./base-page";

export class ApplicantDetails extends BasePage{
  //readonly page: Page;
  readonly applicantDetailsHeading: Locator;
  readonly teamManagerName: Locator;
  readonly pbaNumber: Locator;
  readonly customerReference: Locator;
  readonly clientCode: Locator;
  readonly phoneNumber: Locator;
  readonly addNew: Locator;
  readonly colleagueHeading: Locator;
  readonly colleagueRole_SocialWorker: Locator;
  readonly colleagueName: Locator;
  readonly colleagueEmail: Locator;
  readonly colleaguePhoneNumber: Locator;
  readonly caseUpdateNotification_No: Locator;
 // readonly caseNameText: Locator;
  readonly removeColleague: Locator;
  public teamManagerNameString: string;

  public constructor(page: Page) {
    super(page);
    this.applicantDetailsHeading = page.getByRole('heading', { name: 'Applicant details' });
    this.teamManagerName = page.getByLabel('Legal team manager\'s name and');
    this.pbaNumber = page.getByLabel('*PBA number (Optional)');
    this.customerReference = page.getByLabel('Customer reference');
    this.clientCode = page.getByLabel('Client code (Optional)');
    this.phoneNumber = page.getByLabel('*Phone number');
    this.addNew = page.getByRole('button', { name: 'Add new' });
    this.colleagueHeading = page.locator('h2').filter({ hasText: 'Colleague' });
    this.colleagueRole_SocialWorker = page.getByLabel('Social worker');
    this.colleagueName = page.getByLabel('*Full name');
    this.colleagueEmail = page.getByLabel('*Email (Optional)');
    this.colleaguePhoneNumber = page.getByLabel('Phone number (Optional)');
    this.caseUpdateNotification_No = page.getByLabel('No');
    this.removeColleague = page.getByLabel('Remove Colleague');
    this.teamManagerNameString = 'Sarah Johnson';

  }

  async applicantDetailsNeeded() {
    await expect(this.applicantDetailsHeading).toBeVisible();
    await this.teamManagerName.click();
    await this.teamManagerName.fill(this.teamManagerNameString);
    await this.pbaNumber.click();
    await this.pbaNumber.fill('PBA1234567');
    await this.customerReference.click();
    await this.customerReference.fill('1234567');
    await this.clientCode.click();
    await this.clientCode.fill('1234567');
    await this.phoneNumber.click();
    await this.phoneNumber.fill('1234567890');
    await this.clickContinue();
    // clickContinue is required twice
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }

  async colleagueDetailsNeeded(){
   // await expect(this.colleagueHeading).toBeVisible();
    await this.continueButton.click();
    await this.addNew.click();
    await this.colleagueRole_SocialWorker.check();
    await this.colleagueName.click();
    await this.colleagueName.fill('Peter Green');
    await this.colleagueEmail.click();
    await this.colleagueEmail.fill('petergreen@socialworker.com');
    await this.colleaguePhoneNumber.click();
    await this.colleaguePhoneNumber.fill('0123456789');
    await this.caseUpdateNotification_No.check(); //this checks no. Same as above, these radio buttons are not grouped.
    await expect(this.removeColleague).toBeVisible();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }
}
