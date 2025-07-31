variable "team_name" {
  type        = string
  description = "Prefix for naming"
}

variable "subnet_ids" {
  type        = list(string)
  description = "Subnet IDs for EKS"
}

variable "cluster_iam_role_arn" {
  type        = string
  description = "IAM role ARN for EKS control plane"
}

variable "node_iam_role_arn" {
  type        = string
  description = "IAM role ARN for EKS node group"
}

variable "bastion_role_arn" {
  type        = string
  description = "IAM role ARN for bastion host"
  default     = ""
}