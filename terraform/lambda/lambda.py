import boto3
import json

def lambda_handler(event, context):
    # get the data to be written to DynamoDB from the event
    data = event['data']
    
    # create the DynamoDB client
    dynamodb = boto3.client('dynamodb')
    
    # write the data to DynamoDB
    response = dynamodb.put_item(
        TableName= "unisync-table",
        Item={
            'id': {'S': data['id']}
        }
    )
    
    # return a response indicating success or failure
    return {
        'statusCode': 200,
        'body': json.dumps('Data written to DynamoDB')
    }





