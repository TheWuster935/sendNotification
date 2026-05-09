import * as cdk from 'aws-cdk-lib/core';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as apigw from 'aws-cdk-lib/aws-apigatewayv2';
import * as integrations from 'aws-cdk-lib/aws-apigatewayv2-integrations';
import { Construct } from 'constructs';
import * as path from 'path';

export class CdkStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const flightLambda = new lambda.Function(this, 'FlightLambdaHandler', {
      runtime: lambda.Runtime.JAVA_17,
      handler: 'com.example.lambda.FlightLambdaHandler',
      code: lambda.Code.fromAsset(path.join(__dirname, '../../lambda/target/flight-lambda-1.0.0.jar')),
      memorySize: 512,
      timeout: cdk.Duration.seconds(30),
      environment: {
        AVIATION_STACK_API: process.env.AVIATION_STACK_API || '',
        WEBHOOK_URL: process.env.WEBHOOK_URL || '',
      },
    });

    const httpApi = new apigw.HttpApi(this, 'FlightHttpApi', {
      apiName: 'flight-api',
      corsPreflight: {
        allowOrigins: ['*'],
        allowMethods: [apigw.CorsHttpMethod.GET],
        allowHeaders: ['Content-Type'],
      },
    });

    httpApi.addRoutes({
      path: '/flight',
      methods: [apigw.HttpMethod.GET],
      integration: new integrations.HttpLambdaIntegration('FlightIntegration', flightLambda),
    });

    new cdk.CfnOutput(this, 'ApiGatewayUrl', {
      value: httpApi.url ?? 'undefined',
      description: 'API Gateway endpoint URL',
    });
  }
}
