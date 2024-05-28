import { type Page, type Locator, expect } from "@playwright/test";

export class HearingUrgency {
  readonly page: Page;
  readonly timeFrameLabels: string[];
  readonly typeOfHearingLabels: string[];
  readonly doYouNeedAWithoutNoticeHearing: string[];
  readonly doYouNeedAHearingWithReducedNotice: string[];
  readonly areRespondentsAwareOfProceedings: string[];
  readonly hearingNeeded: Record<string, Locator>;
  readonly continueButton: Locator;
  readonly checkYourAnswers: Locator;
  readonly saveAndContinueButton: Locator;

  readonly doYouNeedAWithoutNoticeGroup: Locator;
  readonly doYouNeedAHearingWithReducedNoiseGroup: Locator;
  readonly areRespondentsAwareOfProceedingsGroup: Locator;

  readonly giveReasonTextBox: Locator;

  public constructor(page: Page) {
    this.page = page;
    this.timeFrameLabels = [
      "Within 18 days",
      "Within 12 days",
      "Within 7 days",
      "Within 2 days",
      "Same day",
      "Other",
    ];
    this.typeOfHearingLabels = [
      "Standard case management",
      "Urgent preliminary case",
      "Contested interim care order",
      "Accelerated discharge of care",
      "Other",
    ];
    this.doYouNeedAWithoutNoticeHearing = ["Yes", "No"];
    this.doYouNeedAHearingWithReducedNotice = ["Yes", "No"];
    this.areRespondentsAwareOfProceedings = ["Yes", "No"];

    this.hearingNeeded = {};

    [
      ...this.timeFrameLabels,
      ...this.typeOfHearingLabels,
      ...this.doYouNeedAWithoutNoticeHearing,
      ...this.doYouNeedAHearingWithReducedNotice,
      ...this.areRespondentsAwareOfProceedings,
    ].forEach((label) => {
      this.hearingNeeded[label] = page.getByLabel(label);
    });

    this.continueButton = page.getByRole("button", { name: "Continue" });
    this.checkYourAnswers = page.getByRole("heading", {
      name: "Check your answers",
    });
    this.saveAndContinueButton = page.getByRole("button", {
      name: "Save and continue",
    });

    this.giveReasonTextBox = page.getByRole("textbox", {
      name: "*Give reason (Optional)",
    });

    // Adding group locators
    this.doYouNeedAWithoutNoticeGroup = page.getByRole("group", {
      name: "Do you need a without notice hearing",
    });
    this.doYouNeedAHearingWithReducedNoiseGroup = page.getByRole("group", {
      name: "Do you need a hearing with reduced notice",
    });
    this.areRespondentsAwareOfProceedingsGroup = page.getByRole("group", {
      name: "Are respondents aware of proceedings",
    });
  }

  async handleHearingOption(group: Locator, option: string) {
    const locator = await group.getByLabel(option);
    await expect(locator).toBeVisible();
    await locator.click();
  }

  async whenDoYouNeedHearingRadio(timeFrame: string) {
    const locator = this.hearingNeeded[timeFrame];
    await expect(locator).toBeVisible();
    await locator.click();
  }

  async whatTypeOfHearingDoYouNeed(typeOfHearing: string) {
    const locator = this.hearingNeeded[typeOfHearing];
    await expect(locator).toBeVisible();
    await locator.click();
  }

  async withoutNoticeHearing(doYouNeedAWithoutNoticeHearing: string) {
    await this.handleHearingOption(
      this.doYouNeedAWithoutNoticeGroup,
      doYouNeedAWithoutNoticeHearing,
    );
  }

  async needAHearingWithReducedNoise(
    doYouNeedAHearingWithReducedNoise: string,
  ) {
    await this.handleHearingOption(
      this.doYouNeedAHearingWithReducedNoiseGroup,
      doYouNeedAHearingWithReducedNoise,
    );
  }

  async respondentsAwareOfProceedings(
    areRespondentsAwareOfProceedings: string,
  ) {
    await this.handleHearingOption(
      this.areRespondentsAwareOfProceedingsGroup,
      areRespondentsAwareOfProceedings,
    );
  }

  async giveReasonTextBoxFill() {
    //await expect(this.giveReasonTextBox).toBeVisible();
    await this.giveReasonTextBox.fill("Eum laudantium tempor, yet magni beatae. Architecto tempor. Quae adipisci, and labore, but voluptate, but est voluptas. Ipsum error minima. Suscipit eiusmod excepteur veniam. Consequat aliqua ex. Nostrud elit nostrum fugiat, yet esse nihil. Natus anim perspiciatis, and illum, so magni. Consequuntur eiusmod, so error. Anim magna. Dolores nequeporro, yet tempora. Amet rem aliquid.");
  }
}
