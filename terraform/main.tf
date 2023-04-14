terraform {

  required_version = ">= 1.2.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }
}

provider "aws" {
  region = "us-west-2"
  default_tags {
    tags = {
      Project = "unisync"
    }
  }
}

resource "aws_dynamodb_table" "table_info_table" {
  name         = "_tableInfo"
  billing_mode = var.table_billing_mode
  hash_key     = "tableName"
  range_key    = "userId"

  attribute {
    name = "tableName"
    type = "S"
  }

  attribute {
    name = "userId"
    type = "S"
  }
}

resource "aws_dynamodb_table" "data_table" {
  for_each     = toset(["contacts", "inbox", "outbox"])
  name         = each.key
  billing_mode = var.table_billing_mode
  hash_key     = "userId"
  range_key    = "rowId"

  attribute {
    name = "userId"
    type = "S"
  }

  attribute {
    name = "rowId"
    type = "N"
  }
}

# Creating lambda function to process data
resource "aws_lambda_function" "lambda_function" {
  filename      = "../server/apps/im/target/im-1.0-SNAPSHOT.jar"
  function_name = "unisync-lambda"
  role          = aws_iam_role.lambda_role.arn
  handler       = "com.lightspeed.unisync.apps.im.SyncLambda::handleRequest"
  runtime       = "java11"
  memory_size   = 2048

  source_code_hash = filebase64sha256("../server/apps/im/target/im-1.0-SNAPSHOT.jar")

  environment {}
}


# Creating IAM role for Lambda Execution
resource "aws_iam_role" "lambda_role" {
  name = "lambda-execution-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })
}

# Create Policies
resource "aws_iam_role_policy_attachment" "lambda_dynamo" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
  role       = aws_iam_role.lambda_role.name
}

resource "aws_iam_role_policy_attachment" "lambda_logs" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.lambda_role.name
}

# Create API Gateway
resource "aws_api_gateway_rest_api" "unisync_api" {
  name        = "unisync_api"
  description = "Unisync API"
}

# Create a resource
resource "aws_api_gateway_resource" "example_resource" {
  rest_api_id = aws_api_gateway_rest_api.unisync_api.id
  parent_id   = aws_api_gateway_rest_api.unisync_api.root_resource_id
  path_part   = "sync"
}

# Create a method for the resource
resource "aws_api_gateway_method" "example_method" {
  rest_api_id   = aws_api_gateway_rest_api.unisync_api.id
  resource_id   = aws_api_gateway_resource.example_resource.id
  http_method   = "POST"
  authorization = "NONE"
}

# Set up Lambda permissions for API
resource "aws_lambda_permission" "allow-api-permission" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda_function.arn
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${replace(aws_api_gateway_deployment.example_deployment.execution_arn, aws_api_gateway_deployment.example_deployment.stage_name, "")}*/*"
}

# Create integration between API Gateway and Lambda
resource "aws_api_gateway_integration" "example_integration" {
  rest_api_id = aws_api_gateway_rest_api.unisync_api.id
  resource_id = aws_api_gateway_resource.example_resource.id
  http_method = aws_api_gateway_method.example_method.http_method

  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.lambda_function.invoke_arn
  integration_http_method = "POST"
}

# Deploy the API
resource "aws_api_gateway_deployment" "example_deployment" {
  depends_on  = [aws_api_gateway_integration.example_integration]
  rest_api_id = aws_api_gateway_rest_api.unisync_api.id
  stage_name  = "dev"
}

output "gateway_url" {
  value = aws_api_gateway_deployment.example_deployment.invoke_url
}
