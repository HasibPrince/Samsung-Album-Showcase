package com.hasib.samsungalbumshowcase.domain.entities

import org.junit.Test
import com.hasib.samsungalbumshowcase.domain.entities.User

class UserTest {
    @Test
    fun testUser() {
        val user = User(1, "Hasib", "")
        assert(user.id == 1)
    }
}