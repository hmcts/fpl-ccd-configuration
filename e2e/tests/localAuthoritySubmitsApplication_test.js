const config = require('../config.js');

const children = require('../fixtures/children.js');
const respondents = require('../fixtures/respondents.js');
const applicant = require('../fixtures/applicant.js');
const solicitor = require('../fixtures/solicitor.js');
const others = require('../fixtures/others.js');
const otherProceedings = require('../fixtures/otherProceedingData');
const caseDocs = require('../fragments/caseDocuments');

let caseId;

Feature('Application draft (populated draft)');

Before(async (I) => {
  if (!caseId) {
    await I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);

    // eslint-disable-next-line require-atomic-updates
    caseId = await I.grabTextFrom('.heading-h1');
    console.log(`Application draft ${caseId} has been created`);
  } else {
    await I.navigateToCaseDetails(caseId);
  }
});

Scenario('local authority changes case name @create-case-with-mandatory-sections-only', async (I, caseViewPage, changeCaseNameEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.changeCaseName);
  changeCaseNameEventPage.changeCaseName();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.changeCaseName);
});

Scenario('local authority enters orders and directions @create-case-with-mandatory-sections-only', async (I, caseViewPage, enterOrdersAndDirectionsNeededEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterOrdersAndDirectionsNeeded);
  enterOrdersAndDirectionsNeededEventPage.checkCareOrder();
  enterOrdersAndDirectionsNeededEventPage.checkInterimCareOrder();
  enterOrdersAndDirectionsNeededEventPage.checkSupervisionOrder();
  enterOrdersAndDirectionsNeededEventPage.checkInterimSupervisionOrder();
  enterOrdersAndDirectionsNeededEventPage.checkEducationSupervisionOrder();
  enterOrdersAndDirectionsNeededEventPage.checkEmergencyProtectionOrder();
  enterOrdersAndDirectionsNeededEventPage.checkOtherOrder();
  enterOrdersAndDirectionsNeededEventPage.checkWhereabouts();
  enterOrdersAndDirectionsNeededEventPage.checkEntry();
  enterOrdersAndDirectionsNeededEventPage.checkSearch();
  enterOrdersAndDirectionsNeededEventPage.checkProtectionOrdersOther();
  enterOrdersAndDirectionsNeededEventPage.enterProtectionOrdersDetails('Test');
  enterOrdersAndDirectionsNeededEventPage.checkContact();
  enterOrdersAndDirectionsNeededEventPage.checkAssessment();
  enterOrdersAndDirectionsNeededEventPage.checkMedicalPractitioner();
  enterOrdersAndDirectionsNeededEventPage.checkExclusion();
  enterOrdersAndDirectionsNeededEventPage.checkProtectionDirectionsOther();
  enterOrdersAndDirectionsNeededEventPage.enterProtectionDirectionsDetails('Test');
  enterOrdersAndDirectionsNeededEventPage.enterOrderDetails('Test');
  enterOrdersAndDirectionsNeededEventPage.checkDirections();
  enterOrdersAndDirectionsNeededEventPage.enterDirections('Test');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOrdersAndDirectionsNeeded);
  caseViewPage.selectTab(caseViewPage.tabs.orders);
  I.seeAnswerInTab(1, 'Orders and directions needed', 'Which orders do you need?', ['Care order', 'Interim care order', 'Supervision order', 'Interim supervision order', 'Education supervision order', 'Emergency protection order', 'Other order under part 4 of the Children Act 1989']);
  I.seeAnswerInTab(2, 'Orders and directions needed', 'Do you need any of these related orders?', ['Information on the whereabouts of the child', 'Authorisation for entry of premises', 'Authorisation to search for another child on the premises', 'Other order under section 48 of the Children Act 1989']);
  I.seeAnswerInTab(3, 'Orders and directions needed', 'Give details', 'Test');
  I.seeAnswerInTab(4, 'Orders and directions needed', 'Do you need any of these directions?', ['Contact with any named person', 'A medical or psychiatric examination, or another assessment of the child', 'To be accompanied by a registered medical practitioner, nurse or midwife', 'An exclusion requirement', 'Other direction relating to an emergency protection order']);
  I.seeAnswerInTab(5, 'Orders and directions needed', 'Give details', 'Test');
  I.seeAnswerInTab(6, 'Orders and directions needed', 'Which order do you need?', 'Test');
  I.seeAnswerInTab(7, 'Orders and directions needed', 'Do you need any other directions?', 'Yes');
  I.seeAnswerInTab(8, 'Orders and directions needed', 'Give details', 'Test');
});

