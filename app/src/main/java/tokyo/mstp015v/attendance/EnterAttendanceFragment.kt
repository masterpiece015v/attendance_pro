package tokyo.mstp015v.attendance

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import tokyo.mstp015v.attendance.databinding.FragmentEnterAttendanceBinding
import tokyo.mstp015v.attendance.realm.Attendance
import tokyo.mstp015v.attendance.realm.Student

class EnterAttendanceFragment : Fragment() {
    private var _binding : FragmentEnterAttendanceBinding? = null
    private val binding get() = _binding!!
    private val args : EnterAttendanceFragmentArgs by navArgs()
    private lateinit var realm : Realm
    private val atcodemap = mutableMapOf<Long,Int>()

    class RealmAdapter( data: OrderedRealmCollection<Attendance>) :
        RealmRecyclerViewAdapter<Attendance,RealmAdapter.ViewHolder>(data,true){
        private var at_code : Int = 0

        private var listener : ((Long,Int)->Unit)? = null

        fun setOnRadioGroupCheckListener(listener: ((Long,Int)->Unit)){
            this.listener = listener
        }

        class ViewHolder(view : View ) : RecyclerView.ViewHolder( view){
            val textno = view.findViewById<TextView>(R.id.textAttNo)
            val textname = view.findViewById<TextView>(R.id.textAttName)
            val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.attendance_item,parent,false)
            return ViewHolder( view )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem( position )
            holder.textno.text = item!!.no.toString()
            holder.textname.text = item!!.st_name

            when( item!!.at_code){
                0-> holder.radioGroup.check(R.id.radio0)
                1-> holder.radioGroup.check(R.id.radio1)
                2-> holder.radioGroup.check(R.id.radio2)
                3-> holder.radioGroup.check(R.id.radio3)
            }

            holder.radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when( checkedId ){
                    R.id.radio0-> at_code = 0
                    R.id.radio1-> at_code = 1
                    R.id.radio2-> at_code = 2
                    R.id.radio3-> at_code = 3
                }
                Log.d("item_id,at_code","${item.id},${at_code}")
                listener!!.invoke( item!!.id, at_code )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEnterAttendanceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val g_name = args.gName
        val sub_name = args.subName
        val year = args.year
        val month = args.month
        val date = args.date
        val timed = args.timed

        realm = Realm.getDefaultInstance()
        var ret = realm.where<Attendance>()
            .equalTo("g_name",g_name)
            .equalTo("sub_name",sub_name)
            .equalTo("year",year)
            .equalTo("month",month)
            .equalTo("date",date)
            .equalTo("timed",timed)
            .findAll()

        if( ret.size == 0 ){
            //１件もなければ作る
            realm.executeTransaction{
                val stret = realm.where<Student>().equalTo("g_name",g_name).findAll()
                for( st in stret ){
                    val maxid = it.where<Attendance>().max("id")
                    val nextid = (maxid?.toLong() ?: 0L ) + 1L
                    val row = it.createObject<Attendance>(nextid)
                    row.g_name = g_name
                    row.at_code = 0
                    row.date = date
                    row.month = month
                    row.no = st.no
                    row.st_id = st.st_id
                    row.st_name = st.st_name
                    row.sub_name = sub_name
                    row.year = year
                    row.timed = timed
                }
            }
            ret = realm.where<Attendance>()
                .equalTo("g_name",g_name)
                .equalTo("sub_name",sub_name)
                .equalTo("year",year)
                .equalTo("month",month)
                .equalTo("date",date)
                .equalTo("timed",timed)
                .findAll()
        }
        //mapにコピー
        ret.forEach {
            atcodemap.put( it.id,it.at_code )
        }
        val adapter = RealmAdapter( ret )
        binding.recyclerAttendance.adapter = adapter
        adapter.setOnRadioGroupCheckListener { id, at_code ->
            //realm.executeTransactionAsync {
            //    val ret = it.where<Attendance>().equalTo("id",id).findFirst()
            //    ret?.at_code = at_code
            //}
            atcodemap[id] = at_code
        }
        binding.recyclerAttendance.layoutManager = LinearLayoutManager( context )


    }
    override fun onDestroy() {
        super.onDestroy()

        for( item in atcodemap){
            realm.executeTransactionAsync {
                val ret = it.where<Attendance>().equalTo("id",item.key).findFirst()
                ret!!.at_code = item.value
            }
        }
        _binding = null
        realm.close()
    }
}