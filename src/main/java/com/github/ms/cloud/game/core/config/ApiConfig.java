package com.github.ms.cloud.game.core.config;

import com.github.microservice.components.swagger.config.SwaggerConfiguration;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;


@Configuration
public class ApiConfig extends SwaggerConfiguration {

    @Override
    @Bean
    @ConditionalOnMissingBean
    public OperationCustomizer addCustomGlobalHeader() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            operation.addParametersItem(new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .required(false)
                    .schema(new StringSchema()._default(""))
                    .name("token")
                    .example("password!@#")
            );

            return operation;
        };
    }



}
