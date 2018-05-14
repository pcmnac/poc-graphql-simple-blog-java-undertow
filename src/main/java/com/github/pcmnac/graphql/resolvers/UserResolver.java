package com.github.pcmnac.graphql.resolvers;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.github.pcmnac.graphql.bean.Post;
import com.github.pcmnac.graphql.bean.User;
import com.github.pcmnac.graphql.utils.Config;
import com.mashape.unirest.http.Unirest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UserResolver implements GraphQLResolver<User> {

    public CompletableFuture<List<Post>> getPosts(User user) throws Exception {

        List<Post> postIds = Arrays.asList(Unirest.get("http://jsonplaceholder.typicode.com/posts?userId=" + user.getId())
                .asObject(Post[].class).getBody());

        List<CompletableFuture<Post>> futures = postIds.stream()
                .map(post -> Config.instance().postDataLoader.load(post.getId()))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures.stream()
                        .map(future -> future.join())
                        .collect(Collectors.toList())
                );
    }
}
