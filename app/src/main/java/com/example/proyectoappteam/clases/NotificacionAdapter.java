package com.example.proyectoappteam.clases;

import android.content.Context;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoappteam.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Adapter para mostrar la lista de objetos Notificaciones en un RecyclerView.
 */
public class NotificacionAdapter extends RecyclerView.Adapter<NotificacionAdapter.NotificacionViewHolder> {

    private final Context context;
    private List<Notificaciones> notificacionesList = new ArrayList<>();
    private OnNotificacionClickListener listener;

    public interface OnNotificacionClickListener {
        void onNotificacionClick(Notificaciones notificacion);
    }

    // Constructor sin listener
    public NotificacionAdapter(Context context, List<Notificaciones> notificacionesList) {
        this.context = context;
        if (notificacionesList != null) this.notificacionesList = notificacionesList;
    }

    // Constructor con listener
    public NotificacionAdapter(Context context, List<Notificaciones> notificacionesList, OnNotificacionClickListener listener) {
        this.context = context;
        if (notificacionesList != null) this.notificacionesList = notificacionesList;
        this.listener = listener;
    }

    public void setNotificacionesList(List<Notificaciones> nuevaLista) {
        this.notificacionesList = (nuevaLista != null) ? nuevaLista : new ArrayList<>();
        notifyDataSetChanged();
    }

    /** Actualiza un item por objectId (útil al marcar como leída) */
    public void updateItem(Notificaciones actualizado) {
        if (actualizado == null || actualizado.getObjectId() == null) return;
        for (int i = 0; i < notificacionesList.size(); i++) {
            Notificaciones n = notificacionesList.get(i);
            if (Objects.equals(n.getObjectId(), actualizado.getObjectId())) {
                notificacionesList.set(i, actualizado);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public NotificacionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_notificacion, parent, false);
        return new NotificacionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificacionViewHolder holder, int position) {
        Notificaciones notificacion = notificacionesList.get(position);

        // 1) Mensaje
        holder.tvMensaje.setText(notificacion.getMensaje());

        // 2) Icono según tipo (normalizado)
        setupIcon(holder.ivIcon, notificacion.getTipoNotificacion());

        // 3) Tiempo transcurrido: usa timestamposimulado (double) -> long; fallback a created
        long ts = safeTimestamp(notificacion);
        holder.tvTiempo.setText(formatTimeAgo(ts));

        // 4) Estado de lectura
        setupReadState(holder, notificacion);

        // 5) Click
        if (listener != null) {
            holder.cardNotificacion.setOnClickListener(v -> listener.onNotificacionClick(notificacion));
        }
    }

    private long safeTimestamp(Notificaciones n) {
        long ts = 0L;
        try {
            double d = n.getTimestamposimulado(); // tu POJO lo expone como double
            if (d > 0) ts = (long) d;
        } catch (Throwable ignored) { }
        if (ts <= 0 && n.getCreated() != null) {
            ts = n.getCreated().getTime();
        }
        if (ts <= 0) ts = System.currentTimeMillis();
        return ts;
    }

    private void setupIcon(ImageView imageView, String tipoRaw) {
        int drawableRes;
        String tipo = (tipoRaw == null) ? "" : tipoRaw.trim().toUpperCase();

        switch (tipo) {
            // Nuevos (consistentes con lo que grabamos)
            case "COMENTARIO":
            case "NEW_COMMENT":     // compatibilidad hacia atrás
                drawableRes = R.drawable.ic_comment;
                break;

            case "CALIFICACION":
            case "NEW_RATING":      // compatibilidad hacia atrás
            case "ESTRELLA":
                drawableRes = R.drawable.ic_star;
                break;

            case "URGENTE":
            case "URGENT_NEAR":
                drawableRes = R.drawable.ic_urgent;
                break;

            default:
                drawableRes = R.drawable.ic_info;
                break;
        }
        imageView.setImageResource(drawableRes);
    }

    private void setupReadState(NotificacionViewHolder holder, Notificaciones notificacion) {
        Boolean leida = notificacion.getLeida();
        boolean isRead = (leida != null && leida);

        holder.vUnreadIndicator.setVisibility(isRead ? View.GONE : View.VISIBLE);
        holder.tvMensaje.setTypeface(null, isRead ? Typeface.NORMAL : Typeface.BOLD);
        holder.cardNotificacion.setCardBackgroundColor(
                ContextCompat.getColor(context, isRead ? R.color.colorCardBackground : R.color.colorUnreadBackground)
        );
    }

    private String formatTimeAgo(long time) {
        long now = System.currentTimeMillis();
        long diff = now - time;

        if (diff < 0 || time == 0) return context.getString(R.string.time_now);

        if (diff < TimeUnit.MINUTES.toMillis(1)) {
            return context.getString(R.string.time_now);
        } else if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            return context.getString(R.string.time_minutes_ago, (int) minutes);
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            return context.getString(R.string.time_hours_ago, (int) hours);
        } else if (diff < TimeUnit.DAYS.toMillis(30)) {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return context.getString(R.string.time_days_ago, (int) days);
        } else {
            return DateFormat.format("dd MMM", time).toString();
        }
    }

    @Override
    public int getItemCount() {
        return notificacionesList.size();
    }

    public static class NotificacionViewHolder extends RecyclerView.ViewHolder {
        CardView cardNotificacion;
        ImageView ivIcon;
        TextView tvMensaje;
        TextView tvTiempo;
        View vUnreadIndicator;

        public NotificacionViewHolder(@NonNull View itemView) {
            super(itemView);
            cardNotificacion = itemView.findViewById(R.id.card_notificacion);
            ivIcon = itemView.findViewById(R.id.iv_notificacion_icon);
            tvMensaje = itemView.findViewById(R.id.tv_notificacion_mensaje);
            tvTiempo = itemView.findViewById(R.id.tv_notificacion_tiempo);
            vUnreadIndicator = itemView.findViewById(R.id.v_unread_indicator);
        }
    }
}