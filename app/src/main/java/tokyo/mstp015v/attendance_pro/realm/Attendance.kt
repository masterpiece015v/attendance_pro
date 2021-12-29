package tokyo.mstp015v.attendance_pro.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Attendance(
    @PrimaryKey
    var id:Long=0,
    var st_id:String="",
    var st_name:String="",
    var g_name:String="",
    var no:Int=0,
    var year:Int=0,
    var month:Int=0,
    var date:Int=0,
    var timed:Int=0,
    var sub_name:String="",
    var at_code:Int=0,
): RealmObject()