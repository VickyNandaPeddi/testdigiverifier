package com.aashdit.digiverifier;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
//@EnableSwagger2
@ComponentScan(basePackages = "com.aashdit.*")
public class SwaggerConfiguration //extends WebMvcConfigurationSupport 
{
	
//	@Bean
//    public Docket produceApi(){
//		return new Docket(DocumentationType.SWAGGER_2)
//				.apiInfo(apiInfo())
//				.select()
//				.apis(RequestHandlerSelectors.any())
//				.paths(PathSelectors.any())
//				.build();
//	}
//	
//	@Override
//	public void addResourceHandlers(ResourceHandlerRegistry registry) {
//	    registry.addResourceHandler("swagger-ui.html")
//	            .addResourceLocations("classpath:/META-INF/resources/");
//
//	    registry.addResourceHandler("/webjars/**")
//	            .addResourceLocations("classpath:/META-INF/resources/webjars/");
//	}
//
//	private ApiInfo apiInfo() {
//	    return new ApiInfoBuilder()
//	    		.title("Digiverifier")
//	    		.description("Background verification")
//	    		.version("0.0.1-SNAPSHOT")
//	    		.build();
//	}
	
	@Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
            .group("swagger")
            .pathsToMatch("/api/**") // Customize the path to your API endpoints
            .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Digiverifier").version("3.0"));
    }

}
