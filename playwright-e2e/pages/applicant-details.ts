import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ApplicantDetails extends BasePage{
  readonly page: Page;
  readonly ApplicantDetailsHeading: Locator;
  readonly TeamManagerName: Locator;
  readonly PBA_Number: Locator;
  readonly CustomerReference: Locator;
  readonly ClientCode: Locator;
  readonly PhoneNumber: Locator;
  //readonly Continue: Locator; 
  readonly AddNew: Locator; 
  readonly ColleagueHeading: Locator;
  readonly ColleagueRole_SocialWorker: Locator;
  readonly ColleagueName: Locator;
  readonly ColleagueEmail: Locator;
  readonly ColleaguePhoneNumber: Locator;
  readonly CaseUpdateNotification_No: Locator;
  readonly CheckAnswersHeading: Locator;
  //readonly SaveAndContinue: Locator;

  public constructor(page: Page) {
    super(page);
    this.ApplicantDetailsHeading = page.getByRole('heading', { name: 'Applicant details' });
    this.TeamManagerName = page.getByLabel('Legal team manager\'s name and');
    this.PBA_Number = page.getByLabel('*PBA number (Optional)');
    this.CustomerReference = page.getByLabel('Customer reference');
    this.ClientCode = page.getByLabel('Client code (Optional)');
    this.PhoneNumber = page.getByLabel('*Phone number');
    this.AddNew = page.getByRole('button', { name: 'Add new' });
    this.ColleagueHeading = page.locator('h2').filter({ hasText: 'Colleague' });
    this.ColleagueRole_SocialWorker = page.getByLabel('Social worker');
    this.ColleagueName = page.getByLabel('*Full name');
    this.ColleagueEmail = page.getByLabel('*Email (Optional)');
    this.ColleaguePhoneNumber = page.getByLabel('Phone number (Optional)');
    this.CaseUpdateNotification_No = page.getByLabel('No');
  }

  async applicantDetailsNeeded() {
    await this.ApplicantDetailsHeading.isVisible;
    await this.TeamManagerName.click();
    await this.TeamManagerName.fill('Sarah Johnson');
    await this.PBA_Number.click();
    await this.PBA_Number.fill('PBA1234567');
    await this.CustomerReference.click();
    await this.CustomerReference.fill('1234567');
    await this.ClientCode.click();
    await this.ClientCode.fill('1234567');
    await this.PhoneNumber.click();
    await this.PhoneNumber.fill('1234567890');
    await this.clickContinue();
  }

  async colleagueDetailsNeeded(){
    await this.ColleagueHeading.isVisible;
    await this.AddNew.click();
    await this.ColleagueRole_SocialWorker.check();
    await this.ColleagueName.click();
    await this.ColleagueName.fill('Peter Green');
    await this.ColleagueEmail.click();
    await this.ColleagueEmail.fill('petergreen@socialworker.com');
    await this.ColleaguePhoneNumber.click();
    await this.ColleaguePhoneNumber.fill('0123456789');
    await this.CaseUpdateNotification_No.check(); //this checks no. Same as above, these radio buttons are not grouped.
    await this.clickContinue();
    await this.checkYourAnswersHeader.isVisible;
    await this.checkYourAnsAndSubmit();
  }
  
}