Scenario('local authority enters hearing @create-case-with-mandatory-sections-only', async (I, caseViewPage, enterHearingNeededEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterHearingNeeded);
  enterHearingNeededEventPage.enterTimeFrame();
  enterHearingNeededEventPage.enterHearingType();
  enterHearingNeededEventPage.enterWithoutNoticeHearing();
  enterHearingNeededEventPage.enterReducedHearing();
  enterHearingNeededEventPage.enterRespondentsAware();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterHearingNeeded);
  caseViewPage.selectTab(caseViewPage.tabs.hearings);
  I.seeAnswerInTab(1, 'Hearing needed', 'When do you need a hearing?', enterHearingNeededEventPage.fields.timeFrame.sameDay);
  I.seeAnswerInTab(2, 'Hearing needed', 'Give reason', 'test reason');
  I.seeAnswerInTab(3, 'Hearing needed', 'What type of hearing do you need?', enterHearingNeededEventPage.fields.hearingType.contestedICO);
  I.seeAnswerInTab(4, 'Hearing needed', 'Do you need a without notice hearing?', 'Yes');
  I.seeAnswerInTab(5, 'Hearing needed', 'Do you need a hearing with reduced notice?', 'No');
  I.seeAnswerInTab(6, 'Hearing needed', 'Are respondents aware of proceedings?', 'Yes');
});

