package com.example.reposimais;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private EditText editCodigoBarras, editQtdMinima;
    private Button btnSalvarMinimo;
    private DatabaseReference databaseReference;
    private DrawerLayout drawerLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("produtos");

        editCodigoBarras = findViewById(R.id.editCodigoBarras);
        editQtdMinima = findViewById(R.id.editQtdMinima);
        btnSalvarMinimo = findViewById(R.id.btnSalvarMinimo);

        // Configuração da Toolbar e Menu Lateral
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ReposiMais");
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
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
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
        }

        // Lógica de Salvar a Quantidade Mínima
        btnSalvarMinimo.setOnClickListener(v -> {
            String codigo = editCodigoBarras.getText().toString().trim();
            String qtdMinimaStr = editQtdMinima.getText().toString().trim();

            if (codigo.isEmpty() || qtdMinimaStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            int qtdMinima = Integer.parseInt(qtdMinimaStr);

            databaseReference.child(codigo).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        databaseReference.child(codigo).child("qtdMinima").setValue(qtdMinima)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainActivity.this, "Quantidade mínima salva!", Toast.LENGTH_SHORT).show();
                                    editCodigoBarras.setText("");
                                    editQtdMinima.setText("");
                                    esconderTeclado();
                                });
                    } else {
                        // Mantendo seu padrão de criar o produto simulado caso não exista
                        Produto produtoSimulado = new Produto(codigo, "Produto Teste Empresa", 3, qtdMinima);
                        databaseReference.child(codigo).setValue(produtoSimulado)
                                .addOnSuccessListener(aVoid -> esconderTeclado());
                        Toast.makeText(MainActivity.this, "Produto simulado criado!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        });
    }

    // Gerencia as ações dos cliques no Menu Lateral
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_tarefas) {
            startActivity(new Intent(this, TarefasActivity.class));
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

    private void esconderTeclado() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
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