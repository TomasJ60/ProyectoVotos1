package co.edu.unipiloto.proyectovotos.decisor;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.edu.unipiloto.proyectovotos.R;

public class AdapterConteoDeVotos extends RecyclerView.Adapter<AdapterConteoDeVotos.ViewHolder> {
    private List<ConteoVotos> conteoVotosList;

    public AdapterConteoDeVotos(List<ConteoVotos> conteoVotosList) {
        this.conteoVotosList = conteoVotosList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conteo_votos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ConteoVotos conteo = conteoVotosList.get(position);
        holder.bind(conteo);
    }

    @Override
    public int getItemCount() {
        return conteoVotosList.size();
    }

    public void updateList(List<ConteoVotos> newConteoVotosList) {
        conteoVotosList.clear();
        conteoVotosList.addAll(newConteoVotosList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView projectNameTextView;
        private TextView votosTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(R.id.projectNameTextView);
            votosTextView = itemView.findViewById(R.id.votosTextView);
        }

        public void bind(ConteoVotos conteo) {
            projectNameTextView.setText(conteo.getProjectName());
            String votos = "Sí: " + conteo.getYesVotes() + " No: " + conteo.getNoVotes() + " Blanco: " + conteo.getBlankVotes();
            votosTextView.setText(votos);

        }
    }
}


