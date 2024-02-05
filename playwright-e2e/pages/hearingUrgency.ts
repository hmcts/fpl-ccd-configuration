import { type Page, type Locator, expect } from "@playwright/test";

export class HearingUrgency{
  readonly page: Page;
  readonly HearingNeededText: Locator;
  readonly HearingUrgencyHeading: Locator;
  readonly Withing18daysRadioBtn:Locator;
  readonly Withing12daysRadioBtn:Locator;
  readonly Withing7dayRadioBtn:Locator;
  readonly Withing2daysRadioBtn:Locator;
  readonly SamedayRadioBtn:Locator;
  readonly Other1RadioBtn:Locator;
  readonly GiveReasonOption1:Locator;
  readonly GiveReasonOption2:Locator;
  readonly HearingReasonBox1:Locator;
  readonly BoxText1: Locator;
  readonly BoxText2: Locator;
  readonly TypeOfHearingOptions:Locator;
  readonly StandardcaseManagementHearing:Locator;
  readonly UrgentPreliminaryCaseManagementHearing:Locator;
  readonly ContestedInterrinCareOrder:Locator;
  readonly AcceleratedDischargeOfCare:Locator;
  readonly Other2:Locator;
  readonly HearingWithoutNoticeOfHearing:Locator;
  readonly HearingWithoutNoticeRadioBtnYes:Locator;
  readonly HearingWithoutNoticeRadioBtnNo:Locator;
  readonly ReducedNoticeText:Locator;
  readonly ReducedNoticeYesRadioBtn:Locator;
  readonly ReducedNoticeNoRadioBtn:Locator;
  readonly AwareOfProceedingsText:Locator;
  readonly AwareOfProceedingsYesRadioBtn:Locator;
  readonly AwareOfProceedingsNoRadioBtn:Locator;
  readonly ContinueButton:Locator;
  readonly CancelButton:Locator;
  readonly PreviousButton:Locator;
  HearingWithoutNotice: Locator;
  static HearingUrgentlyHeading: any;


