package com.github.pcmnac.graphql.resolvers;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.github.pcmnac.graphql.bean.Post;
import com.github.pcmnac.graphql.bean.User;
import com.github.pcmnac.graphql.utils.Config;
import com.github.pcmnac.graphql.utils.Futurify;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class QueryResolver implements GraphQLQueryResolver {

    public String hello() {
        return "Hello GraphQL Java (graphql-java-tools + Undertow + DataLoader)";
    }

    // Sync Example
    public List<Post> posts() throws Exception {
        return Arrays.asList(Unirest.get("http://jsonplaceholder.typicode.com/posts")
                .asObject(Post[].class).getBody());
    }

    public CompletableFuture<Post> post(int id) {
        return Config.instance().postDataLoader.load(id);
    }


    // Async example using CompletableFuture
    public CompletableFuture<List<User>> users() throws Exception {
        Future<HttpResponse<User[]>> response = Unirest.get("http://jsonplaceholder.typicode.com/users")
                .asObjectAsync(User[].class);

        return Futurify.futurify(response)
                .thenApply(users -> Arrays.asList(users.getBody()));
    }

}
