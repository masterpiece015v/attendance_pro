package tokyo.mstp015v.attendance_pro

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.where
import tokyo.mstp015v.attendance_pro.databinding.FragmentMakeTimeTableDayBinding
import tokyo.mstp015v.attendance_pro.realm.TimeTable

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

class MakeTimeTableDayFragment : Fragment() {
    //レルムのアダプター
    class TimeTableAdapter( data:OrderedRealmCollection<TimeTable>):RealmRecyclerViewAdapter<TimeTable,TimeTableAdapter.ViewHolder>(data,true){

        private var listener : ((Long,String?,String?)->Unit)? = null
        private var listener2 : ((Long)->Unit)? = null
        fun setOnItemClickListener( listener : ((Long,String?,String?)->Unit)){
            this.listener = listener
        }
        fun setOnImageTrushClickListener( listener2 : ((Long)->Unit)){
            this.listener2 = listener2
        }
        class ViewHolder( view:View): RecyclerView.ViewHolder( view ){
            val text3_1 = view.findViewById<TextView>(R.id.textTrush3_1)
            val text3_2 = view.findViewById<TextView>(R.id.textTrush3_2)
            val text3_3 = view.findViewById<TextView>(R.id.textTrush3_3)
            val linear3 = view.findViewById<ConstraintLayout>(R.id.linearTrush3 )
            val image3 = view.findViewById<ImageButton>(R.id.imageTrushButton )
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from( parent.context).inflate( R.layout.text_item_3_trush,parent,false)
            return ViewHolder( view )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem( position )
            holder.text3_1.text = item!!.timed.toString()
            holder.text3_2.text = item!!.sub_name
            holder.text3_3.text = item!!.sub_mentor
            holder.linear3.setOnClickListener {
                listener?.invoke( item.id ,item.sub_name,item.sub_mentor)
            }
            holder.image3.setOnClickListener {
                listener2?.invoke( item.id )
            }
        }
    }

    //更新
    class EditGroupDialog(
        val id:Long, val sub_name: String?,
        val sub_mentor:String?,
        val okSelected: (id:Long,sub_name:String?,sub_mentor:String?) -> Unit,
        val cancelSelected:()->Unit): DialogFragment(){
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val edit1 = EditText(this.context).apply{
                this.hint="科目名"
                this.setText( sub_name )
                this.width = 800
                this.textSize = 24.0f
            }
            val edit2 = EditText(this.context).apply{
                this.hint="科目担当者"
                this.setText( sub_mentor )
                this.width = 800
                this.textSize = 24.0f
            }
            val linear = LinearLayout( this.context ).apply{
                this.addView( edit1 )
                this.addView( edit2 )
                this.orientation = LinearLayout.VERTICAL
                this.setPadding(10)

            }
            val builder = AlertDialog.Builder( requireActivity()).apply{
                this.setView( linear )
                this.setPositiveButton("更新"){ dialog,which->
                    okSelected.invoke(id.toString().toLong(), edit1.text.toString() , edit2.text.toString())

                }
                this.setNegativeButton("戻る"){dialog,which->
                    cancelSelected()
                }
            }

            return builder.create()
        }

    }

    // TODO: Rename and change types of parameters
    private var g_name : String? = null
    private var day: String? = null
    private var timed: Int? = null
    private var _binding : FragmentMakeTimeTableDayBinding? = null
    private val binding get() = _binding!!
    private lateinit var realm : Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            g_name = it.getString(ARG_PARAM1)
            day = it.getString(ARG_PARAM2)
            timed = it.getInt(ARG_PARAM3)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMakeTimeTableDayBinding.inflate( inflater,container,false)
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
        binding.recyclerMakeTimeTableDay.adapter = adapter

        adapter.setOnItemClickListener { id,sub_name,sub_mentor->
            val dialog = EditGroupDialog( id ,sub_name,sub_mentor,{ id, sub_name,sub_mentor ->
                realm.executeTransaction {
                    val result = it.where<TimeTable>().equalTo("id",id).findFirst()
                    result!!.sub_name = sub_name ?: ""
                    result!!.sub_mentor = sub_mentor ?: ""
                }
            },{
                Snackbar.make(binding.root,"キャンセルしました。", Snackbar.LENGTH_SHORT).show()
            })
            dialog.show(parentFragmentManager,"dialog")
        }
        adapter.setOnImageTrushClickListener { id->
            realm.executeTransaction {
                Log.d("id" , id.toString())
                val result = it.where<TimeTable>().equalTo("id",id).findFirst()
                result!!.sub_name = ""
                result!!.sub_mentor = ""
            }
        }

        binding.recyclerMakeTimeTableDay.layoutManager = LinearLayoutManager(context)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    companion object {
        fun newInstance(g_name:String,day: String, timed: Int) =
            MakeTimeTableDayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1,g_name)
                    putString(ARG_PARAM2, day)
                    putInt(ARG_PARAM3, timed)
                }
            }
    }
}