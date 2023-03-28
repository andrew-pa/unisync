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

resource "aws_dynamodb_table" "sync_table" {
 name = var.table_name
 hash_key = "syncId"
 billing_mode = var.table_billing_mode

 attribute {
    name = "syncId"
  type = "S"
 }

 tags = {
   environment = "${var.environment}"
 }
}

data "archive_file" "lambda_function_code" {
  type        = "zip"
  source_dir  = "${path.module}/lambda"
  output_path = "${path.module}/lambda_function_code.zip"
}

resource "aws_lambda_function" "lambda_function" {
  filename      = data.archive_file.lambda_function_code.output_path
  function_name = "unisync-Lambda"
  role          = aws_iam_role.lambda_execution.arn
  handler       = "lambda_function.handler"
  runtime       = "python3.7"

  source_code_hash = data.archive_file.lambda_function_code.output_base64sha256

  environment {
    variables = {
      TABLE_NAME = var.table_name
    }
  }
}


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

resource "aws_iam_role_policy_attachment" "lambda_execution" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
  role       = aws_iam_role.lambda_execution.name
}

resource "aws_lambda_permission" "allow_dynamodb" {
  statement_id  = "AllowExecutionFromDynamoDB"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.lambda_function.arn
  principal     = "dynamodb.amazonaws.com"

  source_arn = aws_dynamodb_table.sync_table.arn
}