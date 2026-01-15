package at.pulseone.app

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.journeyapps.barcodescanner.camera.CameraSettings

class CustomScannerActivity : AppCompatActivity() {

    private lateinit var capture: CaptureManager
    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private lateinit var switchCameraButton: ImageButton
    private lateinit var torchButton: ImageButton

    private var isTorchOn = false
    private var rearCameraId: String? = null
    private var frontCameraId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_scanner_layout)

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)
        switchCameraButton = findViewById(R.id.switch_camera_button)
        torchButton = findViewById(R.id.torch_button)

        if (!hasFlash()) {
            torchButton.visibility = View.GONE
        }

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        for (cameraId in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                CameraCharacteristics.LENS_FACING_BACK -> rearCameraId = cameraId
                CameraCharacteristics.LENS_FACING_FRONT -> frontCameraId = cameraId
            }
        }

        if (rearCameraId == null || frontCameraId == null) {
            switchCameraButton.visibility = View.GONE
        }

        capture = CaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)

        // 1. Only scan for QR codes to be faster
        val formats = listOf(BarcodeFormat.QR_CODE)
        barcodeScannerView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)

        // 2. Configure camera settings for continuous focus
        barcodeScannerView.barcodeView.cameraSettings.apply {
            isContinuousFocusEnabled = true
            isAutoFocusEnabled = true
            focusMode = CameraSettings.FocusMode.CONTINUOUS
            requestedCameraId = rearCameraId?.toIntOrNull() ?: 0 // Start with the rear camera
        }

        // 3. Limit the decoding area to a central square for much faster processing.
        // We post this to ensure the view has been measured and laid out.
        barcodeScannerView.post {
            val size = (barcodeScannerView.width * 0.6).toInt() // Define a square that's 60% of the view width
            val left = (barcodeScannerView.width - size) / 2
            val top = (barcodeScannerView.height - size) / 2
            val framingRect = Rect(left, top, left + size, top + size)
        }


        val cancelButton: Button = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener { finish() }

        switchCameraButton.setOnClickListener { switchCamera() }
        torchButton.setOnClickListener { toggleTorch() }
    }

    private fun hasFlash(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun switchCamera() {
        if (rearCameraId == null || frontCameraId == null) return

        barcodeScannerView.pause()
        val settings = barcodeScannerView.barcodeView.cameraSettings

        val currentId = settings.requestedCameraId
        val rearIdInt = rearCameraId!!.toInt()
        val frontIdInt = frontCameraId!!.toInt()

        settings.requestedCameraId = if (currentId == rearIdInt) frontIdInt else rearIdInt

        barcodeScannerView.barcodeView.cameraSettings = settings
        barcodeScannerView.resume()
    }

    private fun toggleTorch() {
        if (isTorchOn) {
            barcodeScannerView.setTorchOff()
        } else {
            barcodeScannerView.setTorchOn()
        }
        isTorchOn = !isTorchOn
    }

    override fun onResume() {
        super.onResume()
        try {
            capture.onResume()
            capture.decode()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
