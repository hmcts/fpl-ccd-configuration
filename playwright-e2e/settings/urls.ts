interface UrlConfig {
  [key: string]: URL | string;
}

const urlConfig: UrlConfig = {
  aatIdamUrl: process.env.AAT_IDAM_URL || "",
  aatServiceUrl: process.env.AAT_SERVICE_URL || "",
  frontEndBaseURL: process.env.FE_BASE_URL || "",
  // You can add other URLs as needed
};

export default urlConfig as {
  aatIdamUrl: string;
  aatServiceUrl: string;
  frontEndBaseURL: string;
};
