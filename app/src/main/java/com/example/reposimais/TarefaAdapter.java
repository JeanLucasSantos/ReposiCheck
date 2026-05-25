package com.example.reposimais;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    private List<Produto> listaProdutosEmFalta;
    private Context context;

    public TarefaAdapter(List<Produto> listaProdutosEmFalta, Context context) {
        this.listaProdutosEmFalta = listaProdutosEmFalta;
        this.context = context;
    }

    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tarefa, parent, false);
        return new TarefaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        Produto produto = listaProdutosEmFalta.get(position);

        holder.txtNome.setText(produto.getNome());
        holder.txtStatus.setText("Na Gôndola: " + produto.getQtdGondola() + " / Cap. Max: " + produto.getCapacidadeMax());

        // Passo 4: Exibe a quantidade atual dentro do Badge vermelho arredondado
        holder.txtBadge.setText(produto.getQtdGondola() + " un.");

        // AÇÃO 1: Botão Gôndola Cheia (Sincroniza direto para o limite máximo)
        holder.btnGondolaCheia.setOnClickListener(v -> {
            produto.setQtdGondola(produto.getCapacidadeMax());
            atualizarProdutoNoFirebase(produto);
            Toast.makeText(context, "Gôndola atualizada para cheia!", Toast.LENGTH_SHORT).show();
        });

        // AÇÃO 2: Botão Repor Manual (Abre a janela com as travas matemáticas)
        holder.btnRepor.setOnClickListener(v -> {
            int espacoDisponivel = produto.getCapacidadeMax() - produto.getQtdGondola();

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Repor: " + produto.getNome());
            builder.setMessage("Espaço livre na gôndola: " + espacoDisponivel + " un.\nDisponível no Depósito: " + produto.getEstoqueGeral() + " un.");

            // Cria o campo de entrada numérica
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setHint("Quantidade a repor");
            builder.setView(input);

            builder.setPositiveButton("Confirmar Abastecimento", (dialog, which) -> {
                String valorDigitado = input.getText().toString();
                if (valorDigitado.isEmpty()) return;

                int qtdColocada = Integer.parseInt(valorDigitado);

                // TRAVA 1: Não pode colocar mais do que cabe fisicamente
                if (qtdColocada > espacoDisponivel) {
                    Toast.makeText(context, "Erro! A gôndola só suporta mais " + espacoDisponivel + " unidades.", Toast.LENGTH_LONG).show();
                }
                // TRAVA 2: Não pode tirar do depósito o que a empresa não tem
                else if (qtdColocada > produto.getEstoqueGeral()) {
                    Toast.makeText(context, "Erro! O depósito só possui " + produto.getEstoqueGeral() + " unidades.", Toast.LENGTH_LONG).show();
                }
                // LÓGICA PERFEITA: Atualiza ambos os estoques
                else {
                    produto.setQtdGondola(produto.getQtdGondola() + qtdColocada); // Entra na gôndola
                    produto.setEstoqueGeral(produto.getEstoqueGeral() - qtdColocada); // Abate do estoque geral/empresa

                    atualizarProdutoNoFirebase(produto);
                    Toast.makeText(context, "Estoque abastecido com sucesso!", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return listaProdutosEmFalta.size();
    }

    private void atualizarProdutoNoFirebase(Produto produto) {
        FirebaseDatabase.getInstance().getReference("produtos")
                .child(produto.getId())
                .setValue(produto);
    }

    public static class TarefaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtStatus, txtBadge; // <-- Incluído o txtBadge aqui
        Button btnGondolaCheia, btnRepor;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNomeProduto);
            txtStatus = itemView.findViewById(R.id.txtStatusEstoque);
            txtBadge = itemView.findViewById(R.id.txtBadgeQtd); // <-- Vinculado ao ID do novo layout de ontem
            btnGondolaCheia = itemView.findViewById(R.id.btnGondolaCheia);
            btnRepor = itemView.findViewById(R.id.btnRepor);
        }
    }
}