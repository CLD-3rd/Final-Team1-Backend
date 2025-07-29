resource "kubernetes_config_map" "aws_auth" {
  metadata {
    name      = "aws-auth"
    namespace = "kube-system"
  }

  data = {
    mapRoles = yamlencode([
      {
        rolearn  = module.iam.eks_node_role_arn
        username = "system:node:{{EC2PrivateDNSName}}"
        groups   = ["system:bootstrappers", "system:nodes"]
      }
    ])
    mapUsers = yamlencode([
      {
        userarn  = module.iam.bastion_role_arn
        username = "bastion-admin"
        groups   = ["system:masters"]
      }
    ])
  }

  # ← EKS 모듈이 완전히 준비된 뒤에야 이 리소스를 생성
    depends_on = [
        module.eks,
        module.iam                # iam 모듈이 완전히 적용된 뒤

  ]
}
