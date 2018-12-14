const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

let activeChild = 'firstChild';

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
        addressOfChild: `#children_${childNo}_address_address`,
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

  enterChildDetails(name, day, month, year, gender = 'Boy') {
    I.fillField(this.fields(activeChild).fullName, name);
    I.click(this.fields(activeChild).DOB.day);
    I.fillField(this.fields(activeChild).DOB.day, day);
    I.fillField(this.fields(activeChild).DOB.month, month);
    I.fillField(this.fields(activeChild).DOB.year, year);
    I.selectOption(this.fields(activeChild).gender, gender);
  },

  defineChildSituation(day, month, year, situation = 'Living with respondents') {
    I.selectOption(this.fields(activeChild).situation.selector, situation);
    I.fillField(this.fields(activeChild).situation.dateStartedStaying.day, day);
    I.fillField(this.fields(activeChild).situation.dateStartedStaying.month, month);
    I.fillField(this.fields(activeChild).situation.dateStartedStaying.year, year);
  },

  enterAddress(address) {
    within(this.fields(activeChild).situation.addressOfChild, () => {
      postcodeLookup.lookupPostcode(address);
    });
  },

  enterKeyDatesAffectingHearing(keyDates = 'Tuesday the 11th') {
    I.fillField(this.fields(activeChild).keyDates, keyDates);
  },

  enterSummaryOfCarePlan(carePlan = 'care plan summary') {
    I.fillField(this.fields(activeChild).careAndContactPlan, carePlan);
  },

  defineAdoptionIntention() {
    I.click(this.fields(activeChild).adoptionNo);
  },

  enterParentsDetails(fatherResponsible = 'Yes', motherName = 'Laura Smith', fatherName = 'David Smith') {
    I.fillField(this.fields(activeChild).mothersName, motherName);
    I.fillField(this.fields(activeChild).fathersName, fatherName);
    I.selectOption(this.fields(activeChild).fatherResponsible, fatherResponsible);
  },

  enterSocialWorkerDetails(socialWorkerName = 'James Jackson', socialWorkerTel = '01234567') {
    I.fillField(this.fields(activeChild).socialWorkerName, socialWorkerName);
    I.fillField(this.fields(activeChild).socialWorkerTel, socialWorkerTel);
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
