package co.edu.unipiloto.proyectovotos.decisor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.votos.Proyecto;

public class ProyectosAdapter extends RecyclerView.Adapter<ProyectosAdapter.ProyectoViewHolder> {

    private List<Proyecto> listaProyectos;
    private Context context;

    private List<ConteoVotos> conteoVotosList;


    public ProyectosAdapter(List<Proyecto> listaProyectos, Context context) {
        this.listaProyectos = listaProyectos;
        this.context = context;
    }

    @NonNull
    @Override
    public ProyectoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_proyecto, parent, false);
        return new ProyectoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProyectoViewHolder holder, int position) {
        Proyecto proyecto = listaProyectos.get(position);
        // Configura los datos del proyecto en las vistas
        holder.textViewTitulo.setText(proyecto.getTitulo());

        // Configura el click listener para el botón Ver
        holder.buttonVer.setOnClickListener(v -> {
            String nombreProyecto = proyecto.getTitulo();
            obtenerIdProyecto(nombreProyecto, idProyecto -> {
                if (idProyecto != null) {
                    Intent intent = new Intent(holder.itemView.getContext(), boton_Ver.class);
                    intent.putExtra("idProyecto", idProyecto);
                    intent.putExtra("nombreProyecto", nombreProyecto);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "No se encuentra el id del proyecto", Toast.LENGTH_SHORT).show();
                }
            });
        });


//        holder.buttonEditar.setOnClickListener(v -> {
//            String nombreProyecto = proyecto.getTitulo();
//            obtenerIdProyecto(nombreProyecto, idProyecto -> {
//                if (idProyecto != null) {
//                    Intent intent = new Intent(holder.itemView.getContext(), boton_editar.class);
//                    intent.putExtra("idProyecto", idProyecto);  // ID del proyecto
//                    intent.putExtra("nombreProyecto", nombreProyecto);  // Nombre del proyecto
//                    ((Activity) context).startActivityForResult(intent, 1);  // Usa startActivityForResult para editar
//                } else {
//                    Toast.makeText(context, "No se encuentra el id del proyecto para editar", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });

        // Configura el click listener para el botón Eliminar
//        holder.buttonEliminar.setOnClickListener(v -> {
//            String nombreProyecto = proyecto.getTitulo();
//            obtenerIdProyecto(nombreProyecto, idProyecto -> {
//                if (idProyecto != null) {
//                    Intent intent = new Intent(holder.itemView.getContext(), boton_eliminar.class);
//                    intent.putExtra("idProyecto", idProyecto);  // ID del proyecto
//                    intent.putExtra("nombreProyecto", nombreProyecto);  // Nombre del proyecto
//                    ((Activity) context).startActivityForResult(intent, 2);  // Usa startActivityForResult para eliminar
//                } else {
//                    Toast.makeText(context, "No se encuentra el id del proyecto para eliminar", Toast.LENGTH_SHORT).show();
//                }
//            });
//        });

    }

    private void obtenerIdProyecto(String nombreProyecto, OnIdProyectoListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference proyectosRef = db.collection("guardarProyectos");
        Query query = proyectosRef.whereEqualTo("titulo", nombreProyecto);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String idProyecto = document.getId();
                    Log.d("obtenerIdProyecto", "ID del proyecto: " + idProyecto);
                    listener.onIdProyectoObtenido(idProyecto);
                    return;
                }
                Log.d("obtenerIdProyecto", "No se encontró el proyecto");
                listener.onIdProyectoObtenido(null); // No se encontró el proyecto
            } else {
                Log.d("obtenerIdProyecto", "Error en la consulta: ", task.getException());
                listener.onIdProyectoObtenido(null); // Error en la consulta
            }
        });
    }


    public interface OnIdProyectoListener {
        void onIdProyectoObtenido(String idProyecto);
    }



    @Override
    public int getItemCount() {
        return listaProyectos.size();
    }

    public void actualizarListaProyectos(List<Proyecto> nuevaListaProyectos) {
        this.listaProyectos = nuevaListaProyectos;
        notifyDataSetChanged();
    }

    public static class ProyectoViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitulo;
        Button buttonVer;
        //Button buttonEditar;
        //Button buttonEliminar;
        public ProyectoViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitulo = itemView.findViewById(R.id.textViewTituloProyecto); // Título del proyecto
            buttonVer = itemView.findViewById(R.id.buttonVer); // Botón Ver en item_proyecto.xml
            //buttonEditar = itemView.findViewById(R.id.buttonEditar); // Botón Editar en item_proyecto.xml
            //buttonEliminar = itemView.findViewById(R.id.buttonEliminar); // Botón Eliminar en item_proyecto.xml
        }
    }
}