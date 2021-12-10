package tokyo.mstp015v.attendance.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class TimeTable(
    @PrimaryKey
    var id : Long=0,
    var g_name:String="",
    var day:String="",
    var timed:Int=0,
    var sub_name:String="",
    var sub_mentor:String=""
): RealmObject()