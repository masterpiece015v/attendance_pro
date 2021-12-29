package tokyo.mstp015v.attendance_pro

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.navigation.fragment.findNavController
import tokyo.mstp015v.attendance_pro.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    private var _binding : FragmentSettingBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSettingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //初期化
        val pref = context?.getSharedPreferences("init",Context.MODE_PRIVATE)
        val max_timed = pref?.getInt("max_timed",5)
        val sat_enable = pref?.getBoolean("sat_enable",false)
        binding.seekOneDateTimed.progress = max_timed!!
        binding.textTimed.text = max_timed!!.toString()
        binding.switchSatEnable.isChecked = sat_enable!!

        //時間数のチェンジイベント(SeekBar)
        binding.seekOneDateTimed.setOnSeekBarChangeListener(
            object:SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean,
                ) {
                    binding.textTimed.text = progress.toString()

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }
                //seekbarを離したら共有に書き込む
                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    val pref = context?.getSharedPreferences("init", Context.MODE_PRIVATE)
                    pref?.edit()?.putInt("max_timed", seekBar?.progress!! )?.apply()
                }

            }
        )

        //土曜日の有効化(Switch)
        binding.switchSatEnable.setOnCheckedChangeListener { buttonView, isChecked ->
            val pref =context?.getSharedPreferences("init",Context.MODE_PRIVATE)
            pref?.edit()?.putBoolean("sat_enable",isChecked)?.apply()

        }

        //グループ作成に移動
        binding.textDataMakeGroup.setOnClickListener {
            val action = SettingFragmentDirections.actionToMakeGroupFragment()
            findNavController().navigate( action )
        }
        //学生作成に移動
        binding.textDataMakeStudent.setOnClickListener {
            val action = SettingFragmentDirections.actionToMakeStudentForGroupListFragment()
            findNavController().navigate( action )
        }
        //時間割作成に移動
        binding.textDataMakeTimetable.setOnClickListener {
            val action = SettingFragmentDirections.actionToMakeTimeTableForGroupFragment()
            findNavController().navigate( action )
        }

        //出席入力に移動

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

