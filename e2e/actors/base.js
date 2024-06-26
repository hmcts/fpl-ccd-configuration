const output = require('codeceptjs').output;
const lodash = require('lodash');
const config = require('../config');
const moment = require('moment');
const apiHelper = require('../helpers/api_helper.js');

//const loginPage = require('../pages/login.page');
//const caseListPage = require('../pages/caseList.page');
const eventSummaryPage = require('../pages/eventSummary.page');
const openApplicationEventPage = require('../pages/events/openApplicationEvent.page');
const mandatorySubmissionFields = require('../fixtures/caseData/mandatorySubmissionFields.json');

const normalizeCaseId = caseId => caseId.toString().replace(/\D/g, '');

const baseUrl = config.baseUrl;
//const signedInSelector = 'exui-header';
//const signedOutSelector = '#global-header';
const maxRetries = 10;
let currentUser = {};

const { I } = inject();

'use strict';

module.exports = {
  async signIn(user) {
    console.log('base signIn');
    if (!(currentUser === user)) {
      console.log(`Logging in as ${user.email}`);
      output.debug(`Logging in as ${user.email}`);
      currentUser = {}; // reset in case the login fails

      //await this.retryUntilExists(async () => {
      //To mitigate situation when idam response with blank page
      await this.goToPage(baseUrl, user);
      I.grabCurrentUrl();

      // if (await this.waitForAnySelector([signedOutSelector, signedInSelector], 30) == null) {
      //   await this.refreshPage();
      //   I.grabCurrentUrl();
      // }
      //
      // if (await this.hasSelector(signedInSelector)) {
      //   await this.retryUntilExists(() => this.click('Sign out'), signedOutSelector, false, 10);
      //   I.grabCurrentUrl();
      // }
      //
      // await this.retryUntilExists(() =>  loginPage.signIn(user), signedInSelector, false, 10);
      // I.grabCurrentUrl();

      //}, signedInSelector, false, 10);
      //await this.rejectCookies();
      I.grabCurrentUrl();
      output.debug(`Logged in as ${user.email}`);
      currentUser = user;
    } else {
      console.log(`Already logged in as ${user.email}`);
      output.debug(`Already logged in as ${user.email}`);
    }
    I.grabCurrentUrl();
  },

  async goToPage(url, user) {
    this.amOnPage(url);
    await this.logWithHmctsAccount(user);
  },

  async logWithHmctsAccount(user) {
    //const hmctsLoginIn = 'div.win-scroll';
    if (!user.email || !user.password) {
      throw new Error('For environment requiring hmcts authentication please provide HMCTS_USER_USERNAME and HMCTS_USER_PASSWORD environment variables');
    }
    // await within(hmctsLoginIn, () => {
    this.waitForElement('//input[@type="text"]', 20);
    this.fillField('//input[@type="text"]', user.email);
    //this.wait(0.2);
    this.fillField('//input[@type="password"]', user.password);
    //this.wait(0.5);
    this.click('Sign in');
    // });
    this.waitForText('Case list');
  },

  async rejectCookies() {
    const rejectCookiesButton = '//button[@name="cookies" and @value="reject"]';
    if (await this.hasSelector(rejectCookiesButton)) {
      this.click(rejectCookiesButton);
    }
  },

  async logInAndCreateCase(user, caseName, outsourcingLA) {
    await this.signIn(user);
    await this.waitForSelector('ccd-search-result');
    await this.waitForSelector('a[href="/cases/case-filter"]');
    await this.retryUntilExists(() => this.click('a[href="/cases/case-filter"]'), openApplicationEventPage.fields.jurisdiction, true, 10);

    await openApplicationEventPage.populateForm(caseName, outsourcingLA);
    await this.completeEvent('Save and continue');
    this.waitForElement('.alert-message', 60);
    const caseId = normalizeCaseId(await this.grabTextFrom('.alert-message'));
    output.print(`Case created #${caseId}`);
    return caseId;
  },

  async createCaseSmokeTest(user, caseName, outsourcingLA) {
    await this.waitForSelector('ccd-search-result');
    await this.waitForSelector('a[href="/cases/case-filter"]');
    await this.retryUntilExists(() => this.click('a[href="/cases/case-filter"]'), openApplicationEventPage.fields.jurisdiction, true, 10);

    await openApplicationEventPage.populateForm(caseName, outsourcingLA);
    I.click('Submit');
    this.waitForElement('markdown[class=\'markdown\'] h2 strong', 90);
    const caseId = normalizeCaseId(await this.grabTextFrom('markdown[class=\'markdown\'] h2 strong'));
    output.print(`Case created #${caseId}`);
    return caseId;
  },
  //new function to CYA page and submit event
  async submitEventWithCYA(button)
  {
    await this.click('Continue');
    await this.waitForText('Check your answers');
    await this.click(button);

  },


  async completeEvent(button, changeDetails, confirmationPage = false, selector = '.mat-tab-list') {
    await this.retryUntilExists(() => this.click('submit'), '.check-your-answers');
    if (changeDetails != null) {
      await eventSummaryPage.provideSummary(changeDetails.summary, changeDetails.description);
    }
    await this.submitEvent(button, confirmationPage, selector);
  },

  async seeCheckAnswersAndCompleteEvent(button, confirmationPage = false) {
    await this.retryUntilExists(() => this.click('Continue'), '.check-your-answers');
    this.see('Check the information below carefully.');
    await this.submitEvent(button, confirmationPage);
  },

  async submitEvent(button, confirmationPage, selector) {
    if (!confirmationPage) {
      await eventSummaryPage.submit(button, selector);
    } else {
      await eventSummaryPage.submit(button, '#confirmation-body');
      await this.retryUntilExists(() => this.click('Close and Return to case details'), '.mat-tab-list');
    }
  },

  seeEventSubmissionConfirmation() {
    // remove temporarily since it may disappear sometimes.
    //this.waitForText(`updated with event: ${event}`);
  },

  clickHyperlink(link, urlNavigatedTo) {
    this.click(link);
    this.seeCurrentUrlEquals(urlNavigatedTo);
  },

  async seeAvailableEvents(expectedEvents) {
    const actualEvents = await this.grabTextFrom('//ccd-event-trigger//select')
      .then(options => Array.isArray(options) ? options : options.split('\n'))
      .then(options => {
        return lodash.without(options, 'Select action');
      });

    if (!lodash.isEqual(lodash.sortBy(expectedEvents), lodash.sortBy(actualEvents))) {
      throw new Error(`Events wanted: [${expectedEvents}], found: [${actualEvents}]`);
    }
  },

  async dontSeeEvent(eventName) {
    await within('ccd-event-trigger', () => this.dontSee(eventName));
  },

  async seeEvent(eventName) {
    await within('ccd-event-trigger', () => this.see(eventName));
  },

  async startEventViaHyperlink(linkLabel) {
    await this.retryUntilExists(() => {
      this.click(locate(`//p/a[text()="${linkLabel}"]`));
    }, 'ccd-case-event-trigger');
  },

  seeDocument(title, name, status = '', reason = '', dateAndTimeUploaded, uploadedBy) {
    this.see(title);
    if (status !== '') {
      this.see(status);
    }
    if (reason !== '') {
      this.see(reason);
    } else {
      this.see(name);
    }
    if (dateAndTimeUploaded) {
      this.see(dateAndTimeUploaded);
    }
    if (uploadedBy) {
      this.see(uploadedBy);
    }
  },

  seeFamilyManNumber(familyManNumber) {
    this.seeElement(`//*[@class="markdown"]//h2/strong[text()='FamilyMan ID: ${familyManNumber}']`);
  },

  seeCaseInSearchResult(caseId) {
    caseId = normalizeCaseId(caseId);
    this.seeElement(`a[href$='${caseId}']`);
  },

  dontSeeCaseInSearchResult(caseId) {
    caseId = normalizeCaseId(caseId);
    this.dontSeeElement(`a[href$='${caseId}']`);
  },

  seeEndStateForEvent(eventName, state) {
    this.click(`//table[@class="EventLogTable"]//tr[td[contains(., "${eventName}")]][1]`);
    this.seeElement(`//table[@class="EventLogDetails"]//tr[.//span[text()="End state"] and .//span[text()="${state}"]]`);
  },

  async navigateToCaseDetails(caseId) {
    this.amOnPage(`${baseUrl}/cases/case-details/${caseId}`);
    // I.wait(0.5);
    I.waitForText('CCD ID');
    //await this.goToPage(`${baseUrl}/cases/case-details/${caseId}`);
    // const currentUrl = await this.grabCurrentUrl();
    // if (!currentUrl.replace(/#.+/g, '').endsWith(caseId)) {
    //   await this.retryUntilExists(async () => {
    //
    //   }, '#next-step');
    // }
  },

  async navigateToCaseDetailsAs(user, caseId) {
    await I.goToPage(config.baseUrl, user);
    await this.navigateToCaseDetails(caseId);
  },

  navigateToCaseList(caseIdAndName, caseListPage) {
    caseListPage.searchForCasesWithName(caseIdAndName.caseName);
    I.wait(90);
    I.waitForElement(`//ccd-search-result/table/tbody//tr//td//a[contains(@href,'/cases/case-details/${caseIdAndName.caseId}')]`, 100);
    I.seeCaseInSearchResult(caseIdAndName.caseId);
  },

  async fillDate(date, sectionId = 'form') {
    if (date instanceof Date) {
      date = {
        day: date.getDate(),
        month: date.getMonth() + 1,
        year: date.getFullYear(),
      };
    }

    if (date) {
      await within(sectionId, () => {
        this.fillField('Day', date.day);
        this.fillField('Month', date.month);
        this.fillField('Year', date.year);
      });
    }
  },

  async fillDateAndTime(date, sectionId = 'form') {
    if (date instanceof Date) {
      date = {
        day: date.getDate(),
        month: date.getMonth() + 1,
        year: date.getFullYear(),
        hour: date.getHours(),
        minute: date.getMinutes(),
        second: date.getSeconds(),
      };
    }

    if (date) {
      await within(sectionId, () => {
        if (date.day) {
          this.fillField('Day', date.day);
        }
        if (date.month) {
          this.fillField('Month', date.month);
        }
        if (date.year) {
          this.fillField('Year', date.year);
        }
        if (date.hour) {
          this.fillField('Hour', date.hour);
        }
        if (date.minute) {
          this.fillField('Minute', date.minute);
        }
        if (date.second) {
          this.fillField('Second', date.second);
        }
      });
    }
  },

  async addElementToCollection(index = 0) {
    const numberOfElements = await this.grabNumberOfVisibleElements('.collection-title');

    for(let i = numberOfElements; i <= index; i++){
      this.click('Add new');
      this.waitNumberOfVisibleElements('.collection-title', i + 1);
      this.wait(0.5);
    }
  },

  async addAnotherElementToCollection(collectionName) {
    const numberOfElements = await this.grabNumberOfVisibleElements('.collection-title');
    if (collectionName) {
      this.click(locate('button')
        .inside(locate('div').withChild(locate('h2').withText(collectionName)))
        .withText('Add new'));
    } else {
      this.click('Add new');
    }
    this.waitNumberOfVisibleElements('.collection-title', numberOfElements + 1);
    this.wait(0.5); // add extra time to allow slower browsers to render all fields (just extra precaution)
  },

  async removeElementFromCollection(collectionName, index = 1) {
    if (collectionName) {
      await this.click(locate('button')
        .inside(locate('div').withChild(locate('h2').withText(collectionName)))
        .withText('Remove')
        .at(index));
    } else {
      await this.click('Remove');
    }
    this.click(locate('button')
      .inside('.mat-dialog-container')
      .withText('Remove'));
  },

  async submitNewCaseWithData(data = mandatorySubmissionFields) {
    const caseId = await this.submitNewCase(config.newSwanseaLocalAuthorityUserOne);
    let caseName = `Test case (${moment().format('YYYY-MM-DD HH:MM')})`;
    await apiHelper.populateWithData(caseId, data, caseName);
    output.print(`Case #${caseId} has been populated with data`);
    return {caseId, caseName};
  },

  async submitNewCase(user, name) {
    const caseName = name || `Test case (${moment().format('YYYY-MM-DD HH:MM')})`;
    const creator = user || config.newSwanseaLocalAuthorityUserOne;
    const caseData = await apiHelper.createCase(creator, caseName);
    const caseId = caseData.id;
    output.print(`Case #${caseId} has been created`);

    return caseId;
  },

  async goToNextPage(label = 'Continue', maxNumberOfTries = maxRetries) {
    const originalUrl = await this.grabCurrentUrl();

    for (let tryNumber = 1; tryNumber <= maxNumberOfTries; tryNumber++) {
      this.click(label);
      //Caused by https://tools.hmcts.net/jira/browse/EUI-2498
      for (let attempt = 0; attempt < 20; attempt++) {
        let currentUrl = await this.grabCurrentUrl();
        if (currentUrl !== originalUrl) {
          if (attempt > 5) {
            output.print(`Page changed in try ${tryNumber} in ${attempt} sec - (${originalUrl} -> ${currentUrl})`);
          }
          return;
        } else {
          // @todo flaky
          this.wait(1);
        }
      }

      output.print(`Page change failed (${originalUrl})`);
    }
  },

  async goToPreviousPage() {
    this.click('Previous');
  },

  uiFormatted(id) { return '#' + id.match(/.{1,4}/g).join('-');},

  async getActiveElementIndex() {
    return await this.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
  /**
   * Retries defined action util element described by the locator is present. If element is not present
   * after 4 tries (run + 3 retries) this step throws an error.
   *
   * Warning: action logic should avoid framework steps that stop test execution upon step failure as it will
   *          stop test execution even if there are retries still available. Catching step error does not help.
   *
   * @param action - an action that will be retried until either condition is met or max number of retries is reached
   * @param locator - locator for an element that is expected to be present upon successful execution of an action
   * @param checkUrlChanged - check if the url has changed, if true skip the action
   * @param maxNumberOfTries - maximum number of attempts to retry
   * @returns {Promise<void>} - promise holding no result if resolved or error if rejected
   */
  async retryUntilExists(action, locator, checkUrlChanged = true, maxNumberOfTries = maxRetries) {
    const originalUrl = await this.grabCurrentUrl();
    // override this for now
    //maxNumberOfTries = 1;

    for (let tryNumber = 1; tryNumber <= maxNumberOfTries; tryNumber++) {
      output.log(`retryUntilExists(${locator}): starting try #${tryNumber}`);
      if (tryNumber > 1 && await this.hasSelector(locator)) {
        output.log(`retryUntilExists(${locator}): element found before try #${tryNumber} was executed`);
        break;
      }
      try {
        if (checkUrlChanged && (originalUrl !== await this.grabCurrentUrl())) {
          output.print('Url changed, action skipped');
        } else {
          await action();
        }
      } catch (error) {
        output.error(error);
      }
      if (await this.waitForSelector(locator) != null) {
        output.log(`retryUntilExists(${locator}): element found after try #${tryNumber} was executed`);
        break;
      } else {
        output.print(`retryUntilExists(${locator}): element not found after try #${tryNumber} was executed`);
      }
      if (tryNumber === maxNumberOfTries) {
        throw new Error(`Maximum number of tries (${maxNumberOfTries}) has been reached in search for ${locator}`);
      }
    }
  },
};
