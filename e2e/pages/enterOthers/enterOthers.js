const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');
let activeOther = 'firstOther';

module.exports = {

  fields: (otherNo) => {
    return {
      name: `#others_${otherNo}_name`,
      DOB: {
        day: `#others_${otherNo}_DOB-day`,
        month: `#others_${otherNo}_DOB-month`,
        year: `#others_${otherNo}_DOB-year`,
      },
      gender: `#others_${otherNo}_gender`,
      birthPlace: `#others_${otherNo}_birthPlace`,
      address: `#others_${otherNo}_address_address`,
      telephoneNumber: `#others_${otherNo}_telephone`,
      relationshipToChild: `#others_${otherNo}_childInformation`,
      litigationIssue: `#others_${otherNo}_litigationIssues`,
      litigationIssuesReason: `#others_${otherNo}_litigationIssuesReason`,
      detailsHidden: (option) => {
        return {
          option: `#others_${otherNo}_detailsHidden-${option}`,
          reason: `#others_${otherNo}_detailsHiddenReason`,
        };
      },
    };
  },

  addOtherButton: 'Add new',

  addOther() {
    if (activeOther === 'additionalOthers_0') {
      throw new Error('Adding additional others is not supported in the test');
    }

    I.click(this.addOtherButton);
    activeOther = 'additionalOthers_0';
  },

  enterOtherDetails(other) {
    I.waitForElement(this.fields(activeOther).name);
    I.fillField(this.fields(activeOther).name, other.name);
    I.fillField(this.fields(activeOther).DOB.day, other.DOB.day);
    I.fillField(this.fields(activeOther).DOB.month, other.DOB.month);
    I.fillField(this.fields(activeOther).DOB.year, other.DOB.year);
    I.selectOption(this.fields(activeOther).gender, other.gender);
    I.fillField(this.fields(activeOther).birthPlace, other.birthPlace);
    within(this.fields(activeOther).address, () => {
      postcodeLookup.lookupPostcode(other.address);
    });
    I.fillField(this.fields(activeOther).telephoneNumber, other.telephoneNumber);
  },

  enterRelationshipToChild(childInformation) {
    I.fillField(this.fields(activeOther).relationshipToChild, childInformation);
  },

  enterContactDetailsHidden(option) {
    I.click(this.fields(activeOther).detailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields(activeOther).detailsHidden(option).reason, 'mock reason');
    }
  },

  enterLitigationIssues(litigationIssue = 'No', litigationIssueReason = 'Mock reason') {
    I.selectOption(this.fields(activeOther).litigationIssue, litigationIssue);
    if (litigationIssue === 'Yes') {
      I.fillField(this.fields(activeOther).litigationIssuesReason, litigationIssueReason);
    }
  },
};
