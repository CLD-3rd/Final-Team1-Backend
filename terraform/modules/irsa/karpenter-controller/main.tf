data "aws_caller_identity" "current" {}

resource "aws_iam_role" "karpenter_controller" {
  name = "${var.team_name}-karpenter-controller-role"

  assume_role_policy = data.aws_iam_policy_document.karpenter_assume_role.json
  
  tags = {
    Name      = "${var.team_name}-karpenter-controller-role"
    ManagedBy = "Terraform"
  }
  }

data "aws_iam_policy_document" "karpenter_assume_role" {
  statement {
    effect = "Allow"
    principals {
      type        = "Federated"
      identifiers = [var.oidc_provider_arn]
    }

    actions = ["sts:AssumeRoleWithWebIdentity"]

    condition {
      test     = "StringEquals"
      variable = "${var.oidc_provider_url}:sub"
      values   = ["system:serviceaccount:kube-system:karpenter"]
    }

    condition {
      test     = "StringEquals"
      variable = "${var.oidc_provider_url}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_policy" "karpenter_controller" {
  name        = "${var.team_name}-karpenter-controller-policy"
  description = "Karpenter controller policy"
  policy      = templatefile("${path.module}/karpenter-controller-policy.json.tpl", {
    CLUSTER_NAME        = var.cluster_name
    AWS_DEFAULT_REGION  = "ap-northeast-2"
    ACCOUNT_ID          = data.aws_caller_identity.current.account_id
    AWS_PARTITION       = "aws"
  })
}

resource "aws_iam_role_policy_attachment" "karpenter_attach" {
  role       = aws_iam_role.karpenter_controller.name
  policy_arn = aws_iam_policy.karpenter_controller.arn
}
