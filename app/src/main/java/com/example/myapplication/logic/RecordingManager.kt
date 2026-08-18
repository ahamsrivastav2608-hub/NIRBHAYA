package com.example.myapplication.logic

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class RecordingManager(private val context: Context) {
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentAudioUri: Uri? = null
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    
    var onRecordingFinalized: ((Uri) -> Unit)? = null
    var onAudioRecordingFinalized: ((Uri) -> Unit)? = null

    fun startRecording(lifecycleOwner: LifecycleOwner, previewView: PreviewView? = null) {
        if (recording != null) return // Already recording

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                
                val useCases = mutableListOf<androidx.camera.core.UseCase>(videoCapture!!)
                
                previewView?.let {
                    val preview = Preview.Builder().build()
                    preview.setSurfaceProvider(it.surfaceProvider)
                    useCases.add(preview)
                }

                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, *useCases.toTypedArray()
                )
                
                recordVideo()
                startAudioRecording()
            } catch (exc: Exception) {
                Log.e("RecordingManager", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun recordVideo() {
        val videoCapture = this.videoCapture ?: return

        val name = "NIRBHAYA_SOS_" + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Nirbhaya-Emergency")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        try {
            recording = videoCapture.output
                .prepareRecording(context, mediaStoreOutputOptions)
                .apply {
                    try {
                        withAudioEnabled()
                    } catch (e: SecurityException) {
                        Log.e("RecordingManager", "Audio recording permission not granted", e)
                    }
                }
                .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                    when(recordEvent) {
                        is VideoRecordEvent.Start -> {
                            Log.d("RecordingManager", "Recording started")
                        }
                        is VideoRecordEvent.Finalize -> {
                            if (!recordEvent.hasError()) {
                                Log.d("RecordingManager", "Video capture succeeded: ${recordEvent.outputResults.outputUri}")
                                onRecordingFinalized?.invoke(recordEvent.outputResults.outputUri)
                            } else {
                                recording?.close()
                                recording = null
                                Log.e("RecordingManager", "Video capture ends with error: ${recordEvent.error}")
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("RecordingManager", "Failed to start recording", e)
        }
    }

    private fun startAudioRecording() {
        val name = "NIRBHAYA_AUDIO_" + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
            .format(System.currentTimeMillis())
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/Nirbhaya-Emergency")
            }
        }

        currentAudioUri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
        
        currentAudioUri?.let { uri ->
            try {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "w")
                parcelFileDescriptor?.let { pfd ->
                    mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        MediaRecorder(context)
                    } else {
                        @Suppress("DEPRECATION")
                        MediaRecorder()
                    }.apply {
                        setAudioSource(MediaRecorder.AudioSource.MIC)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setOutputFile(pfd.fileDescriptor)
                        prepare()
                        start()
                        Log.d("RecordingManager", "Audio recording started at $uri")
                    }
                }
            } catch (e: Exception) {
                Log.e("RecordingManager", "MediaRecorder failed", e)
            }
        }
    }

    fun stopRecording() {
        recording?.stop()
        recording = null
        
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
            mediaRecorder = null
            currentAudioUri?.let {
                onAudioRecordingFinalized?.invoke(it)
            }
            currentAudioUri = null
            Log.d("RecordingManager", "Audio recording stopped")
        } catch (e: Exception) {
            Log.e("RecordingManager", "Error stopping MediaRecorder", e)
        }
    }
}
