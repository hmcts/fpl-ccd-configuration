import {expect, type Locator, Page} from "@playwright/test";
import {BasePage} from "../base-page";

export function HearingDetailsMixin() {
  return class extends BasePage {
      readonly hearingTypesLabelLocator: Locator;
      readonly feePaidJudgeTitleFieldSet: Locator;
      readonly legalAdviserTitlesFieldSet: Locator;

    constructor(page: Page) {
      super(page);
      this.hearingTypesLabelLocator = this.page.locator('#hearingType .multiple-choice > label');
      this.feePaidJudgeTitleFieldSet = this.page.getByRole('group',{name:'Select judge title'});
      this.legalAdviserTitlesFieldSet = this.page.getByRole('group',{name:'Judge or magistrate\'s title'});
    }

    async completeHearingDetails() {
      await expect(this.page.getByText('Type of hearing')).toBeVisible();
      await this.verifyHearingTypesSelection();
      await this.page.getByLabel('Case management', { exact: true }).check();
      await this.page.locator('#hearingVenue').selectOption({ label: 'Swansea Crown Court' });
      if (!(await this.page.getByLabel('In person').isChecked())) {
        await this.page.getByLabel('In person').check();
      }
      await this.page.getByRole('textbox', { name: 'Day' }).fill('5');
      await this.page.getByRole('textbox', { name: 'Month' }).fill((new Date().getMonth()+1).toString());
      await this.page.getByRole('textbox', { name: 'Year' }).fill((new Date().getUTCFullYear()+1).toString());
      await this.page.getByRole('spinbutton', { name: 'Hour' }).fill('01');
      await this.page.getByLabel('Set number of hours and').check();
      await this.page.getByLabel('Hearing length, in hours').fill('1');
      await this.page.getByLabel('Hearing length, in minutes').fill('30');
      await this.clickContinue();
    }

    async verifyHearingTypesSelection() {
      const expectedHearingTypes = [
        'Emergency protection order',
        'Interim care order',
        'Case management',
        'Further case management',
        'Fact finding',
        'Issue resolution',
        'Final',
        'Judgment after hearing',
        'Discharge of care',
        'Family drug & alcohol court',
        'Placement hearing'
      ];
      const hearingTypes = await this.hearingTypesLabelLocator.allTextContents();
      expect(hearingTypes).toEqual(expectedHearingTypes);
    }
      async assertFeePaidJudgeTitle() {
          const FeePaidTiltleList = [
              'Deputy High Court Judge',
              'Recorder',
              'Deputy District Judge',
              'Deputy District Judge (Magistrates)',
          ];

          for (const title of FeePaidTiltleList) {
              await expect(this.feePaidJudgeTitleFieldSet).toContainText(title);
          }
      }
      async assertLegalAdvisorTitle() {
          const LegalAdviserTitle = [
              'Magistrates (JP)',
              'Legal Adviser'
          ];

          for (const title of LegalAdviserTitle) {
              await expect(this.legalAdviserTitlesFieldSet).toContainText(title);
          }

      }

      async assertJudgeType(){
          await await expect.soft(this.page.getByRole('radio', { name: 'Salaried judge' })).toBeVisible();
          await await expect.soft(this.page.getByRole('radio', { name: 'Fee paid judge' })).toBeVisible();
          await await expect.soft(this.page.getByRole('radio', { name: 'Legal adviser' })).toBeVisible();
      }

      async selectjudgeType(judgeType: string) {
          await this.assertJudgeType();
          await this.page.getByRole('radio', { name: `${judgeType}` }).check();
      }
  };
}
