#!/usr/bin/env bash
# =============================================================================
# ActionTracker - AWS Infrastructure Setup
# Provisions: Security Groups, RDS PostgreSQL, Secrets Manager, IAM Roles,
#             CloudWatch Log Group, ECS Cluster + Task Definition + Service,
#             and Application Load Balancer.
#
# Prerequisites:
#   - AWS CLI v2 installed and configured (aws configure)
#   - jq installed (brew install jq)
#   - Run from the repo root
#
# Usage:
#   chmod +x infrastructure/setup-aws.sh
#   ./infrastructure/setup-aws.sh
#
# After running, note the ALB_DNS output and:
#   1. Update backend/task-definition.json: replace REPLACE_WITH_RDS_ENDPOINT
#   2. Update frontend/src/environments/environment.prod.ts: replace apiUrl
#   3. Add ALB redirect URI to Google OAuth console
# =============================================================================

set -euo pipefail

AWS_REGION="us-east-1"
ACCOUNT_ID="717292229036"
APP="actiontracker"

echo "==> Fetching default VPC..."
VPC_ID=$(aws ec2 describe-vpcs \
  --filters "Name=isDefault,Values=true" \
  --query "Vpcs[0].VpcId" \
  --output text \
  --region $AWS_REGION)
echo "    VPC: $VPC_ID"

echo "==> Fetching public subnets in default VPC..."
SUBNET_IDS=$(aws ec2 describe-subnets \
  --filters "Name=vpc-id,Values=$VPC_ID" "Name=defaultForAz,Values=true" \
  --query "Subnets[*].SubnetId" \
  --output text \
  --region $AWS_REGION)
SUBNET_1=$(echo $SUBNET_IDS | awk '{print $1}')
SUBNET_2=$(echo $SUBNET_IDS | awk '{print $2}')
echo "    Subnets: $SUBNET_1, $SUBNET_2"

# =============================================================================
# SECURITY GROUPS
# =============================================================================

echo "==> Creating ALB security group..."
ALB_SG_ID=$(aws ec2 create-security-group \
  --group-name "${APP}-alb-sg" \
  --description "ActionTracker ALB - allow HTTP/HTTPS from internet" \
  --vpc-id $VPC_ID \
  --query "GroupId" --output text \
  --region $AWS_REGION)
aws ec2 authorize-security-group-ingress --group-id $ALB_SG_ID \
  --protocol tcp --port 80 --cidr 0.0.0.0/0 --region $AWS_REGION
aws ec2 authorize-security-group-ingress --group-id $ALB_SG_ID \
  --protocol tcp --port 443 --cidr 0.0.0.0/0 --region $AWS_REGION
echo "    ALB SG: $ALB_SG_ID"

echo "==> Creating App security group..."
APP_SG_ID=$(aws ec2 create-security-group \
  --group-name "${APP}-app-sg" \
  --description "ActionTracker Fargate tasks - allow 8080 from ALB only" \
  --vpc-id $VPC_ID \
  --query "GroupId" --output text \
  --region $AWS_REGION)
aws ec2 authorize-security-group-ingress --group-id $APP_SG_ID \
  --protocol tcp --port 8080 \
  --source-group $ALB_SG_ID \
  --region $AWS_REGION
echo "    App SG: $APP_SG_ID"

echo "==> Creating RDS security group..."
RDS_SG_ID=$(aws ec2 create-security-group \
  --group-name "${APP}-rds-sg" \
  --description "ActionTracker RDS - allow 5432 from app SG only" \
  --vpc-id $VPC_ID \
  --query "GroupId" --output text \
  --region $AWS_REGION)
aws ec2 authorize-security-group-ingress --group-id $RDS_SG_ID \
  --protocol tcp --port 5432 \
  --source-group $APP_SG_ID \
  --region $AWS_REGION
echo "    RDS SG: $RDS_SG_ID"

# =============================================================================
# RDS SUBNET GROUP
# =============================================================================

echo "==> Creating RDS DB subnet group..."
aws rds create-db-subnet-group \
  --db-subnet-group-name "${APP}-db-subnet-group" \
  --db-subnet-group-description "ActionTracker RDS subnet group" \
  --subnet-ids $SUBNET_1 $SUBNET_2 \
  --region $AWS_REGION > /dev/null
echo "    Subnet group created."

# =============================================================================
# RDS POSTGRESQL
# =============================================================================

echo "==> Creating RDS PostgreSQL instance (this takes ~5 minutes)..."
DB_PASSWORD=$(openssl rand -base64 24 | tr -d '/+=' | head -c 24)
echo ""
echo "    !! SAVE THIS DB PASSWORD - it will NOT be shown again !!"
echo "    DB_PASSWORD: $DB_PASSWORD"
echo ""

