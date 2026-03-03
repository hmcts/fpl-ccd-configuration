import {expect, type Locator, Page} from "@playwright/test";
import {BasePage} from "../base-page";

export function HearingDetailsMixin() {
  return class extends BasePage {
      readonly hearingTypesLabelLocator: Locator;
      readonly feePaidJudgeTitleFieldSet: Locator;
      readonly legalAdviserTitlesFieldSet: Locator;
      readonly hearingNotification:Locator;

    constructor(page: Page) {
      super(page);
      this.hearingTypesLabelLocator = this.page.locator('#hearingType .multiple-choice > label');
      this.feePaidJudgeTitleFieldSet = this.page.getByRole('group',{name:'Select judge title'});
      this.legalAdviserTitlesFieldSet = this.page.getByRole('group',{name:'Judge or magistrate\'s title'});
      this.hearingNotification = this.page.getByRole('group', { name: 'Do you want to send a notice' })

    }

    async completeHearingDetails(hearingType: string) {
      await expect(this.page.getByText('Type of hearing')).toBeVisible();
      await this.verifyHearingTypesSelection();
     await this.selectHearingType(hearingType);
      await this.selectHearingVenue('No');
      await this.selectHearingAttence('In person');
      await this.enterHearingDate();
      await this.enterHearingLength();
      await this.clickContinue();
    }
    async selectHearingVenue(previousVenue='No') {
        if (previousVenue === 'Yes') {
            await this.page.getByRole('radio', { name: 'Yes' }).check();
        }
        else{
            await this.page.locator('#hearingVenue').selectOption({ label: 'Swansea Crown Court' });
        }

    }
    async enterHearingLength() {
        await this.page.getByLabel('Set number of hours and').check();
        await this.page.getByLabel('Hearing length, in hours').fill('1');
        await this.page.getByLabel('Hearing length, in minutes').fill('30');
    }
    async enterHearingDate() {
        await this.page.getByRole('textbox', { name: 'Day' }).fill('5');
        await this.page.getByRole('textbox', { name: 'Month' }).fill((new Date().getMonth()+1).toString());
        await this.page.getByRole('textbox', { name: 'Year' }).fill((new Date().getUTCFullYear()+1).toString());
        await this.page.getByRole('textbox', { name: 'Hour' }).fill('01');
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
      async assertLastHearingVenue(courtname: string) {
          await expect(this.page.locator('ccd-field-read-label', {hasText: 'Last court'}).locator('dd')).
          toHaveText(` ${courtname}`);

      }
      async selectHearingType(hearingType: string) {
          await this.page.getByRole('radio', { name: `${hearingType}`, exact: true }).check();
      }
      async selectHearingAttence(attendance: string) {
          await this.page.getByRole('group', {name: 'Hearing attendance', exact: true}).getByLabel(attendance).check();
      }
      async selectAllocatedJusticeAsHearingJudge(){
        await this.page.getByRole('group', {name: 'Is the allocated judge or magistrate sitting this hearing?', exact: true}).getByLabel('Yes').check();
      }
      async sendHearingNotifications(){

          await this.hearingNotification.getByLabel('Yes').check();
          await this.page.getByRole('textbox', { name: 'Additional notes (Optional)' }).fill('test');

      }
  };
}
