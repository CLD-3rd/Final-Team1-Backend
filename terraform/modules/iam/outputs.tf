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


#karpenter
output "karpenter_node_role_arn" {
  value       = aws_iam_role.karpenter_node.arn
  description = "IAM Role ARN for Karpenter node"
}

output "karpenter_node_instance_profile_name" {
  value       = aws_iam_instance_profile.karpenter_node.name
  description = "Instance profile name for Karpenter node"
}
