import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class InternationalElement extends BasePage {
  readonly internationalElementHeading: Locator;
  readonly areThereAnySuitableCarers: Locator;
  readonly anySignificantEventsOutsideUk: Locator;
  readonly anyIssueWithJurisdictionOfThisCase: Locator;
  readonly awareOfAnyProceedingsOutsideTheUk: Locator;
  readonly aGovtOrCentralAuthorityOutsideUkInvolvedInCase: Locator;
  readonly continueButton: Locator;
  readonly saveAndContinueButton: Locator;
  readonly checkYourAnswers: Locator;

  public constructor(page: Page) {
    super(page);
    this.internationalElementHeading = page.getByRole('heading', { name: 'International element' });
    this.areThereAnySuitableCarers = page.getByRole('button', { name: 'No' });
    this.anySignificantEventsOutsideUk = page.getByRole('button', { name: 'No' });
    this.awareOfAnyProceedingsOutsideTheUk = page.getByRole('button', { name: 'No' });
    this.aGovtOrCentralAuthorityOutsideUkInvolvedInCase = page.getByRole('button', { name: 'No' });
    this.awareOfAnyProceedingsOutsideTheUk = page.getByRole('button', { name: 'No' });
    this.continueButton = page.getByRole('button', { name: 'Continue' });
    this.checkYourAnswers = page.getByRole('heading', { name: 'Check your answers' });
    this.saveAndContinueButton = page.getByRole('button', { name: 'Save and continue' });
  }
    async internationalElementSmokeTest(){
      await this.page.getByRole('group', { name: 'Are there any suitable carers' }).getByLabel('No').check();
      await this.page.getByRole('group', { name: 'Are you aware of any significant events that have happened outside the UK? (' }).getByLabel('No').check();
      await this.page.getByRole('group', { name: 'Are you aware of any issues' }).getByLabel('No').check();
      await this.page.getByRole('group', { name: 'Are you aware of any proceedings outside the UK? (Optional)' }).getByLabel('No').check();
      await this.page.getByRole('group', { name: 'Has, or should, a government' }).getByLabel('No').check();
      await this.clickContinue();
      await this.checkYourAnsAndSubmit();
    }
  }
