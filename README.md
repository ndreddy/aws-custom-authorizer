# aws-custom-authorizer
Configure Handler in the AWS console.

- New lambda -> Function Code -> Handler ->com.ndrd.cloud.aws.AuthorizerFunctionHandler

Upload the jar to deploy the function
- mvn -> install -> aws-custom-authorizer-1.0-SNAPSHOT-aws.jar

The function code expects lambda env variables configured in 
- CLIENTAPP_ID, POOL_ID, TOKEN_API

com.ndrd.cloud.aws package
 - Contains AuthorizerHandler function.

com.ndrd.cloud
- AuthorizerFunction -extracts req params, calls service layer and constructs response
- Applicaiton - Has main() method, uses SpringApplication.run() method to launch the application

com.ndrd.cloud.io package
- TokenAuthorizerContext - input to the function
- AuthPolicy - output of the function

com.ndrd.cloud.service package
- Contains IDP specific implementations


Spring Cloud
------------
- How the AuthorizerFunction gets invoked via the AuthorizerFunctionHandler?
- AuthorizerFunctionHandler -> SpringBootRequestHandler -> SpringFunctionInitializer -> application.properties -> function.name -> AuthorizeFunction
 
AuthorizerFunctionHandler extends SpringBootRequestHandler which extends SpringFunctionInitializer which is where the magic happens.

When a request is received, the handler will attempt to initialize the spring context.During initialization, it will look up the property function.name defined in the application.properties which is the name of function component bean that would of been discovered during component scanning.

AWS Cognito Tokens
------------------
aws cognito-idp admin-initiate-auth --user-pool-id us-east-1_OpQZUjeDK --client-id 7amh5jk3tmdqjju7q5fvmq09ar --auth-flow ADMIN_NO_SRP_AUTH --auth-parameters USERNAME=SYSTEM_ADMIN,PASSWORD=Welcome_123
    aws cognito-idp admin-respond-to-auth-challenge --user-pool-id us-east-1_OpQZUjeDK --client-id 7amh5jk3tmdqjju7q5fvmq09ar --challenge-name NEW_PASSWORD_REQUIRED --challenge-responses NEW_PASSWORD=Welcome_123,USERNAME=acb_admin --session ""

Lambda Concurrency
----------------
aws lambda put-function-concurrency --function-name sqs_consumer --reserved-concurrent-executions 25
    
Deploy Lambda via upload to S3
------------------------------
1. Copy jar to an S3 bucker
aws s3 cp target/your-jar-SNAPSHOT-aws.jar s3://bucker-name/ --no-verify-ssl
2. From console Funtion Code-> Code Entry Type -> Upload file from S3 -> enter url like 
https://s3.amazonaws.com/<folder>/<jar-file-name.jar>

