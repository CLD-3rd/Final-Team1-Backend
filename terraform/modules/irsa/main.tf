
# alb controller irsa role
data "aws_iam_openid_connect_provider" "this" {
  url = var.oidc_url
}

resource "aws_iam_role" "alb_irsa" {
  name = "${var.team_name}-alb-irsa-role"

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
          "${replace(var.oidc_url, "https://", "")}:sub" = "system:serviceaccount:${var.namespace}:${var.service_account_name}"
        }
      }
    }]
  })
}

resource "aws_iam_policy" "alb_controller_policy" {
  name        = "${var.team_name}-alb-controller-policy"
  description = "Policy for AWS ALB Ingress Controller"
  policy      = file("${path.module}/alb-controller-policy.json")
}

resource "aws_iam_role_policy_attachment" "attach" {
  role       = aws_iam_role.alb_irsa.name
  policy_arn = aws_iam_policy.alb_controller_policy.arn
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
          "${replace(var.oidc_url, "https://", "")}:sub" = "system:serviceaccount:${var.namespace}:${var.service_account_name}"
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
