const {I} = inject();

module.exports = {
  fields: function (index) {
    return {
      childList: '#childrenList',
      placement: {
        application: {
          file: '#placement_placementApplication',
        },
        supportingDocuments: {
          file: `#placement_placementSupportingDocuments_${index}_document`,
          type: `#placement_placementSupportingDocuments_${index}_type`,
          description: `#placement_placementSupportingDocuments_${index}_description`,
        },
        confidentialDocuments: {
          file: `#placement_placementConfidentialDocuments_${index}_document`,
          type: `#placement_placementConfidentialDocuments_${index}_type`,
          description: `#placement_placementConfidentialDocuments_${index}_description`,
        },
        noticeAndOrders: {
          file: `#placement_placementOrderAndNotices_${index}_document`,
          type: `#placement_placementOrderAndNotices_${index}_type`,
          description: `#placement_placementOrderAndNotices_${index}_description`,
        },
      },
    };
  },

  async selectChild(childName){
    await I.selectOption(this.fields().childList, childName);
    await I.click('Continue');
  },

  async addApplication(file) {
    await I.attachFile(this.fields().placement.application.file, file);
  },

  async addSupportingDocument(index, type, file, description = '') {
    await I.addAnotherElementToCollection('Upload supporting documents');
    await I.selectOption(this.fields(index).placement.supportingDocuments.type, type);
    await I.attachFile(this.fields(index).placement.supportingDocuments.file, file);
    await I.fillField(this.fields(index).placement.supportingDocuments.description, description);
  },

  async addConfidentialDocument(index, type, file, description = '') {
    await I.addAnotherElementToCollection('Upload confidential documents');
    await I.selectOption(this.fields(index).placement.confidentialDocuments.type, type);
    await I.attachFile(this.fields(index).placement.confidentialDocuments.file, file);
    await I.fillField(this.fields(index).placement.confidentialDocuments.description, description);
  },

  async addOrderOrNotice(index, type, file, description) {
    await I.addAnotherElementToCollection('Upload placement order and notices');
    await I.selectOption(this.fields(index).placement.noticeAndOrders.type, type);
    await I.attachFile(this.fields(index).placement.noticeAndOrders.file, file);
    await I.fillField(this.fields(index).placement.noticeAndOrders.description, description);
  },
};
