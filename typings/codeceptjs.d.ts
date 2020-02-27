
type ICodeceptCallback = (i: CodeceptJS.I, config:any, loginPage:any, caseListPage:any, createCasePage:any, addEventSummaryPage:any, caseViewPage:any, selectHearingPage:any, enterGroundsPage:any, enterFactorsAffectingParentingPage:any, enterInternationalElementsPage:any, enterRiskAndHarmToChildPage:any, uploadDocumentsPage:any, enterApplicantPage:any, enterChildrenPage:any, enterOtherProceedingsPage:any, attendingHearingPage:any, enterAllocationProposalPage:any, enterRespondentsPage:any, enterOthersPage:any, ordersNeededPage:any, enterFamilyManPage:any, changeCaseNamePage:any, deleteApplicationPage:any) => void;

declare class FeatureConfig {
  retry(times:number): FeatureConfig
  timeout(seconds:number): FeatureConfig
  config(config:object): FeatureConfig
  config(helperName:string, config:object): FeatureConfig
}

declare class ScenarioConfig {
  throws(err:any) : ScenarioConfig;
  fails() : ScenarioConfig;
  retry(times:number): ScenarioConfig
  timeout(timeout:number): ScenarioConfig
  inject(inject:object): ScenarioConfig
  config(config:object): ScenarioConfig
  config(helperName:string, config:object): ScenarioConfig
}

interface ILocator {
  xpath?: string;
  css?: string;
  name?: string;
  value?: string;
  frame?: string;
  android?: string;
  ios?: string;
}

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
  _test(test): void
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
  get helpers(): any
  /** Print debug message to console (outputs only in debug mode) */
  debug(msg: string): void

  debugSection(section: string, msg: string): void
}

declare class Locator implements ILocator {
  xpath?: string;
  css?: string;
  name?: string;
  value?: string;
  frame?: string;
  android?: string;
  ios?: string;

