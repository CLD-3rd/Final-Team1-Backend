variable "team_name" {
  description = "Team prefix for naming"
  type        = string
}

variable "oidc_url" {
  description = "OIDC provider URL from EKS cluster"
  type        = string
}

variable "cluster_name" {
  description = "Name of the EKS cluster"
  type        = string
}

variable "namespace" {
  description = "Kubernetes namespace where ALB controller runs"
  type        = string
  default     = "kube-system"
}

variable "service_account_name" {
  description = "ServiceAccount name for ALB controller"
  type        = string
  default     = "aws-load-balancer-controller"
}

variable "create_alb_irsa" {
  type    = bool
  default = false
}

variable "create_ebs_csi_irsa" {
  type    = bool
  default = false
}
