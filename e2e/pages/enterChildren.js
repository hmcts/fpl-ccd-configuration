const I = actor();
const postcodeLookup = require('../fragments/addressPostcodeLookup');

module.exports = {
  fields: function(index) {
    return {
      fullName: `#children_${index}_childName`,
      DOB: {
        day: `#children_${index}_childDOB-day`,
        month: `#children_${index}_childDOB-month`,
        year: `#children_${index}_childDOB-year`,
      },
      gender: `#children_${index}_childGender`,
      situation: {
        radioGroup: `#children_${index}_livingSituation`,
        dateStartedStaying: {
          day: `#children_${index}_situationDate-day`,
          month: `#children_${index}_situationDate-month`,
          year: `#children_${index}_situationDate-year`,
        },
        addressOfChild: `#children_${index}_address_address`,
      },
      keyDates: `#children_${index}_keyDates`,
      careAndContactPlan: `#children_${index}_careAndContact`,
      adoptionNo: `#children_${index}_adoption-No`,
      mothersName: `#children_${index}_mothersName`,
      fathersName: `#children_${index}_fathersName`,
      fatherResponsible: `#children_${index}_fathersResponsibility`,
      socialWorkerName: `#children_${index}_socialWorkerName`,
      socialWorkerTel: `#children_${index}_socialWorkerTel`,
      additionalNeedsNo: `#children_${index}_additionalNeeds-No`,
      contactHiddenNo: `#children_${index}_detailsHidden-No`,
      litigationIssues: {
        yes: `#children_${index}_litigationIssues-YES`,
        no: `#children_${index}_litigationIssues-NO`,
        dont_know: `#children_${index}_litigationIssues-DONT_KNOW`,
      },
      litigationIssuesDetails: `#children_${index}_litigationIssuesDetails`,
    };
  },
  addChildButton: 'Add new',

  addChild() {
    I.click(this.addChildButton);
  },

  async enterChildDetails(name, day, month, year, gender = 'Boy') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).fullName, name);
    I.click(this.fields(elementIndex).DOB.day);
    I.fillField(this.fields(elementIndex).DOB.day, day);
    I.fillField(this.fields(elementIndex).DOB.month, month);
    I.fillField(this.fields(elementIndex).DOB.year, year);
    I.selectOption(this.fields(elementIndex).gender, gender);
  },

  async defineChildSituation(day, month, year) {
    const elementIndex = await this.getActiveElementIndex();

    within(this.fields(elementIndex).situation.radioGroup, () => {
      I.click(locate('label').withText('Living with respondents'));
    });
    I.fillField(this.fields(elementIndex).situation.dateStartedStaying.day, day);
    I.fillField(this.fields(elementIndex).situation.dateStartedStaying.month, month);
    I.fillField(this.fields(elementIndex).situation.dateStartedStaying.year, year);
  },

  async enterAddress(address) {
    const elementIndex = await this.getActiveElementIndex();

    within(this.fields(elementIndex).situation.addressOfChild, () => {
      postcodeLookup.lookupPostcode(address);
    });
  },

  async enterKeyDatesAffectingHearing(keyDates = 'Tuesday the 11th') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).keyDates, keyDates);
  },

  async enterSummaryOfCarePlan(carePlan = 'care plan summary') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).careAndContactPlan, carePlan);
  },

  async defineAdoptionIntention() {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).adoptionNo);
  },

  async enterParentsDetails(fatherResponsible = 'Yes', motherName = 'Laura Smith', fatherName = 'David Smith') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).mothersName, motherName);
    I.fillField(this.fields(elementIndex).fathersName, fatherName);
    I.selectOption(this.fields(elementIndex).fatherResponsible, fatherResponsible);
  },

  async enterSocialWorkerDetails(socialWorkerName = 'James Jackson', socialWorkerTel = '01234567') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).socialWorkerName, socialWorkerName);
    I.fillField(this.fields(elementIndex).socialWorkerTel, socialWorkerTel);
  },

  async defineChildAdditionalNeeds() {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).additionalNeedsNo);
  },

  async defineContactDetailsVisibility() {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).contactHiddenNo);
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
    const count = await I.grabNumberOfVisibleElements('//button[text()="Remove"]');
    if (count === 0) {
      return 'firstChild';
    } else {
      return `additionalChildren_${count - 1}`;
    }
  },
};