Scenario('local authority enters children @create-case-with-mandatory-sections-only', async (I, caseViewPage, enterChildrenEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterChildren);
  await enterChildrenEventPage.enterChildDetails('Bran', 'Stark', '01', '08', '2015');
  await enterChildrenEventPage.defineChildSituation('01', '11', '2017');
  await enterChildrenEventPage.enterAddress(children[0].address);
  await enterChildrenEventPage.enterKeyDatesAffectingHearing();
  await enterChildrenEventPage.enterSummaryOfCarePlan();
  await enterChildrenEventPage.defineAdoptionIntention();
  await enterChildrenEventPage.enterParentsDetails();
  await enterChildrenEventPage.enterSocialWorkerDetails();
  await enterChildrenEventPage.defineChildAdditionalNeeds();
  await enterChildrenEventPage.enterContactDetailsHidden('No');
  await enterChildrenEventPage.enterLitigationIssues('Yes', 'mock reason');
  await I.addAnotherElementToCollection();
  await enterChildrenEventPage.enterChildDetails('Susan', 'Wilson', '01', '07', '2016', 'Girl');
  await enterChildrenEventPage.defineChildSituation('02', '11', '2017');
  await enterChildrenEventPage.enterAddress(children[1].address);
  await enterChildrenEventPage.enterKeyDatesAffectingHearing();
  await enterChildrenEventPage.enterSummaryOfCarePlan();
  await enterChildrenEventPage.defineAdoptionIntention();
  await enterChildrenEventPage.enterParentsDetails();
  await enterChildrenEventPage.enterSocialWorkerDetails();
  await enterChildrenEventPage.defineChildAdditionalNeeds();
  await enterChildrenEventPage.enterContactDetailsHidden('Yes');
  await enterChildrenEventPage.enterLitigationIssues('No');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(3, 'Party', 'First name', 'Bran');
  I.seeAnswerInTab(4, 'Party', 'Last name', 'Stark');
  I.seeAnswerInTab(5, 'Party', 'Date of birth', '1 Aug 2015');
  I.seeAnswerInTab(6, 'Party', 'Gender', 'Boy');
  I.seeAnswerInTab(7, 'Party', 'Child\'s living situation', 'Living with respondents');
  I.seeAnswerInTab(8, 'Party', 'What date did they start staying here?', '1 Nov 2017');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', 'Flat 2');
  I.seeAnswerInTab(2, 'Current address', '', 'Caversham House 15-17');
  I.seeAnswerInTab(3, 'Current address', '', 'Church Road');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Reading');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'RG4 7AA');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(10, 'Party', 'Key dates for this child', 'Tuesday the 11th');
  I.seeAnswerInTab(11, 'Party', 'Brief summary of care and contact plan', 'care plan summary');
  I.seeAnswerInTab(12, 'Party', 'Are you considering adoption at this stage?', 'No');
  I.seeAnswerInTab(13, 'Party', 'Mother\'s full name', 'Laura Smith');
  I.seeAnswerInTab(14, 'Party', 'Father\'s full name', 'David Smith');
  I.seeAnswerInTab(15, 'Party', 'Does the father have parental responsibility?', 'Yes');
  I.seeAnswerInTab(16, 'Party', 'Name of social worker', 'James Jackson');
  I.seeAnswerInTab(1, 'Social worker\'s telephone number', 'Telephone number', '01234567890');
  I.seeAnswerInTab(18, 'Party', 'Does the child have any additional needs?', 'No');
  I.seeAnswerInTab(19, 'Party', 'Do you need contact details hidden from other parties?', 'No');
  I.seeAnswerInTab(20, 'Party', 'Do you believe this child will have problems with litigation capacity (understanding what\'s happening in the case)', 'Yes');
  I.seeAnswerInTab(21, 'Party', 'Give details, including assessment outcomes and referrals to health services', 'mock reason');

  I.seeAnswerInTab(1, 'Party', 'First name', 'Susan');
  I.seeAnswerInTab(2, 'Party', 'Last name', 'Wilson');
  I.seeAnswerInTab(3, 'Party', 'Date of birth', '1 Jul 2016');
  I.seeAnswerInTab(4, 'Party', 'Gender', 'Girl');
  I.seeAnswerInTab(5, 'Party', 'Child\'s living situation', 'Living with respondents');
  I.seeAnswerInTab(6, 'Party', 'What date did they start staying here?', '2 Nov 2017');
  I.seeAnswerInTab(7, 'Party', 'Key dates for this child', 'Tuesday the 11th');
  I.seeAnswerInTab(8, 'Party', 'Brief summary of care and contact plan', 'care plan summary');
  I.seeAnswerInTab(9, 'Party', 'Are you considering adoption at this stage?', 'No');
  I.seeAnswerInTab(10, 'Party', 'Mother\'s full name', 'Laura Smith');
  I.seeAnswerInTab(11, 'Party', 'Father\'s full name', 'David Smith');
  I.seeAnswerInTab(12, 'Party', 'Does the father have parental responsibility?', 'Yes');
  I.seeAnswerInTab(13, 'Party', 'Name of social worker', 'James Jackson');
  I.seeAnswerInTab(1, 'Social worker\'s telephone number', 'Telephone number', '01234567890');
  I.seeAnswerInTab(15, 'Party', 'Does the child have any additional needs?', 'No');
  I.seeAnswerInTab(16, 'Party', 'Do you need contact details hidden from other parties?', 'Yes');
  I.seeAnswerInTab(17, 'Party', 'Do you believe this child will have problems with litigation capacity (understanding what\'s happening in the case)', 'No');

  caseViewPage.selectTab(caseViewPage.tabs.confidential);
  I.seeAnswerInTab(1, 'Party', 'First name', 'Susan');
  I.seeAnswerInTab(2, 'Party', 'Last name', 'Wilson');
  I.seeAnswerInTab(3, 'Party', 'Date of birth', '1 Jul 2016');
  I.seeAnswerInTab(4, 'Party', 'Gender', 'Girl');
  I.seeAnswerInTab(5, 'Party', 'Child\'s living situation', 'Living with respondents');
  I.seeAnswerInTab(6, 'Party', 'What date did they start staying here?', '2 Nov 2017');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', '2 Three Tuns Wynd');
  I.seeAnswerInTab(2, 'Current address', '', 'High Street');
  I.seeAnswerInTab(3, 'Current address', '', 'Stokesley');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Middlesbrough');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'TS9 5DQ');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(8, 'Party', 'Key dates for this child', 'Tuesday the 11th');
  I.seeAnswerInTab(9, 'Party', 'Brief summary of care and contact plan', 'care plan summary');
  I.seeAnswerInTab(10, 'Party', 'Are you considering adoption at this stage?', 'No');
  I.seeAnswerInTab(11, 'Party', 'Mother\'s full name', 'Laura Smith');
  I.seeAnswerInTab(12, 'Party', 'Father\'s full name', 'David Smith');
  I.seeAnswerInTab(13, 'Party', 'Does the father have parental responsibility?', 'Yes');
  I.seeAnswerInTab(14, 'Party', 'Name of social worker', 'James Jackson');
  I.seeAnswerInTab(1, 'Social worker\'s telephone number', 'Telephone number', '01234567890');
  I.seeAnswerInTab(16, 'Party', 'Does the child have any additional needs?', 'No');
  I.seeAnswerInTab(17, 'Party', 'Do you need contact details hidden from other parties?', 'Yes');
  I.seeAnswerInTab(18, 'Party', 'Do you believe this child will have problems with litigation capacity (understanding what\'s happening in the case)', 'No');
});

