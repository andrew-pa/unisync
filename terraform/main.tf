terraform{

    required_version = ">= 1.2.0"

    required_providers {
      aws = {
        source = "hashicorp/aws"
        version = "~> 4.16"
      }
    }
}  

provider "aws" {
  region = "us-west-2"
}

# Creates a DynamoDB table
resource "aws_dynamodb_table" "sync_table" {
 name = var.table_name
 hash_key = "syncId"
 billing_mode = var.table_billing_mode

 attribute {
    name = "syncId"
  type = "S"
 }

 tags = {
   environment = var.environment
 }
}

#Zip Lambda code
data "archive_file" "lambda_function_code" {
  type        = "zip"
  source_dir  = "${path.module}/lambda"
  output_path = "${path.module}/lambda_function_code.zip"
}

# Creating lambda function to process data
resource "aws_lambda_function" "lambda_function" {
  filename      = data.archive_file.lambda_function_code.output_path
  function_name = "unisync-lambda"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "lambda_function.handler"
  runtime       = "java11"

  source_code_hash = data.archive_file.lambda_function_code.output_base64sha256

  environment {
    variables = {
      TABLE_NAME = var.table_name
    }
  }
}


# Creating IAM role for Lambda Execution
resource "aws_iam_role" "lambda_execution" {
  name = "my-lambda-execution-role"

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
resource "aws_iam_role_policy_attachment" "lambda_execution" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
  role       = aws_iam_role.lambda_execution.name
}

# Set up Dynamo permissions
resource "aws_lambda_permission" "allow_dynamodb" {
  statement_id  = "AllowExecutionFromDynamoDB"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda_function.arn
  principal     = "dynamodb.amazonaws.com"

  source_arn = aws_dynamodb_table.sync_table.arn
}

# Create API Gateway
resource "aws_api_gateway_rest_api" "unisync_api" {
  name        = "unisync_api"
  description = "Example API"
}

# Create a resource
resource "aws_api_gateway_resource" "example_resource" {
  rest_api_id = aws_api_gateway_rest_api.unisync_api.id
  parent_id   = aws_api_gateway_rest_api.unisync_api.root_resource_id
  path_part   = "example"
}

# Create a method for the resource
resource "aws_api_gateway_method" "example_method" {
  rest_api_id   = aws_api_gateway_rest_api.unisync_api.id
  resource_id   = aws_api_gateway_resource.example_resource.id
  http_method   = "GET"
  authorization = "NONE"
}

# Set up Lambda permissions for API
resource "aws_lambda_permission" "allow-api-permission" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda_function.arn
  principal     = "apigateway.amazonaws.com"
  source_arn    = "${aws_api_gateway_deployment.example_deployment.execution_arn}/*/${aws_api_gateway_method.example_method.http_method}${aws_api_gateway_resource.example_resource.path}"
}

# Create integration between API Gateway and Lambda
resource "aws_api_gateway_integration" "example_integration" {
  rest_api_id = aws_api_gateway_rest_api.unisync_api.id
  resource_id = aws_api_gateway_resource.example_resource.id
  http_method = aws_api_gateway_method.example_method.http_method

  type        = "AWS_PROXY"
  uri         = aws_lambda_function.lambda_function.invoke_arn
  integration_http_method     = "GET"
}

# Deploy the API
resource "aws_api_gateway_deployment" "example_deployment" {
  depends_on = [aws_api_gateway_integration.example_integration]
  rest_api_id = aws_api_gateway_rest_api.unisync_api.id
  stage_name  = "dev"
}