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
    this.pbaNumber = page.locator('#localAuthority_pbaNumberDynamicList');
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

  async fillPbaNumber(PBANumber: string): Promise<void> {
      await this.pbaNumber.selectOption(PBANumber); // PBA0096471
  }

  async fillCustomerReference(text: string): Promise<void> {
      await this.customerReference.fill(text);
  }

  async fillNameOfApplicantToSign(name: string): Promise<void> {
      await this.nameOfApplicantToSign.fill(name);
  }

  async fillCountry(country: string): Promise<void> {
      await this.country.fill(country);
  }

  async fillFirstName(name: string): Promise<void> {
      await this.firstName.fill(name);
  }

  async fillLastName(name: string): Promise<void> {
      await this.lastName.fill(name);
  }

  async fillPhoneNumber(phoneNumber: string): Promise<void> {
      await this.phoneNumber.fill(phoneNumber);
  }

  async fillAlternativeNumber(phoneNumber: string): Promise<void> {
      await this.alternativeNumber.fill(phoneNumber);
  }

  async fillDirectEmailAddress(email: string): Promise<void> {
      await this.directEmailAddress.fill(email);
  }

  async applicantDetailsNeeded(PBANumber: string | ''): Promise<void> {
      await this.fillPbaNumber(PBANumber);
      await this.fillCustomerReference('1234567');
      await this.fillNameOfApplicantToSign('Test');
      await this.fillCountry('United Kingdom');

      await Promise.all([
          this.page.waitForResponse(response =>
              !!response.url().match(/\/case-types\/[^/]+\/validate/) &&
              response.request().method() === 'POST' &&
              response.status() === 200
          ),
          await this.clickContinue()
      ]);

      await this.fillFirstName('Peter');
      await this.fillLastName('Smith');
      await this.fillPhoneNumber('0123456789');
      await this.fillAlternativeNumber('0123456789');
      await this.fillDirectEmailAddress('somethingtest@mailnator.com');
      await this.addNew.click();
      await this.page.locator('#applicantContactOthers_0_firstName').fill('Me');
      await this.page.locator('#applicantContactOthers_0_lastName').fill('Two');
      await this.page.getByLabel('Email address', { exact: true }).fill('zee@mail.com');
      await this.role.check();
      await this.enterRole.fill('QA');

      await Promise.all([
          this.page.waitForResponse(response =>
              response.url().includes('validate') &&
              response.request().method() === 'POST' &&
              response.status() === 200
          ),
          this.continue.click()
      ]);

      await Promise.all([
          this.page.waitForResponse(response =>
              response.url().includes('get') &&
              response.request().method() === 'GET' &&
              response.status() === 200
          ),
          this.saveAndContinue.click()
      ]);

  }

  async solicitorC110AApplicationApplicantDetails(PBANumber:string){

      await expect.soft(this.representingPersonDetails).toBeVisible();
      await this.representingPersonDetails.getByLabel('First name').fill('John');
      await this.representingPersonDetails.getByLabel('Last name').fill('Somuy');
      await this.page.getByLabel('Group email address (Optional)').fill('privatesol@gmail.com');
      await this.pbaNumber.selectOption(PBANumber);
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
