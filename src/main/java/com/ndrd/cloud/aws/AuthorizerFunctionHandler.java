package com.ndrd.cloud.aws;

import com.ndrd.cloud.io.AuthPolicy;
import com.ndrd.cloud.io.TokenAuthorizerContext;
import org.springframework.cloud.function.adapter.aws.SpringBootRequestHandler;


public class AuthorizerFunctionHandler extends SpringBootRequestHandler<TokenAuthorizerContext, AuthPolicy> {
}
