import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class ReturnApplication extends BasePage {
  updateProposal() {
    throw new Error('Method not implemented.');
  }
  readonly returnApplication: Locator;
  readonly updateAndSubmit: Locator;
  readonly reasonForRejection: Locator;
  readonly needToChange: Locator;
  readonly submitApplication: Locator;
  readonly saveAndContinue: Locator;
  readonly IAgreeWithThisStatement: Locator;
  readonly DoTheyHaveLegal: Locator;
  readonly DoYouBelieveThisPerson: Locator;
  readonly DoYouNeedContactDetailsHidden: Locator;
  readonly MakeChangesToAllocationProposal: Locator;
  readonly GiveReasonsOptional: Locator;
  readonly MakeChangesToTheRespodentDetails: Locator;
  readonly GiveReason: Locator;
  readonly continueButton: Locator;
  readonly submit: Locator;
  respondentDetailsHeading: Locator;

  public constructor(page: Page) {
    super(page);
    this.returnApplication = page.getByLabel('Return application');
    this.updateAndSubmit = page.getByRole('button', { name: 'Go' });
    this.reasonForRejection = page.getByLabel('Application Incomplete');
    this.needToChange = page.getByLabel('Let the local authority know');
    this.submitApplication = page.getByRole('button', { name: 'Continue' });
    this.saveAndContinue = page.getByRole('button', { name: 'Save and continue' });
    this.IAgreeWithThisStatement = page.getByLabel('I agree with this statement');
    this.DoTheyHaveLegal = page.getByRole('group', { name: '*Do they have legal' }).getByLabel('No');
    this.DoYouBelieveThisPerson = page.getByRole('group', { name: 'Do you believe this person' }).getByLabel('No', { exact: true });
    this.DoYouNeedContactDetailsHidden = page.getByRole('group', { name: 'Do you need contact details' }).getByLabel('No');
    this.MakeChangesToAllocationProposal= page.getByRole('link', { name: 'Make changes to allocation' });
    this.GiveReasonsOptional = page.getByLabel('*Give reason (Optional)');
    this.MakeChangesToTheRespodentDetails = page.getByRole('link', { name: 'Make changes to the respondents\' details' });
    this.GiveReason = page.getByLabel('*Give reason (Optional)');
    this.continueButton = page.getByRole('button', { name: 'Continue' });
    this.submit = page.getByRole('button', { name: 'Submit' });
    this.respondentDetailsHeading = page.getByRole("heading", { name: 'Respondents\' details' });
  }

  async ReturnApplication() {
    await this.reasonForRejection.check();
    await this.needToChange.fill('today');
    await this.submit.click();
    await this.submitApplication.click();
    await this.page.waitForLoadState();
    await this.saveAndContinue.click({timeout:200000});
  }

  public async payForApplication() {
    await this.page.getByLabel('Payment by account (PBA) number').fill('PBA1234567');
    await this.page.getByLabel('Customer reference').fill('Customer reference');
    await this.checkYourAnsAndSubmit();
  }

  async SubmitApplication() {
    await this.IAgreeWithThisStatement.check();
    await this.clickSubmit();
  }

  async UpdateRespondent() {
    await expect(this.respondentDetailsHeading).toBeVisible();
    await this.DoTheyHaveLegal.check();
    await this.DoYouNeedContactDetailsHidden.check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();

  }
}
