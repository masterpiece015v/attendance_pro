package tokyo.mstp015v.attendance

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import tokyo.mstp015v.attendance.databinding.FragmentMakeTimeTableBinding

class MakeTimeTableFragment : Fragment() {

    private var _binding : FragmentMakeTimeTableBinding?=null
    private val binding get() = _binding!!
    private val args : MakeTimeTableFragmentArgs by navArgs()

    private var sat_enable :Boolean = false
    private var max_timed :Int = 0
    private val days1 = listOf("月","火","水","木","金")
    private val days2 = listOf("月","火","水","木","金","土")

    inner class DaysAdapter( fa: FragmentActivity? ): FragmentStateAdapter( fa!! ){
        override fun getItemCount():Int = if( sat_enable ){ days2.size} else {days1.size}
        override fun createFragment( p:Int):Fragment =
            if( sat_enable ){
                MakeTimeTableDayFragment.newInstance(args.gName!!,days2[p],max_timed)
            }else{
                MakeTimeTableDayFragment.newInstance(args.gName!!,days1[p],max_timed)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMakeTimeTableBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref = context?.getSharedPreferences("init", Context.MODE_PRIVATE)
        sat_enable = pref!!.getBoolean("sat_enable",false)
        max_timed = pref!!.getInt("max_timed",5)

        binding.viewPagerMakeTimeTable.adapter = DaysAdapter( activity)
        TabLayoutMediator( binding.tabMakeTimeTable , binding.viewPagerMakeTimeTable){tab,p->
            if( sat_enable ) {
                tab.text = days2[p]
            }else{
                tab.text = days1[p]
            }
        }.attach()

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null

    }
}

