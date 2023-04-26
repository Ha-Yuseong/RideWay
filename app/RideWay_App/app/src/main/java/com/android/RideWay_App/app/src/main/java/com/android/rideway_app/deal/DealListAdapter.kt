package com.android.rideway_app.deal

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.rideway_app.databinding.DealListBinding
import com.android.rideway_app.retrofit.RetrofitClient
import com.android.rideway_app.retrofit.deal.DealListResponse
import com.android.rideway_app.retrofit.deal.DealService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class DealListAdapter(val data: List<DealListResponse.Content>) : RecyclerView.Adapter<DealListAdapter.MyViewHolder>() {

    inner class MyViewHolder(binding : DealListBinding) : RecyclerView.ViewHolder(binding.root){
        var bind = binding
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DealListAdapter.MyViewHolder {
        var binding : DealListBinding = DealListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: DealListAdapter.MyViewHolder, position: Int) {
        var content = data[position]
        val formatter = DecimalFormat("###,###")
        CoroutineScope(Dispatchers.IO).launch {
            val imgString = async {
                val list = getImageList(content.dealId.toLong())
                if(list.isEmpty()) null
                else list[0]
            }

            launch (Dispatchers.Main){
                val img = imgString.await()
                if(img != null) {
                    val content = Base64.decode(img, Base64.DEFAULT);
                    val bmp = BitmapFactory.decodeByteArray(content, 0, content.size)
                    holder.bind.itemImage.setImageBitmap(bmp)
                }
            }
        }
        holder.bind.tvTitle.text = content.title
        holder.bind.tvNickname.text = "유저 : "+ content.userNickname
        holder.bind.tvVisited.text = "조회수 : " + content.visited.toString()
        holder.bind.tvPrice.text = "가격 : ${formatter.format(content.price)}원"

        holder.bind.dealListLayout.setOnClickListener {
            val intent = Intent(holder.bind.root.context,DealDetailActivity::class.java)
            intent.putExtra("dealId",content.dealId.toLong())
            holder.bind.root.context.startActivity(intent)
        }
    }

    private suspend fun getImageList(dealId : Long) : List<String> {
        val retrofitAPI = RetrofitClient.getInstance().create(DealService::class.java)
        return retrofitAPI.getDealImageCoroutine(dealId)
    }
}