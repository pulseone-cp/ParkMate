package at.pulseone.app

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.camera.CameraSettings

class CustomScannerActivity : AppCompatActivity() {

    private lateinit var capture: CaptureManager
    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private lateinit var switchCameraButton: ImageButton
    private lateinit var torchButton: ImageButton

    private var isTorchOn = false

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
        if (cameraManager.cameraIdList.size <= 1) {
            switchCameraButton.visibility = View.GONE
        }

        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            val settings = CameraSettings()
            settings.isContinuousFocusEnabled = true
            settings.requestedCameraId = 0 // Default to rear camera (ID 0)
            barcodeScannerView.barcodeView.cameraSettings = settings
        }

        capture = CaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()

        val cancelButton: Button = findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener { finish() }

        switchCameraButton.setOnClickListener { switchCamera() }
        torchButton.setOnClickListener { toggleTorch() }
    }

    private fun hasFlash(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun switchCamera() {
        barcodeScannerView.pause()
        val settings = barcodeScannerView.barcodeView.cameraSettings

        // Toggle between camera 0 (rear) and 1 (front)
        if (settings.requestedCameraId == 0) {
            settings.requestedCameraId = 1
        } else {
            settings.requestedCameraId = 0
        }

        barcodeScannerView.barcodeView.cameraSettings = settings
        barcodeScannerView.resume()
    }

    private fun toggleTorch() {
        if (isTorchOn) {
            barcodeScannerView.setTorchOff()
            isTorchOn = false
        } else {
            barcodeScannerView.setTorchOn()
            isTorchOn = true
        }
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
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