package com.github.pcmnac.graphql.resolvers;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.github.pcmnac.graphql.bean.Comment;
import com.github.pcmnac.graphql.bean.Post;
import com.github.pcmnac.graphql.utils.Config;

import java.util.concurrent.CompletableFuture;

public class CommentResolver implements GraphQLResolver<Comment> {
    public CompletableFuture<Post> getPost(Comment comment) {
        return Config.instance().postDataLoader.load(comment.getPostId());
    }
}
