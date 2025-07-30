output "eks_cluster_role_arn" {
  value = aws_iam_role.eks_cluster.arn
}

output "eks_node_role_arn" {
  value = aws_iam_role.eks_node.arn
}

output "bastion_role_arn" {
  description = "ARN of the Bastion IAM Role"
  value       = aws_iam_role.bastion.arn
}

output "bastion_instance_profile_name" {
  value = aws_iam_instance_profile.bastion.name
}

#alb
#output "alb_irsa_role_arn" {
#  description = "ARN of IAM Role for ALB Controller (IRSA)"
#  value       = aws_iam_role.alb_irsa_role.arn
#}
