package at.pulseone.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class SignatureActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signature)

        val signatureView: SignatureView = findViewById(R.id.signature_view)
        val acceptButton: Button = findViewById(R.id.accept_button)
        val retryButton: Button = findViewById(R.id.retry_button)
        val abortButton: Button = findViewById(R.id.abort_button)

        retryButton.setOnClickListener {
            signatureView.clear()
        }

        abortButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        acceptButton.setOnClickListener {
            if (signatureView.isBlank()) {
                Toast.makeText(this, "Please sign the document", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val signatureBitmap = Bitmap.createBitmap(signatureView.width, signatureView.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(signatureBitmap)
            signatureView.draw(canvas)

            val signatureFile = File(filesDir, "signature_${UUID.randomUUID()}.png")
            val out = FileOutputStream(signatureFile)
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            val resultIntent = Intent()
            resultIntent.putExtra("SIGNATURE_PATH", signatureFile.absolutePath)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}