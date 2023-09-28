package com.f2fk.geofence_foreground_service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.f2fk.geofence_foreground_service.enums.GeofenceServiceAction
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent


class GeofenceForegroundService : Service() {
    private var serviceId: Int = 525000

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val geofenceAction: GeofenceServiceAction =
            if (intent.getStringExtra(Constants.geofenceAction) != null) {
                GeofenceServiceAction.valueOf(
                    intent.getStringExtra(Constants.geofenceAction)!!
                )
            } else {
                GeofenceServiceAction.valueOf(
                    intent.getStringExtra(applicationContext.packageName + "." + Constants.geofenceAction)!!
                )
            }

        val appIcon: Int = intent.getIntExtra(
            applicationContext.packageName + "." + Constants.appIcon,
            0
        )

        val notificationChannelId: String = intent.getStringExtra(
            applicationContext.packageName + "." + Constants.channelId
        )!!

        val notificationContentTitle: String = intent.getStringExtra(
            applicationContext.packageName + "." + Constants.contentTitle
        )!!

        val notificationContentText: String = intent.getStringExtra(
            applicationContext.packageName + "." + Constants.contentText
        )!!

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }

        val notification: NotificationCompat.Builder = NotificationCompat
            .Builder(
                this.baseContext,
                notificationChannelId,
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSmallIcon(appIcon)
            .setContentTitle(notificationContentTitle)
            .setContentText(notificationContentText)

        if (geofenceAction == GeofenceServiceAction.SETUP) {
            serviceId = intent.getIntExtra(Constants.serviceId, serviceId)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_DETACH)
            }

            startForeground(serviceId, notification.build())
        } else if (geofenceAction == GeofenceServiceAction.TRIGGER) {
            handleGeofenceEvent(notification, intent)
        }

        return START_STICKY
    }

    private fun handleGeofenceEvent(notification: NotificationCompat.Builder, intent: Intent) {
        try {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent?.hasError() == false) {
                val geofenceTransition = geofencingEvent.geofenceTransition

                if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                    notification.setContentText("Entered the zone, IN")
                    /**
                     * The user is inside a geofence now
                     */
//                    requestTripUseCase.execute(
//                        Triple(
//                            geofencingEvent.triggeringGeoFences!!.first().requestId,
//                            geofencingEvent.triggeringLocation!!.latitude,
//                            geofencingEvent.triggeringLocation!!.longitude
//                        )
//                    )
                } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                    notification.setContentText("Exited the zone, OUT")
                }

                startForeground(serviceId, notification.build())
            }
        } catch (e: Exception) {
            println(e.message)
            println(e.toString())
        }
    }
}