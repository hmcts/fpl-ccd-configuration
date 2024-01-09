const newDefaultPassword = "Password1234";

export const newSwanseaLocalAuthorityUserOne = {
  email: "local-authority-swansea-0001@maildrop.cc",
  password: process.env.LA_USER_PASSWORD || newDefaultPassword,
};
