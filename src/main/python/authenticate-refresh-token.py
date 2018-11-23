import json
import boto3


def lambda_handler(event, context):
    print("Request Dump " + json.dumps(event))

    token = event['params']['header']['Authorization']
    print("Authorization Token = " + token)
    client = boto3.client('cognito-idp')

    response = client.admin_initiate_auth(
        UserPoolId='us-east-1_OpQZUjeDK',
        ClientId='7amh5jk3tmdqjju7q5fvmq09ar',
        AuthFlow='REFRESH_TOKEN_AUTH',
        AuthParameters={
            'REFRESH_TOKEN': token
        }
    )

    print("response = " + json.dumps(response))
    return response
