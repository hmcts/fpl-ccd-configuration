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
        notice: {
          localAuthority: {
            required: '#placementNoticeForLocalAuthorityRequired_Yes',
            notRequired: '#placementNoticeForLocalAuthorityRequired_No',
            file: '#placementNoticeForLocalAuthority',
            description: '#placementNoticeForLocalAuthorityDescription',
            response: {
              received: '#placementNoticeResponseFromLocalAuthorityReceived_Yes',
              notReceived: '#placementNoticeResponseFromLocalAuthorityReceived_No',
              file: '#placementNoticeResponseFromLocalAuthority',
              description: '#placementNoticeResponseFromLocalAuthorityDescription',
            },
          },
          cafcass: {
            required: '#placementNoticeForCafcassRequired_Yes',
            notRequired: '#placementNoticeForCafcassRequired_No',
            file: '#placementNoticeForCafcass',
            description: '#placementNoticeForCafcassDescription',
            response: {
              received: '#placementNoticeResponseFromCafcassReceived_Yes',
              notReceived: '#placementNoticeResponseFromCafcassReceived_No',
              file: '#placementNoticeResponseFromCafcass',
              description: '#placementNoticeResponseFromCafcassDescription',
            },
          },
          firstParent: {
            required: '#placementNoticeForFirstParentRequired_Yes',
            notRequired: '#placementNoticeForFirstParentRequired_No',
            parent: '#placementNoticeForFirstParentParentsList',
            file: '#placementNoticeForFirstParent',
            description: '#placementNoticeForFirstParentDescription',
            response: {
              received: '#placementNoticeResponseFromFirstParentReceived_Yes',
              notReceived: '#placementNoticeResponseFromFirstParentReceived_No',
              file: '#placementNoticeResponseFromFirstParent',
              description: '#placementNoticeResponseFromFirstParentDescription',
            },
          },
          secondParent: {
            required: '#placementNoticeForSecondParentRequired_Yes',
            notRequired: '#placementNoticeForSecondParentRequired_No',
            parent: '#placementNoticeForSecondParentParentsList',
            file: '#placementNoticeForSecondParent',
            description: '#placementNoticeForSecondParentDescription',
            response: {
              received: '#placementNoticeResponseFromSecondParentReceived_Yes',
              notReceived: '#placementNoticeResponseFromSecondParentReceived_No',
              file: '#placementNoticeResponseFromSecondParent',
              description: '#placementNoticeResponseFromSecondParentDescription',
            },
          },
        },
        payment:
            {
              pbaNumber: '#placementPayment_pbaNumber',
              clientCode: '#placementPayment_clientCode',
              customerReference: '#placementPayment_fileReference',
            },
        allRespondents: {
          group: '#sendPlacementNoticeToAllRespondents',
          options: {
            all: 'Yes',
            select: 'No',
          },
        },
        respondentsSelector: {
          selector: index => `#respondentsSelector_option${index}-SELECTED`,
        },
      },

  async selectChild(childName) {
    await I.runAccessibilityTest();
    I.selectOption(this.fields.childList, childName);
  },

  async addApplication(file) {
    await I.runAccessibilityTest();
    I.attachFile(this.fields.placement.application.file, file);
  },

  attachSupportingDocument(index, file, description = '') {
    I.attachFile(this.fields.placement.supportingDocuments(index).file, file);
    I.fillField(this.fields.placement.supportingDocuments(index).description, description);
  },

  attachConfidentialDocument(index, file, description = '') {
    I.attachFile(this.fields.placement.confidentialDocuments(index).file, file);
    I.fillField(this.fields.placement.confidentialDocuments(index).description, description);
  },

  async addSupportingDocument(index, type, file, description = '') {
    await I.addAnotherElementToCollection('Supporting document');
    I.selectOption(this.fields.placement.supportingDocuments(index).type, type);
    I.attachFile(this.fields.placement.supportingDocuments(index).file, file);
    I.fillField(this.fields.placement.supportingDocuments(index).description, description);
  },

  async addConfidentialDocument(index, type, file, description = '') {
    await I.addAnotherElementToCollection('Confidential document');
    I.selectOption(this.fields.placement.confidentialDocuments(index).type, type);
    I.attachFile(this.fields.placement.confidentialDocuments(index).file, file);
    I.fillField(this.fields.placement.confidentialDocuments(index).description, description);
  },

  selectLocalAuthorityNoticeOfPlacementRequired() {
    I.click(this.fields.notice.localAuthority.required);
  },

  selectLocalAuthorityNoticeOfPlacementNotRequired() {
    I.click(this.fields.notice.localAuthority.notRequired);
  },

  attachLocalAuthorityNoticeOfPlacement(file, description = '') {
    I.attachFile(this.fields.notice.localAuthority.file, file);
    I.fillField(this.fields.notice.localAuthority.description, description);
  },

  selectLocalAuthorityNoticeOfPlacementResponseReceived() {
    I.click(this.fields.notice.localAuthority.response.received);
  },

  selectLocalAuthorityNoticeOfPlacementResponseNotReceived() {
    I.click(this.fields.notice.localAuthority.response.notReceived);
  },

  attachLocalAuthorityNoticeOfPlacementResponse(file, description = '') {
    I.attachFile(this.fields.notice.localAuthority.response.file, file);
    I.fillField(this.fields.notice.localAuthority.response.description, description);
  },

  selectCafcassNoticeOfPlacementRequired() {
    I.click(this.fields.notice.cafcass.required);
  },

  selectCafcassNoticeOfPlacementNotRequired() {
    I.click(this.fields.notice.cafcass.notRequired);
  },

  attachCafcassNoticeOfPlacement(file, description = '') {
    I.attachFile(this.fields.notice.cafcass.file, file);
    I.fillField(this.fields.notice.cafcass.description, description);
  },

  selectCafcassNoticeOfPlacementResponseReceived() {
    I.click(this.fields.notice.cafcass.response.received);
  },

  selectCafcassNoticeOfPlacementResponseNotReceived() {
    I.click(this.fields.notice.cafcass.response.notReceived);
  },

  attachCafcassNoticeOfPlacementResponse(file, description = '') {
    I.attachFile(this.fields.notice.cafcass.response.file, file);
    I.fillField(this.fields.notice.cafcass.response.description, description);
  },

  selectFirstParentNoticeOfPlacementRequired() {
    I.click(this.fields.notice.firstParent.required);
  },

  selectFirstParentNoticeOfPlacementNotRequired() {
    I.click(this.fields.notice.firstParent.notRequired);
  },

  selectFirstParent(parent) {
    I.selectOption(this.fields.notice.firstParent.parent, parent);
  },

  attachFirstParentNoticeOfPlacement(file, description = '') {
    I.attachFile(this.fields.notice.firstParent.file, file);
    I.fillField(this.fields.notice.firstParent.description, description);
  },

  selectFirstParentNoticeOfPlacementResponseReceived() {
    I.click(this.fields.notice.firstParent.response.received);
  },

  selectFirstParentNoticeOfPlacementResponseNotReceived() {
    I.click(this.fields.notice.firstParent.response.notReceived);
  },

  attachFirstParentNoticeOfPlacementResponse(file, description = '') {
    I.attachFile(this.fields.notice.firstParent.response.file, file);
    I.fillField(this.fields.notice.firstParent.response.description, description);
  },

  selectSecondParentNoticeOfPlacementRequired() {
    I.click(this.fields.notice.secondParent.required);
  },

  selectSecondParentNoticeOfPlacementNotRequired() {
    I.click(this.fields.notice.secondParent.notRequired);
  },

  selectSecondParent(parent) {
    I.selectOption(this.fields.notice.secondParent.parent, parent);
  },

  attachSecondParentNoticeOfPlacement(file, description) {
    I.attachFile(this.fields.notice.secondParent.file, file);
    if (description) {
      I.fillField(this.fields.notice.secondParent.description, description);
    }
  },

  selectSecondParentNoticeOfPlacementResponseReceived() {
    I.click(this.fields.notice.secondParent.response.received);
  },

  selectSecondParentNoticeOfPlacementResponseNotReceived() {
    I.click(this.fields.notice.secondParent.response.received);
  },

  attachSecondParentNoticeOfPlacementResponse(file, description) {
    I.attachFile(this.fields.notice.secondParent.response.file, file);
    if (description) {
      I.fillField(this.fields.notice.secondParent.response.description, description);
    }
  },

  selectNotifyAllRespondents() {
    I.click(`${this.fields.allRespondents.group}_${this.fields.allRespondents.options.all}`);
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
