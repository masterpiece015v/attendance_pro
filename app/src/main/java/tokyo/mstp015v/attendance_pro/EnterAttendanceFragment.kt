package tokyo.mstp015v.attendance_pro

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import tokyo.mstp015v.attendance_pro.databinding.FragmentEnterAttendanceBinding
import tokyo.mstp015v.attendance_pro.realm.Attendance
import tokyo.mstp015v.attendance_pro.realm.Student

class EnterAttendanceFragment : Fragment() {
    //更新
    class EditSubNameSubMentorDialog(val sub_name : String,
                                     val sub_mentor: String,
                                     val okSelected:(g_name:String,mentor:String)->Unit,val cancelSelected:()->Unit): DialogFragment(){
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val edit1 = EditText(this.context).apply{
                this.hint="科目名"
                this.setText( sub_name )
                this.width = 600
                this.textSize = 24.0f
            }
            val edit2 = EditText(this.context).apply{
                this.hint="担当者名"
                this.setText( sub_mentor )
                this.width = 600
                this.textSize = 24.0f
            }
            val linear = LinearLayout( this.context ).apply{
                this.addView( edit1 )
                this.addView( edit2 )
                this.orientation = LinearLayout.VERTICAL
            }

            val builder = AlertDialog.Builder( requireActivity()).apply{
                this.setView( linear )
                this.setPositiveButton("更新"){ dialog,which->
                    okSelected.invoke(edit1.text.toString(), edit2.text.toString())

                }
                this.setNegativeButton("戻る"){dialog,which->
                    cancelSelected()
                }
            }

            return builder.create()
        }

    }

    private var _binding : FragmentEnterAttendanceBinding? = null
    private val binding get() = _binding!!
    private val args : EnterAttendanceFragmentArgs by navArgs()
    private lateinit var realm : Realm
    private val atcodemap = mutableMapOf<Long,Int>()

    class RealmAdapter( data: OrderedRealmCollection<Attendance>) :
        RealmRecyclerViewAdapter<Attendance,RealmAdapter.ViewHolder>(data,false){
        private var at_code : Int = 0

        private var listener : ((Long,Int)->Unit)? = null

        fun setOnRadioGroupCheckListener(listener: ((Long,Int)->Unit)){
            this.listener = listener
        }

        class ViewHolder(view : View ) : RecyclerView.ViewHolder( view){
            val textno = view.findViewById<TextView>(R.id.textAttNo)
            val textname = view.findViewById<TextView>(R.id.textAttName)
            val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
            //val buttonTrush = view.findViewById<ImageButton>(R.id.buttonAttendanceRowTrush)
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

    private lateinit var g_name : String
    private var year : Int = 0
    private var month : Int =0
    private var date : Int = 0
    private var timed : Int = 0
    private lateinit var day : String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("OnViewCreated","EnterAttendanceFragment")
        super.onViewCreated(view, savedInstanceState)
        realm = Realm.getDefaultInstance()
        g_name = args.gName
        val sub_name = args.subName
        year = args.year
        month = args.month
        date = args.date
        timed = args.timed
        day = args.day
        val sub_mentor = args.subMentor
        //年月日を表示する
        binding.textEnterAttendance.text = "${year}年${month}月${date}(${day})"
        //科目名などを表示する
        binding.textEnterAttendanceTimed.text = "${getString(R.string.timed)}:${timed}"
        binding.textSubNameSubMentor.text = "${getString(R.string.subject)}:${sub_name}\n${getString(R.string.mentor)}:${sub_mentor}"
        //科目名や担当者名を変えることができる
        binding.linearSubNameSubMentor.setOnClickListener {
            val dialog = EditSubNameSubMentorDialog(sub_name,sub_mentor,{sub_name,sub_mentor->
                realm.executeTransactionAsync {
                    val editRet = it.where<Attendance>()
                        .equalTo("g_name",g_name)
                        .equalTo("year",year)
                        .equalTo("month",month)
                        .equalTo("date",date)
                        .equalTo("timed",timed)
                        .findAll()

                    editRet.forEach{ att ->
                        att.sub_name = sub_name
                        att.sub_mentor = sub_mentor
                        Log.d("edit",att.sub_name)
                        Log.d("edit",att.sub_mentor)
                    }
                }
                binding.textSubNameSubMentor.text = "${getString(R.string.subject)}:${sub_name}\n${getString(R.string.mentor)}:${sub_mentor}"
            },{})
            dialog.show(parentFragmentManager,"dialog")
        }

        var ret = realm.where<Attendance>()
            .equalTo("g_name",g_name)
            .equalTo("year",year)
            .equalTo("month",month)
            .equalTo("date",date)
            .equalTo("timed",timed)
            .findAll()
        //Log.d("ymd","${year},${month},${date},${timed}")
        //Log.d("size" , ret.size.toString())
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
                    row.sub_mentor = sub_mentor
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
            atcodemap[id] = at_code
        }
        //adapter.setOnImageButtonListener { id ->
        //    realm.executeTransaction{
        //        val ret = it.where<Attendance>().equalTo("id",id).findFirst()
        //        ret!!.deleteFromRealm()
        //    }
        //}
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
        atcodemap.clear()
    }

}