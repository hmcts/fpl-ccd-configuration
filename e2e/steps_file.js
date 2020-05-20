/* global process */
const output = require('codeceptjs').output;

const config = require('./config');
const caseHelper = require('./helpers/case_helper.js');

const loginPage = require('./pages/login.page');
const caseListPage = require('./pages/caseList.page');
const eventSummaryPage = require('./pages/eventSummary.page');
const openApplicationEventPage = require('./pages/events/openApplicationEvent.page');
const mandatorySubmissionFields = require('./fixtures/mandatorySubmissionFields.json');

const normalizeCaseId = caseId => caseId.replace(/\D/g, '');

const baseUrl = process.env.URL || 'http://localhost:3333';
const signedInSelector = 'exui-header';
const signedOutSelector = '#global-header';

'use strict';

module.exports = function () {
  return actor({
    async signIn(user) {
      await this.retryUntilExists(async () => {
        this.amOnPage(baseUrl);

        if(await this.waitForAnySelector([signedOutSelector, signedInSelector]) == null){
          return;
        }

        if(await this.hasSelector(signedInSelector)){
          this.signOut();
        }

        loginPage.signIn(user);
      }, signedInSelector);
    },

    async logInAndCreateCase(user) {
      await this.signIn(user);
      this.click('Create case');
      this.waitForElement(`#cc-jurisdiction > option[value="${config.definition.jurisdiction}"]`);
      await openApplicationEventPage.populateForm();
      await this.completeEvent('Save and continue');
      const caseId = await this.grabTextFrom('.markdown h2');
      console.log(`Case created ${caseId}`);
      return caseId;
    },

    async completeEvent(button, changeDetails) {
      await this.retryUntilExists(() => this.click('Continue'), '.check-your-answers');
      if (changeDetails != null) {
        eventSummaryPage.provideSummary(changeDetails.summary, changeDetails.description);
      }
      await eventSummaryPage.submit(button);
    },

    seeCheckAnswers(checkAnswerTitle) {
      this.click('Continue');
      this.waitForElement('.check-your-answers');
      this.see(checkAnswerTitle);
      eventSummaryPage.submit('Save and continue');
    },

    seeEventSubmissionConfirmation(event) {
      this.see(`updated with event: ${event}`);
    },

    clickHyperlink(link, urlNavigatedTo) {
      this.click(link);
      this.seeCurrentUrlEquals(urlNavigatedTo);
    },

    seeDocument(title, name, status = '', reason = '') {
      this.see(title);
      if (status !== '') {
        this.see(status);
      }
      if (reason !== '') {
        this.see(reason);
      } else {
        this.see(name);
      }
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

    dontSeeInTab(pathToField) {
      this.dontSeeElement(locate(this.tabFieldSelector(pathToField)));
    },

    seeCaseInSearchResult(caseId) {
      this.seeElement(caseListPage.locateCase(normalizeCaseId(caseId)));
    },

    dontSeeCaseInSearchResult(caseId) {
      this.dontSeeElement(caseListPage.locateCase(normalizeCaseId(caseId)));
    },

    signOut() {
      this.click('Sign out');
      this.waitForText('Sign in', 20);
    },

    async navigateToCaseDetails(caseId) {
      const normalisedCaseId = normalizeCaseId(caseId);

      const currentUrl = await this.grabCurrentUrl();
      if (!currentUrl.replace(/#.+/g, '').endsWith(normalisedCaseId)) {
        await this.retryUntilExists(() => {
          this.amOnPage(`${baseUrl}/cases/case-details/${normalisedCaseId}`);
        }, signedInSelector);
      }
    },

    async navigateToCaseDetailsAs(user, caseId) {
      await this.signIn(user);
      await this.navigateToCaseDetails(caseId);
    },

    async navigateToCaseList(){
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

    async addAnotherElementToCollection(collectionName) {
      const numberOfElements = await this.grabNumberOfVisibleElements('.collection-title');
      if(collectionName) {
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
      if(collectionName) {
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
      console.log(`Case ${caseId} has been populated with data`);

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
        await action();
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
