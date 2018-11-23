variable "subscription" {}

variable "product" {
  type    = "string"
}

variable "raw_product" {
  type    = "string"
  default = "fpl" // jenkins-library overrides product for PRs and adds e.g. pr-1-fpl
}

variable "component" {
  type = "string"
}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "ilbIp" {}

variable "capacity" {
  default = "1"
}

variable "common_tags" {
  type = "map"
}

variable "idam_api_url" {
  type = "string"
}
