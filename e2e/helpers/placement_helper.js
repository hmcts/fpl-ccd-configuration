const config = require('../config.js');

const assertCafcassCannotSeePlacementOrder = async (I, caseViewPage, caseId) => {
  I.signOut();
  await I.signIn(config.cafcassEmail, config.cafcassPassword);
  await I.navigateToCaseDetails(caseId);
  caseViewPage.selectTab(caseViewPage.tabs.placement);
  I.dontSee('Placement order');
};

module.exports = {
  assertCafcassCannotSeePlacementOrder,
};
