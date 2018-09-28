Feature('E2E Local Authority Happy Path');

Scenario('Login as Local Authority and create case', (I, config) => {
    I.logInAndCreateCase(config.localAuthorityEmail, config.localAuthorityPassword, config.eventSummary, config.eventDescription);
    I.see('has been created', '.alert-message');
});
