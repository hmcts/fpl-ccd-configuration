import { type Page, type Locator, expect } from "@playwright/test";
import test from "node:test";

export class RespondentsDetails {
    readonly page: Page;
    readonly RespondentsDetailsLink: Locator;
    readonly RespondentsDetailsHeading: Locator;
    readonly LegalCounselTestCase: Locator;
    readonly AddNew: Locator;
    readonly FirstName: Locator;
    readonly LastName : Locator;
    readonly DateOfBirth : Locator;
    readonly DayMonthYear : Locator;
    readonly Gender : Locator;
    readonly CurrentAddressKnown: Locator
    readonly RelationshipToChildLocator : Locator;
    readonly DoYouNeedContactDetailsHiddenFromParties : Locator;
    readonly AbilityToTakePartInProceedings: Locator;
    readonly DoTheyHaveLegalRepresentation: Locator;
    readonly RespondentsDetailsNeeded: Locator;
  static RespondentsDetailsHeading: any;
    
  public constructor(page: Page) {
        this.page = page;
        this.RespondentsDetailsHeading = page.getByRole("heading", {name: "AllocationProposal needed",});
        this.RespondentsDetailsLink = page.locator(".govuk-template__body.js-enabled");
        this.RespondentDetailsNeeded(); 
}
async RespondentDetailsNeeded() {
  await this.page.getByRole('heading', { name: 'Add information about the' }).isVisible();
  await this.page.getByRole('link', { name: 'Respondents\' details' }).click();
  await this.page.getByLabel('*First name (Optional)').fill('Asa');
  await this.page.getByLabel('*Last name (Optional)').click();
  await this.page.getByLabel('*Last name (Optional)').fill('Thierry');
  await this.page.getByText('Date of birth (Optional)').click();
  await this.page.getByLabel('Day').isVisible();
  await this.page.getByLabel('Day').fill('20');
  await this.page.getByLabel('Month').click();   
  await this.page.getByLabel('Month').fill('10');
  await this.page.getByLabel('Year').click();
  await this.page.getByLabel('Year').fill('2010');
  await this.page.getByLabel('Gender (Optional)').selectOption('1: Male');
  await this.page.getByRole('group', { name: '*Current address known?' }).getByLabel('No').click();
  await this.page.getByRole('group', { name: 'Do you need contact details' }).getByLabel('No').check();
  await this.page.locator('#respondents1_0_party_contactDetailsHidden_radio').getByText('No').click();
  await this.page.locator('#respondents1_0_party_litigationIssues').getByText('No', { exact: true }).click();
  await this.page.getByRole('group', { name: '*Current address known?' }).getByLabel('No').check();
  await this.page.getByLabel('*Reason the address is not').selectOption('1: No fixed abode');
  await this.page.getByRole('group', { name: 'Telephone (Optional)' }).locator('#respondents1_0_party_telephoneNumber_telephoneNumber').click();
  await this.page.getByRole('group', { name: 'Telephone (Optional)' }).locator('#respondents1_0_party_telephoneNumber_telephoneNumber').fill('00000000');
  await this.page.getByRole('heading', { name: 'Relationship to the child' }).click();
  await this.page.getByLabel('*What is the respondent\'s').click();
  await this.page.getByLabel('*What is the respondent\'s').fill('Uncle');
  await this.page.getByRole('group', { name: 'Do you need contact details' }).getByLabel('No').check();
  await this.page.getByRole('group', { name: 'Do you believe this person' }).getByLabel('No', { exact: true }).check();
  await this.page.getByRole('group', { name: '*Do they have legal' }).getByLabel('No').check();
  await this.page.getByRole('button', { name: 'Continue' }).click();
  await this.page.getByRole('heading', { name: 'Check your answers' }).click();
  await this.page.getByRole('button', { name: 'Save and continue' }).click();

}
async respondentDetails() {
    await this.RespondentsDetailsLink.isVisible();
    await this.RespondentsDetailsLink.click();
}
}
