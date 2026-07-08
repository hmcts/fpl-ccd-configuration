variable "product" {
  type = string
}

variable "component" {
  type = string
}

variable "location" {
  type    = string
  default = "UK South"
}

variable "env" {
  type = string
}

variable "common_tags" {
  type = map(any)
}

variable "tenant_id" {
  description = "(Required) The Azure Active Directory tenant ID that should be used for authenticating requests to the key vault. This is usually sourced from environment variables and not normally required to be specified."
}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "appinsights_location" {
  type        = string
  default     = "West Europe"
  description = "Location for Application Insights"
}

variable "docmosis_subscription_id" {
  default = "fa397675-6ddf-4cb0-b1fc-bc7df0e51bf4"
}

variable "docmosis_resource_group" {
  default = "docmosis-iaas-dev-rg"
}

variable "docmosis_vault" {
  default = "docmosisiaasdevkv"
}

variable "enable_alerts" {
  default = false
}

variable "fpl_scheduler_db_name_v15" {
  default = "fpl_scheduler"
}

variable "pgsql_admin_username" {
  default = "fpl_scheduler_user"
}

variable "aks_subscription_id" {} # provided by the Jenkins library
