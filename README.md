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


