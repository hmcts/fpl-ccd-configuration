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

class TabAssertions extends Helper {

  seeInTab(pathToField, fieldValue) {
    const helper = this.helpers['Puppeteer'] || this.helpers['WebDriver'];

    const fieldSelector = tabFieldSelector(pathToField);

    if (Array.isArray(fieldValue)) {
      fieldValue.forEach((value, index) => {
        helper.seeElement(locate(`${fieldSelector}//tr[${index + 1}]`).withText(value));
      });
    } else {
      helper.seeElement(locate(fieldSelector).withText(fieldValue));
    }
  }

  seeOrganisationInTab(pathToField, fieldValue) {
    const helper = this.helpers['Puppeteer'] || this.helpers['WebDriver'];

    const fieldSelector = organisationTabFieldSelector(pathToField);

    if (Array.isArray(fieldValue)) {
      fieldValue.forEach((value) => {
        helper.seeElement(locate(`${fieldSelector}//tr[1]`).withText(value));
      });
    } else {
      helper.seeElement(locate(fieldSelector).withText(fieldValue));
    }
  }

  seeTextInTab (pathToField) {
    const helper = this.helpers['Puppeteer'] || this.helpers['WebDriver'];

    const fieldSelector = tabFieldSelector(pathToField);
    helper.seeElement(locate(fieldSelector));
  }

  dontSeeInTab(pathToField) {
    const helper = this.helpers['Puppeteer'] || this.helpers['WebDriver'];
    helper.dontSeeElement(locate(tabFieldSelector(pathToField)));
  }
}

module.exports = TabAssertions;
