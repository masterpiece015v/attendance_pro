package tokyo.mstp015v.attendance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.where
import tokyo.mstp015v.attendance.databinding.FragmentCheckAttendanceForStudentBinding
import tokyo.mstp015v.attendance.realm.Group
import tokyo.mstp015v.attendance.realm.Student

class CheckAttendanceForStudentFragment : Fragment() {
    class RealmAdapter( data : OrderedRealmCollection<Student>)
        : RealmRecyclerViewAdapter<Student, RealmAdapter.ViewHolder>(data,true){

        private var listener : ((g_name : String)->Unit )? = null

        fun setOnItemClickListener( listener : ((g_name:String)->Unit)){
            this.listener = listener
        }

        class ViewHolder(view: View): RecyclerView.ViewHolder(view){
            val textno: TextView? = view.findViewById(R.id.textItem3_1)
            val textid: TextView? = view.findViewById(R.id.textItem3_2)
            val textname: TextView? = view.findViewById(R.id.textItem3_3)
            val linear : LinearLayout? = view.findViewById(R.id.linear3 )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate( R.layout.text_item_3 ,parent,false )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item : Student? = getItem( position )
            holder.textno!!.text = item?.no.toString()
            holder.textid!!.text = item?.st_id
            holder.textname!!.text = item?.st_name
            holder.linear!!.setOnClickListener {
                listener!!.invoke( item!!.st_id )
            }
        }
    }

    private var _binding : FragmentCheckAttendanceForStudentBinding? = null
    private val binding get() = _binding!!
    private val realm = Realm.getDefaultInstance()
    private val args : CheckAttendanceForStudentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCheckAttendanceForStudentBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ret = realm.where<Student>().equalTo("g_name",args.gName).findAll()
        val adapter = RealmAdapter(ret)
        binding.recyclerCheckAttendanceForStudent.adapter = adapter
        adapter.setOnItemClickListener {
            val action = CheckAttendanceForStudentFragmentDirections.actionToCheckAttendanceFragment(it)
            findNavController().navigate( action )
        }
        binding.recyclerCheckAttendanceForStudent.layoutManager = LinearLayoutManager( context )



    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        realm.close()
    }
}