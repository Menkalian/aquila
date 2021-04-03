package de.menkalian.aquila.server.data

import org.springframework.data.mongodb.repository.MongoRepository

interface AuthRepository : MongoRepository<Auth, String> {
}