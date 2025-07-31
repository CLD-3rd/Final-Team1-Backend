variable "team_name" {
  description = "Name of team name"
  type        = string
  default     = "Team1-backend"
}

variable "ami_id" {
  description = "AMI ID for Bastion EC2"
  type        = string
}

variable "bastion_key_name" {
  description = "Name of the existing EC2 key pair for Bastion SSH access"
  type        = string
}



variable "db_password" {
  description = "Master password for RDS"
  type        = string
  sensitive   = true
  default     = "root1234"
}

