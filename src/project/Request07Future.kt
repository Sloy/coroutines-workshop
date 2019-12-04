package project

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

fun loadContributorsConcurrentAsync(req: RequestData): CompletableFuture<List<User>> =
        GlobalScope.future {
            loadContributors(req)
        }