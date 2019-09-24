/* global process */
const config = require('./config');

const loginPage = require('./pages/login.page');
const caseViewPage = require('./pages/caseView.page');
const eventSummaryPage = require('./pages/eventSummary.page');
const openApplicationEventPage = require('./pages/events/openApplicationEvent.page');
const ordersAndDirectionsNeededEventPage  = require('./pages/events/enterOrdersAndDirectionsNeededEvent.page');
const enterHearingNeededEventPage = require('./pages/events/enterHearingNeededEvent.page');
const enterChildrenEventPage = require('./pages/events/enterChildrenEvent.page');
const enterApplicantEventPage  = require('./pages/events/enterApplicantEvent.page');
const enterGroundsEventPage = require('./pages/events/enterGroundsForApplicationEvent.page');
const uploadDocumentsEventPage = require('./pages/events/uploadDocumentsEvent.page');

const applicant = require('./fixtures/applicant');

let baseUrl = process.env.URL || 'http://localhost:3451';

'use strict';

module.exports = function () {
  return actor({
    async signIn(username, password) {
      await this.retryUntilExists(async () => {
        this.amOnPage(process.env.URL || 'http://localhost:3451');
        this.waitForElement('#global-header');

        const user = await this.grabText('#user-name');
        if (user !== undefined) {
          if (user.toLowerCase().includes(username)) {
            return;
          }
          this.signOut();
        }

        loginPage.signIn(username, password);
      }, '#sign-out');
    },

    async logInAndCreateCase(username, password) {
      await this.signIn(username, password);
      this.click('Create new case');
      this.waitForElement(`#cc-jurisdiction > option[value="${config.definition.jurisdiction}"]`);
      openApplicationEventPage.populateForm();
      await this.completeEvent('Save and continue');
    },

    async completeEvent(button, changeDetails) {
      await this.retryUntilExists(() => this.click('Continue'), '.check-your-answers');
      if (changeDetails != null) {
        eventSummaryPage.provideSummary(changeDetails.summary, changeDetails.description);
      }
      await eventSummaryPage.submit(button);
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

    seeAnswerInTab(questionNo, complexTypeHeading, question, answer) {
      const complexType = locate(`.//span[text() = "${complexTypeHeading}"]`);
      const questionRow = locate(`${complexType}/../../../table/tbody/tr[${questionNo}]`);
      this.seeElement(locate(`${questionRow}/th/span`).withText(question));
      if (Array.isArray(answer)) {
        let ansIndex = 1;
        answer.forEach(ans => {
          this.seeElement(locate(`${questionRow}/td/span//tr[${ansIndex}]`).withText(ans));
          ansIndex++;
        });
      } else {
        this.seeElement(locate(`${questionRow}/td/span`).withText(answer));
      }
    },

    signOut() {
      this.click('Sign Out');
      this.wait(2); // in seconds
    },

    async navigateToCaseDetails(caseId) {
      const normalisedCaseId = caseId.replace(/\D/g, '');

      const currentUrl = await this.grabCurrentUrl();
      if (!currentUrl.replace(/#.+/g, '').endsWith(normalisedCaseId)) {
        await this.retryUntilExists(() => {
          this.amOnPage(`${baseUrl}/case/${config.definition.jurisdiction}/${config.definition.caseType}/${normalisedCaseId}`);
        }, '#sign-out');
      }
    },

    async enterMandatoryFields () {
      await caseViewPage.goToNewActions(config.applicationActions.enterOrdersAndDirectionsNeeded);
      ordersAndDirectionsNeededEventPage.checkCareOrder();
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterHearingNeeded);
      enterHearingNeededEventPage.enterTimeFrame();
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterApplicant);
      enterApplicantEventPage.enterApplicantDetails(applicant);
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterChildren);
      await enterChildrenEventPage.enterChildDetails('Timothy', 'Jones', '01', '08', '2015');
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
      enterGroundsEventPage.enterThresholdCriteriaDetails();
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
      uploadDocumentsEventPage.selectSocialWorkChronologyToFollow(config.testFile);
      uploadDocumentsEventPage.uploadSocialWorkStatement(config.testFile);
      uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
      uploadDocumentsEventPage.uploadCarePlan(config.testFile);
      uploadDocumentsEventPage.uploadThresholdDocument(config.testFile);
      uploadDocumentsEventPage.uploadChecklistDocument(config.testFile);
      await this.completeEvent('Save and continue');
    },

    async addAnotherElementToCollection() {
      const numberOfElements = await this.grabNumberOfVisibleElements('.collection-title');
      this.click('Add new');
      this.waitNumberOfVisibleElements('.collection-title', numberOfElements + 1);
      this.wait(0.5); // add extra time to allow slower browsers to render all fields (just extra precaution)
    },

    /**
     * Retries defined action util element described by the locator is present.
     * Note: If element is not present after 4 tries (run + 3 retries) this step throws an error.
     *
     * @param action - an action that will be retried until either condition is met or max number of retries is reached
     * @param locator - locator for an element that is expected to be present upon successful execution of an action
     * @returns {Promise<void>} - promise holding no result if resolved or error if rejected
     */
    async retryUntilExists(action, locator) {
      const maxNumberOfTries = 4;

      for (let tryNumber = 1; tryNumber <= maxNumberOfTries; tryNumber++) {
        if (tryNumber > 1 && (await this.locateSelector(locator)).length > 0) {
          break;
        }
        await action();
        if (await this.waitForSelector(locator) != null) {
          break;
        }
        if (tryNumber === maxNumberOfTries) {
          throw new Error(`Maximum number of tries (${maxNumberOfTries}) has been reached`);
        }
      }
    },
  });
};
