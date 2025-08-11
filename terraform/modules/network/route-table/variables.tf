variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "igw_id" {
  description = "Internet Gateway ID"
  type        = string
}

variable "public_subnet_ids" {
  description = "Public Subnet ID 리스트"
  type        = list(string)
}

variable "name_prefix" {
  description = "리소스 이름 접두어"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod 등)"
  type        = string
}
