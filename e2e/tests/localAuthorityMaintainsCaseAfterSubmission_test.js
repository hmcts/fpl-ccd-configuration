const config = require('../config.js');
const recipients = require('../fixtures/recipients.js');
const legalRepresentatives = require('../fixtures/legalRepresentatives.js');
const api = require('../helpers/api_helper');
const mandatoryWithMultipleChildren = require('../fixtures/caseData/mandatoryWithMultipleChildren.json');

let caseId;

Feature('Case maintenance after submission');

async function setupScenario(I) {
  if (!caseId) {
    caseId = await I.submitNewCaseWithData(mandatoryWithMultipleChildren);
  }
  await I.navigateToCaseDetailsAs(config.swanseaLocalAuthorityUserOne, caseId);
}

Scenario('local authority add an external barrister as a legal representative for the case', async ({ I, caseViewPage, manageLegalRepresentativesEventPage }) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.applicationActions.manageLegalRepresentatives);
  await I.goToNextPage();
  await manageLegalRepresentativesEventPage.addLegalRepresentative(legalRepresentatives.barrister);
  await I.completeEvent('Save and continue');

  I.seeEventSubmissionConfirmation(config.applicationActions.manageLegalRepresentatives);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['LA Legal representatives 1', 'Full name'], legalRepresentatives.barrister.fullName);
  I.seeInTab(['LA Legal representatives 1', 'Role'], legalRepresentatives.barrister.role);
  I.seeInTab(['LA Legal representatives 1', 'Organisation'], legalRepresentatives.barrister.organisation);
  I.seeInTab(['LA Legal representatives 1', 'Email address'], legalRepresentatives.barrister.email);
  I.seeInTab(['LA Legal representatives 1', 'Phone number'], legalRepresentatives.barrister.telephone);
});

Scenario('local authority update its details', async ({ I, caseViewPage, enterLocalAuthorityEventPage }) => {
  await setupScenario(I);
  const solicitorEmail = 'solicitor@test.com';

  await caseViewPage.goToNewActions(config.applicationActions.enterLocalAuthority);
  await I.goToNextPage();
  await enterLocalAuthorityEventPage.enterColleague({ email: solicitorEmail }, 0);
  await I.seeCheckAnswersAndCompleteEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterLocalAuthority);

  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeInTab(['Local authority 1', 'Colleague 1', 'Email address'], solicitorEmail);
});

