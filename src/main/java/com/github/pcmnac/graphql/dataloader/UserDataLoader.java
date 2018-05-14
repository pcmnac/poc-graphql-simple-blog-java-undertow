package com.github.pcmnac.graphql.dataloader;

import com.github.pcmnac.graphql.bean.User;
import com.github.pcmnac.graphql.utils.Futurify;
import com.mashape.unirest.http.Unirest;
import org.dataloader.BatchLoader;
import org.dataloader.DataLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class UserDataLoader extends DataLoader<Integer, User> {

    private static BatchLoader<Integer, User> userBatchLoader = userIds -> {

            System.out.println("Loading users: " + userIds);

            List<CompletableFuture<User>> futures = userIds.stream()
                    .map(userId ->
                            Futurify.futurify(
                                    Unirest.get("http://jsonplaceholder.typicode.com/users/" + userId)
                                        .asObjectAsync(User.class)
                            ).thenApply(response -> response.getBody()))
                    .collect(Collectors.toList());

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
                    .thenApply(v -> futures.stream()
                            .map(future -> future.join())
                            .collect(Collectors.toList())
                    );
    };

    public UserDataLoader() {
        super(userBatchLoader);
    }

    @Override
    public CompletableFuture<User> load(Integer key) {
//        System.out.println("user added to batch: " + key);
        return super.load(key);
    }
}
