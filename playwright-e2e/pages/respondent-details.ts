import { type Page, type Locator, expect } from "@playwright/test";

export class RespondentDetails {

  readonly page: Page;
  readonly RespondentDetailsHeading: Locator;
  readonly FirstName: Locator;
  readonly LastName: Locator;
  readonly DOB_Day: Locator; //DOB (date of birth)
  readonly DOB_Month: Locator;
  readonly DOB_Year: Locator;
  readonly Gender: Locator;
  readonly CurrentAddress: Locator;
  readonly Telephone: Locator;
  readonly RelationToChild: Locator;
  readonly RelationToChildContact: Locator; //corresponds to yes or no radio feature: 'Do you need contact details hidden from other parties? (Optional)'
  readonly RelationToChildContactReason: Locator;
  readonly LitigationCapacity: Locator; //ie Ability to take part in proceedings 
  readonly LitigationCapacityReason: Locator;
  readonly LegalRepresentation: Locator;
  readonly Continue: Locator;
  readonly SaveAndContinue: Locator;
  readonly AddressNotKnownReason: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.RespondentDetailsHeading = page.getByRole("heading", { name: 'Respondents\' details' });
    this.FirstName = page.getByLabel('*First name (Optional)');
    this.LastName = page.getByLabel('*Last name (Optional)');
    this.DOB_Day = page.getByLabel('Day');
    this.DOB_Month = page.getByLabel('Month');
    this.DOB_Year = page.getByLabel('Year');
    this.Gender = page.getByLabel('Gender (Optional)');
    this.CurrentAddress = page.getByRole('group', { name: '*Current address known?' });
    this.AddressNotKnownReason = page.getByLabel('*Reason the address is not known');
    this.Telephone = page.getByRole('group', { name: 'Telephone (Optional)' }).locator('#respondents1_0_party_telephoneNumber_telephoneNumber');
    this.RelationToChild = page.getByLabel('*What is the respondent\'s relationship to the child or children in this case? (Optional)');
    this.RelationToChildContact = page.getByRole('group', { name: 'Do you need contact details hidden from other parties? (Optional)' });
    this.RelationToChildContactReason = page.getByLabel('Give reason (Optional)');
    this.LitigationCapacity = page.getByRole('group', { name: 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)? (Optional)' });
    this.LitigationCapacityReason = page.getByLabel('Give details, including assessment outcomes and referrals to health services (Optional)');
    this.LegalRepresentation = page.getByRole('group', { name: '*Do they have legal representation? (Optional)' });
    this.Continue = page.getByRole('button', { name: 'Continue' });
    this.SaveAndContinue = page.getByRole('button', { name: 'Save and continue' });
  }

  async respondentDetailsNeeded() {
    await this.RespondentDetailsHeading.isVisible;
    await this.FirstName.click()
    await this.FirstName.fill('John')
    await this.LastName.click();
    await this.LastName.fill('Smith');
    await this.DOB_Day.click();
    await this.DOB_Day.fill('10');
    await this.DOB_Month.click();
    await this.DOB_Month.fill('11');
    await this.DOB_Year.click();
    await this.DOB_Year.fill('2001');
    await this.Gender.click(); //not sure if click needed
    await this.Gender.selectOption('1: Male');
    await this.CurrentAddress.getByLabel('No').check();
    await this.AddressNotKnownReason.selectOption('2: Person deceased');
    await this.Telephone.fill('01234567890');
    await this.RelationToChild.click();
    await this.RelationToChild.fill('aunt');
    await this.RelationToChildContact.getByLabel('Yes').check();
    await this.RelationToChildContactReason.click();
    await this.RelationToChildContactReason.fill('this is the reason');
    await this.LitigationCapacity.getByLabel('Yes').check();
    await this.LitigationCapacityReason.click();
    await this.LitigationCapacityReason.fill('these are the details');
    await this.Continue.click();
    await this.SaveAndContinue.click();
  }
}