Scenario('local authority enters respondents @create-case-with-mandatory-sections-only', async (I, caseViewPage, enterRespondentsEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterRespondents);
  await enterRespondentsEventPage.enterRespondent(respondents[0]);
  await enterRespondentsEventPage.enterRelationshipToChild('mock reason');
  await enterRespondentsEventPage.enterContactDetailsHidden('No', 'mock reason');
  await enterRespondentsEventPage.enterLitigationIssues('Yes', 'mock reason');
  await I.addAnotherElementToCollection();
  await enterRespondentsEventPage.enterRespondent(respondents[1]);
  await enterRespondentsEventPage.enterRelationshipToChild('mock reason');
  await enterRespondentsEventPage.enterContactDetailsHidden('Yes', 'mock reason');
  await enterRespondentsEventPage.enterLitigationIssues('No');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRespondents);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(3, 'Party', 'First name', respondents[0].firstName);
  I.seeAnswerInTab(4, 'Party', 'Last name', respondents[0].lastName);
  I.seeAnswerInTab(5, 'Party', 'Date of birth', '1 Jan 1980');
  I.seeAnswerInTab(6, 'Party', 'Gender', respondents[0].gender);
  I.seeAnswerInTab(7, 'Party', 'Place of birth', respondents[0].placeOfBirth);
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', respondents[0].address.buildingAndStreet.lineOne);
  I.seeAnswerInTab(2, 'Current address', '', respondents[0].address.buildingAndStreet.lineTwo);
  I.seeAnswerInTab(3, 'Current address', '', respondents[0].address.buildingAndStreet.lineThree);
  I.seeAnswerInTab(4, 'Current address', 'Town or City', respondents[0].address.town);
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', respondents[0].address.postcode);
  I.seeAnswerInTab(6, 'Current address', 'Country', respondents[0].address.country);
  I.seeAnswerInTab(1, 'Telephone', 'Telephone', respondents[0].telephone);
  I.seeAnswerInTab(10, 'Party', 'What is the respondent\'s relationship to the child or children in this case?', 'mock reason');
  I.seeAnswerInTab(11, 'Party', 'Do you need contact details hidden from other parties?', 'No');
  I.seeAnswerInTab(12, 'Party', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?', 'Yes');
  I.seeAnswerInTab(13, 'Party', 'Give details, including assessment outcomes and referrals to health services', 'mock reason');

  I.seeAnswerInTab(1, 'Party', 'First name', respondents[1].firstName);
  I.seeAnswerInTab(2, 'Party', 'Last name', respondents[1].lastName);
  I.seeAnswerInTab(3, 'Party', 'Date of birth', '1 Jan 1955');
  I.seeAnswerInTab(4, 'Party', 'Gender', respondents[1].gender);
  I.seeAnswerInTab(5, 'Party', 'Place of birth', respondents[1].placeOfBirth);
  I.seeAnswerInTab(6, 'Party', 'What is the respondent\'s relationship to the child or children in this case?', 'mock reason');
  I.seeAnswerInTab(7, 'Party', 'Do you need contact details hidden from other parties?', 'Yes');
  I.seeAnswerInTab(8, 'Party', 'Give reason', 'mock reason');
  I.seeAnswerInTab(9, 'Party', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?', 'No');

  caseViewPage.selectTab(caseViewPage.tabs.confidential);
  I.seeAnswerInTab(1, 'Party', 'First name', respondents[1].firstName);
  I.seeAnswerInTab(2, 'Party', 'Last name', respondents[1].lastName);
  I.seeAnswerInTab(3, 'Party', 'Date of birth', '1 Jan 1955');
  I.seeAnswerInTab(4, 'Party', 'Gender', respondents[1].gender);
  I.seeAnswerInTab(5, 'Party', 'Place of birth', respondents[1].placeOfBirth);
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', respondents[1].address.buildingAndStreet.lineOne);
  I.seeAnswerInTab(2, 'Current address', '', respondents[1].address.buildingAndStreet.lineTwo);
  I.seeAnswerInTab(3, 'Current address', '', respondents[1].address.buildingAndStreet.lineThree);
  I.seeAnswerInTab(4, 'Current address', 'Town or City', respondents[1].address.town);
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', respondents[1].address.postcode);
  I.seeAnswerInTab(6, 'Current address', 'Country', respondents[1].address.country);
  I.seeAnswerInTab(1, 'Telephone', 'Telephone', respondents[1].telephone);
  I.seeAnswerInTab(8, 'Party', 'What is the respondent\'s relationship to the child or children in this case?', 'mock reason');
  I.seeAnswerInTab(9, 'Party', 'Do you need contact details hidden from other parties?', 'Yes');
  I.seeAnswerInTab(10, 'Party', 'Give reason', 'mock reason');
  I.seeAnswerInTab(11, 'Party', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?', 'No');
});

Scenario('local authority enters applicant @create-case-with-mandatory-sections-only', async (I, caseViewPage, enterApplicantEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterApplicant);
  enterApplicantEventPage.enterApplicantDetails(applicant);
  enterApplicantEventPage.enterSolicitorDetails(solicitor);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterApplicant);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(3, 'Party', 'Name of applicant', applicant.name);
  I.seeAnswerInTab(4, 'Party', 'Payment by account (PBA) number', applicant.pbaNumber);
  I.seeAnswerInTab(5, 'Party', 'Client code', applicant.clientCode);
  I.seeAnswerInTab(6, 'Party', 'Customer reference', applicant.customerReference);
  I.seeAnswerInTab(1, 'Address', 'Building and Street', applicant.address.buildingAndStreet.lineOne);
  I.seeAnswerInTab(2, 'Address', '', applicant.address.buildingAndStreet.lineTwo);
  I.seeAnswerInTab(3, 'Address', '', applicant.address.buildingAndStreet.lineThree);
  I.seeAnswerInTab(4, 'Address', 'Town or City', applicant.address.town);
  I.seeAnswerInTab(5, 'Address', 'Postcode/Zipcode', applicant.address.postcode);
  I.seeAnswerInTab(6, 'Address', 'Country', applicant.address.country);
  I.seeAnswerInTab(1, 'Telephone number', 'Telephone number', applicant.telephoneNumber);
  I.seeAnswerInTab(2, 'Telephone number', 'Name of person to contact', applicant.nameOfPersonToContact);
  I.seeAnswerInTab(9, 'Party', 'Job title', applicant.jobTitle);
  I.seeAnswerInTab(1, 'Mobile number', 'Mobile number', applicant.mobileNumber);
  I.seeAnswerInTab(1, 'Email', 'Email', applicant.email);
  I.seeAnswerInTab(1, 'Solicitor', 'Solicitor\'s full name', 'John Smith');
  I.seeAnswerInTab(2, 'Solicitor', 'Solicitor\'s mobile number', '7000000000');
  I.seeAnswerInTab(3, 'Solicitor', 'Solicitor\'s telephone number', '00000000000');
  I.seeAnswerInTab(4, 'Solicitor', 'Solicitor\'s email', 'solicitor@email.com');
  I.seeAnswerInTab(5, 'Solicitor', 'DX number', '160010 Kingsway 7');
  I.seeAnswerInTab(6, 'Solicitor', 'Solicitor\'s reference', 'reference');
});

