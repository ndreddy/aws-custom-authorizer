import json
import boto3


def lambda_handler(event, context):
    print("Request Dump " + json.dumps(event))

    token = event['params']['header']['Authorization']
    print("Authorization Token = " + token)
    client = boto3.client('cognito-idp')
    response = client.get_user(
        AccessToken=token
    )
    print("response = " + json.dumps(response))
    return {"userId": response['Username']}
