package tokyo.mstp015v.attendance_pro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.realm.Realm
import io.realm.kotlin.where
import tokyo.mstp015v.attendance_pro.databinding.FragmentMakeStudentForGroupListBinding
import tokyo.mstp015v.attendance_pro.realm.Group
import tokyo.mstp015v.attendance_pro.tools.GroupRealmAdapter

class MakeStudentForGroupListFragment : Fragment() {

    private var _binding : FragmentMakeStudentForGroupListBinding? = null
    private val binding get() = _binding!!
    private lateinit var realm : Realm

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMakeStudentForGroupListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        realm = Realm.getDefaultInstance()
        val result = realm.where<Group>().distinct("g_name").findAll()
        val adapter = GroupRealmAdapter( result )
        binding.recyclerMakeStudentForGroup.adapter = adapter
        adapter.setOnItemClickListener {
            val action = MakeStudentForGroupListFragmentDirections.actionToMakeStudentFragment(it)
            findNavController().navigate( action )
        }
        binding.recyclerMakeStudentForGroup.layoutManager = LinearLayoutManager( context )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        realm.close()
    }
}

