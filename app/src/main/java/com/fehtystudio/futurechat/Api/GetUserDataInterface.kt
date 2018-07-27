package com.fehtystudio.futurechat.Api

import retrofit2.Call
import retrofit2.http.GET

interface GetUserDataInterface {

    @GET("/method/users.get?user_ids=81294142&fields=photo_max_orig&access_token=9d7dadb2fededb45eec7797535c0b895a8994ca71e5dca7efa8dd519727ab2229fbe6c04cd374b3f0ff4f&v=5.80")
    fun getUserData(): Call<GetUserData>
}
