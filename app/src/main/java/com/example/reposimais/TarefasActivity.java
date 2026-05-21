package com.example.reposimais;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TarefasActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ListView listViewTarefas;
    private List<Produto> listaTarefas;
    private TarefaAdapter adapter;
    private DatabaseReference databaseReference;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tarefas);

        mAuth = FirebaseAuth.getInstance();
        listViewTarefas = findViewById(R.id.listViewTarefas);
        listaTarefas = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("produtos");

        // Otimização: Inicializa o adapter uma única vez no início
        adapter = new TarefaAdapter(TarefasActivity.this, listaTarefas);
        listViewTarefas.setAdapter(adapter);

        Toolbar toolbar = findViewById(R.id.toolbar_tarefas);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tarefas de Reposição");
        }

        drawerLayout = findViewById(R.id.drawer_layout_tarefas);
        NavigationView navigationView = findViewById(R.id.nav_view_tarefas);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Busca e coloca o Nome e E-mail reais na aba lateral
        if (mAuth.getCurrentUser() != null && navigationView.getHeaderCount() > 0) {
            TextView txtNomeMenu = navigationView.getHeaderView(0).findViewById(R.id.txtNomeUsuarioMenu);
            TextView txtEmailMenu = navigationView.getHeaderView(0).findViewById(R.id.txtEmailUsuarioMenu);

            txtEmailMenu.setText(mAuth.getCurrentUser().getEmail());

            String uid = mAuth.getCurrentUser().getUid();
            FirebaseDatabase.getInstance().getReference("usuarios").child(uid).child("nome")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists() && snapshot.getValue() != null) {
                                String nomeCadastrado = snapshot.getValue(String.class);
                                txtNomeMenu.setText(nomeCadastrado);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
        }

        // Monitoramento em tempo real do estoque do Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaTarefas.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Produto produto = dataSnapshot.getValue(Produto.class);
                    // Regra de negócio: Se o estoque for menor que o limite mínimo, vai para a lista
                    if (produto != null && produto.estoqueEmpresa < produto.qtdMinima) {
                        listaTarefas.add(produto);
                    }
                }
                // Otimização: Apenas avisa o adapter que a lista atualizou (evita recriar objetos à toa)
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_configurar) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("manterLogado", false);
            editor.apply();

            startActivity(new Intent(this, LoginActivity.class));
            finishAffinity();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}