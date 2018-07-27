package com.fehtystudio.futurechat.Api
import com.google.gson.annotations.SerializedName

data class GetUserData(
    @SerializedName("response") var response: List<Response>
)

data class Response(
    @SerializedName("id") var id: Int,
    @SerializedName("first_name") var firstName: String,
    @SerializedName("last_name") var lastName: String,
    @SerializedName("photo_max_orig") var photoMaxOrig: String
)