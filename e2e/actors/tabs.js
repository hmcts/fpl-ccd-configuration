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

function organisationTabFieldSelector(pathToField) {
  let path = [].concat(pathToField);
  let fieldName = path.splice(-1, 1)[0];
  let selector = TAB_CLASS_SELECTOR;

  path.forEach(step => {
    selector = `${selector}//*[@class="complex-panel" and .//*[@class="complex-panel-title" and .//*[text()="${step}"]]]`;
  });

  return `${selector}//*[contains(@class,"complex-panel-compound-field") and ..//*[text()="${fieldName}:"]]`;
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
    this.seeElement(locate('details')
      .withChild(locate('summary')
        .withText(title))
      .withChild(locate('div'))
      .withText('Uploaded by')
      .withText(uploadedBy)
      .withText('Date and time uploaded')
      .withText(dateTimeUploaded));
  },

  expandDocumentSection(documentSection, documentTitle) {
    this.click(locate('summary').withText(documentSection));
    this.expandDocument(documentTitle);
  },

  expandDocument(documentTitle) {
    this.click(locate('summary').withText(documentTitle));
  },
};
