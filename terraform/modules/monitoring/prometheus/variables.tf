variable "namespace" {
  description = "Namespace to deploy Prometheus"
  type        = string
}

variable "chart_version" {
  description = "Version of Prometheus Helm chart"
  type        = string
}

variable "namespace_dependency" {
  description = "Dependency to wait for namespace module"
  type        = any
  default     = null
}

#삭제 필요~
variable "enabled" {
  description = "Enable or disable this module"
  type        = bool
  default     = true
}
