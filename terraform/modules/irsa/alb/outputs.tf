output "alb_irsa_role_arn" {
  description = "IAM Role ARN for ALB Controller ServiceAccount"
  value       = aws_iam_role.alb_irsa.arn
}

# output "ebs_csi_irsa_role_arn" {
#   description = "IAM Role ARN for EBS CSI Driver ServiceAccount"
#   value       = aws_iam_role.ebs_csi_irsa.arn
# }

