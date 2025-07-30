output "vpc_id" {
  value = module.vpc.vpc_id
}

# EKS 클러스터 이름 (kubectl 연결, Helm, 모니터링 등에서 사용)
output "cluster_name" {
  value = module.eks.cluster_name
}

# EKS 클러스터 엔드포인트 (kubectl 접속 시 필요)
output "cluster_endpoint" {
  value = module.eks.cluster_endpoint   
}

# EKS 클러스터 인증서 (kubeconfig 설정 시 필요)
output "cluster_certificate_authority" {
  value = module.eks.cluster_certificate_authority   
}

# EKS 노드 그룹 이름 (노드 그룹 확인, 확장 등 관리에 사용)
output "node_group_name" {
  value = module.eks.node_group_name
}

# Bastion EC2 인스턴스 ID (AWS 콘솔이나 CLI에서 Bastion 추적용)
output "bastion_instance_id" {
  value = module.bastion.bastion_instance_id   
}

# Bastion EC2의 공인 IP (SSH 또는 SSM으로 접속 시 필요)
output "bastion_public_ip" {
  value = module.bastion.bastion_public_ip   
}

# Bastion EC2의 사설 IP (VPC 내부 접근 시 사용)
output "bastion_private_ip" {
  value = module.bastion.bastion_private_ip   
}

# EKS 클러스터용 IAM Role ARN (클러스터 생성 시 사용)
output "eks_cluster_role_arn" {
  value = module.iam.eks_cluster_role_arn
}

# EKS 노드용 IAM Role ARN (노드 그룹 생성 시 사용)
output "eks_node_role_arn" {
  value = module.iam.eks_node_role_arn
}

# Bastion EC2 IAM Role ARN (SSM, EKS, EC2, RDS 등 접근 권한)
output "bastion_role_arn" {
  value = module.iam.bastion_role_arn
}

# Bastion EC2 IAM 인스턴스 프로파일 이름 (EC2에 Role 연결 시 사용)
output "bastion_instance_profile_name" {
  value = module.iam.bastion_instance_profile_name
}


# 보안그룹 
output "bastion_sg_id" {
  value       = module.sg.bastion_sg_id
  description = "Bastion EC2의 보안 그룹 ID"
}

output "eks_node_sg_id" {
  value       = module.sg.eks_node_sg_id
  description = "EKS 워커 노드 보안 그룹 ID"
}

output "eks_control_plane_sg_id" {
  value       = module.sg.eks_control_plane_sg_id
  description = "Bastion에서 EKS Control Plane 접근 허용 SG ID"
}
