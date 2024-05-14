import { type Page, type Locator, expect } from "@playwright/test";

export class OtherProceedings {
  readonly page: Page;
  readonly otherProceedingsHeading: Locator;
  readonly areThereAnyPastOrOngoingProccedingsReleventToCase: Locator;

public constructor(page: Page) {
    this.page = page;
    this.otherProceedingsHeading = page.getByRole('heading', { name: 'Other Proceedings' });
    this.areThereAnyPastOrOngoingProccedingsReleventToCase = page.getByRole('radio',  { name: 'No', exact: true });
       }
    
  async otherProceedingsSmokeTest() {
    await this.otherProceedingsHeading.isVisible();
    await this.areThereAnyPastOrOngoingProccedingsReleventToCase.check();  
  }
}
