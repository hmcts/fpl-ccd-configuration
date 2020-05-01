provider "azurerm" {
  version = "=1.44.0"
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"

  tags = "${var.common_tags}"
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-appinsights-${var.env}"
  location            = "${var.appinsights_location}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  application_type    = "Web"

  tags = "${var.common_tags}"
}

#Copying appinsights key to the valut
resource "azurerm_key_vault_secret" "AZURE_APPINSGHTS_KEY" {
  name         = "AppInsightsInstrumentationKey"
  value        = "${azurerm_application_insights.appinsights.instrumentation_key}"
  key_vault_id = "${module.key-vault.key_vault_id}"
}

module "key-vault" {
  source                  = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  name                    = "fpl-${var.env}"
  product                 = "${var.product}"
  env                     = "${var.env}"
  tenant_id               = "${var.tenant_id}"
  object_id               = "${var.jenkins_AAD_objectId}"
  resource_group_name     = "${azurerm_resource_group.rg.name}"
  product_group_object_id = "bb778c38-9e7a-4d03-8dad-4fe0b207e8a3"
  common_tags             = "${var.common_tags}"

  #aks migration
  managed_identity_object_id = "${var.managed_identity_object_id}"
}

module "fpl-scheduler-db" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = "${var.product}-${var.component}"
  location           = "${var.location_db}"
  env                = "${var.env}"
  database_name      = "fpl_scheduler"
  postgresql_user    = "fpl_scheduler"
  postgresql_version = "10"
  sku_name           = "GP_Gen5_2"
  sku_tier           = "GeneralPurpose"
  common_tags        = "${var.common_tags}"
  subscription       = "${var.subscription}"
}

data "azurerm_key_vault_secret" "fpla_support_email_secret" {
  name      = "fpla-support-email"
  vault_uri = "${module.key-vault.key_vault_uri}"
}

module "fpla-action-group" {
  source                 = "git@github.com:hmcts/cnp-module-action-group"
  location               = "global"
  env                    = "aat"
  resourcegroup_name     = "fpl-case-service-aat"
  action_group_name      = "fpla-support"
  short_name             = "fpla-support"
  email_receiver_name    = "FPLA Support Mailing List"
  email_receiver_address = "${data.azurerm_key_vault_secret.fpla_support_email_secret.value"
}

module "fpla-performance-alert" {
  source                     = "git@github.com:hmcts/cnp-module-metric-alert"
  location                   = "${var.appinsights_location}"

  app_insights_name          = "fpl-case-service-appinsights-aat"

  alert_name                 = "fpla-bad-requests"
  alert_desc                 = "Web pages took longer than 1 seconds to load"
  app_insights_query         = "requests | where url !contains '/health' | where success == 'True' | where duration > 1000"
  custom_email_subject       = "Alert: performance errors"
  frequency_in_minutes       = 5
  time_window_in_minutes     = 5
  severity_level             = "2"
  action_group_name          = "fpla-support"
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = 5
  resourcegroup_name         = "fpl-case-service-aat"
}

resource "azurerm_key_vault_secret" "scheduler-db-password" {
  name      = "scheduler-db-password"
  value     = "${module.fpl-scheduler-db.postgresql_password}"
  vault_uri = "${module.key-vault.key_vault_uri}"
}
