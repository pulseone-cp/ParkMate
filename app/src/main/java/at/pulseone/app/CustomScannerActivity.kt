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
import android.widget.Toast
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
    private var useBackCamera = true // Track which camera we're using
    private var hasMultipleCameras = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_scanner_layout)

        barcodeScannerView = findViewById(R.id.zxing_barcode_scanner)
        switchCameraButton = findViewById(R.id.switch_camera_button)
        torchButton = findViewById(R.id.torch_button)

        if (!hasFlash()) {
            torchButton.visibility = View.GONE
        }

        // Check if device has multiple cameras
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            var backCameraCount = 0
            var frontCameraCount = 0
            
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                when (characteristics.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_BACK -> backCameraCount++
                    CameraCharacteristics.LENS_FACING_FRONT -> frontCameraCount++
                }
            }
            
            hasMultipleCameras = backCameraCount > 0 && frontCameraCount > 0
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Hide switch camera button if we don't have both front and back cameras
        if (!hasMultipleCameras) {
            switchCameraButton.visibility = View.GONE
        }

        // 1. Only scan for QR codes to be faster
        val formats = listOf(BarcodeFormat.QR_CODE)
        barcodeScannerView.barcodeView.decoderFactory = DefaultDecoderFactory(formats)

        // 2. Configure camera settings BEFORE initializing capture manager
        // Use 0 for back camera (standard convention)
        val cameraSettings = CameraSettings()
        cameraSettings.requestedCameraId = 0 // 0 is typically the back camera
        cameraSettings.isAutoFocusEnabled = true
        cameraSettings.isContinuousFocusEnabled = true
        cameraSettings.focusMode = CameraSettings.FocusMode.CONTINUOUS
        
        barcodeScannerView.barcodeView.cameraSettings = cameraSettings

        // 3. Initialize capture manager AFTER camera settings are configured
        capture = CaptureManager(this, barcodeScannerView)
        capture.initializeFromIntent(intent, savedInstanceState)

        // 4. Limit the decoding area to a central square for much faster processing.
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
        if (!hasMultipleCameras) return

        try {
            // Turn off torch if it's on
            if (isTorchOn) {
                barcodeScannerView.setTorchOff()
                isTorchOn = false
            }

            // Use BarcodeView's pause/resume instead of CaptureManager
            barcodeScannerView.barcodeView.stopDecoding()
            barcodeScannerView.pause()
            
            // Small delay to ensure camera is released
            barcodeScannerView.postDelayed({
                try {
                    // Toggle camera preference
                    useBackCamera = !useBackCamera
                    
                    // Create new camera settings
                    val newSettings = CameraSettings()
                    // 0 is typically back camera, 1 is typically front camera
                    newSettings.requestedCameraId = if (useBackCamera) 0 else 1
                    newSettings.isAutoFocusEnabled = true
                    newSettings.isContinuousFocusEnabled = true
                    newSettings.focusMode = CameraSettings.FocusMode.CONTINUOUS
                    
                    // Apply the new settings
                    barcodeScannerView.barcodeView.cameraSettings = newSettings
                    
                    // Restart camera with new settings
                    barcodeScannerView.resume()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error switching camera: ${e.message}", Toast.LENGTH_SHORT).show()
                    
                    // Try to recover by switching back
                    try {
                        useBackCamera = !useBackCamera
                        val fallbackSettings = CameraSettings()
                        fallbackSettings.requestedCameraId = if (useBackCamera) 0 else 1
                        fallbackSettings.isAutoFocusEnabled = true
                        fallbackSettings.isContinuousFocusEnabled = true
                        fallbackSettings.focusMode = CameraSettings.FocusMode.CONTINUOUS
                        barcodeScannerView.barcodeView.cameraSettings = fallbackSettings
                        barcodeScannerView.resume()
                    } catch (resumeException: Exception) {
                        resumeException.printStackTrace()
                        Toast.makeText(this, "Camera error. Please restart the app.", Toast.LENGTH_LONG).show()
                    }
                }
            }, 100) // 100ms delay
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error switching camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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
