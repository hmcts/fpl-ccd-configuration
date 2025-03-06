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
  }
}
