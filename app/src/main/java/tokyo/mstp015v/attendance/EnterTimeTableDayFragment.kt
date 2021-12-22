package tokyo.mstp015v.attendance

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.where
import tokyo.mstp015v.attendance.databinding.FragmentEnterTimeTableDayBinding
import tokyo.mstp015v.attendance.realm.TimeTable

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_YEAR = "param4"
private const val ARG_MONTH = "param5"
private const val ARG_DATE = "param6"

class EnterTimeTableDayFragment : Fragment() {
    private var g_name : String? = null
    private var day: String? = null
    private var timed: Int? = null
    private var _binding : FragmentEnterTimeTableDayBinding? = null
    private val binding get() = _binding!!
    private lateinit var realm : Realm
    private var year: Int = 0
    private var month: Int = 0
    private var date: Int = 0
    //レルムのアダプター
    inner class TimeTableAdapter(
        data:OrderedRealmCollection<TimeTable>):RealmRecyclerViewAdapter<TimeTable,TimeTableAdapter.ViewHolder>(data,true){

        private var listener : ((g_name:String?,sub_name:String?,year:Int,month:Int,date:Int,timed:Int)->Unit)? = null
        fun setOnItemClickListener( listener : ((g_name:String?,sub_name:String?,year:Int,month:Int,date:Int,timed:Int)->Unit)){
            this.listener = listener
        }

        inner class ViewHolder( view:View): RecyclerView.ViewHolder( view ){
            val text3_1 = view.findViewById<TextView>(R.id.textItem3_1)
            val text3_2 = view.findViewById<TextView>(R.id.textItem3_2)
            val text3_3 = view.findViewById<TextView>(R.id.textItem3_3)
            val linear3 = view.findViewById<LinearLayout>(R.id.linear3 )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from( parent.context).inflate( R.layout.text_item_3,parent,false)
            return ViewHolder( view )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem( position )
            holder.text3_1.text = item!!.timed.toString()
            holder.text3_2.text = item!!.sub_name
            holder.text3_3.text = item!!.sub_mentor
            holder.linear3.setOnClickListener {
                listener?.invoke( g_name ,item.sub_name,year,month,date,item!!.timed)
            }
        }
    }

    // TODO: Rename and change types of parameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            g_name = it.getString(ARG_PARAM1)
            day = it.getString(ARG_PARAM2)
            timed = it.getInt(ARG_PARAM3)
            year = it.getInt(ARG_YEAR)
            month = it.getInt(ARG_MONTH)
            date = it.getInt(ARG_DATE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentEnterTimeTableDayBinding.inflate( inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = Realm.getDefaultInstance()
        val ret = realm.where<TimeTable>()
            .equalTo("g_name",g_name)
            .equalTo("day",day)
            .lessThanOrEqualTo("timed",timed!!)
            .findAll()
        Log.d("realm","${g_name},${day},${timed}")
        val adapter = TimeTableAdapter( ret )
        binding.recyclerEnterTimeTableDay.adapter = adapter
        adapter.setOnItemClickListener{g_name, sub_name, year, month, date ,timed ->
            val action = EnterAttendanceTimeTableFragmentDirections.actionToEnterAttendanceFragment(g_name!!,sub_name!!,year,month,date,timed)
            findNavController().navigate( action )
        }
        binding.recyclerEnterTimeTableDay.layoutManager = LinearLayoutManager(context)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    companion object {
        fun newInstance(g_name:String,day: String, timed: Int,year:Int,month:Int,date:Int) =
            EnterTimeTableDayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1,g_name)
                    putString(ARG_PARAM2, day)
                    putInt(ARG_PARAM3, timed)
                    putInt(ARG_YEAR,year)
                    putInt(ARG_MONTH,month)
                    putInt(ARG_DATE,date)
                }
            }
    }
}