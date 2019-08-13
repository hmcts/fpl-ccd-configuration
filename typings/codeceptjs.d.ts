
type ICodeceptCallback = (i: CodeceptJS.I, config:CodeceptJS.config, loginPage:CodeceptJS.loginPage, caseListPage:CodeceptJS.caseListPage, createCasePage:CodeceptJS.createCasePage, eventSummaryPage:CodeceptJS.eventSummaryPage, caseViewPage:CodeceptJS.caseViewPage, selectHearingPage:CodeceptJS.selectHearingPage, enterGroundsPage:CodeceptJS.enterGroundsPage, enterFactorsAffectingParentingPage:CodeceptJS.enterFactorsAffectingParentingPage, enterInternationalElementsPage:CodeceptJS.enterInternationalElementsPage, enterRiskAndHarmToChildPage:CodeceptJS.enterRiskAndHarmToChildPage, uploadDocumentsPage:CodeceptJS.uploadDocumentsPage, enterApplicantPage:CodeceptJS.enterApplicantPage, enterChildrenPage:CodeceptJS.enterChildrenPage, enterOtherProceedingsPage:CodeceptJS.enterOtherProceedingsPage, attendingHearingPage:CodeceptJS.attendingHearingPage, enterAllocationProposalPage:CodeceptJS.enterAllocationProposalPage, enterRespondentsPage:CodeceptJS.enterRespondentsPage, enterOthersPage:CodeceptJS.enterOthersPage, ordersNeededPage:CodeceptJS.ordersNeededPage, enterFamilyManPage:CodeceptJS.enterFamilyManPage, changeCaseNamePage:CodeceptJS.changeCaseNamePage, submitApplicationPage:CodeceptJS.submitApplicationPage, sendToGatekeeperPage:CodeceptJS.sendToGatekeeperPage, standardDirectionsPage:CodeceptJS.standardDirectionsPage, deleteApplicationPage:CodeceptJS.deleteApplicationPage, ...args: any) => void;

declare class FeatureConfig {
  retry(times: number): FeatureConfig
  timeout(seconds: number): FeatureConfig
  config(config: object): FeatureConfig
  config(helperName: string, config: object): FeatureConfig
}

declare class ScenarioConfig {
  throws(err: any): ScenarioConfig;
  fails(): ScenarioConfig;
  retry(times: number): ScenarioConfig
  timeout(timeout: number): ScenarioConfig
  inject(inject: object): ScenarioConfig
  config(config: object): ScenarioConfig
  config(helperName: string, config: object): ScenarioConfig
  injectDependencies(dependencies: { [key: string]: any }): ScenarioConfig
}

interface ILocator {
  id?: string;
  xpath?: string;
  css?: string;
  name?: string;
  frame?: string;
  android?: string;
  ios?: string;
}

type LocatorOrString = string | ILocator | Locator;

declare class Helper {
  /** Abstract method to provide required config options */
  static _config(): any;
  /** Abstract method to validate config */
  _validateConfig<T>(config: T): T;
  /** Sets config for current test */
  _setConfig(opts: any): void;
  /** Hook executed before all tests */
  _init(): void
  /** Hook executed before each test. */
  _before(): void
  /** Hook executed after each test */
  _after(): void
  /**
   * Hook provides a test details
   * Executed in the very beginning of a test
   */
  _test(test: () => void): void
  /** Hook executed after each passed test */
  _passed(test: () => void): void
  /** Hook executed after each failed test */
  _failed(test: () => void): void
  /** Hook executed before each step */
  _beforeStep(step: () => void): void
  /** Hook executed after each step */
  _afterStep(step: () => void): void
  /** Hook executed before each suite */
  _beforeSuite(suite: () => void): void
  /** Hook executed after each suite */
  _afterSuite(suite: () => void): void
  /** Hook executed after all tests are executed */
  _finishTest(suite: () => void): void
  /**Access another configured helper: this.helpers['AnotherHelper'] */
  readonly helpers: any
  /** Print debug message to console (outputs only in debug mode) */
  debug(msg: string): void

