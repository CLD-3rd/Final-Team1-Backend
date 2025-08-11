# ebs-csi irsa role
data "aws_iam_openid_connect_provider" "this" {
  url = var.oidc_url
}

### ebs-csi-driver irsa role
resource "aws_iam_role" "ebs_csi_irsa" {
  name = "${var.team_name}-ebs-csi-irsa-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Effect = "Allow",
      Principal = {
        Federated = data.aws_iam_openid_connect_provider.this.arn
      },
      Action = "sts:AssumeRoleWithWebIdentity",
      Condition = {
        StringEquals = {
          "${replace(var.oidc_url, "https://", "")}:sub" = "system:serviceaccount:kube-system:${var.service_account_name}"
        }
      }
    }]
  })
}

resource "aws_iam_policy" "ebs_csi_policy" {
  name        = "${var.team_name}-ebs-csi-policy"
  description = "Policy for AWS EBS CSI Driver"
  policy      = file("${path.module}/ebs-csi-policy.json")
}

resource "aws_iam_role_policy_attachment" "ebs_csi_attach" {
  role       = aws_iam_role.ebs_csi_irsa.name
  policy_arn = aws_iam_policy.ebs_csi_policy.arn
}

