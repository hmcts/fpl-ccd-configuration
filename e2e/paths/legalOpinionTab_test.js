const config = require('../config.js');

Feature('Information displayed based on form data');

Before((I) => {
  I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
});

Scenario('test something', (I, legalOpinionStep, caseViewPage) => {
  legalOpinionStep.completeLegalOpinion();
  caseViewPage.selectTab(caseViewPage.tabs.legalOpinion);
  locate('div').withChild('dl').withChild('table') //try locate the info
  pause();
});
