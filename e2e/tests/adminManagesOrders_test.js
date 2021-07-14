const config = require('../config.js');
const dateFormat = require('dateformat');
const caseData = require('../fixtures/caseData/gatekeepingWithPastHearingDetails.json');
const hearingDetails = require('../fixtures/hearingTypeDetails.js');
const caseDataWithApplication = require('../fixtures/caseData/gatekeepingWithPastHearingDetailsAndApplication.json');
const closedCaseData = require('../fixtures/caseData/closedCase.json');

const approvalDate = new Date(2021, 3, 9);
const orderTitle = 'some title';
const aYearAgo = new Date(Date.now() - (3600 * 1000 * 24));
const today = new Date(Date.now());
const futureDate = new Date(Date.now() + (3600 * 1000 * 24));
const removalAddress = { buildingAndStreet: { lineOne: 'Flat 2 Caversham', town: 'Reading' }, postcode: 'RG4 7AA' };
const applicationToLink = 'C2, 16 June 2021, 11:49am';
let caseId;

Feature('HMCTS Admin manages orders');

async function setupScenario(I, caseViewPage) {
  if (!caseId) { caseId = await I.submitNewCaseWithData(caseData); }
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
}

Scenario('Create C32A care order (with pre filled hearing details)', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c32);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.yes);
  await manageOrdersEventPage.selectHearing('Case management hearing, 3 November 2012');
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();

  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.yes);
  await I.goToNextPage();
  // Judge and approval date is already preFilled
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Care order (C32A)',
    approvalDate: new Date(2012, 10, 3),
    others: 'John Doe',
  });
});

Scenario('Create 32b discharge of care order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c32b);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(today);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterCareOrderIssuedDate(aYearAgo);
  manageOrdersEventPage.enterCareOrderIssuedVenue(hearingDetails[0]);
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Discharge of care order (C32B)',
    approvalDate: today,
  });
});

Scenario('Create EPO order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c23);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(today);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectEpoType(manageOrdersEventPage.section4.epoTypes.options.removeAccommodation);
  manageOrdersEventPage.selectIncludePhrase(manageOrdersEventPage.section4.includePhrase.options.yes);
  await manageOrdersEventPage.enterEPOEndDateTime(futureDate);
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Emergency protection order (C23)',
    approvalDateTime: today,
    others: 'John Doe',
  });
});

Scenario('Create EPO Prevent removal order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c23);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(today);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectEpoType(manageOrdersEventPage.section4.epoTypes.options.preventRemoval);
  manageOrdersEventPage.enterRemovalAddress(removalAddress);
  manageOrdersEventPage.selectExclusionRequirementEPO(manageOrdersEventPage.section4.exclusionRequirement.options.yes);
  manageOrdersEventPage.enterWhoIsExcluded('John Doe');
  await manageOrdersEventPage.enterExclusionStartDate(approvalDate);
  manageOrdersEventPage.uploadPowerOfArrest(config.testPdfFile);
  manageOrdersEventPage.selectIncludePhrase(manageOrdersEventPage.section4.includePhrase.options.yes);
  await manageOrdersEventPage.enterEPOEndDateTime(futureDate);
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Emergency protection order (C23)',
    approvalDateTime: today,
    others: 'John Doe',
  });
});

Scenario('Create C21 blank order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c21);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.enterTitle(orderTitle);
  await manageOrdersEventPage.enterDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 4,
    orderType: 'Blank order (C21)',
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    others: 'John Doe',
  });
});

Scenario('Create C21 blank order in closed case', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  const newCaseId = await I.submitNewCaseWithData(closedCaseData);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, newCaseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  I.dontSee('Upload an order');
  await manageOrdersEventPage.selectOperationInClosedState(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.enterTitle(orderTitle);
  await manageOrdersEventPage.enterDirections('some text');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Blank order (C21)',
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    others: 'John Doe',
  });
});

Scenario('Create C35a Supervision order', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c35A);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('Supervision order further details.');
  await manageOrdersEventPage.selectOrderTypeWithMonth(manageOrdersEventPage.section4.orderTypeWithMonth.options.numberOfMonths);
  await manageOrdersEventPage.enterSuperVisionNumOfMonths(12);
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 3,
    orderType: 'Supervision order (C35A)',
    approvalDate: today,
    others: 'John Doe',
  });
});

