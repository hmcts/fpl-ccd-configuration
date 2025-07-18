import { BasePage } from "./base-page";
import { expect, Page } from "@playwright/test";
import config from "../settings/test-docs/config";

export class Placement extends BasePage {

  constructor(page: Page) {
    super(page);

  }

  async submitPlacementOrder() {
    await this.page.getByLabel('Which child?').selectOption('Timothy Jones');
    await this.clickContinue();
    await this.page.locator("#placement_placementApplication").setInputFiles(config.testPdfFile);
    await this.waitForAllUploadsToBeCompleted();
    await this.page.waitForTimeout(5000);
    await this.page.locator('#placement_placementSupportingDocuments_0_document').setInputFiles(config.testPdfFile2);
    await this.waitForAllUploadsToBeCompleted();
    await this.page.waitForTimeout(5000);
    await this.page.locator('#placement_placementSupportingDocuments_1_document').setInputFiles(config.testPdfFile3);
    await this.waitForAllUploadsToBeCompleted();
    await this.page.waitForTimeout(5000);
    await this.page.locator('#placement_placementConfidentialDocuments_0_document').setInputFiles(config.testPdfFile4);
    await this.waitForAllUploadsToBeCompleted();
    await this.clickContinue();
    await this.page.getByRole('radio', { name: 'Yes' }).check();
    await this.clickContinue();
    await this.payForApplication();
    await this.checkYourAnsAndSubmit();
  }

  public async payForApplication() {
    await expect(this.page.getByText('£556.00')).toBeVisible();
    await this.page.getByLabel('Payment by account (PBA) number').fill('PBA1234567');
    await this.page.getByLabel('Customer reference').fill('Customer reference');
    await this.clickContinue();
  }

  public async noticeOfPlacement() {
    await this.submitPlacementOrder();
    await this.page.getByLabel('Next step').selectOption('Notice of Placement - Hearing');
    await this.page.getByRole('button', { name: 'Go' }).click();
    await this.page.getByLabel('Which placement application').selectOption('Timothy Jones');
    await this.page.getByRole('button', { name: 'Continue' }).click();
    await this.page.getByRole('textbox', { name: 'Day' }).fill('10');
    await this.page.getByRole('textbox', { name: 'Month' }).fill('10');
    await this.page.getByRole('textbox', { name: 'Year' }).fill('2024');
    await this.page.getByRole('textbox', { name: 'Hour', exact: true }).fill('10');
    await this.page.getByRole('textbox', { name: 'Hearing duration (hours)' }).fill('1');
    await this.page.getByLabel('Hearing venue').selectOption('2: -1');
    await this.page.getByRole('button', { name: 'Continue' }).click();
    await this.page.getByRole('button', { name: 'Continue' }).click();
    await this.page.getByRole('button', { name: 'Save and continue' }).click();
  }
}
