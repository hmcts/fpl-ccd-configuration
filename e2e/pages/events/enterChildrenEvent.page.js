const { I } = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: function (index) {
    return {
      mainSolicitor: {
        childrenHaveLegalRepresentation: {
          group: '#childrenHaveRepresentation',
          options: {
            yes: 'Yes',
            no: 'No',
          },
        },
        childrenHaveSameRepresentation: {
          group: '#childrenHaveSameRepresentation',
          options: {
            yes: 'Yes',
            no: 'No',
          },
        },
        firstName: '#childrenMainRepresentative_firstName',
        lastName: '#childrenMainRepresentative_lastName',
        email: '#childrenMainRepresentative_email',
      },
      childSolicitor: {
        useMainSolicitor: {
          group: `#childRepresentationDetails${index}_useMainSolicitor`,
          options: {
            yes: 'Yes',
            no: 'No',
          },
        },
      },
      child: {
        firstName: `#children1_${index}_party_firstName`,
        lastName: `#children1_${index}_party_lastName`,
        dateOfBirth: {
          day: `#children1_${index}_party_dateOfBirth-day`,
          month: `#children1_${index}_party_dateOfBirth-month`,
          year: `#children1_${index}_party_dateOfBirth-year`,
        },
        address: `#children1_${index}_party_address_address`,
        gender: `#children1_${index}_party_gender`,
        genderIdentification: `#children1_${index}_party_genderIdentification`,
        situation: {
          radioGroup: `#children1_${index}_party_livingSituation`,
          situationDetails: `#children1_${index}_party_livingSituationDetails`,
          dateStartedStaying: {
            day: `#children1_${index}_party_addressChangeDate-day`,
            month: `#children1_${index}_party_addressChangeDate-month`,
            year: `#children1_${index}_party_addressChangeDate-year`,
          },
          addressOfChild: `div[id="children1_${index}_party_address_address"]`,
        },
        keyDates: `#children1_${index}_party_keyDates`,
        careAndContactPlan: `#children1_${index}_party_careAndContactPlan`,
        adoptionNo: `#children1_${index}_party_adoption-No`,
        mothersName: `#children1_${index}_party_mothersName`,
        fathersName: `#children1_${index}_party_fathersName`,
        fatherResponsible: `#children1_${index}_party_fathersResponsibility`,
        socialWorkerName: `#children1_${index}_party_socialWorkerName`,
        socialWorkerTel: `#children1_${index}_party_socialWorkerTelephoneNumber_telephoneNumber`,
        additionalNeedsNo: `#children1_${index}_party_additionalNeeds-No`,
        contactHiddenNo: `#children1_${index}_party_detailsHidden-No`,
        contactHiddenYes: `#children1_${index}_party_detailsHidden-Yes`,
        litigationIssues: {
          yes: `#children1_${index}_party_litigationIssues-YES`,
          no: `#children1_${index}_party_litigationIssues-NO`,
          dont_know: `#children1_${index}_party_litigationIssues-DONT_KNOW`,
        },
        litigationIssuesDetails: `#children1_${index}_party_litigationIssuesDetails`,
      },
    };
  },

  async enterChildDetails(firstName, lastName, day, month, year, gender = 'Boy') {
    const elementIndex = await this.getActiveElementIndex();
    I.fillField(this.fields(elementIndex).child.firstName, firstName);
    I.fillField(this.fields(elementIndex).child.lastName, lastName);
    I.fillField(this.fields(elementIndex).child.dateOfBirth.day, day);
    I.fillField(this.fields(elementIndex).child.dateOfBirth.month, month);
    I.fillField(this.fields(elementIndex).child.dateOfBirth.year, year);
    I.selectOption(this.fields(elementIndex).child.gender, gender);
  },

  async defineChildSituation(day, month, year) {
    const elementIndex = await this.getActiveElementIndex();

    await within(this.fields(elementIndex).child.situation.radioGroup, () => {
      I.click(locate('label').withText('Living with respondents'));
    });
    await I.runAccessibilityTest();
    I.fillField(this.fields(elementIndex).child.situation.dateStartedStaying.day, day);
    I.fillField(this.fields(elementIndex).child.situation.dateStartedStaying.month, month);
    I.fillField(this.fields(elementIndex).child.situation.dateStartedStaying.year, year);
  },

  async enterAddress(address) {
    const elementIndex = await this.getActiveElementIndex();

    await within(this.fields(elementIndex).child.situation.addressOfChild, () => {
      //XXX removed postcode lookup due to instability
      postcodeLookup.enterAddressManually(address);
    });
  },

  async enterKeyDatesAffectingHearing(keyDates = 'Tuesday the 11th') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).child.keyDates, keyDates);
  },

  async enterSummaryOfCarePlan(carePlan = 'care plan summary') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).child.careAndContactPlan, carePlan);
  },

  async defineAdoptionIntention() {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).child.adoptionNo);
  },

  async enterParentsDetails(fatherResponsible = 'Yes', motherName = 'Laura Smith', fatherName = 'David Smith') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).child.mothersName, motherName);
    I.fillField(this.fields(elementIndex).child.fathersName, fatherName);
    I.selectOption(this.fields(elementIndex).child.fatherResponsible, fatherResponsible);
  },

  async enterSocialWorkerDetails(socialWorkerName = 'James Jackson', socialWorkerTel = '01234567890') {
    const elementIndex = await this.getActiveElementIndex();

    I.fillField(this.fields(elementIndex).child.socialWorkerName, socialWorkerName);
    I.fillField(this.fields(elementIndex).child.socialWorkerTel, socialWorkerTel);
  },

  async defineChildAdditionalNeeds() {
    const elementIndex = await this.getActiveElementIndex();

    I.click(this.fields(elementIndex).child.additionalNeedsNo);
  },

  async enterContactDetailsHidden(hideContactDetails) {
    const elementIndex = await this.getActiveElementIndex();

    switch (hideContactDetails) {
      case 'Yes':
        I.click(this.fields(elementIndex).child.contactHiddenYes);
        break;
      case 'No':
        I.click(this.fields(elementIndex).child.contactHiddenNo);
        break;
    }
  },

  async enterLitigationIssues(litigationIssue = 'No', litigationIssueDetail = 'mock reason') {
    const elementIndex = await this.getActiveElementIndex();

    litigationIssue = litigationIssue.toLowerCase();
    switch (litigationIssue) {
      case 'yes':
        I.checkOption(this.fields(elementIndex).child.litigationIssues.yes);
        break;
      case 'no':
        I.checkOption(this.fields(elementIndex).child.litigationIssues.no);
        break;
      case 'dont know':
        I.checkOption(this.fields(elementIndex).child.litigationIssues.dont_know);
        break;
    }
    if (litigationIssue === 'yes') {
      I.fillField(this.fields(elementIndex).child.litigationIssuesDetails, litigationIssueDetail);
    }
  },

  async selectAnyChildHasLegalRepresentation(answer) {
    I.click(`${this.fields().mainSolicitor.childrenHaveLegalRepresentation.group}-${answer}`);
  },

  async selectChildrenHaveSameRepresentation(answer) {
    I.click(`${this.fields().mainSolicitor.childrenHaveSameRepresentation.group}-${answer}`);
  },

  async selectChildUseMainRepresentation(answer, index, child) {
    await within(`#childRepresentationDetails${index}_childRepresentationDetails${index}`, () => I.see(`Child ${index+1} - ${child.firstName} ${child.lastName}`));
    I.click(`${this.fields(index).childSolicitor.useMainSolicitor.group}-${answer}`);
  },

  async enterChildrenMainRepresentation(solicitor) {
    I.fillField(this.fields().mainSolicitor.firstName, solicitor.forename);
    I.fillField(this.fields().mainSolicitor.lastName, solicitor.surname);
    I.fillField(this.fields().mainSolicitor.email, solicitor.email);
  },

  async enterRegisteredOrganisation(solicitor) {
    I.fillField('//input[@id="search-org-text"]', solicitor.organisation);
    let selectedItem = `//*[@id="organisation-table"]/caption/h3[text()="${solicitor.organisation}"]/../../tbody//a`;
    I.click(selectedItem);
    //postcodeLookup.enterAddressManually(solicitor.regionalOfficeAddress);
  },

  async getActiveElementIndex() {
    return await I.grabNumberOfVisibleElements('//button[text()="Remove"]') - 1;
  },
};
