package com.example.destined

import com.google.firebase.database.Exclude

data class Locashan (
    @get:Exclude
    var id:String?=null,
    @get:Exclude
    var isDeleted: Boolean = false,

    var longit:String?=null,
    var latit:String?=null,
    var name:String?=null
) {
    override fun equals(other: Any?): Boolean {
        return if (other is Locashan) {
            other.id == id
        } else false
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (longit?.hashCode() ?: 0)
        result = 31 * result + (latit?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}