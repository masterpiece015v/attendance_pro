package tokyo.mstp015v.attendance_pro

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import io.realm.Realm
import io.realm.kotlin.where
import tokyo.mstp015v.attendance_pro.databinding.FragmentEnterTimeTableDay2Binding
import tokyo.mstp015v.attendance_pro.realm.Attendance
import tokyo.mstp015v.attendance_pro.realm.TimeTable

class EnterTimeTableDay2Fragment(var g_name:String,
                                 var day: String,
                                 var max_timed: Int,
                                 var year:Int,
                                 var month:Int,
                                 var date:Int) : Fragment() {

    private var _binding : FragmentEnterTimeTableDay2Binding? = null
    private val binding get() = _binding!!
    private lateinit var realm : Realm
    private val list  = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEnterTimeTableDay2Binding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = Realm.getDefaultInstance()
    }

    override fun onResume() {
        super.onResume()
        //チェックリストの作成
        list.clear()
        val checkret = realm.where<Attendance>()
            .equalTo("g_name",g_name)
            .equalTo("year",year)
            .equalTo("month",month)
            .equalTo("date",date)
            .sort("timed")
            //.distinct("year","month","date","timed")
            .findAll()
        checkret.forEach {
            list.add("${it.year}${it.month}${it.date}${it.timed}")
            //Log.d("listmake","${it.year}${it.month}${it.date}${it.timed}")
        }

        //一度消す
        binding.linearEnterTimeTable2.removeAllViews()

        val ret = realm.where<TimeTable>()
            .equalTo("g_name",g_name)
            .equalTo("day",day)
            .lessThanOrEqualTo("timed",max_timed)
            .sort("timed")
            .findAll()

        ret.forEach {db->
            val linear = LinearLayout(context)
            val row = layoutInflater.inflate(R.layout.text_item_3_trush,linear)
            row.findViewById<TextView>(R.id.textTrush3_1).let{
                it.text = db.timed.toString()
            }
            row.findViewById<TextView>(R.id.textTrush3_2).let{
                it.text = db.sub_name
            }
            row.findViewById<TextView>(R.id.textTrush3_3).let{
                it.text = db.sub_mentor
            }
            val linearTrush = row.findViewById<ConstraintLayout>(R.id.linearTrush3)

            //クリックイベント
            linearTrush.setOnClickListener{

                if( db.sub_mentor.length > 0 ) {
                //科目名があれば出席入力ができる
                    val action =
                        EnterAttendanceTimeTableFragmentDirections.actionToEnterAttendanceFragment(
                            db.g_name, db.sub_name!!, year, month, date, db.timed, day)
                    findNavController().navigate(action)
                    linearTrush.setBackgroundColor(Color.parseColor("#FFFF99"))

                }
            }

            //ロングクリックイベント
            linearTrush.setOnLongClickListener{
                //ゴミ箱の設定
                val buttonTrush = row.findViewById<ImageButton>(R.id.imageTrushButton)
                buttonTrush.setOnClickListener {
                    realm.executeTransaction{ deleteDb->
                        val ret = deleteDb.where<Attendance>()
                            .equalTo("g_name",g_name)
                            .equalTo("year",year)
                            .equalTo("month",month)
                            .equalTo("date",date)
                            .equalTo("timed",db.timed)
                            .findAll()
                        ret.deleteAllFromRealm()
                        linearTrush.setBackgroundColor(Color.WHITE )
                        buttonTrush.visibility = ImageButton.INVISIBLE
                    }
                }

                //すでに長押しされているかチェック
                if( buttonTrush.visibility == ImageButton.INVISIBLE ) {
                    buttonTrush.visibility = ImageButton.VISIBLE
                    val drawable = GradientDrawable()
                    drawable.mutate()
                    drawable.shape = GradientDrawable.RECTANGLE
                    drawable.cornerRadius = 20.0f
                    drawable.color = ColorStateList.valueOf(Color.parseColor("#FF9999"))
                    it.background = drawable
                }else{
                    buttonTrush.visibility = ImageButton.INVISIBLE
                    it.background = null
                    if("${year}${month}${date}${db.timed}" in list){
                        linearTrush.setBackgroundColor( Color.parseColor("#FFFF99") )
                    }
                }
                true
            }

            if("${year}${month}${date}${db.timed}" in list){
                linearTrush.setBackgroundColor( Color.parseColor("#FFFF99") )
            }else{
                linearTrush.setBackgroundColor(Color.parseColor("#FFFFFF"))
            }

            binding.linearEnterTimeTable2.addView( linear )

        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    fun setday( year:Int,month:Int,date:Int){
        this.year = year
        this.month = month
        this.date = date
    }

}