  debugSection(section: string, msg: string): void
}

declare class Locator {
  constructor(locator: LocatorOrString, defaultType?: string);

  or(locator: LocatorOrString): Locator;
  find(locator: LocatorOrString): Locator;
  withChild(locator: LocatorOrString): Locator;
  withDescendant(locator: LocatorOrString): Locator;
  at(position: number): Locator;
  first(): Locator;
  last(): Locator;
  inside(locator: LocatorOrString): Locator;
  before(locator: LocatorOrString): Locator;
  after(locator: LocatorOrString): Locator;
  withText(text: string): Locator;
  withAttr(attrs: object): Locator;
  as(output: string): Locator;
}


declare function actor(customSteps?: {
  [action: string]: (this: CodeceptJS.I, ...args: any[]) => void
}): CodeceptJS.I;
declare function actor(customSteps?: {}): CodeceptJS.I;
declare function Feature(title: string, opts?: {}): FeatureConfig;
declare const Scenario: {
  (title: string, callback: ICodeceptCallback): ScenarioConfig;
  (title: string, opts: {}, callback: ICodeceptCallback): ScenarioConfig;
  only(title: string, callback: ICodeceptCallback): ScenarioConfig;
  only(title: string, opts: {}, callback: ICodeceptCallback): ScenarioConfig;
}
declare interface IScenario {
  Scenario(title: string, callback: ICodeceptCallback): ScenarioConfig;
  Scenario(title: string, opts: {}, callback: ICodeceptCallback): ScenarioConfig;
}
declare function xScenario(title: string, callback: ICodeceptCallback): ScenarioConfig;
declare function xScenario(title: string, opts: {}, callback: ICodeceptCallback): ScenarioConfig;
declare interface IData {
  Scenario(title: string, callback: ICodeceptCallback): ScenarioConfig;
  Scenario(title: string, opts: {}, callback: ICodeceptCallback): ScenarioConfig;
  only: IScenario;
}
declare function Data(data: any): IData;
declare function xData(data: any): IData;
declare function Before(callback: ICodeceptCallback): void;
declare function BeforeSuite(callback: ICodeceptCallback): void;
declare function After(callback: ICodeceptCallback): void;
declare function AfterSuite(callback: ICodeceptCallback): void;

declare function inject(): { [key: string]: any };
declare function locate(selector: LocatorOrString): Locator;
declare function within(selector: LocatorOrString, callback: Function): Promise<any>;
declare function session(selector: LocatorOrString, callback: Function): Promise<any>;
declare function session(selector: LocatorOrString, config: any, callback: Function): Promise<any>;
declare function pause(): void;
declare function secret(secret: any): string;

declare const codeceptjs: any;

