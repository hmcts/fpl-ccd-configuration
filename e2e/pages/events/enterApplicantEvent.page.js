const { I } = inject();
const postcodeLookup = require('../../fragments/addressPostcodeLookup');

module.exports = {
  fields: {
    applicant: index => ({
      name: `#applicants_${index}_party_organisationName`,
      address: `#applicants_${index}_party_address_address`,
      email: `input[id="applicants_${index}_party_email_email"]`,
      telephone: `input[id="applicants_${index}_party_telephoneNumber_telephoneNumber"]`,
      nameOfPersonToContact: `input[id="applicants_${index}_party_telephoneNumber_contactDirection"]`,
      mobileNumber: `#applicants_${index}_party_mobileNumber_telephoneNumber`,
      jobTitle: `#applicants_${index}_party_jobTitle`,
      pbaNumber: `input[id="applicants_${index}_party_pbaNumber"]`,
      clientCode: `input[id="applicants_${index}_party_clientCode"]`,
      customerReference: `input[id="applicants_${index}_party_customerReference"]`,
    }),
    solicitor: {
      name: '#solicitor_name',
      mobileNumber: '#solicitor_mobile',
      telephoneNumber: '#solicitor_telephone',
      email: '#solicitor_email',
      dx: '#solicitor_dx',
      reference: '#solicitor_reference',
    },
  },

  async enterApplicantDetails(applicant, applicantIndex = 0) {
    I.fillField(this.fields.applicant(applicantIndex).name, applicant.name);
    this.enterPbaNumber(applicant.pbaNumber, applicantIndex);
    I.fillField(this.fields.applicant(applicantIndex).clientCode, applicant.clientCode);
    I.fillField(this.fields.applicant(applicantIndex).customerReference, applicant.customerReference);
    await within(this.fields.applicant(applicantIndex).address, async () => {
      await postcodeLookup.enterAddressIfNotPresent(applicant.address);
    });

    I.fillField(this.fields.applicant(applicantIndex).telephone, applicant.telephoneNumber);
    I.fillField(this.fields.applicant(applicantIndex).nameOfPersonToContact, applicant.nameOfPersonToContact);
    I.fillField(this.fields.applicant(applicantIndex).mobileNumber, applicant.mobileNumber);
    I.fillField(this.fields.applicant(applicantIndex).jobTitle, applicant.jobTitle);
    I.fillField(this.fields.applicant(applicantIndex).email, applicant.email);
  },

  async enterSolicitorDetails(solicitor) {
    if(solicitor.name) {
      I.fillField(this.fields.solicitor.name, solicitor.name);
    }
    if(solicitor.mobileNumber) {
      I.fillField(this.fields.solicitor.mobileNumber, solicitor.mobileNumber);
    }
    if(solicitor.telephoneNumber) {
      I.fillField(this.fields.solicitor.telephoneNumber, solicitor.telephoneNumber);
    }
    if(solicitor.email) {
      I.fillField(this.fields.solicitor.email, solicitor.email);
    }
    if(solicitor.dx){
      I.fillField(this.fields.solicitor.dx, solicitor.dx);
    }
    if(solicitor.reference){
      I.fillField(this.fields.solicitor.reference, solicitor.reference);
    }
  },

  enterPbaNumber(pbaNumber, applicantIndex = 0 ) {
    I.fillField(this.fields.applicant(applicantIndex).pbaNumber, pbaNumber);
  },
};
