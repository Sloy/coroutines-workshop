package project

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun loadContributorsCallbacks(req: RequestData, callback: (List<User>) -> Unit) {
    val service = createGitHubService(req.username, req.password)
    log.info("Loading ${req.org} repos")


    service.listOrgRepos(req.org).responseCallback { repos ->
        log.info("${req.org}: loaded ${repos.size} repos")

        val loadedUsers = mutableListOf<User>()
        fun loadUsers(i: Int) {
            if (i < repos.size) {
                service.listRepoContributors(req.org, repos[i].name).responseCallback { users ->
                    loadedUsers += users
                    loadUsers(i + 1)
                }
            } else {
                val contribs = loadedUsers.aggregate()
                callback(contribs)
                log.info("Total: ${contribs.size} contributors")

            }
        }
        loadUsers(0)
    }
}


@Suppress("UNCHECKED_CAST")
fun <T> Call<T>.responseCallback(
        callback: (T) -> Unit,
        noContent: (Response<T>) -> T = { errorResponse(it) }
) {
    enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            when (response.code()) {
                200 -> callback(response.body() as T) // OK
                204 -> callback(noContent(response)) // NO CONTENT
                else -> errorResponse(response)
            }
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            log.error("Call failed", t)
        }
    })
}

fun <T> Call<List<T>>.responseCallback(
        callback: (List<T>) -> Unit
) =
        responseCallback(callback, noContent = { emptyList() })
