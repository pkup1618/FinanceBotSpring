package com.example.demo.services

class UserStateContainer {
    var userState: UserState = UserState.UNNECESSARY

    var habitHeader: String? = null
    var habitDescription: String? = null
    var notifictaionCron: String? = null

    fun changeState(userState: UserState) {
        this.userState = userState
    }
}