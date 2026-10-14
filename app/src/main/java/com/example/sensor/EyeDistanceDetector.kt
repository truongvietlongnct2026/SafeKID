package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.model.DistanceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EyeDistanceDetector(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val proximitySensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val _distanceState = MutableStateFlow<DistanceState>(DistanceState.Safe(38))
    val distanceState: StateFlow<DistanceState> = _distanceState.asStateFlow()

    private var safeThresholdCm: Int = 30
    private var isDetectionActive: Boolean = false

    // Tone & Vibrator
    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun startListening(thresholdCm: Int = 30) {
        safeThresholdCm = thresholdCm
        isDetectionActive = true
        proximitySensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        isDetectionActive = false
        sensorManager?.unregisterListener(this)
    }

    fun setSafeThreshold(thresholdCm: Int) {
        safeThresholdCm = thresholdCm
    }

    fun updateSimulatedDistance(distanceCm: Int, soundEnabled: Boolean, vibrateEnabled: Boolean) {
        val newState = evaluateDistance(distanceCm)
        _distanceState.value = newState
        if (newState is DistanceState.TooClose) {
            triggerAlerts(soundEnabled, vibrateEnabled)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isDetectionActive || event == null) return
        if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
            val rawValue = event.values[0]
            val maxRange = event.sensor.maximumRange

            // Proximity sensors typically return 0 to maxRange (often 5.0 cm)
            // If near (e.g. 0 to 3cm), it means the face/phone is dangerously close
            val estimatedDistanceCm = if (rawValue < maxRange && rawValue < 4f) {
                15 // Dangerously close
            } else if (rawValue < maxRange) {
                25 // Caution
            } else {
                38 // Safe distance
            }

            val state = evaluateDistance(estimatedDistanceCm)
            _distanceState.value = state
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun evaluateDistance(distanceCm: Int): DistanceState {
        return when {
            distanceCm < safeThresholdCm - 5 -> DistanceState.TooClose(distanceCm)
            distanceCm <= safeThresholdCm -> DistanceState.Caution(distanceCm)
            else -> DistanceState.Safe(distanceCm)
        }
    }

    fun triggerAlerts(soundEnabled: Boolean, vibrateEnabled: Boolean) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 250)
            } catch (_: Exception) {}
        }
        if (vibrateEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(300)
                }
            } catch (_: Exception) {}
        }
    }

    fun release() {
        stopListening()
        toneGenerator?.release()
        toneGenerator = null
    }
}