  public constructor(page: Page) {
    this.page = page;
    this.HearingNeededText = page.locator("h2.heading-h2");
    this.Withing18daysRadioBtn = page.locator("#hearing_timeFrame-Within 18 days");
    this.Withing12daysRadioBtn = page.locator("#hearing_timeFrame-Within 12 days");
    this.Withing7dayRadioBtn = page.locator("#hearing_timeFrame-Within 7 days");
    this.Withing2daysRadioBtn = page.locator("#hearing_timeFrame-Within 2 days");
    this.SamedayRadioBtn = page.locator("#hearing_timeFrame-Same day");
    this.Other1RadioBtn = page.locator("#hearing_timeFrame-Other");
    this.GiveReasonOption1= page.locator('//*[@id="hearing_hearing"]/fieldset/ccd-field-write[2]/div/ccd-write-text-area-field/div/label/span');
    this.GiveReasonOption2 = page.locator(".form-label");
    this.BoxText1 =page.locator("#hearing_reason");
    this.TypeOfHearingOptions =page.locator(".form-label");
    this.StandardcaseManagementHearing =page.locator("#hearing_type-Standard case management hearing");
    this.UrgentPreliminaryCaseManagementHearing =page.locator("#hearing_type-Urgent preliminary case management heari");
    this.ContestedInterrinCareOrder =page.locator("#hearing_type-Urgent preliminary case management heari");
    this.AcceleratedDischargeOfCare = page .locator ("#hearing_type-Accelerated discharge of care");
    this.Other2 = page.locator("#hearing_type-Other");
    this.HearingWithoutNotice =page.locator("#hearing_withoutNotice");
    this.HearingWithoutNoticeRadioBtnYes = page. locator ("#hearing_withoutNotice_Yes");
    this.HearingWithoutNoticeRadioBtnNo = page. locator("#hearing_withoutNotice_No");
    this.ReducedNoticeText =page. locator ("#hearing_reducedNotice");
    this.ReducedNoticeYesRadioBtn = page . locator ("#hearing_reducedNotice_Yes");
    this.ReducedNoticeNoRadioBtn = page . locator ("#hearing_reducedNotice_Yes");
    this.AwareOfProceedingsText = page.locator("#hearing_respondentsAware");
    this.AwareOfProceedingsYesRadioBtn =page.locator("#hearing_respondentsAware_Yes");
    this.AwareOfProceedingsNoRadioBtn =page.locator("#hearing_respondentsAware_No");
    this.ContinueButton =page.locator(".button");
    this.CancelButton = page.locator (".cancel");
    this.PreviousButton = page .locator(".button.button-secondary");

}


async InputOnGiveReasionOption1(reason:any){
  await this.GiveReasonOption1.waitFor();
  await this.GiveReasonOption1.fill(reason);
}
async ClickOnHearingWithoutNoticeYes(){
  await this.HearingWithoutNoticeRadioBtnYes.waitFor();
  await this.HearingWithoutNoticeRadioBtnYes.click();
}

async ClickOnHearingWithoutNoticeNo(){
  await this.ReducedNoticeYesRadioBtn.waitFor();
  await this.ReducedNoticeYesRadioBtn.click();

}

async ClickOnAwareOfProceedingsTextNo(){
  await this.AwareOfProceedingsNoRadioBtn.waitFor();
  await this.AwareOfProceedingsNoRadioBtn.click();

}
    
async ClickOnContinueButton(){
  await this.ContinueButton.waitFor();
  await this.ContinueButton.click();

}
async ClickOnPreviousButton(){
  await this.PreviousButton.waitFor();
  await this.PreviousButton.click();

}
async ClickOnCancelButton(){
  await this.CancelButton.waitFor();
  await this.CancelButton.click();

}
  static WhatTypeOfHearingDoYouNeed(arg0: string) {
    throw new Error("Method not implemented.");
  }
  static GiveReasonTextBox() {
    throw new Error("Method not implemented.");
  }
  static WithoutNoticeHearing(arg0: string) {
    throw new Error("Method not implemented.");
  }
  static NeedAHearingWithReducedNoise(arg0: string) {
    throw new Error("Method not implemented.");
  }
  static RespondentsAwareOfProceedings(arg0: string) {
    throw new Error("Method not implemented.");
  }
  static saveAndContinueButton: any;
  static checkYourAnswers: any;
  static continueButton: any;
  static WhenDoYouNeedHearingRadio(arg0: string) {
    throw new Error("Method not implemented.");
  }

}

  // readonly page: Page;
  // readonly timeFrameLabels: string[];
  // readonly typeOfHearingLabels: string[];
  // readonly doYouNeedAWithoutNoticeHearing: string[];
  // readonly doYouNeedAHearingWithReducedNotice: string[];
  // readonly areRespondentsAwareOfProceedings: string[];
  // readonly hearingNeeded: Record<string, Locator>;
  // readonly continueButton: Locator;
  // readonly checkYourAnswers: Locator;
  // readonly saveAndContinueButton: Locator;

  // readonly doYouNeedAWithoutNoticeGroup: Locator;
  // readonly doYouNeedAHearingWithReducedNoiseGroup: Locator;
  // readonly areRespondentsAwareOfProceedingsGroup: Locator;

  // readonly giveReasonTextBox: Locator;
  // HearingUrgencyHeading: any;
  // Withing18daysRadioBtn: any;
  // HearingWithoutNoticeRadioBtnYes: any;
  // AwareOfProceedingsNoRadioBtn: any;
  // CancelButton: any;
  // PreviousButton: any;
  // ReducedNoticeYesRadioBtn: any;
  // GiveReasonOption1: any;
  
  


  //     this.timeFrameLabels = [
  //       "Within 18 days",
  //       "Within 12 days",
  //       "Within 7 days",
  //       "Within 2 days",
  //       "Same day",
  //       "Other",
  //     ];
  //     this.typeOfHearingLabels = [
  //       "Standard case management",
  //       "Urgent preliminary case",
  //       "Contested interim care order",
  //       "Accelerated discharge of care",
  //       "Other",
  //     ];
//       this.doYouNeedAWithoutNoticeHearing = ["Yes", "No"];
//       this.doYouNeedAHearingWithReducedNotice = ["Yes", "No"];
//       this.areRespondentsAwareOfProceedings = ["Yes", "No"];
  
//       this.hearingNeeded = {};
  
//       [
//         ...this.timeFrameLabels,
//         ...this.typeOfHearingLabels,
//         ...this.doYouNeedAWithoutNoticeHearing,
//         ...this.doYouNeedAHearingWithReducedNotice,
//         ...this.areRespondentsAwareOfProceedings,
//       ].forEach((label) => {
//         this.hearingNeeded[label] = page.getByLabel(label);
//       });
  
//       this.continueButton = page.getByRole("button", { name: "Continue" });
//       this.checkYourAnswers = page.getByRole("heading", {
//         name: "Check your answers",
//       });
//       this.saveAndContinueButton = page.getByRole("button", {
//         name: "Save and continue",
//       });
  
