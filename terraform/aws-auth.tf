# // aws‑auth.tf (루트)
# resource "kubernetes_manifest" "aws_auth" {
#   manifest = {
#     apiVersion = "v1"
#     kind       = "ConfigMap"
#     metadata = {
#       name      = "aws-auth"
#       namespace = "kube-system"
#     }

#   data = {
#     mapRoles = yamlencode([
#       // 1) 워커 노드 역할
#       {
#         rolearn  = module.iam.eks_node_role_arn
#         username = "system:node:{{EC2PrivateDNSName}}"
#         groups   = ["system:bootstrappers", "system:nodes"]
#       },
#       // 2) Bastion EC2 역할
#       {
#         rolearn  = module.iam.bastion_role_arn
#         username = "bastion-admin"
#         groups   = ["system:masters"]
#       },
#     ])
#     // mapUsers 블록은 제거해주세요
#   }

#   depends_on = [
#     module.eks,
#     module.iam,
#     null_resource.wait_for_kubeconfig
#   ]
# }
# }