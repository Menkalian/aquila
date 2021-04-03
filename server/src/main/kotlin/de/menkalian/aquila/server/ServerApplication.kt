package de.menkalian.aquila.server

import de.menkalian.aquila.server.data.User
import de.menkalian.aquila.server.data.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class ServerApplication {
    @Bean
    fun test(repo: UserRepository): CommandLineRunner = CommandLineRunner {
        val u1 = User(name = "Kilian")
        val u2 = User(name = "Baris")

        repo.save(u1)
        repo.save(u2)

        u2.friends.add(u1)
        u1.friends.add(u2)

        repo.save(u1)
        repo.save(u2)
    }
}

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}

