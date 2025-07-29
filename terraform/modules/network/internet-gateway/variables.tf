variable "vpc_id" {
  description = "연결할 VPC ID"
  type        = string
}

variable "name_prefix" {
  description = "리소스 이름 접두어"
  type        = string
}

variable "environment" {
  description = "환경 (dev, prod 등)"
  type        = string
}