//       this.giveReasonTextBox = page.getByRole("textbox", {
//         name: "*Give reason (Optional)",
//       });
  
//       // Adding group locators
//       this.doYouNeedAWithoutNoticeGroup = page.getByRole("group", {
//         name: "Do you need a without notice hearing",
//       });
//       this.doYouNeedAHearingWithReducedNoiseGroup = page.getByRole("group", {
//         name: "Do you need a hearing with reduced notice",
//       });
//       this.areRespondentsAwareOfProceedingsGroup = page.getByRole("group", {
//         name: "Are respondents aware of proceedings",
//       });
//     }
  
//     async handleHearingOption(group: Locator, option: string) {
//       const locator = await group.getByLabel(option);
//       await expect(locator).toBeVisible();
//       await locator.click();
//     }
  
//     async WhenDoYouNeedHearingRadio(timeFrame: string) {
//       const locator = this.hearingNeeded[timeFrame];
//       await expect(locator).toBeVisible();
//       await locator.click();
//     }
  
//     async WhatTypeOfHearingDoYouNeed(typeOfHearing: string) {
//       const locator = this.hearingNeeded[typeOfHearing];
//       await expect(locator).toBeVisible();
//       await locator.click();
//     }
  
//     async WithoutNoticeHearing(doYouNeedAWithoutNoticeHearing: string) {
//       await this.handleHearingOption(
//         this.doYouNeedAWithoutNoticeGroup,
//         doYouNeedAWithoutNoticeHearing,
//       );
//     }
  
//     async NeedAHearingWithReducedNoise(
//       doYouNeedAHearingWithReducedNoise: string,
//     ) {
//       await this.handleHearingOption(
//         this.doYouNeedAHearingWithReducedNoiseGroup,
//         doYouNeedAHearingWithReducedNoise,
//       );
//     }
  
//     async RespondentsAwareOfProceedings(
//       areRespondentsAwareOfProceedings: string,
//     ) {
//       await this.handleHearingOption(
//         this.areRespondentsAwareOfProceedingsGroup,
//         areRespondentsAwareOfProceedings,
//       );
//     }
  
    // async GiveReasonTextBox() {       await this.GiveReasonTextBox.isVisible();
    // await this.GiveReasonTextBox.fill("Eum laudantium tempor, yet magni beatae. Architecto tempor. Quae adipisci, and labore, but voluptate, but est voluptas. Ipsum error minima. Suscipit eiusmod excepteur veniam. Consequat aliqua ex. Nostrud elit nostrum fugiat, yet esse nihil. Natus anim perspiciatis, and illum, so magni. Consequuntur eiusmod, so error. Anim magna. Dolores nequeporro, yet tempora. Amet rem aliquid.");
    // this.HearingUrgencyHeading = page.locator("h1.govuk-heading-l");

    
/////////==============================//////////////////


// async assertPageURL(url: any){
//   await this.page.waitForURL(url)
//   expect(this.page).toHaveURL(url) 
// }

// async assertPageTitle(title: any){
//   expect(this.page).toHaveTitle(title)
// }


// async assertHearingUrgencyText(){
//   await this.HearingUrgencyHeading.waitFor();
//   expect(this.HearingUrgencyHeading.isVisible).toBeTruthy();
// }

//   async assertHearingNeededText(){
//   //await this.hearingNeeded.waitFor();
//   expect(this.hearingNeeded.isVisible).toBeTruthy();
// }

// async clickOnWithin18Days(){
//   await this.Withing18daysRadioBtn.waitFor();
//   await this.Withing18daysRadioBtn.click();
// }
// async ClickOnStandardCaseManagementHearing(){
 // await this.ClickOnStandardCaseManagementHearing.waitFor();
  //await this.ClickOnStandardCaseManagementHearing.click();

// checkYourAnswers: any;
  // WithoutNoticeHearing(arg0: string) {
  //   throw new Error("Method not implemented.");
  // }
  // static WhenDoYouNeedHearingRadio(arg0: string) {
  //   throw new Error("Method not implemented.");
  // }
  // RespondentsAwareOfProceedings(arg0: string) {
  //   throw new Error("Method not implemented.");
  // }
  // NeedAHearingWithReducedNoise(arg0: string) {
  //   throw new Error("Method not implemented.");
  // }
  // GiveReasonTextBox() {
  //   throw new Error("Method not implemented.");
  // }
  // WhatTypeOfHearingDoYouNeed(arg0: string) {
  //   throw new Error("Method not implemented.");
  // }

