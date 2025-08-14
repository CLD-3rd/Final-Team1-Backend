resource "aws_eks_cluster" "this" {
  name     = "${var.team_name}-eks-cluster"
  role_arn = var.cluster_iam_role_arn

  vpc_config {
    subnet_ids = var.subnet_ids
  }

  tags = {
    Name = "${var.team_name}-eks-cluster"
  }
}

resource "aws_eks_node_group" "ng" {
  cluster_name    = aws_eks_cluster.this.name
  node_group_name = "${var.team_name}-ng"
  node_role_arn   = var.node_iam_role_arn
  subnet_ids      = var.subnet_ids

  instance_types = ["t3.large", "m5.large"]  # ← 추가된 부분!

  scaling_config {
    desired_size = 3
    min_size     = 1
    max_size     = 5
  }

  tags = {
    Name = "${var.team_name}-ng"
  }
  launch_template {
    id      = aws_launch_template.ng_launch_template.id
    version = "$Latest"
  }
}

# EKS 클러스터 생성 후 OIDC 정보 추출
data "aws_eks_cluster" "cluster_data" {
  name       = aws_eks_cluster.this.name
  depends_on = [aws_eks_cluster.this]
}

data "tls_certificate" "eks" {
  url = data.aws_eks_cluster.cluster_data.identity[0].oidc[0].issuer
}

resource "aws_iam_openid_connect_provider" "this" {
  url = data.aws_eks_cluster.cluster_data.identity[0].oidc[0].issuer
  client_id_list = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.eks.certificates[0].sha1_fingerprint]

}

# 노드 그룹을 위한 시작 템플릿 생성
resource "aws_launch_template" "ng_launch_template" {
  name = "${var.team_name}-ng-template"

  user_data = base64encode(<<-EOF
    #!/bin/bash
    # Set timezone to Asia/Seoul
    rm -rf /etc/localtime
    ln -s /usr/share/zoneinfo/Asia/Seoul /etc/localtime
    # Restart chronyd service
    systemctl restart chronyd
    EOF
  )
}
