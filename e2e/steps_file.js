/* global process */
const config = require('./config');

const loginPage = require('./pages/login.page');
const openApplicationEventPage = require('./pages/events/openApplicationEvent.page');
const eventSummaryPage = require('./pages/eventSummary.page');

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

    navigateToCaseDetails(caseId) {
      this.amOnPage(`${baseUrl}/case/${config.definition.jurisdiction}/${config.definition.caseType}/${caseId.replace(/\D/g, '')}`);
      this.waitForText('Sign Out');
    },
  });
};
