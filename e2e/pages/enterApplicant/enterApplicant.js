const I = actor();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {

  state: {
    context: 0,
  },

  fields: function () {
    const id = this.state.context;

    return {
      applicant: {
        name: `#applicants_${id}_party_organisationName`,
        address: `#applicants_${id}_party_address_address`,
        email: `input[id="applicants_${id}_party_email_email"]`,
        telephone: `input[id="applicants_${id}_party_telephoneNumber_telephoneNumber"]`,
        nameOfPersonToContact: `input[id="applicants_${id}_party_telephoneNumber_contactDirection"]`,
        mobileNumber: `#applicants_${id}_party_mobileNumber_telephoneNumber`,
        jobTitle: `#applicants_${id}_party_jobTitle`,
        pbaNumber: `input[id="applicants_${id}_party_pbaNumber"]`,
      },
      solicitor: {
        name: '#solicitor_name',
        mobileNumber: '#solicitor_mobile',
        telephoneNumber: '#solicitor_telephone',
        email: '#solicitor_email',
        dx: '#solicitor_dx',
        reference: '#solicitor_reference',
      },
    };
  },

  enterApplicantDetails(applicant) {
    I.fillField(this.fields().applicant.name, applicant.name);
    I.fillField(this.fields().applicant.pbaNumber, applicant.pbaNumber);
    within(this.fields().applicant.address, () => {
      postcodeLookup.lookupPostcode(applicant.address);
    });
    I.fillField(this.fields().applicant.telephone, applicant.telephoneNumber);
    I.fillField(this.fields().applicant.nameOfPersonToContact, applicant.nameOfPersonToContact);
    I.fillField(this.fields().applicant.mobileNumber, applicant.mobileNumber);
    I.fillField(this.fields().applicant.jobTitle, applicant.jobTitle);
    I.fillField(this.fields().applicant.email, applicant.email);
  },

  enterSolicitorDetails(solicitor) {
    I.fillField(this.fields().solicitor.name, solicitor.name);
    I.fillField(this.fields().solicitor.mobileNumber, solicitor.mobileNumber);
    I.fillField(this.fields().solicitor.telephoneNumber, solicitor.telephoneNumber);
    I.fillField(this.fields().solicitor.email, solicitor.email);
    I.fillField(this.fields().solicitor.dx, solicitor.dx);
    I.fillField(this.fields().solicitor.reference, solicitor.reference);
  },
};
