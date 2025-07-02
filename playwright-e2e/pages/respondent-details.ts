import { type Page, type Locator, expect } from "@playwright/test"
import { BasePage } from "./base-page";

export class RespondentDetails extends BasePage {

  readonly respondentDetailsHeading: Locator;
  readonly firstName: Locator;
  readonly lastName: Locator;
  readonly lawyerFirstName: Locator;
  readonly lawyerLastName: Locator;
  readonly authorityOutsourcing: Locator;
  readonly respondentFirstName: Locator;
  readonly respondentLastName: Locator;
  readonly respondentdobDay: Locator;
  readonly respondentdobMonth: Locator;
  readonly respondentdobYear: Locator;
  readonly dobDay: Locator;
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
  readonly nextStep: Locator;
  readonly go: Locator;
  readonly phoneNumber: Locator;
  readonly telephoneNumber: Locator;
  readonly numberConfidential: Locator;
  readonly parentalResponsibility: Locator;
  readonly legalRepresentative: Locator;

  public constructor(page: Page) {
    super(page);
    this.respondentDetailsHeading = page.getByRole("heading", { name: 'Respondents\' details' });
    this.lawyerFirstName = page.getByLabel('Lawyer first name');
    this.lawyerLastName = page.getByLabel('Lawyer last name');
    this.authorityOutsourcing = page.getByRole('group', { name: 'Is this Local Authority' });
    this.firstName = page.getByLabel('First name', { exact: true });
    this.lastName = page.getByLabel('Last name', { exact: true });
    this.dobDay = page.getByLabel('Day');
    this.dobMonth = page.getByLabel('Month');
    this.dobYear = page.getByLabel('Year');
    this.gender = page.getByLabel('What is the respondent\'s gender? (Optional)');
    this.currentAddress = page.getByRole('group', { name: 'Current address known?' });
    this.addressUnknown = page.getByRole('group', { name: 'Why is this address unknown?' });
    this.giveMoreDetails = page.getByLabel('Give more details');
    this.telephone = page.getByRole('group', { name: 'Telephone (Optional)' }).locator('#respondents1_0_party_telephoneNumber_telephoneNumber');
    this.difficultyCapacity = page.getByRole('group', { name: 'Do you believe this person will have difficulty understanding what\'s happening with the case? (Optional)' });
    this.difficultyCapacityReason = page.getByLabel('Give details, including assessment outcomes and referrals to health services (Optional)');
    this.relationToChild = page.getByLabel('Which children does the respondent have parental responsibility for and what is their relationship?');
    this.legalRepresentation = page.getByRole('group', { name: 'Do they have legal representation?' });
    this.addNew = page.getByRole('button', { name: 'Add new' });
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
    this.respondent2DificultyCapacityReason = page.locator('#respondents1_1_legalRepresentation_No');
    this.nextStep = page.getByLabel('Next step');
    this.go = page.getByRole('button', { name: 'Go' });
    this.phoneNumber = page.getByLabel('Phone number');
    this.telephoneNumber = page.getByLabel('Telephone number (Optional)');
    this.numberConfidential = page.getByRole('group', { name: 'Do you need to keep the' });
    this.parentalResponsibility = page.getByLabel('Which children does the');
    this.legalRepresentative = page.getByRole('group', { name: 'Do they have legal' }).getByLabel('No');

  }

  async respondentDetailsNeeded() {
    await expect.soft(this.respondentDetailsHeading).toBeVisible();
    await this.confirmationCheckbox.check();
    await this.firstName.fill('Tom');
    await this.lastName.fill('Jones');
    await this.dobDay.fill('31');
    await this.dobMonth.fill('3');
    await this.dobYear.fill('1980');
    await this.gender.selectOption('1: Male');
    await this.currentAddress.getByLabel('No').click();
    await this.currentAddress.getByLabel('No').click();
    await this.addressUnknown.getByLabel('Whereabouts unknown').click();
    await this.addressUnknown.getByLabel('Whereabouts unknown').click();
    await this.giveMoreDetails.fill('Test');
    await this.relationToChild.fill('uncle');
    await this.difficultyCapacity.getByLabel('Yes').check();
    await this.difficultyCapacityReason.fill('test')
    await this.legalRepresentation.getByLabel('No').check();
    await this.addNew.nth(1).click();
    await this.respondentFirstName.fill('Thierry');
    await this.respondentLastName.fill('Jordan');
    await this.respondentdobDay.fill('31');
    await this.respondentdobMonth.fill('03');
    await this.respondentdobYear.fill('1980');
    await this.respondent2Gender.selectOption('1: Male');
    await this.respondent2CurrentAddressKnown.click();
    await this.respondent2CurrentAddressKnown.click();
    await this.respondent2AddressUnknown.click();
    await this.respondent2HiddenNumber.click();
    await this.respondet2RelationshipToChild.fill('uncle');
    await this.respondent2DifficultyUnderstandingCapacity.click();
    await this.respondent2DificultyCapacityReason.click();
    await this.page.waitForTimeout(300); // this needs to be removed once EXUI resolves issue
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event:')).toBeVisible();
  }

  async respondentDetailsPrivateSolicitor() {
    await expect.soft(this.respondentDetailsHeading).toBeVisible();
    await this.confirmationCheckbox.check();
    await this.lawyerFirstName.fill('Tommy');
    await this.lawyerLastName.fill('Lee');
    await this.enterPostCode('sk3 8pp');
    await this.authorityOutsourcing.getByLabel('No').check();
    await this.confirmationCheckbox.check();
    await this.phoneNumber.fill('07818213678');
    await this.firstName.fill('Dianah');
    await this.lastName.fill('Asa');
    await this.dobDay.fill('11');
    await this.dobMonth.fill('10');
    await this.dobYear.fill('1998');
    await this.gender.selectOption('1: Male');
    await this.currentAddress.getByLabel('No').click();
    await this.currentAddress.getByLabel('No').click();
    await this.addressUnknown.getByLabel('Whereabouts unknown').click();
    await this.addressUnknown.getByLabel('Whereabouts unknown').click();
    await this.giveMoreDetails.fill('Test');
    await this.relationToChild.fill('uncle');
    await this.difficultyCapacity.getByLabel('No', { exact: true }).check();
    await this.legalRepresentation.getByLabel('No').check();
    await this.addNew.nth(1).click();
    await this.respondentFirstName.fill('Moniks');
    await this.respondentLastName.fill('Yaks');
    await this.respondentdobDay.fill('31');
    await this.respondentdobMonth.fill('03');
    await this.respondentdobYear.fill('1988');
    await this.respondent2Gender.selectOption('1: Male');
    await this.respondent2CurrentAddressKnown.click();
    await this.respondent2CurrentAddressKnown.click();
    await this.respondent2AddressUnknown.click();
    await this.respondent2HiddenNumber.click();
    await this.respondet2RelationshipToChild.fill('uncle');
    await this.respondent2DifficultyUnderstandingCapacity.click();
    await this.respondent2DificultyCapacityReason.click();
    await this.page.waitForTimeout(300); // this needs to be removed once EXUI resolves issue
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
    await expect(this.page.getByText('has been updated with event:')).toBeVisible();
  }
}