declare namespace CodeceptJS {
  export interface I {
    amAcceptingPopups() : void,
    acceptPopup() : void,
    amCancellingPopups() : void,
    cancelPopup() : void,
    seeInPopup(text: string) : void,
    grabPopupText() : Promise<string>,
    amOnPage(url: string) : void,
    resizeWindow(width: number, height: number) : void,
    haveRequestHeaders(customHeaders: string) : void,
    moveCursorTo(locator: LocatorOrString, offsetX?: number, offsetY?: number) : void,
    dragAndDrop(srcElement: string, destElement: string) : void,
    refreshPage() : void,
    scrollPageToTop() : void,
    scrollPageToBottom() : void,
    scrollTo(locator: LocatorOrString, offsetX?: number, offsetY?: number) : void,
    seeInTitle(text: string) : void,
    grabPageScrollPosition() : Promise<string>,
    seeTitleEquals(text: string) : void,
    dontSeeInTitle(text: string) : void,
    grabTitle() : Promise<string>,
    switchToNextTab(num?: number) : void,
    switchToPreviousTab(num?: number) : void,
    closeCurrentTab() : void,
    closeOtherTabs() : void,
    openNewTab() : void,
    grabNumberOfOpenTabs() : Promise<string>,
    seeElement(locator: LocatorOrString) : void,
    dontSeeElement(locator: LocatorOrString) : void,
    seeElementInDOM(locator: LocatorOrString) : void,
    dontSeeElementInDOM(locator: LocatorOrString) : void,
    click(locator: LocatorOrString, context?: LocatorOrString) : void,
    clickLink(locator: LocatorOrString, context?: LocatorOrString) : void,
    handleDownloads(downloadPath?: string) : void,
    downloadFile(locator: LocatorOrString, customName: string) : void,
    doubleClick(locator: LocatorOrString, context?: LocatorOrString) : void,
    rightClick(locator: LocatorOrString, context?: LocatorOrString) : void,
    checkOption(field: LocatorOrString, context?: LocatorOrString) : void,
    uncheckOption(field: LocatorOrString, context?: LocatorOrString) : void,
    seeCheckboxIsChecked(field: LocatorOrString) : void,
    dontSeeCheckboxIsChecked(field: LocatorOrString) : void,
    pressKey(key: string) : void,
    fillField(field: LocatorOrString, value: string) : void,
    clearField(field: LocatorOrString) : void,
    appendField(field: LocatorOrString, value: string) : void,
    seeInField(field: LocatorOrString, value: string) : void,
    dontSeeInField(field: LocatorOrString, value: string) : void,
    attachFile(locator: LocatorOrString, pathToFile: string) : void,
    selectOption(select: LocatorOrString, option: string) : void,
    grabNumberOfVisibleElements(locator: LocatorOrString) : Promise<string>,
    seeInCurrentUrl(url: string) : void,
    dontSeeInCurrentUrl(url: string) : void,
    seeCurrentUrlEquals(url: string) : void,
    dontSeeCurrentUrlEquals(url: string) : void,
    see(text: string, context?: LocatorOrString) : void,
    seeTextEquals(text: string, context?: LocatorOrString) : void,
    dontSee(text: string, context?: LocatorOrString) : void,
    grabSource() : Promise<string>,
    grabBrowserLogs() : Promise<string>,
    grabCurrentUrl() : Promise<string>,
    seeInSource(text: string) : void,
    dontSeeInSource(text: string) : void,
    seeNumberOfElements(locator: LocatorOrString, num: number) : void,
    seeNumberOfVisibleElements(locator: LocatorOrString, num: number) : void,
    setCookie(cookie: string) : void,
    seeCookie(name: string) : void,
    dontSeeCookie(name: string) : void,
    grabCookie(name: string) : Promise<string>,
    clearCookie(name: string) : void,
    executeScript(fn: Function) : void,
    executeAsyncScript(fn: Function) : void,
    grabTextFrom(locator: LocatorOrString) : Promise<string>,
    grabValueFrom(locator: LocatorOrString) : Promise<string>,
    grabHTMLFrom(locator: LocatorOrString) : Promise<string>,
    grabCssPropertyFrom(locator: LocatorOrString, cssProperty: string) : Promise<string>,
    seeCssPropertiesOnElements(locator: LocatorOrString, cssProperties: string) : void,
    seeAttributesOnElements(locator: LocatorOrString, attributes: string) : void,
    dragSlider(locator: LocatorOrString, offsetX?: number) : void,
    grabAttributeFrom(locator: LocatorOrString, attr: string) : Promise<string>,
    saveScreenshot(fileName: string, fullPage: string) : void,
    wait(sec: number) : void,
    waitForEnabled(locator: LocatorOrString, sec: number) : void,
    waitForValue(field: LocatorOrString, value: string, sec: number) : void,
    waitNumberOfVisibleElements(locator: LocatorOrString, num: number, sec: number) : void,
    waitForElement(locator: LocatorOrString, sec: number) : void,
    waitForVisible(locator: LocatorOrString, sec: number) : void,
    waitForInvisible(locator: LocatorOrString, sec: number) : void,
    waitToHide(locator: LocatorOrString, sec: number) : void,
    waitInUrl(urlPart: string, sec?: number) : void,
    waitUrlEquals(urlPart: string, sec?: number) : void,
    waitForText(text: string, sec?: number, context?: LocatorOrString) : void,
    waitForRequest(urlOrPredicate: string, sec?: number) : void,
    waitForResponse(urlOrPredicate: string, sec?: number) : void,
    switchTo(locator: LocatorOrString) : void,
    waitForFunction(fn: Function, argsOrSec?: string, sec?: number) : void,
    waitForNavigation(opts?: string) : void,
    waitUntil(fn: Function, sec?: number) : void,
    waitUntilExists(locator: LocatorOrString, sec: number) : void,
    waitForDetached(locator: LocatorOrString, sec: number) : void,
    grabDataFromPerformanceTiming() : Promise<string>,
    debug(msg: string) : void,
    debugSection(section: string, msg: string) : void,
    clickBrowserBack() : void,
    reloadPage() : void,
    logInAndCreateCase(username: string, password: string) : void,
    continueAndSave() : void,
    continueAndProvideSummary(summary: string, description: string) : void,
    continueAndSubmit() : void,
    seeEventSubmissionConfirmation(event: string) : void,
    clickHyperlink(link: string, urlNavigatedTo: string) : void,
    seeDocument(title: string, name: string, status: string, reason?: string) : void,
    seeAnswerInTab(questionNo: string, complexTypeHeading: string, question: string, answer: string) : void,
    signOut() : void,
    navigateToCaseDetails(caseId: string) : void,
    say: () => any; 
    retryStep(opts: string) : void,

  }

