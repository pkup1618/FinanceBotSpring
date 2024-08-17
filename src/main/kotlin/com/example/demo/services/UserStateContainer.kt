package com.example.demo.services

class UserStateContainer {
    var userState: UserState = UserState.UNNECESSARY

    var habitHeader: String? = null
    var habitDescription: String? = null
    // h days

    fun changeState(userState: UserState) {
        this.userState = userState
    }
}