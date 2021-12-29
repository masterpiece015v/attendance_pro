package tokyo.mstp015v.attendance_pro.tools

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import tokyo.mstp015v.attendance_pro.R
import tokyo.mstp015v.attendance_pro.realm.Group

class GroupRealmAdapter( data : OrderedRealmCollection<Group>)
        : RealmRecyclerViewAdapter<Group, GroupRealmAdapter.ViewHolder>(data,true){

    private var listener : ((g_name : String)->Unit )? = null

    fun setOnItemClickListener( listener : ((g_name:String)->Unit)){
        this.listener = listener
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val textMakeStudentForGroup: TextView? = view.findViewById(R.id.textItem1_1)
        val linear : LinearLayout? = view.findViewById(R.id.linear1 )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate( R.layout.text_item_1 ,parent,false )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item : Group? = getItem( position )
        holder.textMakeStudentForGroup!!.text = item!!.g_name
        holder.linear!!.setOnClickListener {
            listener!!.invoke( item.g_name )
        }
    }
}