  export interface config {

  }


  export interface loginPage {
    signIn(username: string, password: string) : void,

  }


  export interface caseListPage {
    changeStateFilter(desiredState: string) : void,

  }


  export interface createCasePage {
    enterCaseName(caseName?: string) : void,
    populateForm() : void,

  }


  export interface eventSummaryPage {
    submit(button: string) : void,
    provideSummaryAndSubmit(button: string, summary: string, description: string) : void,

  }


  export interface caseViewPage {
    goToNewActions(actionSelected: string) : void,
    selectTab(tab: string) : void,

  }


  export interface selectHearingPage {
    enterTimeFrame(reason?: string) : void,
    enterHearingType() : void,
    enterWithoutNoticeHearing() : void,
    enterReducedHearing() : void,
    enterRespondentsAware() : void,

  }


  export interface enterGroundsPage {
    enterThresholdCriteriaDetails() : void,
    enterGroundsForEmergencyProtectionOrder() : void,

  }


  export interface enterFactorsAffectingParentingPage {
    completeAlcoholOrDrugAbuse() : void,
    completeDomesticViolence() : void,
    completeAnythingElse() : void,

  }


  export interface enterInternationalElementsPage {
    halfFillForm() : void,
    fillForm() : void,

  }


  export interface enterRiskAndHarmToChildPage {
    completePhysicalHarm() : void,
    completeEmotionalHarm() : void,
    completeSexualAbuse() : void,
    completeNeglect() : void,

  }


  export interface uploadDocumentsPage {
    selectSocialWorkChronologyToFollow() : void,
    uploadSocialWorkStatement(file: string) : void,
    uploadSocialWorkAssessment(file: string) : void,
    uploadCarePlan(file: string) : void,
    uploadSWET(file: string) : void,
    uploadThresholdDocument(file: string) : void,
    uploadChecklistDocument(file: string) : void,
    uploadAdditionalDocuments(file: string) : void,
    uploadCourtBundle(file: string) : void,

  }


  export interface enterApplicantPage {
    enterApplicantDetails(applicant: string) : void,
    enterSolicitorDetails(solicitor: string) : void,

  }


