const I = actor();
let activeChild = 'firstChild';
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  fields: (childNo) => {
    return {
      fullName: `#children_${childNo}_childName`,
      DOB: {
        day: `#children_${childNo}_childDOB-day`,
        month: `#children_${childNo}_childDOB-month`,
        year: `#children_${childNo}_childDOB-year`,
      },
      gender: `#children_${childNo}_childGender`,
      situation: {
        selector: `#children_${childNo}_livingSituation`,
        dateStartedStaying: {
          day: `#children_${childNo}_situationDate-day`,
          month: `#children_${childNo}_situationDate-month`,
          year: `#children_${childNo}_situationDate-year`,
        },
        addressOfChild: `#children_${childNo}_situationAddress_situationAddress`,
      },
      keyDates: `#children_${childNo}_keyDates`,
      careAndContactPlan: `#children_${childNo}_careAndContact`,
      adoptionNo: `#children_${childNo}_adoption-No`,
      mothersName: `#children_${childNo}_mothersName`,
      fathersName: `#children_${childNo}_fathersName`,
      fatherResponsible: `#children_${childNo}_fathersResponsibility`,
      socialWorkerName: `#children_${childNo}_socialWorkerName`,
      socialWorkerTel: `#children_${childNo}_socialWorkerTel`,
      additionalNeedsNo: `#children_${childNo}_additionalNeeds-No`,
      contactHiddenNo: `#children_${childNo}_detailsHidden-No`,
      litigationNo: `#children_${childNo}_litigationIssues-No`,
    };
  },
  addChildButton: 'Add new',

  addChild() {
    if (activeChild === 'additionalChildren_0') {
      throw new Error('Adding more children is not supported in the test');
    }

    I.click(this.addChildButton);
    activeChild = 'additionalChildren_0';
  },

  enterChildDetails(child) {
    I.fillField(this.fields(activeChild).fullName, child.name);
    I.fillField(this.fields(activeChild).DOB.day, child['DOB'].day);
    I.fillField(this.fields(activeChild).DOB.month, child['DOB'].month);
    I.fillField(this.fields(activeChild).DOB.year, child['DOB'].year);
    I.selectOption(this.fields(activeChild).gender, child.gender);
  },

  defineChildSituation(child) {
    I.selectOption(this.fields(activeChild).situation.selector, child.situation);
    I.fillField(this.fields(activeChild).situation.dateStartedStaying.day, child['DOB'].day);
    I.fillField(this.fields(activeChild).situation.dateStartedStaying.month, child['DOB'].month);
    I.fillField(this.fields(activeChild).situation.dateStartedStaying.year, child['DOB'].year);
    within(this.fields(activeChild).situation.addressOfChild, () => {
      postcodeLookup.lookupPostcode(child);
    });
  },

  enterKeyDatesAffectingHearing(child) {
    I.fillField(this.fields(activeChild).keyDates, child.keyDates);
  },

  enterSummaryOfCarePlan(child) {
    I.fillField(this.fields(activeChild).careAndContactPlan, child.carePlan);
  },

  defineAdoptionIntention() {
    I.click(this.fields(activeChild).adoptionNo);
  },

  enterParentsDetails(child) {
    I.fillField(this.fields(activeChild).mothersName, child.motherName);
    I.fillField(this.fields(activeChild).fathersName, child.fatherName);
    I.selectOption(this.fields(activeChild).fatherResponsible, child.fatherResponsible);
  },

  enterSocialWorkerDetails(child) {
    I.fillField(this.fields(activeChild).socialWorkerName, child.socialWorkerName);
    I.fillField(this.fields(activeChild).socialWorkerTel, child.socialWorkerTel);
  },

  defineChildAdditionalNeeds() {
    I.click(this.fields(activeChild).additionalNeedsNo);
  },

  defineContactDetailsVisibility() {
    I.click(this.fields(activeChild).contactHiddenNo);
  },

  defineAbilityToTakePartInProceedings() {
    I.click(this.fields(activeChild).litigationNo);
  },
};