Scenario('local authority enters others to be given notice', async (I, caseViewPage, enterOthersEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterOthers);
  await enterOthersEventPage.enterOtherDetails(others[0]);
  await enterOthersEventPage.enterRelationshipToChild('Tim Smith');
  await enterOthersEventPage.enterContactDetailsHidden('No');
  await enterOthersEventPage.enterLitigationIssues('No');
  await I.addAnotherElementToCollection('Other person');
  await enterOthersEventPage.enterOtherDetails(others[1]);
  await enterOthersEventPage.enterRelationshipToChild('Tim Smith');
  await enterOthersEventPage.enterContactDetailsHidden('Yes');
  await enterOthersEventPage.enterLitigationIssues('Yes', 'mock reason');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOthers);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(1, 'Person 1', 'Full name', 'John Smith');
  I.seeAnswerInTab(2, 'Person 1', 'Date of birth', '1 Jan 1985');
  I.seeAnswerInTab(3, 'Person 1', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Person 1', 'Place of birth', 'Scotland');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', 'Flat 2');
  I.seeAnswerInTab(2, 'Current address', '', 'Caversham House 15-17');
  I.seeAnswerInTab(3, 'Current address', '', 'Church Road');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Reading');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'RG4 7AA');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(6, 'Person 1', 'Telephone number', '07888288288');
  I.seeAnswerInTab(7, 'Person 1', 'What is this person\'s relationship to the child or children in this case?', 'Tim Smith');
  I.seeAnswerInTab(8, 'Person 1', 'Do you need contact details hidden from other parties?', 'No');
  I.seeAnswerInTab(9, 'Person 1', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?', 'No');

  I.seeAnswerInTab(1, 'Other person 1', 'Full name', 'Paul Wilsdon');
  I.seeAnswerInTab(2, 'Other person 1', 'Date of birth', '1 Jan 1984');
  I.seeAnswerInTab(3, 'Other person 1', 'Gender', 'Male');
  I.seeAnswerInTab(4, 'Other person 1', 'Place of birth', 'Wales');
  I.seeAnswerInTab(5, 'Other person 1', 'What is this person\'s relationship to the child or children in this case?', 'Tim Smith');
  I.seeAnswerInTab(6, 'Other person 1', 'Do you need contact details hidden from other parties?', 'Yes');
  I.seeAnswerInTab(7, 'Other person 1', 'Give reason', 'mock reason');
  I.seeAnswerInTab(8, 'Other person 1', 'Do you believe this person will have problems with litigation capacity (understanding what\'s happening in the case)?', 'Yes');
  I.seeAnswerInTab(9, 'Other person 1', 'Give details, including assessment outcomes and referrals to health services', 'mock reason');

  caseViewPage.selectTab(caseViewPage.tabs.confidential);
  I.seeAnswerInTab(1, 'Others 1', 'Full name', 'Paul Wilsdon');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', '2 Three Tuns Wynd');
  I.seeAnswerInTab(2, 'Current address', '', 'High Street');
  I.seeAnswerInTab(3, 'Current address', '', 'Stokesley');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Middlesbrough');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'TS9 5DQ');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(3, 'Others 1', 'Telephone number', '07888288288');
});

