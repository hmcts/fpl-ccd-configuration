const config = require('../config.js');

const addresses = [
  {
    lookupOption: 'Flat 2, Caversham House 15-17, Church Road, Reading',
    buildingAndStreet: {
      lineOne: 'Flat 2',
      lineTwo: 'Caversham House 15-17',
      lineThree: 'Church Road',
    },
    town: 'Reading',
    postcode: 'RG4 7AA',
    country: 'United Kingdom',
  },
  {
    lookupOption: '2 Three Tuns Wynd, High Street, Stokesley, Middlesbrough',
    buildingAndStreet: {
      lineOne: '2 Three Tuns Wynd',
      lineTwo: 'High Street',
      lineThree: 'Stokesley',
    },
    town: 'Middlesbrough',
    postcode: 'TS9 5DQ',
    country: 'United Kingdom',
  },
];

Feature('Enter children in application');

Before((I, caseViewPage) => {
  I.logInAndCreateCase(config.swanseaLocalAuthorityEmailUserOne, config.localAuthorityPassword);
  caseViewPage.goToNewActions(config.applicationActions.enterChildren);
});

Scenario('completing half of the enter children in the c110a application', async(I, enterChildrenEventPage, caseViewPage) => {
  await enterChildrenEventPage.enterChildDetails('Timothy', 'Jones', '01', '08', '2015');
  await enterChildrenEventPage.defineChildSituation('01', '11', '2017');
  await enterChildrenEventPage.enterAddress(addresses[0]);
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
});

Scenario('completing entering child information in the c110a application', async(I, enterChildrenEventPage, caseViewPage) => {
  await enterChildrenEventPage.enterChildDetails('Bran', 'Stark', '01', '08', '2015');
  await enterChildrenEventPage.defineChildSituation('01', '11', '2017');
  await enterChildrenEventPage.enterAddress(addresses[0]);
  await enterChildrenEventPage.enterKeyDatesAffectingHearing();
  await enterChildrenEventPage.enterSummaryOfCarePlan();
  await enterChildrenEventPage.defineAdoptionIntention();
  await enterChildrenEventPage.enterParentsDetails();
  await enterChildrenEventPage.enterSocialWorkerDetails();
  await enterChildrenEventPage.defineChildAdditionalNeeds();
  await enterChildrenEventPage.defineContactDetailsVisibility();
  await enterChildrenEventPage.enterLitigationIssues('Yes', 'mock reason');
  await enterChildrenEventPage.addChild();
  await enterChildrenEventPage.enterChildDetails('Susan', 'Wilson', '01', '07', '2016', 'Girl');
  await enterChildrenEventPage.defineChildSituation('02', '11', '2017');
  await enterChildrenEventPage.enterAddress(addresses[1]);
  await enterChildrenEventPage.enterKeyDatesAffectingHearing();
  await enterChildrenEventPage.enterSummaryOfCarePlan();
  await enterChildrenEventPage.defineAdoptionIntention();
  await enterChildrenEventPage.enterParentsDetails();
  await enterChildrenEventPage.enterSocialWorkerDetails();
  await enterChildrenEventPage.defineChildAdditionalNeeds();
  await enterChildrenEventPage.defineContactDetailsVisibility();
  await enterChildrenEventPage.enterLitigationIssues('No');
  I.continueAndSave();
  I.seeEventSubmissionConfirmation(config.applicationActions.enterChildren);
  caseViewPage.selectTab(caseViewPage.tabs.casePeople);
  I.seeAnswerInTab(3, 'Party', 'First name', 'Bran');
  I.seeAnswerInTab(4, 'Party', 'Last name', 'Stark');
  I.seeAnswerInTab(5, 'Party', 'Date of birth', '1 Aug 2015');
  I.seeAnswerInTab(6, 'Party', 'Gender', 'Boy');
  I.seeAnswerInTab(7, 'Party', 'Describe child\'s situation', 'Living with respondents');
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
  I.seeAnswerInTab(1, 'Social worker\'s telephone number', 'Telephone number', '01234567');
  I.seeAnswerInTab(18, 'Party', 'Does the child have any additional needs?', 'No');
  I.seeAnswerInTab(19, 'Party', 'Do you need contact details hidden from other parties?', 'No');
  I.seeAnswerInTab(20, 'Party', 'Do you believe this child will have problems with litigation capacity (understanding what\'s happening in the case)', 'Yes');
  I.seeAnswerInTab(21, 'Party', 'Give details, including assessment outcomes and referrals to health services', 'mock reason');

  I.seeAnswerInTab(3, 'Party', 'First name', 'Susan');
  I.seeAnswerInTab(4, 'Party', 'Last name', 'Wilson');
  I.seeAnswerInTab(5, 'Party', 'Date of birth', '1 Jul 2016');
  I.seeAnswerInTab(6, 'Party', 'Gender', 'Girl');
  I.seeAnswerInTab(7, 'Party', 'Describe child\'s situation', 'Living with respondents');
  I.seeAnswerInTab(8, 'Party', 'What date did they start staying here?', '2 Nov 2017');
  I.seeAnswerInTab(1, 'Current address', 'Building and Street', '2 Three Tuns Wynd');
  I.seeAnswerInTab(2, 'Current address', '', 'High Street');
  I.seeAnswerInTab(3, 'Current address', '', 'Stokesley');
  I.seeAnswerInTab(4, 'Current address', 'Town or City', 'Middlesbrough');
  I.seeAnswerInTab(5, 'Current address', 'Postcode/Zipcode', 'TS9 5DQ');
  I.seeAnswerInTab(6, 'Current address', 'Country', 'United Kingdom');
  I.seeAnswerInTab(10, 'Party', 'Key dates for this child', 'Tuesday the 11th');
  I.seeAnswerInTab(11, 'Party', 'Brief summary of care and contact plan', 'care plan summary');
  I.seeAnswerInTab(12, 'Party', 'Are you considering adoption at this stage?', 'No');
  I.seeAnswerInTab(13, 'Party', 'Mother\'s full name', 'Laura Smith');
  I.seeAnswerInTab(14, 'Party', 'Father\'s full name', 'David Smith');
  I.seeAnswerInTab(15, 'Party', 'Does the father have parental responsibility?', 'Yes');
  I.seeAnswerInTab(16, 'Party', 'Name of social worker', 'James Jackson');
  I.seeAnswerInTab(1, 'Social worker\'s telephone number', 'Telephone number', '01234567');
  I.seeAnswerInTab(18, 'Party', 'Does the child have any additional needs?', 'No');
  I.seeAnswerInTab(19, 'Party', 'Do you need contact details hidden from other parties?', 'No');
  I.seeAnswerInTab(20, 'Party', 'Do you believe this child will have problems with litigation capacity (understanding what\'s happening in the case)', 'No');
});
