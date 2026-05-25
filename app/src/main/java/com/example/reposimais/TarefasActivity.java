package com.example.reposimais;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TarefasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TarefaAdapter adapter;
    private List<Produto> listaTarefas;
    private DatabaseReference databaseReference;

    // Componentes do Menu Lateral adicionados
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarefas);

        // 1. Inicialização dos componentes do Menu Lateral e Toolbar
        toolbar = findViewById(R.id.toolbar_tarefas);
        drawerLayout = findViewById(R.id.drawer_layout_tarefas);
        navigationView = findViewById(R.id.nav_view_tarefas);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(""); // Deixa vazio para destacar os textos customizados do layout
        }

        // 2. Configuração do botão Hambúrguer (Abre e fecha o menu)
        toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 3. Gerenciamento de cliques no Menu Lateral
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Nota: Se der erro em alguma ID abaixo, mude para os nomes exatos do seu res/menu/drawer_menu.xml
            if (id == R.id.nav_tarefas) {
                // Já está nesta tela, apenas fecha o menu
                drawerLayout.closeDrawers();
            } else if (id == R.id.nav_configurar) {
                // Abre a tela de cadastro/configuração (MainActivity)
                Intent intent = new Intent(TarefasActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Fecha a tela atual para evitar pilhas na memória
            } else if (id == R.id.nav_logout) {
                // Exemplo de ação para o botão sair
                Toast.makeText(TarefasActivity.this, "Saindo do sistema...", Toast.LENGTH_SHORT).show();
                finish();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // 4. Configuração do RecyclerView (Sua listagem operacional)
        recyclerView = findViewById(R.id.recyclerViewTarefas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaTarefas = new ArrayList<>();
        adapter = new TarefaAdapter(listaTarefas, this);
        recyclerView.setAdapter(adapter);

        // 5. Conexão e sincronização em tempo real com o Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("produtos");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaTarefas.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Produto produto = dataSnapshot.getValue(Produto.class);

                    if (produto != null) {
                        // REGRA DE GATILHO: Se a gôndola caiu para o nível crítico (mínimo)
                        if (produto.getQtdGondola() <= produto.getQtdMinima() && produto.getEstoqueGeral() > 0 )  {
                            listaTarefas.add(produto);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TarefasActivity.this, "Erro ao carregar dados.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}