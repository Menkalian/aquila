package de.menkalian.aquila.server.data

import org.springframework.data.mongodb.repository.MongoRepository

interface UserRepository : MongoRepository<User, String>{
}