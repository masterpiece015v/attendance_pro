package tokyo.mstp015v.attendance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import tokyo.mstp015v.attendance.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private var _binding : FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate( inflater , container , false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textMainEnterAttendance.setOnClickListener {
            val action = MainFragmentDirections.actionToEnterAttendanceForGroupFragment()
            findNavController().navigate( action )
        }

        binding.textMainCheckAttendance.setOnClickListener {
            val action = MainFragmentDirections.actionToCheckAttendanceForGroupFragment()
            findNavController().navigate( action )
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}