import { type Page, type Locator, expect } from "@playwright/test"

export class RespondentDetails {

  readonly page: Page;
  readonly respondentDetailsHeading: Locator;
  readonly firstName: Locator;
  readonly respondentFirstName: Locator;
  readonly lastName: Locator;
  readonly respondentLastName: Locator;
  readonly dobDay: Locator;
  readonly respondentdobDay: Locator;
  readonly respondentdobMonth: Locator;
  readonly respondentdobYear: Locator;
  readonly dobMonth: Locator;
  readonly dobYear: Locator;
  readonly gender: Locator;
  readonly respondent2Gender: Locator;
  readonly respondent2CurrentAddressKnown: Locator;
  readonly respondent2AddressUnknown: Locator;
  readonly respondet2RelationshipToChild: Locator;
  readonly currentAddress: Locator;
  readonly addressUnknown: Locator;
  readonly giveMoreDetails: Locator;
  readonly telephone: Locator;
  readonly respondent2DifficultyUnderstandingCapacity: Locator;
  readonly respondent2DificultyCapacityReason: Locator;
  readonly respondent2HiddenNumber: Locator;
  readonly relationToChild: Locator;
  readonly difficultyCapacity: Locator;
  readonly difficultyCapacityReason: Locator;
  readonly legalRepresentation: Locator;
  readonly continue: Locator;
  readonly saveAndContinue: Locator;
  readonly confirmationCheckbox: Locator;
  readonly addNew: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.respondentDetailsHeading = page.getByRole("heading", { name: 'Respondents\' details' });
    this.firstName = page.getByLabel('*First name (Optional)', { exact: true });
    this.lastName = page.getByLabel('*Last name (Optional)', { exact: true })
    this.dobDay = page.getByLabel('Day');
    this.dobMonth = page.getByLabel('Month');
    this.dobYear = page.getByLabel('Year');
    this.gender = page.getByLabel('Gender (Optional)');
    this.currentAddress = page.getByRole('group', { name: '*Current address known?' });
    this.addressUnknown = page.getByLabel('*Reason the address is not');
    this.giveMoreDetails = page.getByLabel('Give more details');
    this.telephone = page.getByRole('group', { name: 'Telephone (Optional)' }).locator('#respondents1_0_party_telephoneNumber_telephoneNumber');
    this.relationToChild = page.getByLabel('*What is the respondent\'s relationship to the child or children in this case? (Optional)');
    this.difficultyCapacity = page.getByLabel('Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)? (Optional)' );
    this.difficultyCapacityReason = page.getByLabel('Give details, including assessment outcomes and referrals to health services (Optional)');
    this.legalRepresentation = page.getByRole('group', { name: 'Do they have legal representation?' });
    this.addNew = page.getByRole('button', { name: 'Add new' }).nth(1);
    this.continue = page.getByRole('button', { name: 'Continue' });
    this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });
    this.confirmationCheckbox = page.getByLabel('I confirm that each person');
    this.respondentFirstName = page.locator('#respondents1_1_party_firstName');
    this.respondentLastName = page.locator('#respondents1_1_party_lastName');
    this.respondentdobDay = page.locator('#respondents1_1_party_party #dateOfBirth-day');
    this.respondentdobMonth = page.locator('#respondents1_1_party_party #dateOfBirth-month');
    this.respondentdobYear = page.locator('#respondents1_1_party_party #dateOfBirth-year');
    this.respondent2Gender = page.locator('#respondents1_1_party_gender');
    this.respondent2CurrentAddressKnown = page.locator('#respondents1_1_party_addressKnow-No');
    this.respondent2AddressUnknown = page.locator('[id="respondents1_1_party_addressNotKnowReason-No\\ fixed\\ abode"]');
    this.respondent2HiddenNumber = page.locator('#respondents1_1_party_hideTelephone_No');
    this.respondet2RelationshipToChild = page.locator('#respondents1_1_party_relationshipToChild');
    this.respondent2DifficultyUnderstandingCapacity = page.locator('#respondents1_1_party_litigationIssues-NO');
    this.respondent2DificultyCapacityReason = page.locator('#respondents1_1_legalRepresentation_No')
  }

  async respondentDetailsNeeded() {
    await expect.soft(this.respondentDetailsHeading).toBeVisible();
    //await this.confirmationCheckbox.check();
    await this.firstName.fill('Tom');
    await this.lastName.fill('Jones');
    await this.dobDay.fill('01');
    await this.dobMonth.fill('01');
    await this.dobYear.fill('1990');
    await this.gender.selectOption('1: Male');
    //await this.page.getByRole('group', { name: '*Current address known?' }).getByLabel('No').check();
    //await this.currentAddress.getByLabel('No').check();
   // await this.currentAddress.getByLabel('No').click({force: true});
    await this.addressUnknown.selectOption('1: No fixed abode');
   // await this.giveMoreDetails.fill('Test');
    await this.relationToChild.fill('uncle');
    await this.difficultyCapacity.getByLabel('Yes').check();
    await this.difficultyCapacityReason.fill('test')
    await this.legalRepresentation.getByLabel('No').check();
    await this.addNew.click();
    await this.respondentFirstName.fill('Thierry');
    await this.respondentLastName.fill('Jordan');
    await this.respondentdobDay.fill('31');
    await this.respondentdobMonth.fill('03');
    await this.respondentdobYear.fill('1980');
    await this.respondent2Gender.selectOption('1: Male');
    await this.respondent2CurrentAddressKnown.click();
    await this.respondent2AddressUnknown.click();
    await this.respondent2HiddenNumber.click();
    await this.respondet2RelationshipToChild.fill('uncle');
    await this.respondent2DifficultyUnderstandingCapacity.click();
    await this.respondent2DificultyCapacityReason.click();
    //await this.page.waitForTimeout(1000);
    await this.continue.click();
    await this.saveAndContinue.click();
  }
}
