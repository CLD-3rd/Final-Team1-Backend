# Bastion 보안 그룹 ID
output "bastion_sg_id" {
  description = "Security Group ID for Bastion host"
  value       = aws_security_group.bastion.id
}

# EKS 워커 노드 보안 그룹 ID
output "eks_node_sg_id" {
  description = "Security Group ID for EKS worker nodes"
  value       = aws_security_group.eks_node.id
}

# Bastion에서 EKS API 서버로 접근 허용용 SG ID
output "eks_control_plane_sg_id" {
  description = "Security Group ID to allow Bastion to access EKS Control Plane"
  value       = aws_security_group.eks_control_plane.id
}
