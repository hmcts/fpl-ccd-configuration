const assert = require('assert');
const TAB_CLASS_SELECTOR = '//mat-tab-body';

function tabFieldSelector(pathToField) {
  let path = [].concat(pathToField);
  let fieldName = path.splice(-1, 1)[0];
  let selector = TAB_CLASS_SELECTOR;

  // if it is a simple case field then it will not have a complex-panel-[title|simple-field] class
  if (path.length === 0) {
    return `${selector}//tr[.//th/div[text()="${fieldName}"]]`;

  }
  path.forEach(step => {
    selector = `${selector}//*[@class="complex-panel" and .//*[@class="complex-panel-title" and .//*[text()="${step}"]]]`;

  });
  return `${selector}//*[contains(@class,"complex-panel-simple-field") and .//th/span[text()="${fieldName}"]]`;
}

function tabLabelSelector(pathToField) {
  let path = [].concat(pathToField);
  let fieldName = path.splice(-1, 1)[0];
  let selector = TAB_CLASS_SELECTOR;

  path.forEach(step => {
    selector = `${selector}//*[@class="complex-panel" and .//*[@class="complex-panel-title" and .//*[text()="${step}"]]]`;
  });

  return `${selector}//*//p[text()="${fieldName}"]`;
}

function tabTagSelector(pathToField) {
  let path = [].concat(pathToField);
  let fieldName = path.splice(-1, 1)[0];
  let selector = TAB_CLASS_SELECTOR;

  path.forEach(step => {
    selector = `${selector}//*[@class="complex-panel" and .//*[@class="complex-panel-title" and .//*[text()="${step}"]]]`;
  });

  return `${selector}//*[contains(@class,"govuk-tag") and text()="${fieldName}"]`;
}

function organisationTabFieldSelector(pathToField) {
  let path = [].concat(pathToField);
  let fieldName = path.splice(-1, 1)[0];
  let selector = TAB_CLASS_SELECTOR;

  path.forEach(step => {
    selector = `${selector}//*[@class="complex-panel" and .//*[@class="complex-panel-title" and .//*[text()="${step}"]]]`;
  });

  return `${selector}//*[contains(@class,"complex-panel-compound-field") and ..//*[text()="${fieldName}:"]]`;
}

function getTabSelector(tab){
  return `//*[@role="tab"]/div[text() = "${tab}"]`;
}

