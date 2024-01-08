const defaultPassword = "Password12";
const newDefaultPassword = "Password1234";

export const oldSwanseaLocalAuthorityUserOne = {
  email: "kurt@swansea.gov.uk",
  password: process.env.LA_USER_PASSWORD || defaultPassword,
  forename: "kurt@swansea.gov.uk",
  surname: "(local-authority)",
};

export const newSwanseaLocalAuthorityUserOne = {
  email: "local-authority-swansea-0001@maildrop.cc",
  password: process.env.LA_USER_PASSWORD || newDefaultPassword,
  // forename: "kurt@swansea.gov.uk",
  // surname: "(local-authority)",
};
