resource "kubernetes_cluster_role" "karpenter_iac_admin" {
  metadata {
    name = "karpenter-iac-admin"
  }

  # nodepools (karpenter.sh)
  rule {
    api_groups = ["karpenter.sh"]
    resources  = ["nodepools"]
    verbs      = ["get", "list", "watch", "create", "update", "patch", "delete"]
  }

  # ec2nodeclasses (karpenter.k8s.aws)
  rule {
    api_groups = ["karpenter.k8s.aws"]
    resources  = ["ec2nodeclasses"]
    verbs      = ["get", "list", "watch", "create", "update", "patch", "delete"]
  }
}


resource "kubernetes_cluster_role_binding" "karpenter_iac_admin_binding" {
  metadata {
    name = "karpenter-iac-admin-binding"
  }
  subject {
    kind      = "Group"
    name      = "karpenter-iac-group"   # aws_auth : mapUsers와 반드시 동일!
    api_group = "rbac.authorization.k8s.io"
  }
  role_ref {
    kind      = "ClusterRole"
    name      = kubernetes_cluster_role.karpenter_iac_admin.metadata[0].name
    api_group = "rbac.authorization.k8s.io"
  }
}
