package com.cookandroid.s2_archiving.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cookandroid.s2_archiving.*
import com.cookandroid.s2_archiving.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_view.*
import kotlinx.android.synthetic.main.activity_view.view.*

class ViewpageFragment: Fragment() {

    lateinit var adapterV : RecyclerView.Adapter<ViewAdapter.CustomViewHolder>
    lateinit var viewDataList: ArrayList<PostData>
    lateinit var friendId:String

    //xml 연결
    lateinit var ivFriendpProfile:ImageView
    lateinit var tvFriendName : TextView

    // context
    private lateinit var activitys: Activity


    private var mFirebaseAuth: FirebaseAuth? = FirebaseAuth.getInstance() //파이어베이스 인증
    private var mDatabaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Firebase")//실시간 데이터베이스
    private lateinit var fbStorage: FirebaseStorage

    companion object {
        const val TAG : String = "로그"
    }

    // 프레그먼트를 안고 있는 액티비티에 붙었을 때(프래그먼트가 엑티비티에 올라온 순간)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        activitys = context as Activity
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_view,container, false)
        view?.rv_view?.layoutManager = LinearLayoutManager(this.requireContext(), LinearLayoutManager.VERTICAL, false)
        friendId = requireArguments().getString("friend_id").toString()
        viewDataList = ArrayList()
        ivFriendpProfile = view.findViewById(R.id.ivViewProfileImage)
        tvFriendName = view.findViewById(R.id.tvViewName)


        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        mDatabaseRef.child("UserFriends").child("${mFirebaseAuth?.currentUser!!.uid}").child(friendId)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var friend:FriendData? = snapshot.getValue(FriendData::class.java)
                    tvFriendName.text = friend!!.fName
                    if (friend!!.fImgUri == "") {
                        ivFriendpProfile.setImageResource(R.drawable.user)
                    } else { // Uri가 있으면 그 사진 로드하기
                        Glide.with(activitys)
                            .load(friend!!.fImgUri)
                            .into(ivFriendpProfile)
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        mDatabaseRef.child("UserPosts").child("${mFirebaseAuth?.currentUser!!.uid}")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    viewDataList.clear()

                    for (data: DataSnapshot in snapshot.children) {
                        var post:PostData? = data.getValue(PostData::class.java)
                        if(post!!.postFriendId == friendId) {
                            viewDataList.add(post!!)
                        }
                    }
                    adapterV.notifyDataSetChanged() //리스트 저장 및 새로고침

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })



        adapterV = ViewAdapter(viewDataList,this.requireContext(),this)
        rv_view.adapter = adapterV

    }



}