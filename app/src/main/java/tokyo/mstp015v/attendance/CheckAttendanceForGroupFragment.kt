package tokyo.mstp015v.attendance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.Realm
import io.realm.kotlin.where
import tokyo.mstp015v.attendance.databinding.FragmentCheckAttendanceForGroupBinding
import tokyo.mstp015v.attendance.realm.Group
import tokyo.mstp015v.attendance.tools.GroupRealmAdapter


class CheckAttendanceForGroupFragment : Fragment() {
    private var _binding : FragmentCheckAttendanceForGroupBinding? = null
    private val binding get() = _binding!!
    private val realm = Realm.getDefaultInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentCheckAttendanceForGroupBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ret = realm.where<Group>().findAll()
        val adapter = GroupRealmAdapter(ret)
        binding.recyclerCheckAttendanceForGroup.adapter = adapter
        adapter.setOnItemClickListener {
            val action = CheckAttendanceForGroupFragmentDirections.actionToCheckAttendanceForStudentFragment(it)
            findNavController().navigate( action )
        }
        binding.recyclerCheckAttendanceForGroup.layoutManager = LinearLayoutManager( context )
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        realm.close()
    }
}