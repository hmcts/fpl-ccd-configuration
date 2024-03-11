import { type Page, type Locator } from "@playwright/test";
export class BasePage {
  readonly nextStep: Locator;
  readonly go: Locator;
  readonly page: Page;
  readonly saveAndContinue: Locator;
  readonly continueButton: Locator;
  readonly checkYourAnswersHeader: Locator;

  constructor(page: Page) {
    this.page = page;
    this.nextStep = page.getByLabel("Next step");
    this.go = page.getByRole("button", { name: "Go" });
    this.saveAndContinue = page.getByRole("button", { name: "Save and continue" });
    this.continueButton = page.getByRole("button", { name: "Continue" });
    this.checkYourAnswersHeader = page.getByRole('heading', { name: 'Check your answers' });
  }

  async gotoNextStep(eventName: string) {
    await this.nextStep.selectOption(eventName);
    await this.go.click();
  }

  async checkYourAnsAndSubmit() {
    await this.saveAndContinue.click();
  }

  async tabNavigation(tabName: string) {
    await this.page.getByText(tabName).click();
  }

  async clickContinue() {
    await this.continueButton.click();
  }
  
  async clickContinue(){
    await this.continue.click();
  }

  async signOut() {
    await this.page.getByText('Sign out').click();
  }
}