module.exports = {
  seeInTab: function (pathToField, fieldValue) {
    const fieldSelector = tabFieldSelector(pathToField);

    if (Array.isArray(fieldValue)) {
      fieldValue.forEach((value, index) => {
        this.seeElement(locate(`${fieldSelector}//tr[${index + 1}]`).withText(value));
      });
    } else {
      this.seeElement(locate(fieldSelector).withText(fieldValue));
    }
  },

  assertValueInTab: function (pathToField, fieldValue) {
    const fieldSelector = tabFieldSelector(pathToField);

    if (Array.isArray(fieldValue)) {
      fieldValue.forEach((value, index) => {
        let actualValue = this.grabValueFrom(`${fieldSelector}//tr[${index + 1}]`);
        assert.strictEqual(actualValue, value);
      });
    } else {
      let actualValue = this.grabValueFrom(fieldSelector);
      assert.strictEqual(actualValue, fieldValue);
    }
  },

  seeTagInTab: function (pathToTag) {
    const fieldSelector = tabTagSelector(pathToTag);
    this.seeElement(locate(fieldSelector));
  },

  dontSeeTagInTab: function (pathToTag) {
    const fieldSelector = tabTagSelector(pathToTag);
    this.dontSeeElement(locate(fieldSelector));
  },

  seeLabelInTab: function (pathToTag) {
    const fieldSelector = tabLabelSelector(pathToTag);
    this.seeElement(locate(fieldSelector));
  },

  dontSeeLabelInTab: function (pathToTag) {
    const fieldSelector = tabLabelSelector(pathToTag);
    this.dontSeeElement(locate(fieldSelector));
  },

  seeOrganisationInTab(pathToField, fieldValue) {
    const fieldSelector = organisationTabFieldSelector(pathToField);

    if (Array.isArray(fieldValue)) {
      fieldValue.forEach((value) => {
        this.seeElement(locate(`${fieldSelector}//tr[1]`).withText(value));
      });
    } else {
      this.seeElement(locate(fieldSelector).withText(fieldValue));
    }
  },

  seeTextInTab(pathToField) {
    const fieldSelector = tabFieldSelector(pathToField);
    this.seeElement(locate(fieldSelector));
  },

  dontSeeInTab(pathToField) {
    this.dontSeeElement(locate(tabFieldSelector(pathToField)));
  },

  seeInExpandedDocument(title, uploadedBy, dateTimeUploaded) {
    if(uploadedBy == null) {
      this.seeElement(locate('details').withAttr({open})
        .withChild(locate('summary')
          .withText(title))
        .withChild(locate('div'))
        .withText('Date and time uploaded')
        .withText(dateTimeUploaded)
        .withText('Document')
        .withText('mockFile.txt'));
    } else {
      this.seeElement(locate('details').withAttr({open})
        .withChild(locate('summary')
          .withText(title))
        .withChild(locate('div'))
        .withText('Uploaded by')
        .withText(uploadedBy)
        .withText('Date and time uploaded')
        .withText(dateTimeUploaded)
        .withText('Document')
        .withText('mockFile.txt'));
    }
  },

  seeInExpandedConfidentialDocument(title, uploadedBy, dateTimeUploaded) {
    this.seeElement(locate('details').withAttr({open})
      .withChild(locate('summary')
        .withText(title))
      .withChild(locate('div'))
      .withText('Uploaded by')
      .withText(uploadedBy)
      .withText('Date and time uploaded')
      .withText(dateTimeUploaded)
      .withDescendant(locate('img').withAttr({ title : 'Confidential'}))
      .withText('Document')
      .withText('mockFile.txt'));
  },

  seeInExpandedDocumentSentForTranslation(title, uploadedBy, dateTimeUploaded) {
    this.waitForElement(locate('details').withAttr({open})
      .withChild(locate('summary')
        .withText(title))
      .withChild(locate('div'))
      .withText('Uploaded by')
      .withText(uploadedBy)
      .withText('Date and time uploaded')
      .withText(dateTimeUploaded)
      .withText('Document')
      .withText('mockFile.pdf')
      .withText('Sent for translation'));
  },

  seeInExpandedDocumentTranslated(title, uploadedBy, dateTimeUploaded) {
    this.waitForElement(locate('details').withAttr({open})
      .withChild(locate('summary')
        .withText(title))
      .withChild(locate('div'))
      .withText('Uploaded by')
      .withText(uploadedBy)
      .withText('Date and time uploaded')
      .withText(dateTimeUploaded)
      .withText('Document')
      .withText('mockFile.pdf')
      .withText('Translated document')
      .withText('mockFile-Welsh.pdf'));
  },

  dontSeeDocumentSection(documentSection, documentTitle) {
    this.dontSeeElement(locate('summary').withAttr({class: 'govuk-details__summary'}).withText(documentTitle)
      .inside(locate('details').withChild(locate('summary').withText(documentSection))));
  },

  dontSeeConfidentialInExpandedDocument(documentSection, documentTitle) {
    this.dontSeeElement(locate('summary').withAttr({class: 'govuk-details__summary'})
      .withText(documentTitle).withChild(locate('div'))
      .withChild(locate('img'))
      .inside(locate('details').withChild(locate('summary').withText(documentSection))));
  },

  expandDocumentSection(documentSection, documentTitle) {
    this.click(locate('summary').withText(documentSection));
    this.expandDocument(documentSection, documentTitle);
  },

  expandDocument(documentSection, documentTitle) {
    this.click(locate('summary').withAttr({class: 'govuk-details__summary'}).withText(documentTitle)
      .inside(locate('details').withChild(locate('summary').withText(documentSection))));
  },

  selectTabFunction(tab){
    const tabSelector = getTabSelector(tab);
    this.waitForText('CCD ID');
    this.focus(tabSelector);
    this.click(tabSelector);
    this.click(tabSelector);

  },

  async selectTab(tab){
    const tabSelector = getTabSelector(tab);

    const numberOfElements = await this.grabNumberOfVisibleElements('//*[@role="tab"]');

    for(let i=0; i<numberOfElements; i++){
      if((await this.canClick(tabSelector))){
        break;
      }
      console.log(`Scrolling to tab '${tab}'`);
      this.click('.mat-tab-header-pagination-after');
    }

    return this.click(tabSelector);
  },

  async dontSeeTab(tab){
    this.dontSeeElement(getTabSelector(tab));
  },
};
