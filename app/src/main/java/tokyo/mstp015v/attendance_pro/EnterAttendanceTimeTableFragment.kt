package tokyo.mstp015v.attendance_pro

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import io.realm.Realm
import io.realm.kotlin.where
import tokyo.mstp015v.attendance_pro.databinding.FragmentEnterAttendanceTimeTableBinding
import tokyo.mstp015v.attendance_pro.realm.Attendance
import tokyo.mstp015v.attendance_pro.tools.CalendarDialog
import java.util.*


class EnterAttendanceTimeTableFragment : Fragment() {

    private val args : EnterAttendanceTimeTableFragmentArgs by navArgs()
    private var _binding : FragmentEnterAttendanceTimeTableBinding? = null
    private val binding get() = _binding!!
    private var sat_enable :Boolean = false
    private var max_timed :Int = 0
    private var calendar = Calendar.getInstance()
    private val days1 = listOf("月","火","水","木","金")
    private val days2 = listOf("月","火","水","木","金","土")
    private var year = 0
    private var month = 0
    private var date = 0
    private var day = 0
    private lateinit var realm : Realm
    //private val list = mutableSetOf<String>()

    //ページャー用の
    inner class DaysAdapter( fa: FragmentActivity? ): FragmentStateAdapter( fa!! ){
        override fun getItemCount():Int = if( sat_enable ){ days2.size} else {days1.size}
        override fun createFragment(p:Int):Fragment =
            if( sat_enable ){
                //EnterTimeTableDayFragment.newInstance(args.gName!!,days2[p],max_timed,year,month,date)
                //EnterTimeTableDay2Fragment(args.gName!!,days2[p],max_timed,year,month,date,list)
                EnterTimeTableDay2Fragment(args.gName!!,days2[p],max_timed,year,month,date)
            }else{
                //EnterTimeTableDayFragment.newInstance(args.gName!!,days1[p],max_timed,year,month,date)
                //EnterTimeTableDay2Fragment(args.gName!!,days1[p],max_timed,year,month,date,list)
                EnterTimeTableDay2Fragment(args.gName!!,days1[p],max_timed,year,month,date)
            }

        fun findFragment(position : Int ):EnterTimeTableDay2Fragment? =
            activity!!.supportFragmentManager.findFragmentByTag("f${position}") as EnterTimeTableDay2Fragment?

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEnterAttendanceTimeTableBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //キーボードを閉じる

        Log.d("EnterAttendanceTimeTableFragment","onViewCreated")
        //入力チェックリストの作成
        realm = Realm.getDefaultInstance()

        //シェアードプリファレンスの取得
        val pref = context?.getSharedPreferences("init", Context.MODE_PRIVATE)
        sat_enable = pref!!.getBoolean("sat_enable",false)
        max_timed = pref!!.getInt("max_timed",5)

        //viewPagerの設定
        val adapter = DaysAdapter( activity )
        binding.viewPagerEnterAttendance.adapter = adapter
        TabLayoutMediator( binding.tabEnterAttendance , binding.viewPagerEnterAttendance ){tab,p->
            if( sat_enable ) {
                tab.text = days2[p]
            }else{
                tab.text = days1[p]
            }
        }.attach()

        //日付の設定
        calendarRefresh(0)
        Log.d("EnterAttendancetimeTable","calendarRefresh")
        //最初に表示するタブは曜日になるので
        binding.viewPagerEnterAttendance.setCurrentItem( day ,false )
        Log.d("day",day.toString())
        adapter.findFragment(day)?.onResume()
        //ページが変わるときのイベント
        binding.viewPagerEnterAttendance.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position:Int){
                super.onPageSelected(position)
                calendarRefresh( position - day )
            }
        })

        //日付をタップするとカレンダーを表示するイベント
        binding.textDate.setOnClickListener {
            val ret = realm.where<Attendance>().equalTo("g_name",args.gName )
                .sort("year").sort("month").sort("date")
                .distinct("date","month","year")
                .findAll()

            val list = mutableListOf<String>()
            ret.forEach {
                list.add( "${it.year}${it.month}${it.date}")
                //Log.d( "datelist" , "${it.year}${it.month}${it.date}")
            }

            val dialog = CalendarDialog(args.gName,year,month,date,list,
                {year,month,date->
                    //OKを押したときのイベント
                    calendar.set(Calendar.YEAR,year)
                    calendar.set(Calendar.MONTH,month-1)
                    //日付が選択されていない場合dateは0になる
                    calendar.set(Calendar.DATE,if(date==0) 1 else date )
                    calendarRefresh(0)

                    val i = calendar.get( Calendar.DAY_OF_WEEK) - 2
                    binding.viewPagerEnterAttendance.setCurrentItem(i, false)
                    //Log.d( "i" , "${i},${binding.viewPagerEnterAttendance.currentItem}")
                    if( i == binding.viewPagerEnterAttendance.currentItem ) {
                        //日付は変わるが曜日が変わらないとき
                        adapter.findFragment(i)?.onResume()
                    }
                },{
                    //cancel時のイベントはなし

                })
            dialog.show(parentFragmentManager,"dialog")
        }

    }

    fun calendarRefresh(i : Int){
        calendar.add(Calendar.DATE, i )
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH) + 1
        date = calendar.get(Calendar.DATE)
        day = calendar.get(Calendar.DAY_OF_WEEK) - 2
        day = if( sat_enable ){
            //土曜が有効
            if( day < 0 ) 0 else day
        }else{
            //土曜が無効
            if( day < 0 || day >= 5 ) 0 else day
        }

        if( sat_enable ) {
            binding.textDate.text = "${year}年${month}月${date}日(${days2[day]})"
        }else{
            binding.textDate.text = "${year}年${month}月${date}日(${days1[day]})"
        }

        val adapter : DaysAdapter = binding.viewPagerEnterAttendance.adapter as DaysAdapter
        adapter.findFragment(day)?.setday(year,month,date)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        realm.close()
    }
}