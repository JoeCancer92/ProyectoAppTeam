package com.example.proyectoappteam.clases;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proyectoappteam.R;

import java.util.List;

public class InteraccionesAdapter extends RecyclerView.Adapter<InteraccionesAdapter.ViewHolder> {

    private final List<InteraccionItem> interacciones;

    public InteraccionesAdapter(List<InteraccionItem> interacciones) {
        this.interacciones = interacciones;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_interaccion, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InteraccionItem item = interacciones.get(position);

        // Nombre
        holder.tvNombre.setText(
                item.getNombre() != null && !item.getNombre().isEmpty()
                        ? item.getNombre()
                        : "Usuario desconocido"
        );

        // Correo
        holder.tvCorreo.setText(
                item.getCorreo() != null && !item.getCorreo().isEmpty()
                        ? item.getCorreo()
                        : ""
        );

        // Comentario
        if (item.getComentario() != null && !item.getComentario().isEmpty()) {
            holder.tvComentario.setVisibility(View.VISIBLE);
            holder.tvComentario.setText("💬 " + item.getComentario());
        } else {
            holder.tvComentario.setVisibility(View.GONE);
        }

        // Calificación
        if (item.getPuntuacion() != null) {
            holder.tvCalificacion.setVisibility(View.VISIBLE);
            holder.tvCalificacion.setText("⭐ Calificación: " + item.getPuntuacion() + " estrellas");
        } else {
            holder.tvCalificacion.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return interacciones.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvCorreo, tvComentario, tvCalificacion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreUsuario);
            tvCorreo = itemView.findViewById(R.id.tvCorreoUsuario);
            tvComentario = itemView.findViewById(R.id.tvComentario);
            tvCalificacion = itemView.findViewById(R.id.tvCalificacion);
        }
    }
}