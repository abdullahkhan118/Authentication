package com.horux.data.user

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MongoUserDataSource(
    private val db: CoroutineDatabase
): UserDataSource {

    private val userCollection = db.getCollection<User>()

    override suspend fun getUserByUsername(username: String): User? =
        userCollection.findOne(User::username eq username)

    override suspend fun insertUser(user: User): Boolean =
        userCollection.insertOne(user).wasAcknowledged()
}