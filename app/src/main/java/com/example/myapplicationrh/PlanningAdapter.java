package com.example.myapplicationrh;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Import de ta classe Pointage
import com.example.myapplicationrh.model.Pointage;

public class PlanningAdapter extends RecyclerView.Adapter<PlanningAdapter.ViewHolder> {

    private List<Pointage> pointages;

    public PlanningAdapter(List<Pointage> pointages) {
        this.pointages = pointages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pointage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pointage p = pointages.get(position);
        holder.textDate.setText(p.date);
        holder.textHeureEntree.setText("Entrée : " + (p.heureEntree != null ? p.heureEntree : "--"));
        holder.textHeureSortie.setText("Sortie : " + (p.heureSortie != null ? p.heureSortie : "--"));
    }

    @Override
    public int getItemCount() {
        return pointages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDate, textHeureEntree, textHeureSortie;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDate = itemView.findViewById(R.id.textDate);
            textHeureEntree = itemView.findViewById(R.id.textHeureEntree);
            textHeureSortie = itemView.findViewById(R.id.textHeureSortie);
        }
    }
}
