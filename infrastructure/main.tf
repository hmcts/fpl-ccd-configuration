provider "azurerm" {}

locals {
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"

  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "aat" : "saat" : var.env}"
  local_ase = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "core-compute-aat" : "core-compute-saat" : local.ase_name}"

  vault_name = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview") ? "${var.raw_product}-aat" : "${var.raw_product}-saat" : "${var.raw_product}-${var.env}"}"

//  tags = "${merge(var.common_tags,
//    map("Team Contact", "${var.team_contact}")
//  )}"

  # URLs
  IDAM_S2S_AUTH_URL = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"
  DOCUMENT_MANAGEMENT_URL = "http://dm-store-${local.local_env}.service.${local.local_ase}.internal"
  CORE_CASE_DATA_API_URL = "http://ccd-data-store-api-${local.local_env}.service.${local.local_ase}.internal"
}

//resource "azurerm_resource_group" "rg" {
//  name     = "${var.product}-${var.env}"
//  location = "${var.location}"
//
//  tags = "${local.tags}"
//}

data "azurerm_key_vault" "key_vault" {
  name = "${local.vault_name}"
  resource_group_name = "${local.vault_name}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "fpl-case-service-s2s-secret"
  vault_uri = "${data.azurerm_key_vault.key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "local_authority_name_mapping" {
  name = "local-authority-name-mapping"
  vault_uri = "${data.azurerm_key_vault.key_vault.vault_uri}"
}

module "case-service" {
  source              = "git@github.com:hmcts/moj-module-webapp?ref=master"
  product             = "${var.product}-${var.component}"
  location            = "${var.location}"
  env                 = "${var.env}"
  ilbIp               = "${var.ilbIp}"
  subscription        = "${var.subscription}"
  capacity            = "${var.capacity}"
  common_tags         = "${var.common_tags}"

  app_settings = {
    IDAM_API_URL = "${var.idam_api_url}"
    IDAM_S2S_AUTH_URL = "${local.IDAM_S2S_AUTH_URL}"
    IDAM_S2S_AUTH_TOTP_SECRET = "${data.azurerm_key_vault_secret.s2s_secret.value}"
    DOCUMENT_MANAGEMENT_URL = "${local.DOCUMENT_MANAGEMENT_URL}"
    CORE_CASE_DATA_API_URL = "${local.CORE_CASE_DATA_API_URL}"
    FPL_LOCAL_AUTHORITY_NAME_MAPPING = "${data.azurerm_key_vault_secret.local_authority_name_mapping.value}"

    LOGBACK_REQUIRE_ALERT_LEVEL = false
    LOGBACK_REQUIRE_ERROR_CODE  = false
  }
}

module "fpl-vault" {
  source = "git@github.com:hmcts/cnp-module-key-vault.git?ref=master"
  name = "fpl-${var.env}"
  product = "${var.product}"
  env = "${var.env}"
  tenant_id = "${var.tenant_id}"
  object_id = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.case-service.resource_group_name}"
  product_group_object_id = "68839600-92da-4862-bb24-1259814d1384"
}
