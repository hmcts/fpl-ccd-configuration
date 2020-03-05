const { I } = inject();

module.exports = {
  fields: function (index) {
    return {
      applicant: {
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
    const elementIndex = 0;

    I.fillField(this.fields(elementIndex).applicant.name, applicant.name);
    I.fillField(this.fields(elementIndex).applicant.pbaNumber, applicant.pbaNumber);
    I.fillField(this.fields(elementIndex).applicant.clientCode, applicant.clientCode);
    I.fillField(this.fields(elementIndex).applicant.customerReference, applicant.customerReference);
    within(this.fields(elementIndex).applicant.address, () => {
      //XXX postcode lookup
      I.fillField(this.fields.buildingAndStreet.lineOne, applicant.address.buildingAndStreet.lineOne);
      I.fillField(this.fields.buildingAndStreet.lineTwo, applicant.address.buildingAndStreet.lineTwo);
      I.fillField(this.fields.buildingAndStreet.lineThree, applicant.address.buildingAndStreet.lineThree);
      I.fillField(this.fields.town, applicant.address.town);
      I.fillField(this.fields.postcode, applicant.address.postcode);
      I.fillField(this.fields.country, applicant.address.country);
    });
    I.fillField(this.fields(elementIndex).applicant.telephone, applicant.telephoneNumber);
    I.fillField(this.fields(elementIndex).applicant.nameOfPersonToContact, applicant.nameOfPersonToContact);
    I.fillField(this.fields(elementIndex).applicant.mobileNumber, applicant.mobileNumber);
    I.fillField(this.fields(elementIndex).applicant.jobTitle, applicant.jobTitle);
    I.fillField(this.fields(elementIndex).applicant.email, applicant.email);
  },

  enterSolicitorDetails(solicitor) {
    const elementIndex = 0;

    I.fillField(this.fields(elementIndex).solicitor.name, solicitor.name);
    I.fillField(this.fields(elementIndex).solicitor.mobileNumber, solicitor.mobileNumber);
    I.fillField(this.fields(elementIndex).solicitor.telephoneNumber, solicitor.telephoneNumber);
    I.fillField(this.fields(elementIndex).solicitor.email, solicitor.email);
    I.fillField(this.fields(elementIndex).solicitor.dx, solicitor.dx);
    I.fillField(this.fields(elementIndex).solicitor.reference, solicitor.reference);
  },
};
