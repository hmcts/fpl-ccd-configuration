import { type Page, type Locator, expect } from "@playwright/test";
import test from "node:test";

export class RespondentsDetails {
  respondentsDetailsSmokeTest() {
    throw new Error("Method not implemented.");
  }
    readonly page: Page;
    readonly RespondentsDetailsLink: Locator;
    readonly RespondentsDetailsHeading: Locator;
    readonly LegalCounselTestCase: Locator;
    readonly AddNew: Locator;
    readonly FirstName: Locator;
    readonly LastName : Locator;
    readonly DateOfBirth : Locator;
    readonly Gender : Locator;
    readonly CurrentAddressKnown: Locator;
    readonly RelationshipToChildLocator : Locator;
    readonly DoYouNeedContactDetailsHiddenFromParties : Locator;
    readonly AbilityToTakePartInProceedings: Locator;
    readonly DoTheyHaveLegalRepresentation: Locator;
    readonly RespondentsDetailsNeeded: Locator;
    readonly Day: Locator;
    readonly Month: Locator;
    readonly Year: Locator;
    

  static RespondentsDetailsHeading: any;
  SaveAndContinue: any;

    
public constructor(page: Page) {
        this.page = page;
        this.RespondentsDetailsHeading = page.getByRole("heading", {name: "AllocationProposal needed",});
        this.RespondentsDetailsLink = page.locator(".govuk-template__body.js-enabled");
        this.RespondentDetailsNeeded(); 
        this.DateOfBirth =page.getByText('Date of birth (Optional)');
        this.Day = page.getByLabel('Day');  
        this.Month = page.getByLabel('Month');
        this.Year = page.getByLabel('Year');
        this.CurrentAddressKnown = page.getByRole('group', { name: '*Current address known?' });
}
async RespondentDetailsNeeded() {
  await this.page.getByRole('heading', { name: 'Add information about the' }).isVisible();
  await this.page.getByRole('link', { name: 'Respondents\' details' }).click();
  await this.page.getByLabel('*First name (Optional)').fill('Asa');
  await this.page.getByLabel('*Last name (Optional)').fill('Thierry');
  await this.DateOfBirth.click();
  await this.Day.fill('10');
  await this.Month.fill('12');
  await this.Year. fill('2008');
  await this.page.getByLabel('Gender (Optional)').selectOption('1: Male');
  await this.CurrentAddressKnown.getByLabel('No').dblclick();
  //await this.page.getByRole('group', { name: '*Current address known?' }).getByLabel('No').click();
  //await this.page.pause();
  await this.page.getByLabel('*Reason the address is not').selectOption('1: No fixed abode');
  await this.page.getByLabel('*What is the respondent\'s').click();
  await this.page.getByLabel('*What is the respondent\'s').fill('uncle');
  await this.page.getByRole('group', { name: 'Do you need contact details' }).getByLabel('No').check();
  await this.page.getByRole('group', { name: 'Do you believe this person' }).getByLabel('No', { exact: true }).check();
  await this.page.getByRole('group', { name: '*Do they have legal' }).getByLabel('No').check();
  await this.page.getByRole('button', { name: 'Continue' }).click();
  await this.page.getByRole('button', { name: 'Save and continue' }).click();
}
async  RespondentsDetails() {
  await this.RespondentsDetailsLink.isVisible();
  await this.RespondentsDetailsLink.click();
}}
