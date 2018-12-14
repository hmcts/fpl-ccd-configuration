const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  state: {
    context: 'firstOther',
  },

  fields: function () {
    const otherNo = this.state.context;
    
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
    if (this.state.context === 'additionalOthers_0') {
      throw new Error('Adding additional others is not supported in the test');
    }

    I.click(this.addOtherButton);
    this.state.context = 'additionalOthers_0';
  },

  enterOtherDetails(other) {
    I.fillField(this.fields().name, other.name);
    I.click(this.fields().DOB.day);
    I.fillField(this.fields().DOB.day, other.DOB.day);
    I.fillField(this.fields().DOB.month, other.DOB.month);
    I.fillField(this.fields().DOB.year, other.DOB.year);
    I.selectOption(this.fields().gender, other.gender);
    I.fillField(this.fields().birthPlace, other.birthPlace);
    within(this.fields().address, () => {
      postcodeLookup.lookupPostcode(other.address);
    });
    I.fillField(this.fields().telephoneNumber, other.telephoneNumber);
  },

  enterRelationshipToChild(childInformation) {
    I.fillField(this.fields().relationshipToChild, childInformation);
  },

  enterContactDetailsHidden(option) {
    I.click(this.fields().detailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields().detailsHidden(option).reason, 'mock reason');
    }
  },

  enterLitigationIssues(litigationIssue = 'No', litigationIssueReason = 'Mock reason') {
    I.selectOption(this.fields().litigationIssue, litigationIssue);
    if (litigationIssue === 'Yes') {
      I.fillField(this.fields().litigationIssuesReason, litigationIssueReason);
    }
  },
};
