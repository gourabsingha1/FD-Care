package com.example.fdcare

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Make Caretaker's Device Ring
        val data = remoteMessage.data
        if(data["start_ringing"] == "true") {
            startRinging()
        }

        // Show notification in foreground
        remoteMessage.notification?.let {
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(it.title)
                .setContentText(it.body)
                .setSmallIcon(R.mipmap.ic_fd_care_logo_round)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(notification_ID, notificationBuilder.build())
        }
    }

    private fun startRinging() {
        // Set the volume to maximum for the ringtone
        val audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(
            AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(
                AudioManager.STREAM_RING
            ), 0
        )

//            val ringtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val ringtoneUri: Uri = Uri.parse("android.resource://${packageName}/${R.raw.let_it_happen}") // Replace with your ringtone URI
        MediaPlayerSingleton.mediaPlayer = MediaPlayer.create(applicationContext, ringtoneUri)
        MediaPlayerSingleton.mediaPlayer?.start()
        MediaPlayerSingleton.mediaPlayer?.isLooping = true
    }

    override fun onNewToken(token: String) {

    }

    companion object {
        const val CHANNEL_ID = "Fall Notification Foreground"
        const val notification_ID = 0
    }
}
