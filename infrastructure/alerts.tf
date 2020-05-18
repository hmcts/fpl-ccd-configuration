locals {
  alert_resource_group_name = "${var.product}-${var.component}-${var.env}"
}

module "fpl-action-group" {
  source                 = "git@github.com:hmcts/cnp-module-action-group"
  location               = "global"
  env                    = "${var.env}"
  resourcegroup_name     = "${local.alert_resource_group_name}"
  action_group_name      = "${var.product}-support"
  short_name             = "${var.product}-support"
  email_receiver_name    = "FPL Support Mailing List"
  email_receiver_address = "${data.azurerm_key_vault_secret.fpl_support_email_secret.value}"
}

module "fpl-performance-alert" {
  source                     = "git@github.com:hmcts/cnp-module-metric-alert"
  location                   = "${var.appinsights_location}"
  app_insights_name          = "${var.product}-${var.component}-appinsights-${var.env}"
  alert_name                 = "${var.product}-performance-alert"
  alert_desc                 = "Requests that took longer than 1 seconds to complete"
  app_insights_query         = "requests | where url !contains '/health' and success == 'True' and duration > 1000 | project timestamp, name, operation_Id, duration | sort by duration nulls last"
  custom_email_subject       = "Alert: performance errors"
  frequency_in_minutes       = 5
  time_window_in_minutes     = 5
  severity_level             = "2"
  action_group_name          = "${var.product}-support"
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = 2
  resourcegroup_name         = "${local.alert_resource_group_name}"
  enabled                    = "${var.enable_alerts}"
}

module "fpl-grant-case-access-failure-alert" {
  source                     = "git@github.com:hmcts/cnp-module-metric-alert"
  location                   = "${var.appinsights_location}"
  app_insights_name          = "${var.product}-${var.component}-appinsights-${var.env}"
  alert_name                 = "${var.product}-grant-case-access-failure"
  alert_desc                 = "Grant case access failure"
  app_insights_query         = "exceptions | where type contains 'GrantCaseAccessException' | project timestamp , outerMessage"
  custom_email_subject       = "Alert: Grant case access failed"
  frequency_in_minutes       = 5
  time_window_in_minutes     = 5
  severity_level             = "1"
  action_group_name          = "${var.product}-support"
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = 0
  resourcegroup_name         = "${local.alert_resource_group_name}"
  enabled                    = "${var.enable_alerts}"
}
