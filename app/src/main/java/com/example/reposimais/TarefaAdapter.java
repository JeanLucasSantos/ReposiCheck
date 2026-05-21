package com.example.reposimais;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class TarefaAdapter extends ArrayAdapter<Produto> {

    private Context context;
    private List<Produto> listaProdutos;
    private DatabaseReference databaseReference;

    public TarefaAdapter(Context context, List<Produto> lista) {
        super(context, 0, lista);
        this.context = context;
        this.listaProdutos = lista;
        // Correção: Ajustado para "produtos" em minúsculo para seguir o padrão do seu banco
        this.databaseReference = FirebaseDatabase.getInstance().getReference("produtos");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tarefa, parent, false);
        }

        Produto produto = listaProdutos.get(position);

        TextView txtDescricao = convertView.findViewById(R.id.txtDescricaoTarefa);
        TextView txtCodigo = convertView.findViewById(R.id.txtCodigoTarefa);
        TextView txtQtd = convertView.findViewById(R.id.txtQtdTarefa);
        Button btnConcluir = convertView.findViewById(R.id.btnConcluirTarefa);

        txtDescricao.setText(produto.descricao);
        txtCodigo.setText("Código: " + produto.codigo);

        // Correção: Substituído qtdAtual por estoqueEmpresa e corrigido o texto visual para o usuário
        txtQtd.setText("Estoque Atual: " + produto.estoqueEmpresa + " / Mínimo Exigido: " + produto.qtdMinima);

        // Ação do botão de Confirmação da Reposição
        btnConcluir.setOnClickListener(v -> {

            // 1. REGRA: O estoque atual da prateleira assume o valor da quantidade mínima cadastrada para sanar o alerta
            produto.estoqueEmpresa = produto.qtdMinima;

            // 2. Salvamos a mudança de forma limpa no Firebase utilizando o objeto otimizado
            databaseReference.child(produto.codigo).setValue(produto)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Reposição concluída com sucesso!", Toast.LENGTH_SHORT).show();

                        // 3. Remove o item do array local para sumir da lista
                        listaProdutos.remove(position);

                        // 4. Avisa o Android para atualizar a interface
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Erro ao atualizar estoque: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        return convertView;
    }
}