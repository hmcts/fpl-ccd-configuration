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
    },

    async logInAndCreateCase(username, password) {
      await this.signIn(username, password);
      this.click('Create Case');
      this.waitForElement(`#cc-jurisdiction > option[value="${config.definition.jurisdiction}"]`);
      openApplicationEventPage.populateForm();
      this.continueAndSave();
    },

    continueAndSave() {
      this.click('Continue');
      this.waitForElement('.check-your-answers');
      eventSummaryPage.submit('Save and continue');
    },

    continueAndProvideSummary(summary, description) {
      this.click('Continue');
      this.waitForElement('.check-your-answers');
      eventSummaryPage.provideSummaryAndSubmit('Save and continue', summary, description);
    },

    continueAndSubmit() {
      this.click('Continue');
      this.waitForElement('.check-your-answers');
      eventSummaryPage.submit('Submit');
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
        this.amOnPage(`${baseUrl}/case/${config.definition.jurisdiction}/${config.definition.caseType}/${normalisedCaseId}`);
        this.waitForText('Sign Out');
      }
    },

    async enterMandatoryFields () {
      caseViewPage.goToNewActions(config.applicationActions.enterOrdersAndDirectionsNeeded);
      ordersAndDirectionsNeededEventPage.checkCareOrder();
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.enterHearingNeeded);
      enterHearingNeededEventPage.enterTimeFrame();
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.enterApplicant);
      enterApplicantEventPage.enterApplicantDetails(applicant);
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.enterChildren);
      await enterChildrenEventPage.enterChildDetails('Timothy', 'Jones', '01', '08', '2015');
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
      enterGroundsEventPage.enterThresholdCriteriaDetails();
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
      uploadDocumentsEventPage.selectSocialWorkChronologyToFollow(config.testFile);
      uploadDocumentsEventPage.uploadSocialWorkStatement(config.testFile);
      uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
      uploadDocumentsEventPage.uploadCarePlan(config.testFile);
      uploadDocumentsEventPage.uploadThresholdDocument(config.testFile);
      uploadDocumentsEventPage.uploadChecklistDocument(config.testFile);
      this.continueAndSave();
    },
  });
};
