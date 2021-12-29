package tokyo.mstp015v.attendance_pro

import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import tokyo.mstp015v.attendance_pro.databinding.FragmentMakeStudentBinding
import tokyo.mstp015v.attendance_pro.realm.Student

class MakeStudentFragment : Fragment() {
    //リサイクラービューのアダプター
    class RealmAdapter( data : OrderedRealmCollection<Student>): RealmRecyclerViewAdapter<Student,RealmAdapter.ViewHolder>(data,true){

        private var listener : ((id:Long)->Unit)? = null
        //タップした時のイベントを登録
        fun setOnItemClickListener(listener:(id:Long)->Unit){
            this.listener = listener
        }

        class ViewHolder(view:View) : RecyclerView.ViewHolder(view){
            val textGroupId: TextView = view.findViewById(R.id.textItem3_1)
            val textGroupName: TextView = view.findViewById(R.id.textItem3_2)
            val textMentor: TextView = view.findViewById(R.id.textItem3_3)
            val linear: LinearLayout = view.findViewById(R.id.linear3)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate( R.layout.text_item_3 ,parent,false )
            return ViewHolder( view )

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item : Student? = getItem( position )
            holder.textGroupId.text = item!!.no.toString()
            holder.textGroupName.text = item!!.st_id
            holder.textMentor.text = item!!.st_name

            holder.linear.setOnClickListener{
                listener?.invoke( item.id )
            }
        }

        fun swipedelete(context: Context, realm : Realm) = ItemTouchHelper( object:
            ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item : Student? = getItem( viewHolder.adapterPosition )

                val result = realm.where<Student>().equalTo("id",item!!.id).findAll()
                realm.executeTransaction{
                    result.deleteAllFromRealm()
                }

            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive)
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.RED)
                val deleteIcon = AppCompatResources.getDrawable(
                    context,
                    R.drawable.ic_baseline_delete_24
                )
                val iconMarginVertical =
                    (viewHolder.itemView.height - deleteIcon!!.intrinsicHeight) / 2

                deleteIcon.setBounds(
                    itemView.left + iconMarginVertical,
                    itemView.top + iconMarginVertical,
                    itemView.left + iconMarginVertical + deleteIcon.intrinsicWidth,
                    itemView.bottom - iconMarginVertical
                )
                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.right + dX.toInt(),
                    itemView.bottom
                )
                background.draw( c )
                deleteIcon.draw( c )
            }
        })
    }

    //入力ダイアログ
    //追加
    class AddStudentDialog(val okSelected:(st_id:String,st_name:String,no:Int)->Unit,val cancelSelected:()->Unit): DialogFragment(){
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val edit1 = EditText(this.context).apply{
                this.hint="学籍番号"
                this.width = 600
                this.textSize = 24.0f
            }
            val edit2 = EditText(this.context).apply{
                this.hint="学生名"
                this.width = 600
                this.textSize = 24.0f
            }

            val edit3 = EditText( this.context ).apply{
                this.hint="出席番号"
                this.width = 600
                this.textSize = 24.0f
                this.inputType = InputType.TYPE_NUMBER_VARIATION_NORMAL
            }
            val linear = LinearLayout( this.context ).apply{
                this.addView( edit1 )
                this.addView( edit2 )
                this.addView( edit3 )
                this.orientation = LinearLayout.VERTICAL
            }

            val builder = AlertDialog.Builder( requireActivity()).apply{
                this.setView( linear )
                this.setPositiveButton("追加"){ dialog,which->
                    try{
                        okSelected.invoke(
                            edit1.text.toString(),edit2.text.toString(),edit3.text.toString().toInt() )
                    }catch(e:Exception){

                    }


                }
                this.setNegativeButton("戻る"){dialog,which->
                    cancelSelected()
                }
            }

            return builder.create()
        }

    }

    //更新
    class EditStudentDialog(
        val id:Long, val st_id: String, val st_name:String, val no: Int,
        val okSelected: (id:Long,st_id:String,st_name:String,no:Int) -> Unit,
        val cancelSelected:()->Unit): DialogFragment(){
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val edit1 = EditText(this.context).apply{
                this.hint="学生番号"
                this.setText( st_id )
                this.width = 600
                this.textSize = 24.0f
            }
            val edit2 = EditText(this.context).apply{
                this.hint="学生名"
                this.setText( st_name )
                this.width = 600
                this.textSize = 24.0f
            }
            val edit3 = EditText( this.context ).apply{
                this.hint="出席番号"
                this.setText( no.toString() )
                this.width = 600
                this.textSize = 24.0f
            }
            val linear = LinearLayout( this.context ).apply{
                this.addView( edit1 )
                this.addView( edit2 )
                this.addView( edit3 )
                this.orientation = LinearLayout.VERTICAL
            }

            val builder = AlertDialog.Builder( requireActivity()).apply{
                this.setView( linear )
                this.setPositiveButton("更新"){ dialog,which->
                    okSelected.invoke(id, edit1.text.toString(), edit2.text.toString() ,edit3.text.toString().toInt() )

                }
                this.setNegativeButton("戻る"){dialog,which->
                    cancelSelected()
                }
            }

            return builder.create()
        }
    }

    private var _binding : FragmentMakeStudentBinding? = null
    private val binding get() = _binding!!
    private val args : MakeStudentFragmentArgs by navArgs()
    private lateinit var realm : Realm

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMakeStudentBinding.inflate( inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        realm = Realm.getDefaultInstance()
        val result = realm.where<Student>().equalTo("g_name",args.gName).findAll()

        val adapter = RealmAdapter( result )
        //アダプターにイベントを登録
        adapter.setOnItemClickListener {
            val result = realm.where<Student>().equalTo("id",it ).findFirst()

            val dialog = EditStudentDialog(
                result!!.id,result!!.st_id,result!!.st_name,result!!.no,{ id, st_id, st_name, no ->

                    realm.executeTransaction {
                        val edit = it.where<Student>().equalTo( "id", id ).findFirst()
                        edit!!.st_id = st_id
                        edit!!.st_name = st_name
                        edit!!.no = no
                    }
                    Snackbar.make( binding.root , "更新しました" , Snackbar.LENGTH_SHORT ).show()

            }) {
                Snackbar.make( binding.root , "キャンセルしました" , Snackbar.LENGTH_SHORT ).show()
            }

            dialog.show(parentFragmentManager,"dialog")

        }
        binding.recyclerMakeStudent.adapter = adapter
        binding.recyclerMakeStudent.layoutManager = LinearLayoutManager( context )

        adapter.swipedelete( requireContext() ,realm ).attachToRecyclerView(binding.recyclerMakeStudent)
        //学生追加
        binding.buttonAddStudent.setOnClickListener {
            val dialog = AddStudentDialog({st_id,st_name,no->
                realm.executeTransaction {

                    val maxid = it.where<Student>().max("id")
                    val nextid = (maxid?.toLong() ?: 0L) + 1L
                    val result = it.createObject<Student>( nextid )
                    result.st_id = st_id
                    result.st_name = st_name
                    result.g_name = args.gName
                    result.no = no
                }
            },{
                Snackbar.make(binding.root,"キャンセルしました",Snackbar.LENGTH_SHORT).show()

            })
            dialog.show(parentFragmentManager,"dialog")
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}