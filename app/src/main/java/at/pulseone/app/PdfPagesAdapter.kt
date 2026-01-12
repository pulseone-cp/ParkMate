package at.pulseone.app

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class PdfPagesAdapter(private val pages: List<Bitmap>) : RecyclerView.Adapter<PdfPagesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pageImageView: ImageView = itemView.findViewById(R.id.pdf_page_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pdf_page_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.pageImageView.setImageBitmap(pages[position])
    }

    override fun getItemCount() = pages.size
}