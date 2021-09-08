const {I} = inject();

module.exports = {
  fields:
      {
        childList: '#placementChildrenList',
        placement: {
          application: {
            file: '#placement_placementApplication',
          },
          supportingDocuments: function (index) {
            return {
              file: `#placement_placementSupportingDocuments_${index}_document`,
              type: `#placement_placementSupportingDocuments_${index}_type`,
              description: `#placement_placementSupportingDocuments_${index}_description`,
            };
          },
          confidentialDocuments: function (index) {
            return {
              file: `#placement_placementConfidentialDocuments_${index}_document`,
              type: `#placement_placementConfidentialDocuments_${index}_type`,
              description: `#placement_placementConfidentialDocuments_${index}_description`,
            };
          },
        },
        payment:
            {
              pbaNumber: '#placementPayment_pbaNumber',
              clientCode: '#placementPayment_clientCode',
              customerReference: '#placementPayment_fileReference',
            },
      },

  async selectChild(childName) {
    await I.runAccessibilityTest();
    await I.selectOption(this.fields.childList, childName);
    await I.goToNextPage();
  }
  ,

  async addApplication(file) {
    await I.attachFile(this.fields.placement.application.file, file);
  }
  ,

  async addSupportingDocument(index, type, file, description = '') {
    await I.runAccessibilityTest();
    await I.addAnotherElementToCollection('Supporting document');
    await I.selectOption(this.fields.placement.supportingDocuments(index).type, type);
    await I.attachFile(this.fields.placement.supportingDocuments(index).file, file);
    await I.fillField(this.fields.placement.supportingDocuments(index).description, description);
  },

  async addConfidentialDocument(index, type, file, description = '') {
    await I.addAnotherElementToCollection('Confidential document');
    await I.selectOption(this.fields.placement.confidentialDocuments(index).type, type);
    await I.attachFile(this.fields.placement.confidentialDocuments(index).file, file);
    await I.fillField(this.fields.placement.confidentialDocuments(index).description, description);
  },

  async setPaymentDetails(pbaNumber, clientCode, customerReference) {
    await I.runAccessibilityTest();
    if (pbaNumber) {
      I.fillField(this.fields.payment.pbaNumber, pbaNumber);
    }
    if (clientCode) {
      I.fillField(this.fields.payment.clientCode, clientCode);
    }
    if (customerReference) {
      I.fillField(this.fields.payment.customerReference, customerReference);
    }
  },
};
