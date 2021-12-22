package tokyo.mstp015v.attendance

import android.app.Dialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import io.realm.OrderedRealmCollection
import io.realm.Realm
import io.realm.RealmRecyclerViewAdapter
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import tokyo.mstp015v.attendance.databinding.FragmentMakeGroupBinding
import tokyo.mstp015v.attendance.realm.Attendance
import tokyo.mstp015v.attendance.realm.Group
import tokyo.mstp015v.attendance.realm.TimeTable


class MakeGroupFragment : Fragment() {
    //バインディングプロパティ
    private var _binding : FragmentMakeGroupBinding? = null
    private val binding get() = _binding!!
    private lateinit var realm : Realm

    //RealmAdapterクラス
    class RealmAdapter(data : OrderedRealmCollection<Group>) :
        RealmRecyclerViewAdapter<Group,RealmAdapter.ViewHolder >(data,true) {

        private var listener : ((id:Long)->Unit)? = null
        //タップした時のイベントを登録
        fun setOnItemClickListener(listener:(id:Long)->Unit){
            this.listener = listener
        }

        class ViewHolder(view:View) : RecyclerView.ViewHolder(view){
            val textGroupId: TextView = view.findViewById(R.id.textItem3_1)
            val textGroupName:TextView = view.findViewById(R.id.textItem3_2)
            val textMentor:TextView = view.findViewById(R.id.textItem3_3)
            val linear:LinearLayout = view.findViewById(R.id.linear3)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val view = inflater.inflate( R.layout.text_item_3 ,parent,false )
            return ViewHolder( view )

        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item : Group? = getItem( position )
            holder.textGroupId.text = item!!.id.toString()
            holder.textGroupName.text = item!!.g_name
            holder.textMentor.text = item!!.mentor

            holder.linear.setOnClickListener{
                listener?.invoke( item.id )
            }
        }

        //スワイプで削除
        fun swipedelete(context: Context,listener:(id:Long,g_name:String)->Unit ) = ItemTouchHelper( object:ItemTouchHelper.SimpleCallback(ItemTouchHelper.ACTION_STATE_IDLE,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item : Group? = getItem( viewHolder.adapterPosition )
                Log.d("swipe",item!!.id.toString() )

                listener.invoke( item!!.id, item.g_name )
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

    //追加
    class AddGroupDialog(val okSelected:(g_name:String,mentor:String)->Unit,val cancelSelected:()->Unit): DialogFragment(){

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val edit1 = EditText(this.context).apply{
                this.hint="クラス名"
                this.width = 600
                this.textSize = 24.0f
            }
            val edit2 = EditText(this.context).apply{
                this.hint="担当者名"
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
                this.setPositiveButton("追加"){ dialog,which->
                    okSelected.invoke(edit1.text.toString(),edit2.text.toString())

                }
                this.setNegativeButton("戻る"){dialog,which->
                    cancelSelected()
                }
            }

            return builder.create()
        }

    }

    //更新
    class EditGroupDialog(val id :Long, val g_name : String,val mentor:String,val okSelected:(id:Long,g_name:String,mentor:String)->Unit,val cancelSelected:()->Unit): DialogFragment(){
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val edit1 = EditText(this.context).apply{
                this.hint="クラス名"
                this.setText( g_name )
                this.width = 600
                this.textSize = 24.0f
            }
            val edit2 = EditText(this.context).apply{
                this.hint="担当者名"
                this.setText( mentor )
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
                    okSelected.invoke(id, edit1.text.toString(), edit2.text.toString())

                }
                this.setNegativeButton("戻る"){dialog,which->
                    cancelSelected()
                }
            }

            return builder.create()
        }

    }

    //オーバーライドメソッド
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentMakeGroupBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //recyclerViewの設定
        realm = Realm.getDefaultInstance()
        val group = realm.where<Group>().findAll()
        Log.d("groupの件数" , group.size.toString())
        val adapter = RealmAdapter( group )
        binding.recyclerMakeGroup.adapter = adapter

        //adapterをタップすると更新できる
        adapter.setOnItemClickListener {
            val result = realm.where<Group>().equalTo("id",it ).findFirst()
            val dialog = EditGroupDialog(result!!.id,result!!.g_name,result!!.mentor,{ id,g_name,mentor->

                realm.executeTransaction { db->
                    val ret = db.where<Group>().equalTo("id",id).findFirst()
                    ret!!.g_name = g_name
                    ret!!.mentor = mentor
                }

                //キャンセルの時のプログラム
                Snackbar.make(binding.root,"更新しました。",Snackbar.LENGTH_SHORT).show()
                //adapter.notifyDataSetChanged()
            },{
                //キャンセルの時のプログラム
                Snackbar.make(binding.root,"戻る",Snackbar.LENGTH_SHORT).show()
            })

            dialog.show(parentFragmentManager,"dialog")
        }

        //スワイプすると消せる
        adapter.swipedelete( requireContext()) { id, g_name ->
            realm.executeTransaction {
                val ret1 = it.where<Group>().equalTo("id", id).findAll()
                ret1.deleteAllFromRealm()
                //val ret2 = it.where<TimeTable>().equalTo("g_name",g_name).findAll()
                //ret2.deleteAllFromRealm()
                //val ret3 = it.where<Attendance>().equalTo("g_name",g_name).findAll()
                //ret3.deleteAllFromRealm()
            }
        }.attachToRecyclerView(binding.recyclerMakeGroup)

        binding.recyclerMakeGroup.layoutManager = LinearLayoutManager( this.context )
        //フローティングボタンの設定
        binding.buttonGroupAdd.setOnClickListener {
            val dialog = AddGroupDialog({g_name,mentor->
                //OKの時のプログラム
                realm.executeTransaction{
                    val maxid = it.where<Group>().max("id")
                    val nextid = (maxid?.toLong() ?: 0L) + 1L
                    val g = it.createObject<Group>( nextid )
                    g.g_name = g_name
                    g.mentor = mentor

                    //時間割も作る
                    val t_maxid = it.where<TimeTable>().max("id")
                    var t_nextid = (t_maxid?.toLong() ?: 0L) + 1L
                    val days = arrayListOf<String>("月","火","水","木","金","土")
                    val timeds = arrayListOf<Int>(1,2,3,4,5,6,7,8)

                    days.forEach { d->
                        timeds.forEach{ t->
                            val table = it.createObject<TimeTable>(t_nextid)
                            table.g_name = g_name
                            table.day = d
                            table.timed = t
                            t_nextid++
                        }
                    }

                }
                //キャンセルの時のプログラム
                Snackbar.make(binding.root,"追加しました",Snackbar.LENGTH_SHORT).show()
            },{
                //キャンセルの時のプログラム
                Snackbar.make(binding.root,"戻る",Snackbar.LENGTH_SHORT).show()
            })

            dialog.show(parentFragmentManager,"dialog")
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        realm.close()
    }
}