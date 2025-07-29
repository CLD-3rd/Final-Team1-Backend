variable "team_name" {
  description = "Name of team name"
  type        = string
  default     = "Team1-backend"
}

variable "ami_id" {
  description = "AMI ID for Bastion EC2"
  type        = string
}