const defaultPassword = "Password12";

export const swanseaLocalAuthorityUserOne = {
  email: "kurt@swansea.gov.uk",
  password: process.env.LA_USER_PASSWORD || defaultPassword,
  forename: "kurt@swansea.gov.uk",
  surname: "(local-authority)",
};
