package com.github.pcmnac.graphql;

import com.coxautodev.graphql.tools.SchemaParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pcmnac.graphql.utils.Config;
import com.mashape.unirest.http.Unirest;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.SimpleInstrumentationContext;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionStrategyParameters;
import graphql.schema.GraphQLSchema;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.Headers;

import java.io.IOException;
import java.util.Map;

public class Application {

    public static final String HOST = System.getProperty("host", "0.0.0.0");
    public static final int PORT = Integer.parseInt(System.getProperty("host", "8080"));

    public static void main(String[] args) {
        ObjectMapper jacksonObjectMapper = new ObjectMapper();

        // Only one time
        Unirest.setObjectMapper(new com.mashape.unirest.http.ObjectMapper() {

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        GraphQLSchema graphQLSchema = SchemaParser.newParser().file("simple-blog.schema.graphqls") //
                .resolvers(
                        Config.instance().queryResolver, //
                        Config.instance().postResolver, //
                        Config.instance().userResolver, //
                        Config.instance().commentResolver //
                )
                .build() //
                .makeExecutableSchema();

        GraphQL graphQL = GraphQL.newGraphQL(graphQLSchema)
                .instrumentation(new SimpleInstrumentation() {
                    @Override
                    public InstrumentationContext<ExecutionResult> beginExecutionStrategy(InstrumentationExecutionStrategyParameters parameters) {
                        return SimpleInstrumentationContext.whenDispatched(result -> {
//                            System.out.println("beginExecutionStrategy: " + result);
                            Config.instance().postDataLoader.dispatch();
                            Config.instance().userDataLoader.dispatch();
                            Config.instance().commentDataLoader.dispatch();
                        });
                    }
                })
                .build();

        Undertow server = Undertow.builder() //
                .addHttpListener(PORT, HOST) //
                .setHandler(Handlers.path() //
                                .addPrefixPath("/graphql", exchange -> {
                                    exchange.getRequestReceiver().receiveFullString((ex, data) -> {
                                        try {
                                            Map<String, Object> payload = jacksonObjectMapper.readValue(data, Map.class);

                                            String query = (String) payload.get("query");
                                            String operationname = (String) payload.get("operationName");
                                            Map<String, Object> variables = (Map<String, Object>) payload.get("variables");

                                            ExecutionInput input = ExecutionInput.newExecutionInput() //
                                                    .query(query) //
                                                    .variables(variables) //
                                                    .operationName(operationname) //
                                                    .build();

                                            ExecutionResult executionResult = graphQL.execute(input);
                                            ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
                                            ex.getResponseSender().send(jacksonObjectMapper.writeValueAsString(executionResult.toSpecification()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                            ex.getResponseSender().send(e.getMessage());
                                        }
                                    }, (ex, exception) -> {
                                        exception.printStackTrace();
                                        ex.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                                        ex.getResponseSender().send(exception.getMessage());
                                    });
                                })
                                .addPrefixPath("/graphiql", Handlers.resource(new ClassPathResourceManager(Application.class.getClassLoader())).addWelcomeFiles("index.html"))
                )
                .build();

        System.out.println("Starting server at " + PORT);
        server.start();

    }

}
