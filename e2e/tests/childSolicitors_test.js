const config = require('../config.js');
//const dateFormat = require('dateformat');
const mandatoryWithMaxChildren = require('../fixtures/caseData/mandatoryWithMaxChildren.json');
const apiHelper = require('../helpers/api_helper.js');

const solicitor1 = config.privateSolicitorOne;

let caseId;

Feature('Child solicitors');

BeforeSuite(async ({I}) => {
  caseId = await I.submitNewCaseWithData(mandatoryWithMaxChildren);
  solicitor1.details = await apiHelper.getUser(solicitor1);
  solicitor1.details.organisation = 'Private solicitors';
});

Before(async ({I}) => await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId));

Scenario('HMCTS Confirm that a main solicitor in not assigned for all the children yet', async ({I, caseViewPage, enterChildrenEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);
  await I.goToNextPage();
  await enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.no);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
});

Scenario('HMCTS assign a main solicitor for all the children', async ({I, caseViewPage, enterChildrenEventPage}) => {
  await caseViewPage.goToNewActions(config.administrationActions.amendChildren);
  await I.goToNextPage();
  await enterChildrenEventPage.selectAnyChildHasLegalRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveLegalRepresentation.options.yes);
  await enterChildrenEventPage.enterChildrenMainRepresentation(solicitor1.details);
  await enterChildrenEventPage.enterRegisteredOrganisation(solicitor1.details);
  await I.goToNextPage();
  await enterChildrenEventPage.selectChildrenHaveSameRepresentation(enterChildrenEventPage.fields().mainSolicitor.childrenHaveSameRepresentation.options.yes);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.amendChildren);
});