Scenario('Create Interim care order  (C33)', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c33);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectExclusionRequirementICO(manageOrdersEventPage.section4.exclusionRequirement.options.yes);
  await manageOrdersEventPage.enterExclusionDetails('I need an exclusion because of X,Y and Z');
  await manageOrdersEventPage.enterFurtherDirections('Further details.');
  await manageOrdersEventPage.selectOrderTypeWithEndOfProceedings(manageOrdersEventPage.section4.orderTypeWithEndOfProceedings.options.endOfProceedings);
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 3,
    orderType: manageOrdersEventPage.orders.title.c33,
    approvalDate: today,
    others: 'John Doe',
  });
});

Scenario('Interim supervision order (C35B)', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c35B);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.enterFurtherDirections('Further details.');
  await manageOrdersEventPage.selectOrderTypeWithEndOfProceedings(manageOrdersEventPage.section4.orderTypeWithEndOfProceedings.options.endOfProceedings);
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 3,
    orderType: manageOrdersEventPage.orders.title.c35B,
    approvalDate: today,
    others: 'John Doe',
  });
});

Scenario('Create C43a special guardianship order', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c43a);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDateTime(today);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectOrderByConsent();
  await manageOrdersEventPage.selectGuardian([0]);
  await manageOrdersEventPage.enterFurtherDirections('Further special guardianship details.');
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Special guardianship order (C43A)',
    orderTitle: orderTitle,
    approvalDate: today,
    specialGuardian: 'Joe Bloggs',
  });
});

Scenario('Create Child arrangements, Specific issue, Prohibited steps (C43)', async ({I, caseViewPage, manageOrdersEventPage}) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c43);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select,[0]);
  await I.goToNextPage();
  await manageOrdersEventPage.selectC43Orders();
  manageOrdersEventPage.selectOrderByConsent();
  await manageOrdersEventPage.enterRecitalsAndPreambles('Recitals and Preambles');
  await manageOrdersEventPage.enterDirections('some text');
  await manageOrdersEventPage.enterFurtherDirections('Further details.');
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I,caseViewPage,{
    orderIndex: 4,
    orderType: manageOrdersEventPage.orders.title.c43,
    approvalDate: today,
    others: 'John Doe',
  });
});

Scenario('Create C47A appointment of a Children\'s Guardian', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  const newCaseId = await I.submitNewCaseWithData(caseDataWithApplication);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, newCaseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c47a);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);

  manageOrdersEventPage.linkApplication(applicationToLink);

  await I.goToNextPage();
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  manageOrdersEventPage.selectCafcassRegion('ENGLAND');
  manageOrdersEventPage.selectEnglandOffice('Hull');
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Appointment of a children\'s guardian (C47A)',
    orderTitle: orderTitle,
    approvalDate: approvalDate,
    others: 'John Doe',
  });
});

Scenario('Upload Manual order (other order)', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.upload);
  await I.goToNextPage();
  await manageOrdersEventPage.selectUploadOrder(manageOrdersEventPage.orders.options.other);
  manageOrdersEventPage.specifyOtherOrderTitle('Order F789s');
  await I.goToNextPage();
  await manageOrdersEventPage.enterApprovalDate(approvalDate);
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  await manageOrdersEventPage.uploadManualOrder(config.testPdfFile);
  manageOrdersEventPage.selectManualOrderNeedSealing(manageOrdersEventPage.section4.manualOrderNeedSealing.options.yes);
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 9,
    orderType: 'Other',
    orderTitle: 'Order F789s',
    approvalDate: approvalDate,
  });
});

