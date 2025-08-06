apiVersion: karpenter.k8s.aws/v1
kind: EC2NodeClass
metadata:
  name: default
  labels:
    app.kubernetes.io/managed-by: terraform
spec:
  instanceProfile: "${instance_profile}"
  amiFamily: Custom
  amiSelectorTerms:
    - id: "${ubuntu_ami_id}"
  subnetSelectorTerms:
    - tags:
        karpenter.sh/discovery: "${cluster_name}"
  securityGroupSelectorTerms:
    - tags:
        karpenter.sh/discovery: "${cluster_name}"
