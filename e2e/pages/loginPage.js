const I = actor();

module.exports = {

    fields: {
        username: "#username",
        password: "#password",
    },
    submitButton: "Sign in",

    sendForm(username, password) {
        I.fillField(this.fields.username, username);
        I.fillField(this.fields.password, password);
        I.click("Sign in");
    }
};
