import {type Page, type Locator } from "@playwright/test";
import {BasePage} from "./base-page";
import {HearingDetailsMixin} from "./mixins/hearing-details-mixin";

export class ManageHearings extends HearingDetailsMixin(BasePage)
{
  readonly hearingDetails: Locator;
  readonly hearingDay: Locator;
  readonly hearingMonth: Locator;
  readonly hearingYear: Locator;
  readonly hearingLengthInHours: Locator;
  readonly hearingLengthInMinutes: Locator;
  readonly inpPersonCheckbox: Locator;

  constructor(page: Page) {
    super(page);
    this.hearingDetails = this.page.getByLabel('Add details (Optional)');
    this.hearingDay = this.page.getByRole('textbox', { name: 'Day' });
    this.hearingMonth = this.page.getByRole('textbox', { name: 'Month' });
    this.hearingYear = this.page.getByRole('textbox', { name: 'Year' });
    this.hearingLengthInHours = this.page.getByLabel('Hearing length, in hours');
    this.hearingLengthInMinutes = this.page.getByLabel('Hearing length, in minutes');
    this.inpPersonCheckbox = this.page.getByText('In person');
  }

  async createNewHearingOnCase(){
    await this.page.getByLabel('Add a new hearing').check();
    await this.clickContinue();
    await this.completeHearingDetails();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'No' }).check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }

  async editPastHearingOnCase() {
    await this.page.getByLabel('Edit a hearing that has taken').check();
    await this.page.getByLabel('Which draft hearing do you').selectOption('Case management hearing, 3 November 2012');
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }

  async vacateHearing() {
    await this.page.getByLabel('Vacate a hearing - the').check();
    await this.page.locator('#vacateHearingDateList').selectOption('Case management hearing, 3 November 2012');
    await this.page.getByLabel('Day').fill('3');
    await this.page.getByLabel('Month').fill('11');
    await this.page.getByLabel('Year').fill('2012');
    await this.clickContinue();
    await this.clickContinue();
    await this.page.getByText('Yes - and I can add the new').click();
    await this.clickContinue();
    await this.page.getByText('The local authority').click();
    await this.page.locator('#vacatedReason_reason-LocalAuthority').selectOption('No expert instructed by LA');
    await this.clickContinue();
    await this.completeHearingDetails();
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'No' }).check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }

  async adjournHearing() {
    await this.page.getByLabel('Adjourn a hearing - the').check();
    await this.page.locator('#pastAndTodayHearingDateList').selectOption('Case management hearing, 3 November 2012');
    await this.clickContinue();
    await this.page.getByLabel('The local authority').check();
    await this.page.locator('#adjournmentReason_reason-LocalAuthority').selectOption('No expert instructed by LA');
    await this.clickContinue()
    await this.page.getByText('Yes - and I can add the new').click();
    await this.clickContinue();
    await this.completeHearingDetails();
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue()
    await this.page.getByRole('radio', { name: 'No' }).check();
    await this.clickContinue()
    await this.checkYourAnsAndSubmit();
  }

  async editFutureHearingOnCase(hearingToEdit: string, updatedHearingJudge?: string) {
    await this.page.getByLabel('Edit a future hearing').check();
    await this.page.locator('#futureHearingDateList').selectOption(hearingToEdit);
    await this.clickContinue();
    await this.page.getByLabel('Further case management', { exact: true }).check();
    await this.verifyHearingTypesSelection();
    await this.clickContinue();
    if (typeof updatedHearingJudge !== 'undefined') {
      await this.page.getByRole('radio', { name: 'No' }).check();
      await this.page.getByLabel('Search for Judge (Optional)').fill(updatedHearingJudge);
      await this.page.waitForSelector(`span:text("${updatedHearingJudge}")`);
      await this.page.getByText(updatedHearingJudge).click();
      await this.page.getByRole('group', { name: 'Add legal adviser details' }).getByLabel('No').check();
      await this.clickContinue();
      await this.page.waitForSelector(`span:text("Do you want to send a notice of hearing?")`);
    } else {
      await this.page.getByRole('radio', { name: 'Yes' }).check();
      await this.clickContinue();
    }
    await this.page.getByRole('radio', { name: 'No' }).check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  }

  async reListHearing() {
    await this.page.getByLabel('Re-list an adjourned or vacated hearing').check();
    await this.page.locator('#toReListHearingDateList').selectOption('Case management hearing, 3 November 2012' +
      ' - adjourned');
    await this.clickContinue();
    await this.completeHearingDetails();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'No' }).check();
    await this.clickContinue();
    await this.checkYourAnsAndSubmit();
  };
}
