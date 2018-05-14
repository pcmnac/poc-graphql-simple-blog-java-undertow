package com.github.pcmnac.graphql.dataloader;

import com.github.pcmnac.graphql.bean.Comment;
import com.github.pcmnac.graphql.utils.Futurify;
import com.mashape.unirest.http.Unirest;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommentDataLoader extends DataLoader<Integer, Comment> {

    private static BatchLoader<Integer, Comment> commentBatchLoader = commentIds -> {

        System.out.println("Loading comments: " + commentIds);

        List<CompletableFuture<Comment>> futures = commentIds.stream()
                .map(commentId ->
                        Futurify.futurify(
                                Unirest.get("http://jsonplaceholder.typicode.com/comments/" + commentId)
                                        .asObjectAsync(Comment.class)
                        ).thenApply(response -> response.getBody()))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                .thenApply(v -> futures.stream()
                        .map(future -> future.join())
                        .collect(Collectors.toList())
                );
    };

    public CommentDataLoader() {
        super(commentBatchLoader);
    }

    @Override
    public CompletableFuture<Comment> load(Integer key) {
//        System.out.println("comment added to batch: " + key);
        return super.load(key);
    }
}
