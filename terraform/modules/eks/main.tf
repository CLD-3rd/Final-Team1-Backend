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

  scaling_config {
    desired_size = 3
    min_size     = 1
    max_size     = 5
  }

  tags = {
    Name = "${var.team_name}-ng"
  }
}