Scenario('local authority enters grounds for non EPO application @create-case-with-mandatory-sections-only', async (I, caseViewPage, enterGroundsForApplicationEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
  enterGroundsForApplicationEventPage.enterThresholdCriteriaDetails();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'How does this case meet the threshold criteria?', '', 'Not receiving care that would be reasonably expected from a parent');
});

Scenario('local authority enters grounds for EPO application @create-case-with-mandatory-sections-only', async (I, caseViewPage, enterGroundsForApplicationEventPage, enterOrdersAndDirectionsNeededEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterOrdersAndDirectionsNeeded);
  enterOrdersAndDirectionsNeededEventPage.checkEmergencyProtectionOrder();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOrdersAndDirectionsNeeded);
  await caseViewPage.goToNewActions(config.applicationActions.enterGrounds);
  enterGroundsForApplicationEventPage.enterGroundsForEmergencyProtectionOrder();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterGrounds);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'How are there grounds for an emergency protection order?', '', [enterGroundsForApplicationEventPage.fields.groundsForApplication.harmIfNotMoved, enterGroundsForApplicationEventPage.fields.groundsForApplication.harmIfMoved, enterGroundsForApplicationEventPage.fields.groundsForApplication.urgentAccessRequired]);
});

Scenario('local authority enters risk and harm to children', async (I, caseViewPage, enterRiskAndHarmToChildrenEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterRiskAndHarmToChildren);
  enterRiskAndHarmToChildrenEventPage.completePhysicalHarm();
  enterRiskAndHarmToChildrenEventPage.completeEmotionalHarm();
  enterRiskAndHarmToChildrenEventPage.completeSexualAbuse();
  enterRiskAndHarmToChildrenEventPage.completeNeglect();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterRiskAndHarmToChildren);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Risks and harm to children', 'Physical harm including non-accidental injury', 'Yes');
  I.seeAnswerInTab(2, 'Risks and harm to children', 'Select all that apply', 'Past harm');
  I.seeAnswerInTab(3, 'Risks and harm to children', 'Emotional harm', 'No');
  I.seeAnswerInTab(4, 'Risks and harm to children', 'Sexual abuse', 'No');
  I.seeAnswerInTab(5, 'Risks and harm to children', 'Neglect', 'Yes');
  I.seeAnswerInTab(6, 'Risks and harm to children', 'Select all that apply', ['Past harm', 'Future risk of harm']);
});

