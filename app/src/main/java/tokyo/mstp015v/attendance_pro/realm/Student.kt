package tokyo.mstp015v.attendance_pro.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Student(
    @PrimaryKey
    var id:Long=0,
    var st_id:String="",
    var st_name:String="",
    var g_name:String="",
    var no:Int=0,
): RealmObject()