aws rds create-db-instance \
  --db-instance-identifier "${APP}-db" \
  --db-instance-class db.t4g.micro \
  --engine postgres \
  --engine-version "16" \
  --master-username postgres \
  --master-user-password "$DB_PASSWORD" \
  --db-name actiontracker \
  --allocated-storage 20 \
  --storage-type gp2 \
  --no-multi-az \
  --no-publicly-accessible \
  --vpc-security-group-ids $RDS_SG_ID \
  --db-subnet-group-name "${APP}-db-subnet-group" \
  --backup-retention-period 0 \
  --no-deletion-protection \
  --region $AWS_REGION > /dev/null

echo "    RDS instance creation initiated. Waiting for availability..."
aws rds wait db-instance-available \
  --db-instance-identifier "${APP}-db" \
  --region $AWS_REGION

RDS_ENDPOINT=$(aws rds describe-db-instances \
  --db-instance-identifier "${APP}-db" \
  --query "DBInstances[0].Endpoint.Address" \
  --output text \
  --region $AWS_REGION)
echo "    RDS Endpoint: $RDS_ENDPOINT"

# =============================================================================
# SECRETS MANAGER
# =============================================================================

echo "==> Storing secrets in AWS Secrets Manager..."
# IMPORTANT: Replace the placeholder values below with your real secrets before running,
# OR update the secret manually in the AWS console after creation.
aws secretsmanager create-secret \
  --name "actiontracker/prod" \
  --description "ActionTracker production secrets" \
  --secret-string "{
    \"DB_PASSWORD\": \"${DB_PASSWORD}\",
    \"GOOGLE_CLIENT_SECRET\": \"REPLACE_WITH_GOOGLE_CLIENT_SECRET\",
    \"CLAUDE_API_KEY\": \"REPLACE_WITH_CLAUDE_API_KEY\"
  }" \
  --region $AWS_REGION > /dev/null
echo "    Secret created: actiontracker/prod"
echo "    ACTION REQUIRED: Update GOOGLE_CLIENT_SECRET and CLAUDE_API_KEY in Secrets Manager console."

# =============================================================================
# IAM ROLES
# =============================================================================

echo "==> Creating ECS Task Execution Role..."
aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": { "Service": "ecs-tasks.amazonaws.com" },
      "Action": "sts:AssumeRole"
    }]
  }' > /dev/null 2>&1 || echo "    ecsTaskExecutionRole already exists, skipping."

aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy 2>/dev/null || true

SECRET_ARN=$(aws secretsmanager describe-secret \
  --secret-id "actiontracker/prod" \
  --query "ARN" --output text --region $AWS_REGION)

aws iam put-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-name ActionTrackerSecretsAccess \
  --policy-document "{
    \"Version\": \"2012-10-17\",
    \"Statement\": [{
      \"Effect\": \"Allow\",
      \"Action\": \"secretsmanager:GetSecretValue\",
      \"Resource\": \"${SECRET_ARN}\"
    }]
  }"
echo "    ecsTaskExecutionRole ready."

echo "==> Creating ECS Task Role..."
aws iam create-role \
  --role-name actiontracker-task-role \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [{
      "Effect": "Allow",
      "Principal": { "Service": "ecs-tasks.amazonaws.com" },
      "Action": "sts:AssumeRole"
    }]
  }' > /dev/null 2>&1 || echo "    actiontracker-task-role already exists, skipping."
echo "    actiontracker-task-role ready."

echo "==> Updating IAM permissions for github-actions-user..."
aws iam put-user-policy \
  --user-name github-actions-user \
  --policy-name ActionTrackerECSDeployPolicy \
  --policy-document "{
    \"Version\": \"2012-10-17\",
    \"Statement\": [
      {
        \"Effect\": \"Allow\",
        \"Action\": [
          \"ecs:UpdateService\",
          \"ecs:DescribeServices\",
          \"ecs:RegisterTaskDefinition\",
          \"ecs:DescribeTaskDefinition\",
          \"ecs:ListTaskDefinitions\"
        ],
        \"Resource\": \"*\"
      },
      {
        \"Effect\": \"Allow\",
        \"Action\": \"iam:PassRole\",
        \"Resource\": [
          \"arn:aws:iam::${ACCOUNT_ID}:role/ecsTaskExecutionRole\",
          \"arn:aws:iam::${ACCOUNT_ID}:role/actiontracker-task-role\"
        ]
      }
    ]
  }"
echo "    github-actions-user policy updated."

# =============================================================================
# CLOUDWATCH LOG GROUP
# =============================================================================

echo "==> Creating CloudWatch log group..."
aws logs create-log-group \
  --log-group-name "/ecs/actiontracker-backend" \
  --region $AWS_REGION 2>/dev/null || echo "    Log group already exists."
aws logs put-retention-policy \
  --log-group-name "/ecs/actiontracker-backend" \
  --retention-in-days 30 \
  --region $AWS_REGION
echo "    Log group: /ecs/actiontracker-backend (30-day retention)"

# =============================================================================
# ECS CLUSTER
# =============================================================================

echo "==> Creating ECS cluster..."
aws ecs create-cluster \
  --cluster-name $APP \
  --capacity-providers FARGATE \
  --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1 \
  --region $AWS_REGION > /dev/null
echo "    ECS cluster: $APP"

# =============================================================================
# TASK DEFINITION
# =============================================================================