Scenario('local authority enters factors affecting parenting', async (I, caseViewPage, enterFactorsAffectingParentingEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterFactorsAffectingParenting);
  enterFactorsAffectingParentingEventPage.completeAlcoholOrDrugAbuse();
  enterFactorsAffectingParentingEventPage.completeDomesticViolence();
  enterFactorsAffectingParentingEventPage.completeAnythingElse();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterFactorsAffectingParenting);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Factors affecting parenting', 'Alcohol or drug abuse', 'Yes');
  I.seeAnswerInTab(2, 'Factors affecting parenting', 'Give details', 'mock reason');
  I.seeAnswerInTab(3, 'Factors affecting parenting', 'Domestic violence', 'Yes');
  I.seeAnswerInTab(4, 'Factors affecting parenting', 'Give details', 'mock reason');
  I.seeAnswerInTab(5, 'Factors affecting parenting', 'Anything else', 'Yes');
  I.seeAnswerInTab(6, 'Factors affecting parenting', 'Give details', 'mock reason');
});

Scenario('local authority enters international element', async (I, caseViewPage, enterInternationalElementEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterInternationalElement);
  enterInternationalElementEventPage.fillForm();
  I.see('Give reason');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterInternationalElement);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'International element', 'Are there any suitable carers outside of the UK?', 'Yes');
  I.seeAnswerInTab(2, 'International element', 'Give reason', 'test');
  I.seeAnswerInTab(3, 'International element', 'Are you aware of any significant events that have happened outside the UK?', 'Yes');
  I.seeAnswerInTab(4, 'International element', 'Give reason', 'test');
  I.seeAnswerInTab(5, 'International element', 'Are you aware of any issues with the jurisdiction of this case - for example under the Brussels 2 regulation?', 'No');
  I.seeAnswerInTab(6, 'International element', 'Are you aware of any proceedings outside the UK?', 'Yes');
  I.seeAnswerInTab(7, 'International element', 'Give reason', 'test');
  I.seeAnswerInTab(8, 'International element', 'Has, or should, a government or central authority in another country been involved in this case?', 'Yes');
  I.seeAnswerInTab(9, 'International element', 'Give reason', 'International involvement reason');
});

Scenario('local authority enters other proceedings', async (I, caseViewPage, enterOtherProceedingsEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterOtherProceedings);
  enterOtherProceedingsEventPage.selectNoForProceeding();
  enterOtherProceedingsEventPage.selectYesForProceeding();
  await enterOtherProceedingsEventPage.enterProceedingInformation(otherProceedings[0]);
  await I.addAnotherElementToCollection();
  await enterOtherProceedingsEventPage.enterProceedingInformation(otherProceedings[1]);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterOtherProceedings);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Other proceedings', 'Are there any past or ongoing proceedings relevant to this case?', 'Yes');
  I.seeAnswerInTab(2, 'Other proceedings', 'Are these previous or ongoing proceedings?', 'Ongoing');
  I.seeAnswerInTab(3, 'Other proceedings', 'Case number', '000000');
  I.seeAnswerInTab(4, 'Other proceedings', 'Date started', '01/01/01');
  I.seeAnswerInTab(5, 'Other proceedings', 'Date ended', '02/01/01');
  I.seeAnswerInTab(6, 'Other proceedings', 'Orders made', 'Yes');
  I.seeAnswerInTab(7, 'Other proceedings', 'Judge', 'District Judge Martin Brown');
  I.seeAnswerInTab(8, 'Other proceedings', 'Names of children involved', 'Joe Bloggs');
  I.seeAnswerInTab(9, 'Other proceedings', 'Name of guardian', 'John Smith');
  I.seeAnswerInTab(10, 'Other proceedings', 'Is the same guardian needed?', 'Yes');
  I.seeAnswerInTab(1, 'Additional proceedings 1', 'Are these previous or ongoing proceedings?', 'Previous');
  I.seeAnswerInTab(2, 'Additional proceedings 1', 'Case number', '000123');
  I.seeAnswerInTab(3, 'Additional proceedings 1', 'Date started', '02/02/02');
  I.seeAnswerInTab(4, 'Additional proceedings 1', 'Date ended', '03/03/03');
  I.seeAnswerInTab(5, 'Additional proceedings 1', 'Orders made', 'Yes');
  I.seeAnswerInTab(6, 'Additional proceedings 1', 'Judge', 'District Judge Martin Brown');
  I.seeAnswerInTab(7, 'Additional proceedings 1', 'Names of children involved', 'James Simpson');
  I.seeAnswerInTab(8, 'Additional proceedings 1', 'Name of guardian', 'David Burns');
  I.seeAnswerInTab(9, 'Additional proceedings 1', 'Is the same guardian needed?', 'Yes');
});

