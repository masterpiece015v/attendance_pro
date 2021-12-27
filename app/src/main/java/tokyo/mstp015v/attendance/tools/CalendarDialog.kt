package tokyo.mstp015v.attendance.tools

import android.app.Dialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import tokyo.mstp015v.attendance.R
import java.util.*

class CalendarDialog ( val g_name : String, var year:Int , var month:Int,var date:Int,val list:MutableList<String>,
                       val okSelected:(year:Int,month:Int,date:Int )->Unit,
                       val cancelSelected:()->Unit): DialogFragment(){

    val days = listOf<String>("日","月","火","水","木","金","土")
    var oldTextView : TextView? = null
    var oldCheck = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        //カレンダー
        val calendarContainer = LinearLayout( this.context ).apply{
            this.orientation = LinearLayout.VERTICAL
        }
        //一番外のlayout
        val container = LinearLayout( this.context ).apply{
            this.orientation = LinearLayout.VERTICAL
            this.setPadding(40,40,40,40)
        }
        //年月を表示
        var textTitle = TextView(this.context).apply{
            this.text = "${year}年 ${month}月"
            this.textSize = 24.0f
            this.setPadding(5,5,5,5)
        }
        val title = LinearLayout(this.context).apply{
            this.orientation = LinearLayout.HORIZONTAL
            this.addView( ImageButton(this.context).apply{
                this.setImageResource(R.drawable.ic_baseline_keyboard_arrow_left_24)
                this.setOnClickListener{
                    if( month <= 1 ){
                        month = 12
                        year = year - 1
                    }else{
                        month = month - 1
                    }
                    setCalendar(calendarContainer,year,month)
                    textTitle.text = "${year}年 ${month}月"
                }
            })
            this.addView( textTitle )
            this.addView( ImageButton(this.context).apply{
                this.setImageResource(R.drawable.ic_baseline_keyboard_arrow_right_24)
                this.setOnClickListener{
                    if( month >= 12 ){
                        month = 1
                        year = year + 1
                    }else{
                        month = month + 1
                    }
                    setCalendar(calendarContainer,year,month )
                    textTitle.text = "${year}年 ${month}月"
                }
            })
        }
        container.addView( title )
        //曜日の名前
        val head = LinearLayout( this.context ).apply{
            this.orientation = LinearLayout.HORIZONTAL
            days.forEach{
                this.addView( TextView(this.context).apply{
                    this.width = 120
                    this.text = it
                    this.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                })
            }
        }
        container.addView( head )
        //カレンダーを作る
        setCalendar( calendarContainer , year,month , )
        container.addView( calendarContainer )

        val builder = AlertDialog.Builder( requireActivity()).apply{
            this.setView( container )
            this.setPositiveButton("OK"){ dialog,which->
                okSelected.invoke(year, month, date)
            }
            this.setNegativeButton("Cancel"){dialog,which->
                //cancelSelected()
            }
        }

        return builder.create()
    }

    fun setCalendar( layout : LinearLayout,year:Int,month:Int ){
        layout.removeAllViews()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR,year)
        calendar.set(Calendar.MONTH,month-1)
        calendar.set(Calendar.DATE,1)

        val firstDay = calendar.get(Calendar.DAY_OF_WEEK)
        val maxday = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        var d = 1
        for( i in 0..6){
            val row = LinearLayout( this.context ).apply{
                this.orientation = LinearLayout.HORIZONTAL
                this.setPadding( 0,2,0,2)
            }
            for( j in 0..6 ){
                if( i == 0 && j < firstDay-1 || d > maxday){
                    val col = TextView(this.context).apply{
                        this.width = 120
                        this.height = 120
                        //this.setPadding(2,2,2,2)
                    }
                    row.addView( col )
                }else{
                    val col = TextView(this.context ).apply{
                        this.text = d.toString()
                        this.width = 120
                        this.height = 120
                        this.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
                        this.setPadding(4,35,4,4)
                        if( "${year}${month}${d}" in list ){
                            //this.setBackgroundColor(Color.YELLOW)
                            this.setBackgroundResource(R.drawable.radius_style_light_yellow)
                        }
                        if( "${year}${month}${date}" == "${year}${month}${d}"){
                            this.setBackgroundResource(R.drawable.radius_style_light_red)
                            oldTextView = this
                            if( "${year}${month}${d}" in list ){
                                //this.setBackgroundColor(Color.YELLOW)
                                oldCheck = true
                            }
                        }
                        this.setOnClickListener{

                            if( oldCheck ) {
                                oldTextView?.setBackgroundResource(R.drawable.radius_style_light_yellow)
                            }else{
                                oldTextView?.background = null
                            }

                            oldCheck = ( "${year}${month}${this.text}" in list)

                            this.setBackgroundResource(R.drawable.radius_style_light_red)
                            oldTextView = this

                            date = this.text.toString().toInt()
                        }
                    }
                    d++
                    row.addView( col )
                }
            }
            layout.addView( row )
        }
    }

}