Scenario('Create (C26) Secure accommodation order (deprivation of liberty)', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  const newCaseId = await I.submitNewCaseWithData(caseDataWithApplication);
  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, newCaseId);

  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);

  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c26);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.yes);
  await manageOrdersEventPage.selectHearing('Case management hearing, 3 November 2012');
  manageOrdersEventPage.linkApplication(applicationToLink);
  await I.goToNextPage();

  // Judge and approval date is already preFilled
  await I.goToNextPage();

  await manageOrdersEventPage.selectSingleChild('Timothy Jones');

  I.see(manageOrdersEventPage.orders.title.c26);
  manageOrdersEventPage.selectOrderByConsent('Yes');
  manageOrdersEventPage.selectReasonForSecureAccommodation('ABSCOND');
  manageOrdersEventPage.selectWhetherChildIsRepresented('Yes');
  manageOrdersEventPage.selectJurisdiction('ENGLAND');
  manageOrdersEventPage.selectOrderTypeWithMonth(manageOrdersEventPage.section4.orderTypeWithMonth.options.numberOfMonths);
  manageOrdersEventPage.enterSuperVisionNumOfMonths(12);
  manageOrdersEventPage.selectIsFinalOrder();
  await manageOrdersEventPage.enterFurtherDirections('some text');
  await I.goToNextPage();

  await manageOrdersEventPage.checkPreview();
  await manageOrdersEventPage.selectCloseCase();
  await I.goToNextPage();
  await manageOrdersEventPage.selectOthers(manageOrdersEventPage.whichOthers.allOthers.options.select, [0]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Authority to keep a child in secure accommodation (C26)',
    approvalDate: new Date(2012, 10, 3),
    documentName: 'c26_secure_accommodation_order.pdf',
    others: 'John Doe',
  });
});

Scenario('Create Parental responsibility order (C45A)', async ({ I, caseViewPage, manageOrdersEventPage }) => {
  await setupScenario(I, caseViewPage);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.c45a);
  await I.goToNextPage();
  manageOrdersEventPage.selectRelatedToHearing(manageOrdersEventPage.hearingDetails.linkedToHearing.options.no);
  manageOrdersEventPage.confirmNoApplicationCanBeLinked();
  await I.goToNextPage();
  await manageOrdersEventPage.enterJudge();
  await I.goToNextPage();
  await manageOrdersEventPage.selectChildren(manageOrdersEventPage.section3.allChildren.options.select, [0]);
  await I.goToNextPage();
  manageOrdersEventPage.selectOrderByConsent();
  await manageOrdersEventPage.enterNameOfParentResponsible('T.J. Detweiler');
  await manageOrdersEventPage.selectFatherAsResponsible();
  await manageOrdersEventPage.enterFurtherDirections('Further details. '.repeat(10));
  await manageOrdersEventPage.selectIsFinalOrder();
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.goToNextPage();
  pause();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.manageOrders);
  assertOrder(I, caseViewPage, {
    orderIndex: 1,
    orderType: 'Parental responsibility order (C45A)',
    approvalDate: today,
    others: 'John Doe',
  });
});

function assertOrder(I, caseViewPage, order) {
  const orderElement = `Order ${order.orderIndex}`;
  const dateOfApproval = order.approvalDate !== undefined ? order.approvalDate : order.approvalDateTime;
  const mask = order.approvalDate !== undefined ? 'd mmm yyyy' : 'd mmm yyyy, h:MM:ss TT';

  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeInTab([orderElement, 'Type of order'], order.orderType);
  I.seeInTab([orderElement, 'Approval date'], dateFormat(dateOfApproval, mask));

  // Judge details will be removed anyway based on https://tools.hmcts.net/jira/browse/FPLA-3084
  if (order.allocatedJudge) {
    I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Judge or magistrate\'s title'], order.allocatedJudge.title);
    I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Last name'], order.allocatedJudge.name);

    if (order.allocatedJudge.legalAdviserFullName) {
      I.seeInTab([orderElement, 'Judge and Justices\' Legal Adviser', 'Justices\' Legal Adviser\'s full name'], order.allocatedJudge.legalAdviserFullName);
    }
  }

  if (order.specialGuardian) {
    I.seeInTab([orderElement, 'Special guardians'], order.specialGuardian);
  }

  I.seeInTab([orderElement, 'Children'], order.children);
  I.seeInTab([orderElement, 'Others notified'], order.others);
  if (order.title !== undefined) {
    I.seeInTab([orderElement, 'Order title'], orderTitle);
  }

  if (order.documentName !== undefined) {
    I.seeInTab([orderElement, 'Order document'], order.documentName);
  }
}
