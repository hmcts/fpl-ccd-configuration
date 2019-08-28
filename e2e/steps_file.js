/* global process */
const config = require('./config');

const logIn = require('./pages/login.page');
const caseViewPage = require('./pages/caseView.page');
const eventSummaryPage = require('./pages/eventSummary.page');
const enterApplicantPage  = require('./pages/events/enterApplicantEvent.page');
const enterChildrenPage = require('./pages/events/enterChildrenEvent.page');
const enterGroundsPage = require('./pages/events/enterGroundsForApplicationEvent.page');
const ordersNeededPage  = require('./pages/events/enterOrdersAndDirectionsNeededEvent.page');
const selectHearingPage = require('./pages/events/enterHearingNeededEvent.page');
const uploadDocumentsPage = require('./pages/events/uploadDocumentsEvent.page');
const openApplicationEventPage = require('./pages/events/openApplicationEvent.page');

const applicant = require('./fixtures/applicant');

let baseUrl = process.env.URL || 'http://localhost:3451';

'use strict';

module.exports = function () {
  return actor({
    logInAndCreateCase(username, password) {
      logIn.signIn(username, password);
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

    seeDocument(title, name, status, reason = '') {
      this.see(title);
      this.see(status);
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

    navigateToCaseDetails(caseId) {
      this.amOnPage(`${baseUrl}/case/${config.definition.jurisdiction}/${config.definition.caseType}/${caseId.replace(/\D/g, '')}`);
      this.waitForText('Sign Out');
    },

    async enterMandatoryFields () {
      caseViewPage.goToNewActions(config.applicationActions.selectOrders);
      ordersNeededPage.checkCareOrder();
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.selectHearing);
      selectHearingPage.enterTimeFrame();
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.enterApplicants);
      enterApplicantPage.enterApplicantDetails(applicant);
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.enterChildren);
      await enterChildrenPage.enterChildDetails('Timothy', '01', '08', '2015');
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
      enterGroundsPage.enterThresholdCriteriaDetails();
      this.continueAndSave();
      caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
      uploadDocumentsPage.selectSocialWorkChronologyToFollow(config.testFile);
      uploadDocumentsPage.uploadSocialWorkStatement(config.testFile);
      uploadDocumentsPage.uploadSocialWorkAssessment(config.testFile);
      uploadDocumentsPage.uploadCarePlan(config.testFile);
      uploadDocumentsPage.uploadThresholdDocument(config.testFile);
      uploadDocumentsPage.uploadChecklistDocument(config.testFile);
      this.continueAndSave();
    },
  });
};
