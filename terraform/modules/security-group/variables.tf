# 사용할 VPC ID
variable "vpc_id" {
  description = "ID of the VPC"
  type        = string
}

# 팀 이름 prefix
variable "team_name" {
  description = "Team prefix for naming"
  type        = string
}

variable "cluster_name" {
  type        = string
  description = "EKS 클러스터 이름"
}