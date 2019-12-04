package project

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

suspend fun loadContributorsGather(req: RequestData, callback: suspend (List<User>) -> Unit) = coroutineScope {
    val service = withContext(Dispatchers.Default) {
        createGitHubService(req.username, req.password)
    }
    log.info("Loading ${req.org} repos")
    val repos = service.listOrgRepos(req.org).await()
    log.info("${req.org}: loaded ${repos.size} repos")

    val channel = Channel<List<User>>()
    repos.forEach { repo ->
        launch {
            val users = service.listRepoContributors(req.org, repo.name).await()
            log.info("${repo.name}: loaded ${users.size} contributors")
            channel.send(users)
        }
    }

    var contribs = listOf<User>()
    repeat(repos.size) {
        val users = channel.receive()
        contribs = (contribs + users).aggregateSlow()
        log.info("Partial: ${contribs.size} contributors")
        callback(contribs)
    }
    log.info("Total: ${contribs.size} contributors")
}