  or(locator:string): Locator;
  find(locator:string): Locator;
  withChild(locator:string): Locator;
  find(locator:string): Locator;
  at(position:number): Locator;
  first(): Locator;
  last(): Locator;
  inside(locator:string): Locator;
  before(locator:string): Locator;
  after(locator:string): Locator;
  withText(locator:string): Locator;
  withAttr(locator:object): Locator;
  as(locator:string): Locator;
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
declare function xScenario(title: string, callback: ICodeceptCallback): ScenarioConfig;
declare function xScenario(title: string, opts: {}, callback: ICodeceptCallback): ScenarioConfig;
declare function Data(data: any): any;
declare function xData(data: any): any;
declare function Before(callback: ICodeceptCallback): void;
declare function BeforeSuite(callback: ICodeceptCallback): void;
declare function After(callback: ICodeceptCallback): void;
declare function AfterSuite(callback: ICodeceptCallback): void;

declare function locate(selector: string): Locator;
declare function locate(selector: ILocator): Locator;
declare function within(selector: string, callback: Function): Promise<any>;
declare function within(selector: ILocator, callback: Function): Promise<any>;
declare function session(selector: string, callback: Function): Promise<any>;
declare function session(selector: ILocator, callback: Function): Promise<any>;
declare function session(selector: string, config: any, callback: Function): Promise<any>;
declare function session(selector: ILocator, config: any, callback: Function): Promise<any>;
declare function pause(): void;

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
    moveCursorTo(locator: ILocator, offsetX?: number, offsetY?: number) : void,
    moveCursorTo(locator: string, offsetX?: number, offsetY?: number) : void,
    dragAndDrop(srcElement: string, destElement: string) : void,
    refreshPage() : void,
    scrollPageToTop() : void,
    scrollPageToBottom() : void,
    scrollTo(locator: ILocator, offsetX?: number, offsetY?: number) : void,
    scrollTo(locator: string, offsetX?: number, offsetY?: number) : void,
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
    seeElement(locator: ILocator) : void,
    seeElement(locator: string) : void,
    dontSeeElement(locator: ILocator) : void,
    dontSeeElement(locator: string) : void,
    seeElementInDOM(locator: ILocator) : void,
    seeElementInDOM(locator: string) : void,
    dontSeeElementInDOM(locator: ILocator) : void,
    dontSeeElementInDOM(locator: string) : void,
    click(locator: ILocator, context?: ILocator) : void,
    click(locator: string, context?: ILocator) : void,
    click(locator: ILocator, context?: string) : void,
    click(locator: string, context?: string) : void,
    clickLink(locator: ILocator, context?: ILocator) : void,
    clickLink(locator: string, context?: ILocator) : void,
    clickLink(locator: ILocator, context?: string) : void,
    clickLink(locator: string, context?: string) : void,
    doubleClick(locator: ILocator, context?: ILocator) : void,
    doubleClick(locator: string, context?: ILocator) : void,
    doubleClick(locator: ILocator, context?: string) : void,
    doubleClick(locator: string, context?: string) : void,
    rightClick(locator: ILocator, context?: ILocator) : void,
    rightClick(locator: string, context?: ILocator) : void,
    rightClick(locator: ILocator, context?: string) : void,
    rightClick(locator: string, context?: string) : void,
    checkOption(field: ILocator, context?: ILocator) : void,
    checkOption(field: string, context?: ILocator) : void,
    checkOption(field: ILocator, context?: string) : void,
    checkOption(field: string, context?: string) : void,
    seeCheckboxIsChecked(field: ILocator) : void,
    seeCheckboxIsChecked(field: string) : void,
    dontSeeCheckboxIsChecked(field: ILocator) : void,
    dontSeeCheckboxIsChecked(field: string) : void,
    pressKey(key: string) : void,
    fillField(field: ILocator, value: string) : void,
    fillField(field: string, value: string) : void,
    clearField(field: ILocator) : void,
    clearField(field: string) : void,
    appendField(field: ILocator, value: string) : void,
    appendField(field: string, value: string) : void,
    seeInField(field: ILocator, value: string) : void,
    seeInField(field: string, value: string) : void,
    dontSeeInField(field: ILocator, value: string) : void,
    dontSeeInField(field: string, value: string) : void,
    attachFile(locator: ILocator, pathToFile: string) : void,
    attachFile(locator: string, pathToFile: string) : void,
    selectOption(select: ILocator, option: string) : void,
    selectOption(select: string, option: string) : void,
    grabNumberOfVisibleElements(locator: ILocator) : Promise<string>,
    grabNumberOfVisibleElements(locator: string) : Promise<string>,
    seeInCurrentUrl(url: string) : void,
    dontSeeInCurrentUrl(url: string) : void,
    seeCurrentUrlEquals(url: string) : void,
    dontSeeCurrentUrlEquals(url: string) : void,
    see(text: string, context?: ILocator) : void,
    see(text: string, context?: string) : void,
    seeTextEquals(text: string, context?: ILocator) : void,
    seeTextEquals(text: string, context?: string) : void,
    dontSee(text: string, context?: ILocator) : void,
    dontSee(text: string, context?: string) : void,
    grabSource() : Promise<string>,
    grabBrowserLogs() : Promise<string>,
    grabCurrentUrl() : Promise<string>,
    seeInSource(text: string) : void,
    dontSeeInSource(text: string) : void,
    seeNumberOfElements(selector: string, num: number) : void,
    seeNumberOfVisibleElements(locator: ILocator, num: number) : void,
    seeNumberOfVisibleElements(locator: string, num: number) : void,
    setCookie(cookie: string) : void,
    seeCookie(name: string) : void,
    dontSeeCookie(name: string) : void,
    grabCookie(name: string) : Promise<string>,
    clearCookie(name: string) : void,
    executeScript(fn: Function) : void,
    executeAsyncScript(fn: Function) : void,
    grabTextFrom(locator: ILocator) : Promise<string>,
    grabTextFrom(locator: string) : Promise<string>,
    grabValueFrom(locator: ILocator) : Promise<string>,
    grabValueFrom(locator: string) : Promise<string>,
    grabHTMLFrom(locator: ILocator) : Promise<string>,
    grabHTMLFrom(locator: string) : Promise<string>,
    grabCssPropertyFrom(locator: ILocator, cssProperty: string) : Promise<string>,
    grabCssPropertyFrom(locator: string, cssProperty: string) : Promise<string>,
    seeCssPropertiesOnElements(locator: ILocator, cssProperties: string) : void,
    seeCssPropertiesOnElements(locator: string, cssProperties: string) : void,
    seeAttributesOnElements(locator: ILocator, attributes: string) : void,
    seeAttributesOnElements(locator: string, attributes: string) : void,
    grabAttributeFrom(locator: ILocator, attr: string) : Promise<string>,
    grabAttributeFrom(locator: string, attr: string) : Promise<string>,
    saveScreenshot(fileName: string, fullPage: string) : void,
    wait(sec: number) : void,
    waitForEnabled(locator: ILocator, sec: number) : void,
    waitForEnabled(locator: string, sec: number) : void,
    waitForValue(field: ILocator, value: string, sec: number) : void,
    waitForValue(field: string, value: string, sec: number) : void,
    waitNumberOfVisibleElements(locator: ILocator, num: number, sec: number) : void,
    waitNumberOfVisibleElements(locator: string, num: number, sec: number) : void,
    waitForElement(locator: ILocator, sec: number) : void,
    waitForElement(locator: string, sec: number) : void,
    waitForVisible(locator: ILocator, sec: number) : void,
    waitForVisible(locator: string, sec: number) : void,
    waitForInvisible(locator: ILocator, sec: number) : void,
    waitForInvisible(locator: string, sec: number) : void,
    waitToHide(locator: ILocator, sec: number) : void,
    waitToHide(locator: string, sec: number) : void,
    waitInUrl(urlPart: string, sec?: number) : void,
    waitUrlEquals(urlPart: string, sec?: number) : void,
    waitForText(text: string, sec?: number, context?: ILocator) : void,
    waitForText(text: string, sec?: number, context?: string) : void,
    waitForRequest(urlOrPredicate: string, sec?: number) : void,
    waitForResponse(urlOrPredicate: string, sec?: number) : void,
    switchTo(locator: ILocator) : void,
    switchTo(locator: string) : void,
    waitForFunction(fn: Function, argsOrSec?: string, sec?: number) : void,
    waitForNavigation(opts?: string) : void,
    waitUntil(fn: Function, sec?: number) : void,
    waitUntilExists(locator: ILocator, sec: number) : void,
    waitUntilExists(locator: string, sec: number) : void,
    waitForDetached(locator: ILocator, sec: number) : void,
    waitForDetached(locator: string, sec: number) : void,
    debug(msg: string) : void,
    debugSection(section: string, msg: string) : void,
    clickBrowserBack() : void,
    reloadPage() : void,
    navigateToUrl(url: string) : void,
    navigateToCaseDetails(caseId: string) : void,
    navigateToCaseList() : void,
    logInAndCreateCase(username: string, password: string) : void,
    completeEvent(buttonLocator) : Promise<void>,
    completeEvent(buttonLocator, changeDetails: { summary: string, description: string }) : Promise<void>,
    seeEventSubmissionConfirmation(event: string) : void,
    clickHyperlink(link: string, urlNavigatedTo: string) : void,
    seeDocument(title: string, name: string, status?: string, reason?: string) : void,
    seeAnswerInTab(questionNo: string | number, complexTypeHeading: string, question: string, answer: string | string[]) : void,
    seeNestedAnswerInTab(questionNo: string | number, complexTypeHeading: string, complexTypeSubHeading: string, question: string, answer: string | string[]) : void,
    seeCaseInSearchResult(caseId: string | number): void
    dontSeeCaseInSearchResult(caseId: string | number): void
    signIn(username: string, password: string) : void,
    signOut() : void,
    say(msg: string) : void,
    retryStep(opts: string) : void,
    enterMandatoryFields() : void,
    addAnotherElementToCollection(): void,
    removeElementFromCollection(): void,
    retryUntilExists(action: Function, locator: string) : void,
  }

  export interface config {

  }


  export interface loginPage {
    signIn(username: string, password: string) : void,

  }


  export interface caseListPage {
    openExistingCase(caseId: string) : void,
    changeStateFilter(desiredState: string) : void,

  }


  export interface createCasePage {
    enterCaseName(caseName?: string) : void,
    createNewCase() : void,

  }


  export interface addEventSummaryPage {
    submitCase() : void,

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
    completePhyiscalHarm() : void,
    completeEmotionalHarm() : void,
    completeSexualAbuse() : void,
    completeNeglect() : void,

  }


  export interface uploadDocumentsPage {
    selectSocialWorkStatementIncludedInSWET() : void,
    uploadSocialWorkAssessment(file: string) : void,
    uploadCarePlan(file: string) : void,
    uploadAdditionalDocuments(file: string) : void,
    selectSocialWorkChronologyToFollow() : void,

  }


  export interface enterApplicantPage {
    enterApplicantDetails(applicant: string) : void,
    enterSolicitorDetails(solicitor: string) : void,

  }


  export interface enterChildrenPage {
    fields(childNo: string) : void,
    addChild() : void,
    enterChildDetails(firstName: string, lastName: string, day: string, month: string, year: string, gender?: string) : void,
    defineChildSituation(day: string, month: string, year: string, situation?: string) : void,
    enterAddress(address: string) : void,
    enterKeyDatesAffectingHearing(keyDates?: string) : void,
    enterSummaryOfCarePlan(carePlan?: string) : void,
    defineAdoptionIntention() : void,
    enterParentsDetails(fatherResponsible?: string, motherName?: string, fatherName?: string) : void,
    enterSocialWorkerDetails(socialWorkerName?: string, socialWorkerTel?: string) : void,
    defineChildAdditionalNeeds() : void,
    defineContactDetailsVisibility() : void,
    defineAbilityToTakePartInProceedings() : void,

  }


  export interface enterOtherProceedingsPage {
    selectYesForProceeding() : void,
    selectNoForProceeding() : void,
    enterProceedingInformation(otherProceedingData: string) : void,

  }


  export interface attendingHearingPage {
    enterInterpreter(details?: string) : void,
    enterIntermediary() : void,
    enterLitigationIssues() : void,
    enterLearningDisability(details?: string) : void,
    enterWelshProceedings() : void,
    enterExtraSecurityMeasures(details?: string) : void,

  }


  export interface enterAllocationProposalPage {
    selectAllocationProposal(proposal: string) : void,
    enterProposalReason(reason: string) : void,

  }


  export interface enterRespondentsPage {
    fields(id: string) : void,
    enterRespondent(id: string, respondent: string) : void,
    enterRelationshipToChild(id: string, relationship: string) : void,
    enterContactDetailsHidden(id: string, option: string, reason?: string) : void,
    enterLitigationIssues(id: string, option: string, reason?: string) : void,

  }


  export interface enterOthersPage {
    fields(otherNo: string) : void,
    addOther() : void,
    enterOtherDetails(other: string) : void,
    enterRelationshipToChild(childInformation: string) : void,
    enterContactDetailsHidden(option: string) : void,
    enterLitigationIssues(option: string) : void,

  }


  export interface ordersNeededPage {
    checkCareOrder() : void,
    checkSupervisionOrder() : void,
    checkEducationSupervisionOrder() : void,
    checkEmergencyProtectionOrder() : void,
    checkOtherOrder() : void,
    enterDirectionAndInterim(testString?: string) : void,

  }


  export interface enterFamilyManPage {
    enterCaseID(caseId?: string) : void,

  }


  export interface changeCaseNamePage {
    changeCaseName(caseName?: string) : void,

  }

  export interface deleteApplicationPage {
    tickDeletionConsent() : void,

  }

  export interface handleSupplementaryEvidencePage {
    handleSupplementaryEvidence(): void,

  }

  export interface attachScannedDocsPage {
    enterScannedDocument(): void,

  }
}

declare module "codeceptjs" {
    export = CodeceptJS;
}
