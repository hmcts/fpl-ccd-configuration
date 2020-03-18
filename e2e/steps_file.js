/* global process */
const output = require('codeceptjs').output;

const config = require('./config');

const loginPage = require('./pages/login.page');
const caseViewPage = require('./pages/caseView.page');
const caseListPage = require('./pages/caseList.page');
const eventSummaryPage = require('./pages/eventSummary.page');
const openApplicationEventPage = require('./pages/events/openApplicationEvent.page');
const ordersAndDirectionsNeededEventPage  = require('./pages/events/enterOrdersAndDirectionsNeededEvent.page');
const enterHearingNeededEventPage = require('./pages/events/enterHearingNeededEvent.page');
const enterChildrenEventPage = require('./pages/events/enterChildrenEvent.page');
const enterApplicantEventPage  = require('./pages/events/enterApplicantEvent.page');
const enterGroundsEventPage = require('./pages/events/enterGroundsForApplicationEvent.page');
const uploadDocumentsEventPage = require('./pages/events/uploadDocumentsEvent.page');
const enterAllocationProposalEventPage = require('./pages/events/enterAllocationProposalEvent.page');
const enterRespondentsEventPage = require('./pages/events/enterRespondentsEvent.page');

const applicant = require('./fixtures/applicant');
const solicitor = require('./fixtures/solicitor');
const respondent = require('./fixtures/respondents');
const normalizeCaseId = caseId => caseId.replace(/\D/g, '');

let baseUrl = process.env.URL || 'http://localhost:3451';

'use strict';

module.exports = function () {
  return actor({
    async signIn(username, password) {
      await this.retryUntilExists(async () => {
        this.amOnPage(process.env.URL || 'http://localhost:3451');
        if (await this.waitForSelector('#global-header') == null) {
          return;
        }

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
      await openApplicationEventPage.populateForm();
      await this.completeEvent('Save and continue');
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

    seeNestedAnswerInTab(questionNo, complexTypeHeading, complexTypeSubHeading, question, answer) {
      const panelLocator = name => locate(`//div[@class="complex-panel"][//span[text()="${name}"]]`);

      const topLevelLocator = panelLocator(complexTypeHeading);
      const subLevelLocator = panelLocator(complexTypeSubHeading);
      const rowLocator = locate(`${topLevelLocator}${subLevelLocator}/table/tbody/tr[${questionNo}]`);
      const questionLocator = locate(`${rowLocator}/th/span`);
      const answerLocator = locate(`${rowLocator}/td/span`);

      this.seeElement(topLevelLocator);
      this.seeElement(subLevelLocator);
      this.seeElement(rowLocator);
      this.seeElement(questionLocator.withText(question));
      this.seeElement(answerLocator.withText(answer));
    },

    seeCaseInSearchResult(caseId) {
      this.waitForText('Case List');
      this.seeElement(caseListPage.locateCase(normalizeCaseId(caseId)));
    },

    dontSeeCaseInSearchResult(caseId) {
      this.dontSeeElement(caseListPage.locateCase(normalizeCaseId(caseId)));
    },

    signOut() {
      this.click('Sign Out');
      this.wait(2); // in seconds
    },

    async navigateToCaseDetails(caseId) {
      const normalisedCaseId = normalizeCaseId(caseId);

      const currentUrl = await this.grabCurrentUrl();
      if (!currentUrl.replace(/#.+/g, '').endsWith(normalisedCaseId)) {
        await this.retryUntilExists(() => {
          this.amOnPage(`${baseUrl}/case/${config.definition.jurisdiction}/${config.definition.caseType}/${normalisedCaseId}`);
        }, '#sign-out');
      }
    },

    async navigateToCaseList(){
      await caseListPage.navigate();
    },

    async enterAllocationProposal () {
      await caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
      enterAllocationProposalEventPage.selectAllocationProposal('District judge');
      await this.completeEvent('Save and continue');
    },

    async enterMandatoryFields (settings) {
      await caseViewPage.goToNewActions(config.applicationActions.enterOrdersAndDirectionsNeeded);
      ordersAndDirectionsNeededEventPage.checkCareOrder();
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterHearingNeeded);
      enterHearingNeededEventPage.enterTimeFrame();
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterApplicant);
      enterApplicantEventPage.enterApplicantDetails(applicant);
      enterApplicantEventPage.enterSolicitorDetails(solicitor);
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterChildren);
      await enterChildrenEventPage.enterChildDetails('Timothy', 'Jones', '01', '08', '2015');
      if(settings && settings.multipleChildren){
        await this.addAnotherElementToCollection('Child');
        await enterChildrenEventPage.enterChildDetails('John', 'Black', '02', '09', '2016');
      }
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterRespondents);
      await enterRespondentsEventPage.enterRespondent(respondent[0]);
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
      enterGroundsEventPage.enterThresholdCriteriaDetails();
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
      uploadDocumentsEventPage.selectSocialWorkChronologyToFollow();
      uploadDocumentsEventPage.selectSocialWorkStatementIncludedInSWET();
      uploadDocumentsEventPage.uploadSocialWorkAssessment(config.testFile);
      uploadDocumentsEventPage.uploadCarePlan(config.testFile);
      uploadDocumentsEventPage.uploadSWET(config.testFile);
      uploadDocumentsEventPage.uploadThresholdDocument(config.testFile);
      uploadDocumentsEventPage.uploadChecklistDocument(config.testFile);
      await this.completeEvent('Save and continue');
      await caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
      enterAllocationProposalEventPage.selectAllocationProposal('District judge');
      await this.completeEvent('Save and continue');
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
    async retryUntilExists(action, locator) {
      const maxNumberOfTries = 4;

      for (let tryNumber = 1; tryNumber <= maxNumberOfTries; tryNumber++) {
        output.log(`retryUntilExists(${locator}): starting try #${tryNumber}`);
        if (tryNumber > 1 && (await this.locateSelector(locator)).length > 0) {
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
