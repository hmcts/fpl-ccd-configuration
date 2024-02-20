interface UrlConfig {
  [key: string]: URL | string;
}

const urlConfig: UrlConfig = {
  aatIdamUrl: process.env.AAT_IDAM_URL || "https://idam-api.aat.platform.hmcts.net/",
  aatServiceUrl: process.env.AAT_SERVICE_URL || "http://fpl-case-service-aat.service.core-compute-aat.internal/",
  frontEndBaseURL: process.env.FE_BASE_URL || "https://manage-case.aat.platform.hmcts.net/",
  // You can add other URLs as needed
};

export default urlConfig as {
  aatIdamUrl: string;
  aatServiceUrl: string;
  frontEndBaseURL: string;
};
