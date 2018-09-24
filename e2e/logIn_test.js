const testConfig = require("./config.js");

Feature('LogIn');

Scenario('Login as Local Authority', (I, loginPage) => {
    I.amOnPage(testConfig.testStartingUrl);
    I.see('Sign in');
    loginPage.sendForm(testConfig.testPersonEmail, testConfig.testPersonPassword);
    I.wait(10);
    I.see('Case List');
    I.click('Create new case');
    I.selectOption('jurisdiction', 'Public Law DRAFT');
    I.selectOption('case-type', 'Shared_Storage_DRAFT_v0.3');
    I.selectOption('event', 'Initiate Case');
    I.click('Start');
    I.wait(10);
    I.see('Create a case');
});
