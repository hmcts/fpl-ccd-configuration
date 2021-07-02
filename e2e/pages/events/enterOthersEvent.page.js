const { I } = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: function (index) {
    return {
      name: `#others_${index}_name`,
      gender: `#others_${index}_gender`,
      DOB: `(//*[@id="DOB"])[${index}]`,
      birthPlace: `#others_${index}_birthPlace`,
      address: `#others_${index}_address_address`,
      telephoneNumber: `#others_${index}_telephone`,
      relationshipToChild: `#others_${index}_childInformation`,
      litigationIssues: {
        yes: `#others_${index}_litigationIssues-YES`,
        no: `#others_${index}_litigationIssues-NO`,
        dont_know: `#others_${index}_litigationIssues-DONT_KNOW`,
      },
      litigationIssuesDetails: `#others_${index}_litigationIssuesDetails`,
      detailsHidden: (option) => {
        return {
          option: `#others_${index}_detailsHidden_${option}`,
          reason: `#others_${index}_detailsHiddenReason`,
        };
      },
    };
  },

  async enterOtherDetails(other) {
    const elementSelector = await this.getActiveElementSelector();
    const elementIndex = await this.getActiveElementIndex();

    await I.runAccessibilityTest();
    I.fillField(this.fields(elementSelector).name, other.name);
    I.fillDate(other.DOB, this.fields(elementIndex).dob);
    I.selectOption(this.fields(elementSelector).gender, other.gender);
    I.fillField(this.fields(elementSelector).birthPlace, other.birthPlace);
    await within(this.fields(elementSelector).address, () => {
      postcodeLookup.enterAddressManually(other.address);
    });
    I.fillField(this.fields(elementSelector).telephoneNumber, other.telephoneNumber);
  },

  async enterRelationshipToChild(childInformation) {
    const elementIndex = await this.getActiveElementSelector();
    I.fillField(this.fields(elementIndex).relationshipToChild, childInformation);
  },

  async enterContactDetailsHidden(option) {
    const elementIndex = await this.getActiveElementSelector();

    I.click(this.fields(elementIndex).detailsHidden(option).option);
    if (option === 'Yes') {
      I.fillField(this.fields(elementIndex).detailsHidden(option).reason, 'mock reason');
    }
  },

  async enterLitigationIssues(litigationIssue = 'No', litigationIssueDetail = 'mock reason') {
    const elementIndex = await this.getActiveElementSelector();

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

  async getActiveElementSelector() {
    const count = await I.getActiveElementIndex();
    if (count === -1) {
      return 'firstOther';
    } else {
      return `additionalOthers_${count}`;
    }
  },

  async getActiveElementIndex() {
    const count = await I.getActiveElementIndex();
    if (count === -1) {
      return 1;
    } else {
      return count + 2;
    }
  },
};