Scenario('local authority enters allocation proposal', async (I, caseViewPage, enterAllocationProposalEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAllocationProposal);
  enterAllocationProposalEventPage.selectAllocationProposal('Lay justices');
  enterAllocationProposalEventPage.enterProposalReason('test');
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAllocationProposal);
});

Scenario('local authority enters attending hearing', async (I, caseViewPage, enterAttendingHearingEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.enterAttendingHearing);
  enterAttendingHearingEventPage.enterInterpreter();
  enterAttendingHearingEventPage.enterWelshProceedings();
  enterAttendingHearingEventPage.enterIntermediary();
  enterAttendingHearingEventPage.enterDisabilityAssistance();
  enterAttendingHearingEventPage.enterExtraSecurityMeasures();
  enterAttendingHearingEventPage.enterSomethingElse();
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.enterAttendingHearing);
  caseViewPage.selectTab(caseViewPage.tabs.legalBasis);
  I.seeAnswerInTab(1, 'Attending the hearing', 'Interpreter', 'Yes');
  I.seeAnswerInTab(2, 'Attending the hearing', 'Give details including person, language and dialect', 'French translator');
  I.seeAnswerInTab(3, 'Attending the hearing', 'Spoken or written Welsh', 'No');
  I.seeAnswerInTab(4, 'Attending the hearing', 'Intermediary', 'No');
  I.seeAnswerInTab(5, 'Attending the hearing', 'Facilities or assistance for a disability', 'Yes');
  I.seeAnswerInTab(6, 'Attending the hearing', 'Give details', 'learning difficulty');
  I.seeAnswerInTab(7, 'Attending the hearing', 'Separate waiting room or other security measures', 'Yes');
  I.seeAnswerInTab(8, 'Attending the hearing', 'Give details', 'Separate waiting rooms');
  I.seeAnswerInTab(9, 'Attending the hearing', 'Something else', 'Yes');
  I.seeAnswerInTab(10, 'Attending the hearing', 'Give details', 'I need this for this person');
});

Scenario('local authority uploads documents @create-case-with-mandatory-sections-only', caseDocs.uploadDocuments());

Scenario('local authority cannot upload court bundle', async (I, caseViewPage, uploadDocumentsEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.uploadDocuments);
  I.dontSeeElement(uploadDocumentsEventPage.documents.courtBundle);
  await I.completeEvent('Save and continue');
  I.seeEventSubmissionConfirmation(config.applicationActions.uploadDocuments);
});

Scenario('local authority tries to submit without giving consent', async (I, caseViewPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.see(`I, ${config.swanseaLocalAuthorityUserOne.forename} ${config.swanseaLocalAuthorityUserOne.surname}, believe that the facts stated in this application are true.`);
  I.click('Continue');
  I.seeInCurrentUrl('/submitApplication');
});

Scenario('local authority submits after giving consent @create-case-with-mandatory-sections-only', async (I, caseViewPage, submitApplicationEventPage) => {
  await caseViewPage.goToNewActions(config.applicationActions.submitCase);
  I.see('£2,055.00');
  submitApplicationEventPage.giveConsent();
  await I.completeEvent('Submit');
  I.seeEventSubmissionConfirmation(config.applicationActions.submitCase);
  caseViewPage.selectTab(caseViewPage.tabs.documents);
  I.see('council_v_Smith.pdf');
});

Scenario('local authority confirms payment after submission', async  (I, caseViewPage) => {
  caseViewPage.selectTab(caseViewPage.tabs.paymentHistory);
  I.refreshPage(); // Required for some versions of the local build
  I.see('Processed payments'); // Test to pass AAT, to make better
});
