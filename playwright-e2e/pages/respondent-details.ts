import { type Page, type Locator, expect } from "@playwright/test";

export class RespondentDetails {

  readonly page: Page;
  readonly respondentDetailsHeading: Locator;
  readonly firstName: Locator;
  readonly lastName: Locator;
  readonly dobDay: Locator;
  readonly dobMonth: Locator;
  readonly dobYear: Locator;
  readonly gender: Locator;
  readonly currentAddress: Locator;
  readonly addressUnknown: Locator;
  readonly giveMoreDetails: Locator;
  readonly telephone: Locator;
  readonly relationToChild: Locator;
  readonly difficultyCapacity: Locator;
  readonly difficultyCapacityReason: Locator;
  readonly legalRepresentation: Locator;
  readonly continue: Locator;
  readonly saveAndContinue: Locator;
  readonly addressNotKnownReason: Locator;
  readonly confirmationCheckbox: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.respondentDetailsHeading = page.getByRole("heading", { name: 'Respondents\' details' });
    this.firstName = page.getByLabel('First name', { exact: true });
    this.lastName = page.getByLabel('Last name', { exact: true })
    this.dobDay = page.getByLabel('Day');
    this.dobMonth = page.getByLabel('Month');
    this.dobYear = page.getByLabel('Year');
    this.gender = page.getByLabel('What is the respondent\'s gender? (Optional)');
    this.currentAddress = page.getByRole('group', { name: 'Current address known?' });
    this.addressUnknown = page.getByRole('group', { name: 'Why is this address unknown?' });
    this.addressNotKnownReason = page.getByLabel('*Reason the address is not known');
    this.giveMoreDetails = page.getByLabel('Give more details');
    this.telephone = page.getByRole('group', { name: 'Telephone (Optional)' }).locator('#respondents1_0_party_telephoneNumber_telephoneNumber');
    this.relationToChild = page.getByLabel('Which children does the respondent have parental responsibility for and what is their relationship?');
    this.difficultyCapacity = page.getByRole('group', { name: 'Do you believe this person will have difficulty understanding what\'s happening with the case? (Optional)' });
    this.difficultyCapacityReason = page.getByLabel('Give details, including assessment outcomes and referrals to health services (Optional)');
    this.legalRepresentation = page.getByRole('group', { name: 'Do they have legal representation?' });
    this.continue = page.getByRole('button', { name: 'Continue' });
    this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });
    this.confirmationCheckbox = page.getByLabel('I confirm that each person');
  }

  async respondentDetailsNeeded() {
    await expect.soft(this.respondentDetailsHeading).toBeVisible();
    await this.confirmationCheckbox.check();
    await this.firstName.fill('Tom');
    await this.lastName.fill('Jones');
    await this.dobDay.fill('01');
    await this.dobMonth.fill('01');
    await this.dobYear.fill('1990');
    await this.gender.selectOption('1: Male');
    await this.currentAddress.getByLabel('No').click();
    await this.currentAddress.getByLabel('No').check();
    await this.addressUnknown.getByLabel('Whereabouts unknown').check();
    await this.giveMoreDetails.fill('Test');
    await this.relationToChild.fill('uncle');
    await this.difficultyCapacity.getByLabel('Yes').check();
    await this.difficultyCapacityReason.fill('test')
    await this.legalRepresentation.getByLabel('No').check();
    await this.continue.click();
    await this.saveAndContinue.click();
      await expect(this.page.getByText('has been updated with event:')).toBeVisible();
  }
}
