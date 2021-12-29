package tokyo.mstp015v.attendance_pro.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Group(
    @PrimaryKey
    var id:Long=0,
    var g_name:String="",
    var mentor:String="",
): RealmObject()