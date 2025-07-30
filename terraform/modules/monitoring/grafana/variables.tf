variable "namespace" {
  description = "Namespace to deploy Grafana"
  type        = string
}

variable "chart_version" {
  description = "Version of Grafana Helm chart"
  type        = string
}

variable "namespace_dependency" {
  description = "Optional dependency on namespace module"
  type        = any
  default     = null
}
