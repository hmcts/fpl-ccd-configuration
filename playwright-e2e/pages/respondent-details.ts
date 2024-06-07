import {type Page, type Locator, expect} from "@playwright/test";
import { BasePage } from "./base-page";

export class RespondentDetails extends BasePage{
  readonly respondentDetailsHeading: Locator;
  readonly firstName: Locator;
  readonly lastName: Locator;
  readonly dobDay: Locator; //DOB (date of birth)
  readonly dobMonth: Locator;
  readonly dobYear: Locator;
  readonly gender: Locator;
  readonly currentAddress: Locator;
  readonly telephone: Locator;
  readonly relationToChild: Locator;
  readonly relationToChildContact: Locator; //corresponds to yes or no radio feature: 'Do you need contact details hidden from other parties? (Optional)'
  readonly relationToChildContactReason: Locator;
  readonly litigationCapacity: Locator; //ie Ability to take part in proceedings
  readonly litigationCapacityReason: Locator;
  readonly legalRepresentation: Locator;
  readonly continue: Locator;
  readonly saveAndContinue: Locator;
  readonly addressNotKnownReason: Locator;

  public constructor(page: Page) {
    super(page);
    this.respondentDetailsHeading = page.getByRole("heading", { name: 'Respondents\' details' });
    this.firstName = page.getByLabel('*First name (Optional)');
    this.lastName = page.getByLabel('*Last name (Optional)');
    this.dobDay = page.getByLabel('Day');
    this.dobMonth = page.getByLabel('Month');
    this.dobYear = page.getByLabel('Year');
    this.gender = page.getByLabel('Gender (Optional)');
    this.currentAddress = page.getByRole('group', { name: '*Current address known?' });
    this.addressNotKnownReason = page.getByLabel('*Reason the address is not known');
    this.telephone = page.getByRole('group', { name: 'Telephone (Optional)' }).locator('#respondents1_0_party_telephoneNumber_telephoneNumber');
    this.relationToChild = page.getByLabel('*What is the respondent\'s relationship to the child or children in this case? (Optional)');
    this.relationToChildContact = page.getByRole('group', { name: 'Do you need contact details hidden from other parties? (Optional)' });
    this.relationToChildContactReason = page.getByLabel('Give reason (Optional)');
    this.litigationCapacity = page.getByRole('group', { name: 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)? (Optional)' });
    this.litigationCapacityReason = page.getByLabel('Give details, including assessment outcomes and referrals to health services (Optional)');
    this.legalRepresentation = page.getByRole('group', { name: '*Do they have legal representation? (Optional)' });
    this.continue = page.getByRole('button', { name: 'Continue' });
    this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });
  }

  async respondentDetailsNeeded() {
    await expect(this.respondentDetailsHeading).toBeVisible();
    await this.firstName.fill('John');
    await this.lastName.fill('Smith');
    await this.dobDay.fill('10');
    await this.dobMonth.fill('11');
    await this.dobYear.fill('2001');
    await this.gender.click(); //not sure if click needed
    await this.gender.selectOption('1: Male');
    await this.currentAddress.getByLabel('No').check();
    await this.addressNotKnownReason.selectOption('2: Person deceased');
    await this.telephone.fill('01234567890');
    await this.relationToChild.fill('aunt');
    await this.relationToChildContact.getByLabel('Yes').check();
    await this.relationToChildContactReason.fill('this is the reason');
    await this.litigationCapacity.getByLabel('Yes').check();
    await this.litigationCapacityReason.fill('these are the details');
    await this.legalRepresentation.getByLabel('No').check();
    await this.continueButton.click()
    await this.checkYourAnsAndSubmit();
  }
}
