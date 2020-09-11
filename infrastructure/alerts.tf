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
  alert_name                 = "${var.product}-performance"
  alert_desc                 = "Requests that took longer than 1 seconds to complete"
  app_insights_query         = "requests | where url !contains '/health' and success == 'True' and duration > 6000 | project timestamp, operation_Id, name, duration | sort by duration nulls last"
  custom_email_subject       = "Alert: Performance errors"
  frequency_in_minutes       = 5
  time_window_in_minutes     = 5
  severity_level             = "2"
  action_group_name          = "${var.product}-support"
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = 2
  resourcegroup_name         = "${local.alert_resource_group_name}"
  enabled                    = "${var.enable_alerts}"
}

module "fpl-exceptions-alert" {
  source                     = "git@github.com:hmcts/cnp-module-metric-alert"
  location                   = "${var.appinsights_location}"
  app_insights_name          = "${var.product}-${var.component}-appinsights-${var.env}"
  alert_name                 = "${var.product}-exceptions"
  alert_desc                 = "All exceptions within FPL"
  app_insights_query         = "exceptions | where operation_Name !contains 'health' and outerMessage !contains '[403 Forbidden] during [GET] to [http://rd-professional-api-prod.service.core-compute-prod.internal/refdata/external/v1/organisations]'  | project timestamp, operation_Id, outerMessage, operation_Name"
  custom_email_subject       = "Alert: FPL all exceptions"
  frequency_in_minutes       = 5
  time_window_in_minutes     = 5
  severity_level             = "3"
  action_group_name          = "${var.product}-support"
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = 0
  resourcegroup_name         = "${local.alert_resource_group_name}"
  enabled                    = "${var.enable_alerts}"
}

module "fpl-health-failure-alert" {
  source                     = "git@github.com:hmcts/cnp-module-metric-alert"
  location                   = "${var.appinsights_location}"
  app_insights_name          = "${var.product}-${var.component}-appinsights-${var.env}"
  alert_name                 = "${var.product}-health-failure"
  alert_desc                 = "Failed health requests"
  app_insights_query         = "requests | where url contains 'health' | where resultCode != 200 | project timestamp, operation_Id, resultCode"
  custom_email_subject       = "Alert: Health failure"
  frequency_in_minutes       = 5
  time_window_in_minutes     = 5
  severity_level             = "3"
  action_group_name          = "${var.product}-support"
  trigger_threshold_operator = "GreaterThan"
  trigger_threshold          = 3
  resourcegroup_name         = "${local.alert_resource_group_name}"
  enabled                    = "${var.enable_alerts}"
}
