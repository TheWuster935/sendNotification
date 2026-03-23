import * as cdk from 'aws-cdk-lib/core';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as apigw from 'aws-cdk-lib/aws-apigatewayv2';
import * as integrations from 'aws-cdk-lib/aws-apigatewayv2-integrations';
import { Construct } from 'constructs';
import * as path from 'path';

export class CdkStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    const flightHandler = new lambda.Function(this, 'FlightHandler', {
      runtime: lambda.Runtime.JAVA_17,
      handler: 'com.example.sendnotification.lambda.FlightHandler',
      code: lambda.Code.fromAsset(path.join(__dirname, '../../lambda/target/send-notification-lambda-0.0.1-SNAPSHOT.jar')),
      memorySize: 512,
      timeout: cdk.Duration.seconds(30),
      environment: {
        AVIATION_STACK_API: process.env.AVIATION_STACK_API || '',
        WEBHOOK_URL: process.env.WEBHOOK_URL || '',
      },
    });

    const httpApi = new apigw.HttpApi(this, 'FlightHttpApi', {
      apiName: 'SendNotificationApi',
      corsPreflight: {
        allowOrigins: ['*'],
        allowMethods: [apigw.CorsHttpMethod.GET, apigw.CorsHttpMethod.OPTIONS],
        allowHeaders: ['Content-Type'],
      },
    });

    httpApi.addRoutes({
      path: '/api/flight',
      methods: [apigw.HttpMethod.GET],
      integration: new integrations.HttpLambdaIntegration('FlightIntegration', flightHandler),
    });

    new cdk.CfnOutput(this, 'ApiUrl', {
      value: httpApi.url ?? 'No URL',
      description: 'API Gateway endpoint URL',
    });
  }
}
