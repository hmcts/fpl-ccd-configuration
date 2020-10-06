/* global process */
const output = require('codeceptjs').output;
const lodash = require('lodash');
const config = require('./config');
const caseHelper = require('./helpers/case_helper.js');

const loginPage = require('./pages/login.page');
const caseListPage = require('./pages/caseList.page');
const eventSummaryPage = require('./pages/eventSummary.page');
const openApplicationEventPage = require('./pages/events/openApplicationEvent.page');
const mandatorySubmissionFields = require('./fixtures/caseData/mandatorySubmissionFields.json');

const normalizeCaseId = caseId => caseId.toString().replace(/\D/g, '');

const baseUrl = process.env.URL || 'http://localhost:3333';
const signedInSelector = 'exui-header';
const signedOutSelector = '#global-header';
let currentUser = {};

'use strict';

module.exports = function () {
  return actor({
    async signIn(user) {
      if (currentUser !== user) {
        output.debug(`Logging in as ${user.email}`);
        currentUser = {}; // reset in case the login fails
        await this.retryUntilExists(async () => {
          await this.goToPage(baseUrl);

          if (await this.waitForAnySelector([signedOutSelector, signedInSelector]) == null) {
            return;
          }

          if (await this.hasSelector(signedInSelector)) {
            this.click('Sign out');
          }

          await loginPage.signIn(user);
        }, signedInSelector);
        output.debug(`Logged in as ${user.email}`);
        currentUser = user;
      } else {
        output.debug(`Already logged in as ${user.email}`);
      }
    },

    async goToPage(url){
      this.amOnPage(url);
      await this.logWithHmctsAccount();
    },

    async logWithHmctsAccount(){
      const hmctsLoginIn = 'div.win-scroll';

      if (await this.hasSelector(hmctsLoginIn)) {
        if (!config.hmctsUser.email || !config.hmctsUser.password) {
          throw new Error('For environment requiring hmcts authentication please provide HMCTS_USER_USERNAME and HMCTS_USER_PASSWORD environment variables');
        }
        within(hmctsLoginIn, () => {
          this.fillField('//input[@type="email"]', config.hmctsUser.email);
          this.wait(0.2);
          this.click('Next');
          this.wait(0.2);
          this.fillField('//input[@type="password"]', config.hmctsUser.password);
          this.wait(0.2);
          this.click('Sign in');
          this.click('Yes');
        });
      }
    },

    async logInAndCreateCase(user, caseName) {
      await this.signIn(user);
      await this.retryUntilExists(() => this.click('Create case'), `#cc-jurisdiction > option[value="${config.definition.jurisdiction}"]`);
      await openApplicationEventPage.populateForm(caseName);
      await this.completeEvent('Save and continue');
      this.waitForElement('.markdown h2', 5);
      const caseId = normalizeCaseId(await this.grabTextFrom('.markdown h2'));
      output.print(`Case created #${caseId}`);
      return caseId;
    },

    async completeEvent(button, changeDetails, confirmationPage = false) {
      await this.retryUntilExists(() => this.click('Continue'), '.check-your-answers');
      if (changeDetails != null) {
        eventSummaryPage.provideSummary(changeDetails.summary, changeDetails.description);
      }
      await this.submitEvent(button, confirmationPage);
    },

    async seeCheckAnswersAndCompleteEvent(button, confirmationPage = false) {
      await this.retryUntilExists(() => this.click('Continue'), '.check-your-answers');
      this.see('Check the information below carefully.');
      await this.submitEvent(button, confirmationPage);
    },

    async submitEvent(button, confirmationPage) {
      if (!confirmationPage) {
        await eventSummaryPage.submit(button);
      } else {
        await eventSummaryPage.submit(button, '#confirmation-body');
        await this.retryUntilExists(() => this.click('Close and Return to case details'), '.alert-success');
      }
    },

    seeEventSubmissionConfirmation(event) {
      this.see(`updated with event: ${event}`);
    },

    clickHyperlink(link, urlNavigatedTo) {
      this.click(link);
      this.seeCurrentUrlEquals(urlNavigatedTo);
    },

    async seeAvailableEvents(expectedEvents) {
      const actualEvents = await this.grabTextFrom('//ccd-event-trigger//option')
        .then(options => Array.isArray(options) ? options : [options])
        .then(options => {
          return lodash.without(options, 'Select action');
        });

      if (!lodash.isEqual(lodash.sortBy(expectedEvents), lodash.sortBy(actualEvents))) {
        throw new Error(`Events wanted: [${expectedEvents}], found: [${actualEvents}]`);
      }
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

    tabFieldSelector(pathToField) {
      let path = [].concat(pathToField);
      let fieldName = path.splice(-1, 1)[0];
      let selector = '//div[@class="tabs-panel"]';

      path.forEach(step => {
        selector = `${selector}//*[@class="complex-panel" and .//*[@class="complex-panel-title" and .//*[text()="${step}"]]]`;
      }, this);

      return `${selector}//*[@class="complex-panel-simple-field" and .//th/span[text()="${fieldName}"]]`;
    },

    seeInTab(pathToField, fieldValue) {
      const fieldSelector = this.tabFieldSelector(pathToField);

      if (Array.isArray(fieldValue)) {
        fieldValue.forEach((value, index) => {
          this.seeElement(locate(`${fieldSelector}//tr[${index + 1}]`).withText(value));
        });
      } else {
        this.seeElement(locate(fieldSelector).withText(fieldValue));
      }
    },

    seeTextInTab (pathToField) {
      const fieldSelector = this.tabFieldSelector(pathToField);
      this.seeElement(locate(fieldSelector));
    },

    dontSeeInTab(pathToField) {
      this.dontSeeElement(locate(this.tabFieldSelector(pathToField)));
    },

    seeCaseInSearchResult(caseId) {
      this.seeElement(caseListPage.locateCase(normalizeCaseId(caseId)));
    },

    dontSeeCaseInSearchResult(caseId) {
      this.dontSeeElement(caseListPage.locateCase(normalizeCaseId(caseId)));
    },

    seeEndStateForEvent(eventName, state) {
      this.click(`//table[@class="EventLogTable"]//tr[td[contains(., "${eventName}")]][1]`);
      this.seeElement(`//table[@class="EventLogDetails"]//tr[.//span[text()="End state"] and .//span[text()="${state}"]]`);
    },

    async navigateToCaseDetails(caseId) {
      const currentUrl = await this.grabCurrentUrl();
      if (!currentUrl.replace(/#.+/g, '').endsWith(caseId)) {
        await this.retryUntilExists(async () => {
          await this.goToPage(`${baseUrl}/cases/case-details/${caseId}`);
        }, signedInSelector);
      } else {
        this.refreshPage();
      }
    },

    async navigateToCaseDetailsAs(user, caseId) {
      await this.signIn(user);
      await this.navigateToCaseDetails(caseId);
    },

    async navigateToCaseList() {
      await caseListPage.navigate();
    },

    async fillDate(date, sectionId = 'form') {
      if (date instanceof Date) {
        date = {day: date.getDate(), month: date.getMonth() + 1, year: date.getFullYear()};
      }

      if (date) {
        return within(sectionId, () => {
          this.fillField('Day', date.day);
          this.fillField('Month', date.month);
          this.fillField('Year', date.year);
        });
      }
    },

    fillDateAndTime(date, sectionId = 'form') {
      if (date instanceof Date) {
        date = {
          day: date.getDate(), month: date.getMonth() + 1, year: date.getFullYear(),
          hour: date.getHours(), minute: date.getMinutes(), second: date.getSeconds(),
        };
      }

      if (date) {
        return within(sectionId, () => {
          this.fillField('Day', date.day);
          this.fillField('Month', date.month);
          this.fillField('Year', date.year);
          this.fillField('Hour', date.hour);
          this.fillField('Minute', date.minute);
          this.fillField('Second', date.second);
        });
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
      const caseId = await this.logInAndCreateCase(config.swanseaLocalAuthorityUserOne);
      await caseHelper.populateWithData(caseId, data);
      output.print(`Case #${caseId} has been populated with data`);

      return caseId;
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
     * @returns {Promise<void>} - promise holding no result if resolved or error if rejected
     */
    async retryUntilExists(action, locator, maxNumberOfTries = 6) {
      for (let tryNumber = 1; tryNumber <= maxNumberOfTries; tryNumber++) {
        output.log(`retryUntilExists(${locator}): starting try #${tryNumber}`);
        if (tryNumber > 1 && await this.hasSelector(locator)) {
          output.log(`retryUntilExists(${locator}): element found before try #${tryNumber} was executed`);
          break;
        }
        try {
          await action();
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
  });
};
