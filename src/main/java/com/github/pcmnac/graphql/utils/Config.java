package com.github.pcmnac.graphql.utils;

import com.github.pcmnac.graphql.dataloader.CommentDataLoader;
import com.github.pcmnac.graphql.dataloader.PostDataLoader;
import com.github.pcmnac.graphql.dataloader.UserDataLoader;
import com.github.pcmnac.graphql.resolvers.CommentResolver;
import com.github.pcmnac.graphql.resolvers.PostResolver;
import com.github.pcmnac.graphql.resolvers.QueryResolver;
import com.github.pcmnac.graphql.resolvers.UserResolver;

public class Config {

    public static final Config INSTANCE = new Config();

    public QueryResolver queryResolver = new QueryResolver();
    public PostResolver postResolver = new PostResolver();
    public CommentResolver commentResolver = new CommentResolver();
    public UserResolver userResolver = new UserResolver();

    public UserDataLoader userDataLoader = new UserDataLoader();
    public PostDataLoader postDataLoader = new PostDataLoader();
    public CommentDataLoader commentDataLoader = new CommentDataLoader();


    private Config() {

    }

    public static Config instance() {
        return INSTANCE;
    }


}
