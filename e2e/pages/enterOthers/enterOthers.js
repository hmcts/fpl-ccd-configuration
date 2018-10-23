const I = actor();

module.exports = {

  fields: {
    name: '#others_firstOther_name',
    DOB: {
      day: '#others_firstOther_DOB-day',
      month: '#others_firstOther_DOB-month',
      year: '#others_firstOther_DOB-year',
    },
    gender: '#others_firstOther_gender',
    birthPlace: '#others_firstOther_birthPlace',
    address: '#others_firstOther_address',
    telephoneNumber: '#others_firstOther_telephone',
    relationshipToChild: '#others_firstOther_childInformation',
    detailsHidden: (option) => {
      return {
        option: `#others_firstOther_detailsHidden-${option}`,
        reason: '#others_firstOther_detailsHiddenReason',
      };
    },
    litigationIssues: (option) => {
      return {
        option: `#others_firstOther_litigationIssues-${option}`,
        reason: '#others_firstOther_litigationIssuesReason',
      };
    },
  },

  addOther: 'Add new',

  enterOtherDetails(other) {
    I.fillField(this.fields.name, other.name);
    I.fillField(this.fields.DOB.day, other.DOB.day);
    I.fillField(this.fields.DOB.month, other.DOB.month);
    I.fillField(this.fields.DOB.year, other.DOB.year);
    I.selectOption(this.fields.gender, other.gender);
    I.fillField(this.fields.birthPlace, other.birthPlace);
    I.fillField(this.fields.address, other.address);
    I.fillField(this.fields.telephoneNumber, other.telephoneNumber);
  },

  enterRelationshipToChild(childInformation) {
    I.fillField(this.fields.relationshipToChild, childInformation);
  },

  enterContactDetailsHidden(option) {
    I.click(this.fields.detailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields.detailsHidden(option).reason, 'mock reason');
    }
  },

  enterLitigationIssues(option) {
    I.click(this.fields.litigationIssues(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields.litigationIssues(option).reason, 'mock reason');
    }
  },
};