Scenario('local authority provides a statements of service', async ({ I, caseViewPage, addStatementOfServiceEventPage }) => {
  await setupScenario(I);
  await caseViewPage.goToNewActions(config.administrationActions.addStatementOfService);
  await addStatementOfServiceEventPage.enterRecipientDetails(recipients[0]);
  await I.addAnotherElementToCollection();
  await addStatementOfServiceEventPage.enterRecipientDetails(recipients[1]);
  addStatementOfServiceEventPage.giveDeclaration();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.addStatementOfService);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeInTab(['Recipients 1', 'Name of recipient'], recipients[0].name);
  I.seeInTab(['Recipients 1', 'Do you have the recipient\'s address?'], recipients[0].addressCheck);
  I.seeInTab(['Recipient\'s address', 'Building and Street'], recipients[0].address.buildingAndStreet.lineOne);
  I.seeInTab(['Recipient\'s address', 'Address Line 2'], recipients[0].address.buildingAndStreet.lineTwo);
  I.seeInTab(['Recipient\'s address', 'Address Line 3'], recipients[0].address.buildingAndStreet.lineThree);
  I.seeInTab(['Recipient\'s address', 'Town or City'], recipients[0].address.town);
  I.seeInTab(['Recipient\'s address', 'Postcode/Zipcode'], recipients[0].address.postcode);
  I.seeInTab(['Recipient\'s address', 'Country'], recipients[0].address.country);
  I.seeInTab(['Recipients 1', 'Documents'], recipients[0].documents);
  I.seeInTab(['Recipients 1', 'Date sent'], '1 Jan 2050');
  I.seeInTab(['Recipients 1', 'Time sent'], recipients[0].timeSent);
  I.seeInTab(['Recipients 1', 'How were they sent?'], recipients[0].sentBy);
  I.seeInTab(['Recipients 1', 'Recipient\'s email address'], recipients[0].email);

  I.seeInTab(['Recipients 2', 'Name of recipient'], recipients[1].name);
  I.seeInTab(['Recipients 2', 'Do you have the recipient\'s address?'], recipients[1].addressCheck);
  I.seeInTab(['Recipient\'s address', 'Building and Street'], recipients[1].address.buildingAndStreet.lineOne);
  I.seeInTab(['Recipient\'s address', 'Address Line 2'], recipients[1].address.buildingAndStreet.lineTwo);
  I.seeInTab(['Recipient\'s address', 'Address Line 3'], recipients[1].address.buildingAndStreet.lineThree);
  I.seeInTab(['Recipient\'s address', 'Town or City'], recipients[1].address.town);
  I.seeInTab(['Recipient\'s address', 'Postcode/Zipcode'], recipients[1].address.postcode);
  I.seeInTab(['Recipient\'s address', 'Country'], recipients[1].address.country);
  I.seeInTab(['Recipients 2', 'Documents'], recipients[1].documents);
  I.seeInTab(['Recipients 2', 'Date sent'], '1 Jan 2050');
  I.seeInTab(['Recipients 2', 'Time sent'], recipients[1].timeSent);
  I.seeInTab(['Recipients 2', 'How were they sent?'], recipients[1].sentBy);
  I.seeInTab(['Recipients 2', 'Recipient\'s email address'], recipients[1].email);
});

