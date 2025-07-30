variable "team_name" {
  type        = string
  description = "Prefix for naming"
}

variable "cluster_name" {
  description = "EKS cluster name"
  type        = string
}

variable "oidc_url" {
  type = string
}