import {type Page, type Locator, expect} from "@playwright/test";
import { BasePage } from "./base-page";

export class InternationalElement extends BasePage {
  readonly internationalElementHeading: Locator;
  readonly areThereAnySuitableCarers: Locator;
  readonly anySignificantEventsOutsideUk: Locator;
  readonly anyIssueWithJurisdictionOfThisCase: Locator;
  readonly awareOfAnyProceedingsOutsideTheUk: Locator;
  readonly aGovtOrCentralAuthorityOutsideUkInvolvedInCase: Locator;

  public constructor(page: Page) {
    super(page);
    this.internationalElementHeading = page.getByRole('heading', { name: 'International element', exact: true });
    this.areThereAnySuitableCarers = page.getByRole('group', { name: 'Are there any suitable carers' });
    this.anySignificantEventsOutsideUk = page.getByRole('group', { name: 'Are you aware of any significant events that have happened outside the UK? (' });
    this.anyIssueWithJurisdictionOfThisCase = page.getByRole('group', { name: 'Are you aware of any issues' });
    this.aGovtOrCentralAuthorityOutsideUkInvolvedInCase = page.getByRole('group', { name: 'Has, or should, a government' });
    this.awareOfAnyProceedingsOutsideTheUk = page.getByRole('group', { name: 'Are you aware of any proceedings outside the UK? (Optional)' });

  }
  async internationalElementSmokeTest() {
    await expect(this.internationalElementHeading).toHaveText('International element');
    await this.areThereAnySuitableCarers.getByLabel('No').check();
    await this.anySignificantEventsOutsideUk.getByLabel('No').check();
    await this.anyIssueWithJurisdictionOfThisCase.getByLabel('No').check();
    await this.awareOfAnyProceedingsOutsideTheUk.getByLabel('No').check();
    await this.aGovtOrCentralAuthorityOutsideUkInvolvedInCase.getByLabel('No').check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }
}
