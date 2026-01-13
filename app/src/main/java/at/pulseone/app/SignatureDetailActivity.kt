package at.pulseone.app

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class SignatureDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature_detail)

        val signaturePath = intent.getStringExtra("SIGNATURE_PATH")
        val imageView: ImageView = findViewById(R.id.signature_detail_image_view)
        val closeButton: ImageButton = findViewById(R.id.close_button)

        if (signaturePath != null) {
            val file = File(signaturePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                imageView.setImageBitmap(bitmap)
            } else {
                Toast.makeText(this, R.string.toast_signature_file_not_found, Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            Toast.makeText(this, R.string.toast_no_signature_path, Toast.LENGTH_SHORT).show()
            finish()
        }

        closeButton.setOnClickListener {
            finish()
        }
    }
}