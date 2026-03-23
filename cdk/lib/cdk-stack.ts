import * as cdk from 'aws-cdk-lib/core';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as apigateway from 'aws-cdk-lib/aws-apigateway';
import { Construct } from 'constructs';
import * as path from 'path';

export class CdkStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const flightLambda = new lambda.Function(this, 'FlightHandler', {
      runtime: lambda.Runtime.JAVA_17,
      handler: 'com.example.lambda.FlightHandler',
      code: lambda.Code.fromAsset(path.join(__dirname, '../../lambda/target/flight-lambda-1.0.0.jar')),
      memorySize: 512,
      timeout: cdk.Duration.seconds(30),
      environment: {
        AVIATION_STACK_API: process.env.AVIATION_STACK_API || '',
        WEBHOOK_URL: process.env.WEBHOOK_URL || '',
      },
    });

    const api = new apigateway.RestApi(this, 'FlightApi', {
      restApiName: 'Flight Notification Service',
      description: 'API Gateway for flight data Lambda',
      defaultCorsPreflightOptions: {
        allowOrigins: apigateway.Cors.ALL_ORIGINS,
        allowMethods: ['GET', 'OPTIONS'],
        allowHeaders: ['Content-Type'],
      },
    });

    const flightResource = api.root.addResource('api').addResource('flight');
    flightResource.addMethod('GET', new apigateway.LambdaIntegration(flightLambda));

    new cdk.CfnOutput(this, 'ApiGatewayUrl', {
      value: api.url,
      description: 'API Gateway endpoint URL',
    });
  }
}
