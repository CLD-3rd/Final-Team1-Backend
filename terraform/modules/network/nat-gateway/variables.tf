variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "igw_id" {
  description = "Internet Gateway ID"
  type        = string
}

variable "public_subnet_id" {
  description = "NAT Gateway가 위치할 Public Subnet ID (1개)"
  type        = string
}

variable "private_subnet_ids" {
  description = "Private Subnet ID 리스트"
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
