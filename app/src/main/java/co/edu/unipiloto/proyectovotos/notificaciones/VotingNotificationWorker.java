package co.edu.unipiloto.proyectovotos.notificaciones;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import co.edu.unipiloto.proyectovotos.iniciodesesion.Login;

public class VotingNotificationWorker extends Worker {

    public VotingNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Obtener los datos pasados al WorkManager
        String nombreProyecto = getInputData().getString("nombreProyecto");
        String votingDeadline = getInputData().getString("votingDeadline");

        // Crear la notificación
        String CHANNEL_ID = "voting_notification_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notificaciones de Votación",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones de votación");
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        String tituloPrincipal = "Ya votaste🤔❓";
        String tituloSecundario = "Faltan 3 minutos para cerrar las votaciones del proyecto " + nombreProyecto;
        String descripcion = "Recuerda que tienes tiempo para votar hasta " + votingDeadline + " El proyecto a votar es: " + nombreProyecto;


        Intent intent = new Intent(getApplicationContext(), Login.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(tituloPrincipal)
                .setContentText(tituloSecundario)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(descripcion))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

        // Verificación de permisos (para Android 13+)
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure();
        }

        // Mostrar la notificación
        notificationManager.notify(2, builder.build());

        return Result.success();
    }
}