  export interface enterChildrenPage {
    fields() : void,
    addChild() : void,
    enterChildDetails(name: string, day: string, month: string, year: string, gender?: string) : void,
    defineChildSituation(day: string, month: string, year: string) : void,
    enterAddress(address: string) : void,
    enterKeyDatesAffectingHearing(keyDates?: string) : void,
    enterSummaryOfCarePlan(carePlan?: string) : void,
    defineAdoptionIntention() : void,
    enterParentsDetails(fatherResponsible?: string, motherName?: string, fatherName?: string) : void,
    enterSocialWorkerDetails(socialWorkerName?: string, socialWorkerTel?: string) : void,
    defineChildAdditionalNeeds() : void,
    defineContactDetailsVisibility() : void,
    enterLitigationIssues(litigationIssue?: string, litigationIssueDetail?: string) : void,

  }


  export interface enterOtherProceedingsPage {
    fields() : void,
    addNewProceeding() : void,
    selectYesForProceeding() : void,
    selectNoForProceeding() : void,
    selectOngoingProceedingStatus(status?: string) : void,
    enterProceedingInformation(otherProceedingData: string) : void,

  }


  export interface attendingHearingPage {
    enterInterpreter(details?: string) : void,
    enterIntermediary() : void,
    enterDisabilityAssistance(details?: string) : void,
    enterWelshProceedings() : void,
    enterExtraSecurityMeasures(details?: string) : void,
    enterSomethingElse(details?: string) : void,

  }


  export interface enterAllocationProposalPage {
    selectAllocationProposal(proposal: string) : void,
    enterProposalReason(reason: string) : void,

  }


  export interface enterRespondentsPage {
    fields() : void,
    addRespondent() : void,
    enterRespondent(respondent: string) : void,
    enterRelationshipToChild(relationship: string) : void,
    enterContactDetailsHidden(option: string, reason?: string) : void,
    enterLitigationIssues(litigationIssue?: string, litigationIssueDetail?: string) : void,

  }


  export interface enterOthersPage {
    fields() : void,
    addOther() : void,
    enterOtherDetails(other: string) : void,
    enterRelationshipToChild(childInformation: string) : void,
    enterContactDetailsHidden(option: string) : void,
    enterLitigationIssues(litigationIssue?: string, litigationIssueDetail?: string) : void,

  }


  export interface ordersNeededPage {
    checkCareOrder() : void,
    checkSupervisionOrder() : void,
    checkEducationSupervisionOrder() : void,
    checkEmergencyProtectionOrder() : void,
    checkOtherOrder() : void,
    enterDirections(details: string) : void,
    checkInterimCareOrder() : void,
    checkInterimSupervisionOrder() : void,
    checkWhereabouts() : void,
    checkEntry() : void,
    checkSearch() : void,
    checkProtectionOrdersOther() : void,
    enterProtectionOrdersDetails(details: string) : void,
    checkContact() : void,
    checkAssessment() : void,
    checkMedicalPractitioner() : void,
    checkExclusion() : void,
    checkProtectionDirectionsOther() : void,
    enterProtectionDirectionsDetails(details: string) : void,
    enterOrderDetails(details: string) : void,
    checkDirections() : void,

  }


  export interface enterFamilyManPage {
    enterCaseID(caseId?: string) : void,

  }


  export interface changeCaseNamePage {
    changeCaseName(caseName?: string) : void,

  }


  export interface submitApplicationPage {
    giveConsent() : void,

  }


  export interface sendToGatekeeperPage {
    enterEmail(email?: string) : void,

  }


  export interface standardDirectionsPage {
    uploadStandardDirections(file: string) : void,
    uploadAdditionalDocuments(file: string) : void,

  }


  export interface deleteApplicationPage {
    tickDeletionConsent() : void,

  }

}

declare module "codeceptjs" {
  export = CodeceptJS;
}