echo "==> Registering ECS task definition..."
# Update the DB_URL in task-definition.json with the real RDS endpoint
TASK_DEF=$(cat backend/task-definition.json | \
  sed "s/REPLACE_WITH_RDS_ENDPOINT/${RDS_ENDPOINT}/g")
TASK_DEF_ARN=$(echo "$TASK_DEF" | aws ecs register-task-definition \
  --cli-input-json file:///dev/stdin \
  --query "taskDefinition.taskDefinitionArn" \
  --output text \
  --region $AWS_REGION)
echo "    Task definition: $TASK_DEF_ARN"

# Persist the updated task definition back to the file
echo "$TASK_DEF" | jq '.' > backend/task-definition.json
echo "    Updated backend/task-definition.json with RDS endpoint."

# =============================================================================
# APPLICATION LOAD BALANCER
# =============================================================================

echo "==> Creating Application Load Balancer..."
ALB_ARN=$(aws elbv2 create-load-balancer \
  --name "${APP}-alb" \
  --subnets $SUBNET_1 $SUBNET_2 \
  --security-groups $ALB_SG_ID \
  --scheme internet-facing \
  --type application \
  --ip-address-type ipv4 \
  --query "LoadBalancers[0].LoadBalancerArn" \
  --output text \
  --region $AWS_REGION)

ALB_DNS=$(aws elbv2 describe-load-balancers \
  --load-balancer-arns $ALB_ARN \
  --query "LoadBalancers[0].DNSName" \
  --output text \
  --region $AWS_REGION)
echo "    ALB ARN: $ALB_ARN"
echo "    ALB DNS: $ALB_DNS"

echo "==> Creating Target Group..."
TG_ARN=$(aws elbv2 create-target-group \
  --name "${APP}-tg" \
  --protocol HTTP \
  --port 8080 \
  --vpc-id $VPC_ID \
  --target-type ip \
  --health-check-protocol HTTP \
  --health-check-path "/actuator/health" \
  --health-check-interval-seconds 30 \
  --health-check-timeout-seconds 5 \
  --healthy-threshold-count 2 \
  --unhealthy-threshold-count 3 \
  --query "TargetGroups[0].TargetGroupArn" \
  --output text \
  --region $AWS_REGION)
echo "    Target Group: $TG_ARN"

echo "==> Creating ALB listener (HTTP:80 -> Target Group)..."
aws elbv2 create-listener \
  --load-balancer-arn $ALB_ARN \
  --protocol HTTP \
  --port 80 \
  --default-actions Type=forward,TargetGroupArn=$TG_ARN \
  --region $AWS_REGION > /dev/null
echo "    Listener created."

# =============================================================================
# ECS SERVICE
# =============================================================================

echo "==> Creating ECS service..."
aws ecs create-service \
  --cluster $APP \
  --service-name "${APP}-backend-svc" \
  --task-definition "actiontracker-backend" \
  --desired-count 1 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={
    subnets=[$SUBNET_1,$SUBNET_2],
    securityGroups=[$APP_SG_ID],
    assignPublicIp=ENABLED
  }" \
  --load-balancers "targetGroupArn=$TG_ARN,containerName=actiontracker-backend,containerPort=8080" \
  --deployment-configuration "minimumHealthyPercent=0,maximumPercent=200" \
  --region $AWS_REGION > /dev/null
echo "    ECS service created: ${APP}-backend-svc"

# =============================================================================
# SUMMARY
# =============================================================================

echo ""
echo "============================================================"
echo "  SETUP COMPLETE - ACTION ITEMS REQUIRED"
echo "============================================================"
echo ""
echo "  RDS Endpoint : $RDS_ENDPOINT"
echo "  ALB DNS      : $ALB_DNS"
echo ""
echo "  Next steps:"
echo ""
echo "  1. Update Google OAuth redirect URI in GCP console:"
echo "     http://$ALB_DNS/login/oauth2/code/google"
echo ""
echo "  2. Update Secrets Manager 'actiontracker/prod' with real values:"
echo "     GOOGLE_CLIENT_SECRET and CLAUDE_API_KEY"
echo "     (DB_PASSWORD was set automatically above)"
echo ""
echo "  3. Create actiontracker_app DB user in RDS:"
echo "     (Use a bastion EC2 or temporarily enable public access)"
echo "     psql -h $RDS_ENDPOINT -U postgres -d actiontracker"
echo "     Then run: infrastructure/create-db-user.sql"
echo ""
echo "  4. Migrate data from Supabase -> RDS:"
echo "     See infrastructure/migrate-data.sh"
echo ""
echo "  5. Update frontend/src/environments/environment.prod.ts:"
echo "     apiUrl: 'http://$ALB_DNS'"
echo "     Then git push to trigger Vercel redeploy."
echo ""
echo "  6. Watch ECS task logs:"
echo "     aws logs tail /ecs/actiontracker-backend --follow --region us-east-1"
echo ""
echo "  7. Test the app at: https://actiontracker-rust.vercel.app"
echo "============================================================"
