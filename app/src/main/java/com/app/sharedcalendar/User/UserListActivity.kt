package  com.app.sharedcalendar.User
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager

import com.app.sharedcalendar.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth

class UserListActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityUserListBinding
    private lateinit var userAdapter: UserAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userAdapter = UserAdapter(emptyList())
        binding.userRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.userRecyclerView.adapter = userAdapter

        loadUserList()
    }

    private fun loadUserList() {
        // Firebase Realtime Database에서 사용자 목록 조회 (가정)
        val userList = mutableListOf<User>()

        // You need to implement the logic to fetch users from the database and add them to the `userList`

        // For example, assuming you have a `UserManager` class:
        val userManager = UserManager()

        userManager.getUserList { users ->
            userList.addAll(users)
            userAdapter.setUserList(userList)
        }
    }
}
