package com.github.pcmnac.graphql.dataloader;

import com.github.pcmnac.graphql.bean.Post;
import com.github.pcmnac.graphql.utils.Futurify;
import com.mashape.unirest.http.Unirest;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PostDataLoader extends DataLoader<Integer, Post> {

    private static BatchLoader<Integer, Post> postBatchLoader = postIds -> {

        System.out.println("Loading posts: " + postIds);

        List<CompletableFuture<Post>> futures = postIds.stream()
                .map(postId ->
                        Futurify.futurify(
                                Unirest.get("http://jsonplaceholder.typicode.com/posts/" + postId)
                                        .asObjectAsync(Post.class)
                        ).thenApply(response -> response.getBody()))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures.stream()
                        .map(future -> future.join())
                        .collect(Collectors.toList())
                );
    };

    public PostDataLoader() {
        super(postBatchLoader);
    }

    @Override
    public CompletableFuture<Post> load(Integer key) {
//        System.out.println("post added to batch: " + key);
        return super.load(key);
    }
}
