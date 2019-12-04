package project

import org.slf4j.Logger
import org.slf4j.LoggerFactory

val log: Logger = LoggerFactory.getLogger("Contributors")

fun List<User>.aggregate(): List<User> =
        groupBy { it.login }
                .mapValues { entry ->
                    val totalContributions = entry.value.map { it.contributions }.sum()
                    User(entry.key, totalContributions)
                }.map { it.value }
                .sortedByDescending { it.contributions }

