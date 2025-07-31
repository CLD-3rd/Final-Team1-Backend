variable "team_name" {
  description = "Prefix for resource naming"
  type        = string
}

variable "ami_id" {
  description = "AMI ID to use for the bastion EC2 instance"
  type        = string
}

variable "instance_type" {
  description = "Instance type for the bastion EC2"
  type        = string
  default     = "t3.micro"
}

variable "public_subnet_id" {
  description = "ID of the public subnet to launch the bastion"
  type        = string
}

variable "security_group_id" {
  description = "Security group ID for bastion EC2"
  type        = string
}

variable "iam_instance_profile_name" {
  description = "IAM instance profile to attach to bastion EC2"
  type        = string
}

variable "cluster_name" {
  description = "EKS Cluster name for hostname or tagging"
  type        = string
}

variable "key_name" {
  description = "Name of the EC2 key pair to assign to the Bastion instance"
  type        = string
}

variable "eks_node_role_arn" {
  type = string
}

variable "bastion_role_arn" {
  type = string
}
<<<<<<< HEAD

=======
>>>>>>> e3c5ed7128e1648b009eeff98a66980fa4eeed9f
