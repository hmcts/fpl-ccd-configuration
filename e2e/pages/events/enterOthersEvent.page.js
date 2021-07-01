const { I } = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: function (index) {
    return {
      name: `#others_${index}_name`,
      DOB: {
        day: `#others_${index}_DOB-day`,
        month: `#others_${index}_DOB-month`,
        year: `#others_${index}_DOB-year`,
      },
      gender: `#others_${index}_gender`,
      birthPlace: `#others_${index}_birthPlace`,
      address: `#others_${index}_address_address`,
      telephoneNumber: `#others_${index}_telephone`,
      relationshipToChild: `#others_${index}_childInformation`,
      litigationIssues: {
        yes: `#others_${index}_litigationIssues_YES`,
        no: `#others_${index}_litigationIssues_NO`,
        dont_know: `#others_${index}_litigationIssues_DONT_KNOW`,
      },
      litigationIssuesDetails: `#others_${index}_litigationIssuesDetails`,
      detailsHidden: (option) => {
        return {
          option: `#others_${index}_detailsHidden-${option}`,
          reason: `#others_${index}_detailsHiddenReason`,
        };
      },
    };
  },

  async enterOtherDetails(other) {
    const elementIndex = await this.getActiveElementIndex();

    await I.runAccessibilityTest();
    I.fillField(this.fields(elementIndex).name, other.name);
    I.click(this.fields(elementIndex).DOB.day);
    I.fillField(this.fields(elementIndex).DOB.day, other.DOB.day);
    I.fillField(this.fields(elementIndex).DOB.month, other.DOB.month);
    I.fillField(this.fields(elementIndex).DOB.year, other.DOB.year);
    I.selectOption(this.fields(elementIndex).gender, other.gender);
    I.fillField(this.fields(elementIndex).birthPlace, other.birthPlace);
    await within(this.fields(elementIndex).address, () => {
      postcodeLookup.enterAddressManually(other.address);
    });
    I.fillField(this.fields(elementIndex).telephoneNumber, other.telephoneNumber);
  },

  async enterRelationshipToChild(childInformation) {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields(elementIndex).relationshipToChild, childInformation);
  },

  async enterContactDetailsHidden(option) {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).detailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields(elementIndex).detailsHidden(option).reason, 'mock reason');
    }
  },

  async enterLitigationIssues(litigationIssue = 'No', litigationIssueDetail = 'mock reason') {
    const elementIndex = await this.getActiveElementIndex();

    litigationIssue = litigationIssue.toLowerCase();
    switch(litigationIssue) {
      case 'yes':
        I.checkOption(this.fields(elementIndex).litigationIssues.yes);
        break;
      case 'no':
        I.checkOption(this.fields(elementIndex).litigationIssues.no);
        break;
      case 'dont know':
        I.checkOption(this.fields(elementIndex).litigationIssues.dont_know);
        break;
    }
    if (litigationIssue === 'yes') {
      I.fillField(this.fields(elementIndex).litigationIssuesDetails, litigationIssueDetail);
    }
  },

  async getActiveElementIndex() {
    const count = await I.getActiveElementIndex();
    if (count === -1) {
      return 'firstOther';
    } else {
      return `additionalOthers_${count}`;
    }
  },
};
