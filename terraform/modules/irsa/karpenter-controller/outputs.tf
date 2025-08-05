output "karpenter_controller_role_arn" {
  value       = aws_iam_role.karpenter_controller.arn
  description = "IAM Role ARN for Karpenter Controller"
}