Scenario('local authority upload placement application and court admin make order', async ({I, caseViewPage, placementEventPage, manageOrdersEventPage}) => {

  const placementFee = '£455.0';
  await setupScenario(I);

  await caseViewPage.goToNewActions(config.administrationActions.placement);
  await placementEventPage.selectChild('Timothy Jones');

  await I.goToNextPage();
  await placementEventPage.addApplication(config.testWordFile);

  placementEventPage.attachSupportingDocument(0, config.testFile);
  placementEventPage.attachSupportingDocument(1, config.testFile2, 'Description 1');
  await placementEventPage.addSupportingDocument(2, 'Maintenance agreement/award', config.testFile3);
  placementEventPage.attachConfidentialDocument(0, config.testFile4);
  await placementEventPage.addConfidentialDocument(1, 'Other confidential documents', config.testFile5, 'Description 2');

  await I.goToNextPage();
  placementEventPage.selectLocalAuthorityNoticeOfPlacementRequired();
  placementEventPage.attachLocalAuthorityNoticeOfPlacement(config.testFile6, 'Description 3');
  placementEventPage.selectLocalAuthorityNoticeOfPlacementResponseReceived();
  placementEventPage.attachLocalAuthorityNoticeOfPlacementResponse(config.testFile7, 'Description 4');
  placementEventPage.selectCafcassNoticeOfPlacementNotRequired();
  placementEventPage.selectFirstParentNoticeOfPlacementRequired();
  placementEventPage.selectFirstParent('Emma Bloggs - Mother');
  placementEventPage.attachFirstParentNoticeOfPlacement(config.testFile8, 'Description 5');
  placementEventPage.selectFirstParentNoticeOfPlacementResponseNotReceived();
  placementEventPage.selectSecondParentNoticeOfPlacementRequired();
  placementEventPage.selectSecondParent('Joe Bloggs - Father');
  placementEventPage.attachSecondParentNoticeOfPlacement(config.testFile9, 'Description 6');
  placementEventPage.selectSecondParentNoticeOfPlacementResponseReceived();
  placementEventPage.attachSecondParentNoticeOfPlacementResponse(config.testFile10, 'Description 7');

  await I.goToNextPage();
  I.see(placementFee);
  await placementEventPage.setPaymentDetails('PBA0082848', '8888', 'Customer reference');

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.placement);

  await caseViewPage.goToNewActions(config.administrationActions.placement);
  await placementEventPage.selectChild('John Black');

  await I.goToNextPage();
  await placementEventPage.addApplication(config.testFile11);
  placementEventPage.attachSupportingDocument(0, config.testFile12);
  placementEventPage.attachSupportingDocument(1, config.testFile13);
  placementEventPage.attachConfidentialDocument(0, config.testFile14);

  await I.goToNextPage();
  placementEventPage.selectLocalAuthorityNoticeOfPlacementNotRequired();
  placementEventPage.selectCafcassNoticeOfPlacementNotRequired();
  placementEventPage.selectFirstParentNoticeOfPlacementNotRequired();
  placementEventPage.selectSecondParentNoticeOfPlacementNotRequired();

  await I.goToNextPage();
  I.see('No further Placement payments required');

  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.placement);

  caseViewPage.selectTab(caseViewPage.tabs.placement);

  I.seeInTab(['Child 1', 'Name'], 'Timothy Jones');
  I.seeInTab(['Child 1', 'Application document'], 'mockFile.pdf');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document type'], 'Birth/Adoption Certificate');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document'], 'mockFile.txt');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document type'], 'Statement of facts');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document'], 'mockFile2.txt');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Description'], 'Description 1');
  I.seeInTab(['Child 1', 'Supporting document 3', 'Document type'], 'Maintenance agreement/award');
  I.seeInTab(['Child 1', 'Supporting document 3', 'Document'], 'mockFile3.txt');

  I.seeInTab(['Child 1', 'Confidential document 1', 'Document type'], 'Annex B');
  I.seeInTab(['Child 1', 'Confidential document 1', 'Document'], 'mockFile4.txt');
  I.seeTagInTab(['Child 1', 'Confidential document 1', 'Confidential']);
  I.seeInTab(['Child 1', 'Confidential document 2', 'Document type'], 'Other confidential documents');
  I.seeInTab(['Child 1', 'Confidential document 2', 'Document'], 'mockFile5.txt');
  I.seeInTab(['Child 1', 'Confidential document 2', 'Description'], 'Description 2');
  I.seeTagInTab(['Child 1', 'Confidential document 1', 'Confidential']);

  I.seeInTab(['Child 1', 'Notice of placement 1', 'Party'], 'Local authority');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement'], 'mockFile6.txt');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement description'], 'Description 3');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement response'], 'mockFile7.txt');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement response description'], 'Description 4');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Party'], 'Emma Bloggs - Mother');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement'], 'mockFile8.txt');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement description'], 'Description 5');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Party'], 'Joe Bloggs - Father');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement'], 'mockFile9.txt');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement description'], 'Description 6');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement response'], 'mockFile10.txt');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement response description'], 'Description 7');

  I.seeInTab(['Child 2', 'Name'], 'John Black');
  I.seeInTab(['Child 2', 'Application document'], 'mockFile11.pdf');
  I.seeInTab(['Child 2', 'Supporting document 1', 'Document type'], 'Birth/Adoption Certificate');
  I.seeInTab(['Child 2', 'Supporting document 1', 'Document'], 'mockFile12.txt');
  I.seeInTab(['Child 2', 'Supporting document 2', 'Document type'], 'Statement of facts');
  I.seeInTab(['Child 2', 'Supporting document 2', 'Document'], 'mockFile13.txt');

  I.seeInTab(['Child 2', 'Confidential document 1', 'Document type'], 'Annex B');
  I.seeInTab(['Child 2', 'Confidential document 1', 'Document'], 'mockFile14.txt');
  I.seeTagInTab(['Child 2', 'Confidential document 1', 'Confidential']);

  await caseViewPage.goToNewActions(config.administrationActions.placement);
  await placementEventPage.selectChild('Timothy Jones');
  await I.goToNextPage();

  placementEventPage.attachSupportingDocument(1, config.testFile15, 'Description updated');
  await I.goToNextPage();

  placementEventPage.selectFirstParentNoticeOfPlacementResponseReceived();
  placementEventPage.attachFirstParentNoticeOfPlacementResponse(config.testFile16, 'Received today');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.administrationActions.placement);

  caseViewPage.selectTab(caseViewPage.tabs.placement);
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document type'], 'Statement of facts');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document'], 'mockFile15.txt');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Description'], 'Description updated');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Party'], 'Emma Bloggs - Mother');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement'], 'mockFile8.txt');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement description'], 'Description 5');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement response'], 'mockFile16.txt');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement response description'], 'Received today');

  I.say('Respondent solicitor can not see notice responses');
  await api.grantCaseAccess(caseId, config.privateSolicitorOne, '[SOLICITORA]');
  await I.navigateToCaseDetailsAs(config.privateSolicitorOne, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.placement);

  I.seeInTab(['Child 1', 'Name'], 'Timothy Jones');
  I.seeInTab(['Child 1', 'Application document'], 'mockFile.pdf');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document type'], 'Birth/Adoption Certificate');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document'], 'mockFile.txt');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document type'], 'Statement of facts');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document'], 'mockFile15.txt');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Description'], 'Description updated');
  I.seeInTab(['Child 1', 'Supporting document 3', 'Document type'], 'Maintenance agreement/award');
  I.seeInTab(['Child 1', 'Supporting document 3', 'Document'], 'mockFile3.txt');

  I.dontSeeInTab(['Child 1', 'Confidential document 1', 'Document type']);
  I.dontSeeInTab(['Child 1', 'Confidential document 1', 'Document']);

  I.seeInTab(['Child 1', 'Notice of placement 1', 'Party'], 'Local authority');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement'], 'mockFile6.txt');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement description'], 'Description 3');
  I.dontSeeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement response']);
  I.dontSeeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement response description']);

  I.seeInTab(['Child 1', 'Notice of placement 2', 'Party'], 'Emma Bloggs - Mother');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement'], 'mockFile8.txt');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement description'], 'Description 5');
  I.dontSeeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement response']);
  I.dontSeeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement response description']);

  I.seeInTab(['Child 1', 'Notice of placement 3', 'Party'], 'Joe Bloggs - Father');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement'], 'mockFile9.txt');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement description'], 'Description 6');
  I.dontSeeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement response']);
  I.dontSeeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement response description']);

  I.seeInTab(['Child 2', 'Name'], 'John Black');
  I.seeInTab(['Child 2', 'Application document'], 'mockFile11.pdf');
  I.seeInTab(['Child 2', 'Supporting document 1', 'Document type'], 'Birth/Adoption Certificate');
  I.seeInTab(['Child 2', 'Supporting document 1', 'Document'], 'mockFile12.txt');
  I.seeInTab(['Child 2', 'Supporting document 2', 'Document type'], 'Statement of facts');
  I.seeInTab(['Child 2', 'Supporting document 2', 'Document'], 'mockFile13.txt');

  I.dontSeeInTab(['Child 2', 'Confidential document 1', 'Document type']);
  I.dontSeeInTab(['Child 2', 'Confidential document 1', 'Document']);

  I.say('Child solicitor can see notice responses');

  await api.grantCaseAccess(caseId, config.privateSolicitorTwo, '[CHILDSOLICITORA]');
  await I.navigateToCaseDetailsAs(config.privateSolicitorTwo, caseId);
  caseViewPage.selectTab(caseViewPage.tabs.placement);

  I.seeInTab(['Child 1', 'Name'], 'Timothy Jones');
  I.seeInTab(['Child 1', 'Application document'], 'mockFile.pdf');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document type'], 'Birth/Adoption Certificate');
  I.seeInTab(['Child 1', 'Supporting document 1', 'Document'], 'mockFile.txt');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document type'], 'Statement of facts');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Document'], 'mockFile15.txt');
  I.seeInTab(['Child 1', 'Supporting document 2', 'Description'], 'Description updated');
  I.seeInTab(['Child 1', 'Supporting document 3', 'Document type'], 'Maintenance agreement/award');
  I.seeInTab(['Child 1', 'Supporting document 3', 'Document'], 'mockFile3.txt');

  I.dontSeeInTab(['Child 1', 'Confidential document 1', 'Document type']);
  I.dontSeeInTab(['Child 1', 'Confidential document 1', 'Document']);

  I.seeInTab(['Child 1', 'Notice of placement 1', 'Party'], 'Local authority');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement'], 'mockFile6.txt');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement description'], 'Description 3');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement response'], 'mockFile7.txt');
  I.seeInTab(['Child 1', 'Notice of placement 1', 'Notice of placement response description'], 'Description 4');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Party'], 'Emma Bloggs - Mother');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement'], 'mockFile8.txt');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement description'], 'Description 5');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement response'], 'mockFile16.txt');
  I.seeInTab(['Child 1', 'Notice of placement 2', 'Notice of placement response description'], 'Received today');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Party'], 'Joe Bloggs - Father');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement'], 'mockFile9.txt');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement description'], 'Description 6');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement response'], 'mockFile10.txt');
  I.seeInTab(['Child 1', 'Notice of placement 3', 'Notice of placement response description'], 'Description 7');

  I.seeInTab(['Child 2', 'Name'], 'John Black');
  I.seeInTab(['Child 2', 'Application document'], 'mockFile11.pdf');
  I.seeInTab(['Child 2', 'Supporting document 1', 'Document type'], 'Birth/Adoption Certificate');
  I.seeInTab(['Child 2', 'Supporting document 1', 'Document'], 'mockFile12.txt');
  I.seeInTab(['Child 2', 'Supporting document 2', 'Document type'], 'Statement of facts');
  I.seeInTab(['Child 2', 'Supporting document 2', 'Document'], 'mockFile13.txt');

  I.dontSeeInTab(['Child 2', 'Confidential document 1', 'Document type']);
  I.dontSeeInTab(['Child 2', 'Confidential document 1', 'Document']);

  I.say('Admin generates placement order');

  await I.navigateToCaseDetailsAs(config.hmctsAdminUser, caseId);
  await caseViewPage.goToNewActions(config.administrationActions.manageOrders);
  await manageOrdersEventPage.selectOperation(manageOrdersEventPage.operations.options.create);
  await I.goToNextPage();
  await manageOrdersEventPage.selectOrder(manageOrdersEventPage.orders.options.a70);
  await I.goToNextPage();
  manageOrdersEventPage.selectPlacementApplication('Timothy Jones');
  manageOrdersEventPage.enterJudge();
  await manageOrdersEventPage.enterApprovalDate({day: '27', month: '04', year:'2021'});
  await I.goToNextPage();
  manageOrdersEventPage.selectIsFinalOrder();
  manageOrdersEventPage.fillPlacementOrderSpecificFields({
    serialNumber: '123',
    birthCertificateNumber: 'BC-123',
    birthCertificateDate: '12-Dec-2019',
    birthCertificateDistrict: 'My District',
    birthCertificateSubDistrict: 'My Sub-district',
    birthCertificateCounty: 'My County',
  });
  await I.goToNextPage();
  await manageOrdersEventPage.checkPreview();
  await I.completeEvent('Save and continue');

  caseViewPage.selectTab(caseViewPage.tabs.orders);

  I.seeInTab(['Order 1', 'Type of order'], 'Placement Order (A70)');
  I.seeInTab(['Order 1', 'Order document'], 'a70_placement_order.pdf');
  I.seeInTab(['Order 1', 'Approval date'], 	'27 Apr 2021');
  I.seeInTab(['Order 1', 'Children'], 'Timothy Jones');
  I.seeInTab(['Order 1', 'Notification document'], 'placement_order_notification_a206.pdf');

});

