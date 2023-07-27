package com.aashdit.digiverifier.utils;

import com.aashdit.digiverifier.globalConfig.EnvironmentVal;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSConfig {

    @Autowired
    private EnvironmentVal env;


    @Bean
    public AmazonS3 s3() {
        AWSCredentials awsCredentials =
                new BasicAWSCredentials(env.getAccessKey(), env.getSecretKey());
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(env.getAwsRegion())
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .build();

    }
//testnanda
//    @Bean
//    public AmazonS3 s3() {
//        AWSCredentials awsCredentials =
//                new BasicAWSCredentials("AKIAVSVRUAJRLIC7BK55", "KH1EJ5kJNv3jQauHCcey3fS/Cy1ZcHfwadqDC6nE");
//        return AmazonS3ClientBuilder
//                .standard()
//                .withRegion("us-east-2")
//                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
//                .build();
//
//    }
}
