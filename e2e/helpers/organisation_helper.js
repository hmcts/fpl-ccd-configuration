function searchAndSelectGivenRegisteredOrganisation(I, legalCounsellor) {
  I.waitForEnabled('#search-org-text');
  I.fillField('#search-org-text', legalCounsellor.organisation);
  I.click(locate('a').withText('Select').inside(locate('#organisation-table').withDescendant(locate('h3').withText(legalCounsellor.organisation))));
}

module.exports = {
  searchAndSelectGivenRegisteredOrganisation,
};