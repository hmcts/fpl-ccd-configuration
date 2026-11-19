import { type Page, type Locator, expect } from "@playwright/test";
import { BasePage } from "./base-page";

export class SubmitCase extends BasePage{
 // readonly page: Page;


  public constructor(page: Page) {
    super(page);

  }
}
