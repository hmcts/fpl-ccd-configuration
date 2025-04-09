import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ApplicantDetails extends BasePage {
  //readonly page: Page;
  readonly applicantDetailsHeading: Locator;
  readonly groupEmailAddress: Locator;
  readonly pbaNumber: Locator;
  readonly customerReference: Locator;
  readonly nameOfApplicantToSign: Locator;
  readonly clientCode: Locator;
  readonly phoneNumber: Locator;
  readonly country: Locator;
  readonly firstName: Locator;
  readonly lastName: Locator;
  readonly alternativeNumber: Locator;
  readonly directEmailAddress: Locator;
  readonly addNew: Locator;
  readonly colleagueHeading: Locator;
  readonly role: Locator;
  readonly enterRole: Locator;
  readonly continue: Locator;
  readonly saveAndContinue: Locator;
  readonly representingPersonDetails: Locator;
  readonly mainContactDetails: Locator;
  readonly otherContactPerson: Locator;



  public constructor(page: Page) {
    super(page);
    this.applicantDetailsHeading = page.getByRole('heading', { name: 'Applicant details' });
    this.groupEmailAddress = page.getByLabel('Legal team manager\'s name and');
    this.pbaNumber = page.getByLabel('PBA number');
    this.customerReference = page.getByLabel('Customer reference');
    this.nameOfApplicantToSign = page.getByLabel('Name of the person who will');
    this.clientCode = page.getByLabel('Client code (Optional)');
    this.country = page.getByLabel('Country (Optional)');
    this.firstName = page.getByLabel('First name');
    this.lastName = page.getByLabel('Last name');
    this.phoneNumber = page.getByLabel('Phone number', { exact: true });
    this.alternativeNumber = page.getByLabel('Alternative phone number (');
    this.directEmailAddress = page.getByText('Direct email address (');
    this.addNew = page.getByRole('button', { name: 'Add new' });
    this.colleagueHeading = page.locator('h2').filter({ hasText: 'Colleague' });
    this.role = page.getByLabel('Other', { exact: true });
    this.enterRole = page.getByLabel('Enter their role (Optional)');
    this.continue = page.getByRole('button', { name: 'Continue' })
    this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });
    this.representingPersonDetails = page.getByRole('group', { name: 'Details of person you are representing' });
    this.mainContactDetails = page.getByRole('group').locator('#applicantContact_applicantContact');
    this.otherContactPerson = page.locator('#applicantContactOthers_0_0');

  }

  async applicantDetailsNeeded() {
    await expect.soft(this.applicantDetailsHeading).toBeVisible();
    await this.pbaNumber.fill('PBA0082848');
    await this.customerReference.fill('1234567');
    await this.nameOfApplicantToSign.fill('Tom Jones');
    await this.country.fill('United Kingdom');
    await this.clickContinue();
    await this.firstName.fill('Peters');
    await this.lastName.fill('John');
    await this.phoneNumber.fill('0123456789');
    await this.alternativeNumber.fill('123456780');
    await this.directEmailAddress.fill('Me2@mail.com');
    await this.addNew.click();
    await this.page.locator('#applicantContactOthers_0_firstName').fill('Me');
    await this.page.locator('#applicantContactOthers_0_lastName').fill('Two');
    await this.page.getByLabel('Email address', { exact: true }).fill('zee@mail.com');
    await this.role.check();
    await this.enterRole.fill('QA');
    await this.continue.click();
    await this.saveAndContinue.click();
    await expect(this.page.getByText('has been updated with event:')).toBeVisible();
  }

  async solicitorC110AApplicationApplicantDetails(){

      await expect.soft(this.representingPersonDetails).toBeVisible();
      await this.representingPersonDetails.getByLabel('First name').fill('John');
      await this.representingPersonDetails.getByLabel('Last name').fill('Somuy');
      await this.page.getByLabel('Group email address (Optional)').fill('privatesol@gmail.com');
      await this.pbaNumber.fill('PBA1234567');
      await this.customerReference.fill('Customer reference 1000');
      await this.clickContinue();

      await expect.soft(this.page.getByText('People within your organization who need notifications')).toBeVisible();
      await expect.soft(this.page.getByText('HMCTS will contact this person if they have any questions')).toBeVisible();

      await this.mainContactDetails.getByLabel('First name').fill('Maie');
      await this.mainContactDetails.getByLabel('Last name').fill('Nouth');
      await this.mainContactDetails.getByLabel('Phone number', { exact: true }).fill('35346878679876');
      await this.mainContactDetails.getByLabel('Direct email address (').fill('email@email.com');

      await expect.soft(this.page.getByRole('heading', { name: 'Others within your' })).toBeVisible();
      await expect.soft(this.page.getByText('Only people with myHMCTS')).toBeVisible();
      await this.addNew.click();
      await this.otherContactPerson.getByLabel('First name').fill('Johnson');
      await this.otherContactPerson.getByLabel('Last name').fill('Johnson');
      await this.otherContactPerson.getByLabel('Email address').fill('Johnson@hmcts.com');
      await this.otherContactPerson.getByRole('radio', { name: 'Other' }).check();
      await this.otherContactPerson.getByLabel('Enter their role (Optional)').fill('assistant');
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();
  }
}
