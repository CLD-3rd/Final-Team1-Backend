#!/bin/bash
hostnamectl --static set-hostname "${cluster_name}-bastion-EC2"

# 계정 설정
echo 'root:eks123' | chpasswd
sed -i "s/^#PermitRootLogin prohibit-password/PermitRootLogin yes/g" /etc/ssh/sshd_config
sed -i "s/^PasswordAuthentication no/PasswordAuthentication yes/g" /etc/ssh/sshd_config
rm -rf /root/.ssh/authorized_keys
systemctl restart ssh

# 편의 설정
echo 'alias vi=vim' >> /etc/profile
echo "sudo su -" >> /home/ubuntu/.bashrc
timedatectl set-timezone Asia/Seoul

# 필수 패키지
apt update -y
apt install -y tree jq git htop unzip vim docker.io mysql-client redis-tools

# aws cli
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install
echo 'export AWS_PAGER=""' >> /etc/profile

# kubectl 설치 (v1.29.7)
curl -LO "https://dl.k8s.io/release/v1.29.7/bin/linux/amd64/kubectl"
chmod +x kubectl
mv kubectl /usr/local/bin/kubectl

# 3. kubeconfig 설정
aws eks --region ap-northeast-2 update-kubeconfig --name "${cluster_name}"

# helm 설치 (v3.14.0 고정)
curl -LO https://get.helm.sh/helm-v3.14.0-linux-amd64.tar.gz
tar -zxvf helm-v3.14.0-linux-amd64.tar.gz
mv linux-amd64/helm /usr/local/bin/helm
chmod +x /usr/local/bin/helm
rm -rf linux-amd64 helm-v3.14.0-linux-amd64.tar.gz

# eksctl
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
mv /tmp/eksctl /usr/local/bin

# docker 권한
usermod -aG docker ubuntu
newgrp docker

# SSH 키 생성
ssh-keygen -t rsa -N "" -f /root/.ssh/id_rsa

echo "cloud-init complete."
