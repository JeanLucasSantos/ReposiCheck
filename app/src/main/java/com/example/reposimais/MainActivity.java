package com.example.reposimais;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private EditText editCodigoBarras, editQtdMinima, editCapacidadeMax;
    private Button btnSalvarMinimo;
    private DatabaseReference databaseReference;

    private CardView cardInfoProduto;
    private TextView txtCardDescricao, txtCardQtdMinima, txtCardCapMax;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private String nome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        nome = intent.getStringExtra("123");

        // Inicializando Database apontando para "produtos"
        databaseReference = FirebaseDatabase.getInstance().getReference("produtos");

        // Linkando componentes do XML
        editCodigoBarras = findViewById(R.id.editCodigoBarras);
        editCapacidadeMax = findViewById(R.id.editCapacidadeMax);
        editQtdMinima = findViewById(R.id.editQtdMinima);
        btnSalvarMinimo = findViewById(R.id.btnSalvarMinimo);

        cardInfoProduto = findViewById(R.id.cardInfoProduto);
        txtCardDescricao = findViewById(R.id.txtCardDescricao);
        txtCardQtdMinima = findViewById(R.id.txtCardQtdMinima);
        txtCardCapMax = findViewById(R.id.txtCardCapMax);

        toolbar = findViewById(R.id.toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("ReposiMais");
        }
        if (drawerLayout != null && toolbar != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
        }
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);

            // ==========================================
            // LOGICA ADICIONADA: EXIBIR DADOS DO USUÁRIO LOGADO DINAMICAMENTE
            // ==========================================
            View headerView = navigationView.getHeaderView(0);
            TextView txtNomeMenu = headerView.findViewById(R.id.txtNomeUsuarioMenu);
            TextView txtEmailMenu = headerView.findViewById(R.id.txtEmailUsuarioMenu);

            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

            if (user != null) {
                // Define o e-mail real do usuário no menu
                if (txtEmailMenu != null && user.getEmail() != null) {
                    txtEmailMenu.setText(user.getEmail());
                }

                // Define o nome real ou extrai o nome baseado no e-mail
                if (txtNomeMenu != null) {


                    String usuarioID = FirebaseAuth.getInstance()
                            .getCurrentUser()
                            .getUid();

                    DatabaseReference reference = FirebaseDatabase
                            .getInstance()
                            .getReference()
                            .child("Usuarios")
                            .child(usuarioID);

                    reference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (snapshot.exists()) {

                                String name = snapshot.child("nome")
                                        .getValue(String.class);

                                txtNomeMenu.setText(name);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
            // ==========================================
        }

        // TEXTWATCHER CORRIGIDO: Monitora a digitação de forma flexível
        editCodigoBarras.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String codigo = s.toString().trim();

                // Dispara a busca se tiver pelo menos 8 dígitos (abrange o seu produto de 8 números)
                if (codigo.length() >= 8) {
                    buscarDadosNoFirebase(codigo);
                } else {
                    cardInfoProduto.setVisibility(View.GONE);
                }
            }
        });

        // SALVAMENTO SEGURO COM CHAVES CORRETAS DO SEU BANCO
        if (btnSalvarMinimo != null) {
            btnSalvarMinimo.setOnClickListener(v -> {
                String codigo = editCodigoBarras.getText().toString().trim();
                String capMaxStr = editCapacidadeMax.getText().toString().trim();
                String qtdMinStr = editQtdMinima.getText().toString().trim();

                if (!codigo.isEmpty() && !capMaxStr.isEmpty() && !qtdMinStr.isEmpty()) {
                    int novaCapMax = Integer.parseInt(capMaxStr);
                    int novaQtdMinima = Integer.parseInt(qtdMinStr);

                    // Mapa estruturado com os nomes exatos salvos no seu Firebase
                    Map<String, Object> atualizacaoFocada = new HashMap<>();
                    atualizacaoFocada.put("qtdMinima", novaQtdMinima);
                    atualizacaoFocada.put("capacidadeMax", novaCapMax);

                    databaseReference.child(codigo).updateChildren(atualizacaoFocada)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(MainActivity.this, "Configurações salvas!", Toast.LENGTH_SHORT).show();
                                editCodigoBarras.setText("");
                                editCapacidadeMax.setText("");
                                editQtdMinima.setText("");
                                cardInfoProduto.setVisibility(View.GONE);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(MainActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void buscarDadosNoFirebase(String codigo) {
        databaseReference.child(codigo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Puxa as informações com as chaves exatas do seu banco
                    String nomeProduto = snapshot.child("nome").getValue(String.class);

                    Object qMinObj = snapshot.child("qtdMinima").getValue();
                    int qtdMinima = qMinObj != null ? Integer.parseInt(qMinObj.toString()) : 0;

                    Object capMaxObj = snapshot.child("capacidadeMax").getValue();
                    int capMax = capMaxObj != null ? Integer.parseInt(capMaxObj.toString()) : 0;

                    if (nomeProduto == null) nomeProduto = "Produto sem descrição";

                    // Preenche o Card
                    txtCardDescricao.setText("Descrição: " + nomeProduto);
                    txtCardQtdMinima.setText("Qtd Mínima Atual: " + qtdMinima);
                    txtCardCapMax.setText("Capacidade Máxima Atual: " + (capMax == 0 ? "Não definida" : capMax));

                    // Preenche os campos de texto para o usuário não ter que digitar do zero
                    editQtdMinima.setText(String.valueOf(qtdMinima));
                    editCapacidadeMax.setText(capMax == 0 ? "" : String.valueOf(capMax));

                    // Mostra o card na tela
                    cardInfoProduto.setVisibility(View.VISIBLE);
                } else {
                    // Se o usuário digitar um código que realmente não existe
                    txtCardDescricao.setText("Descrição: Produto não localizado");
                    txtCardQtdMinima.setText("Qtd Mínima Atual: 0");
                    txtCardCapMax.setText("Capacidade Máxima Atual: Não definida");
                    cardInfoProduto.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Erro no banco: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // 1. Caminho para a tela de Tarefas
        if (id == R.id.nav_tarefas || item.getTitle().toString().toLowerCase().contains("tarefa")) {
            startActivity(new Intent(this, TarefasActivity.class));
            finish();
        }

        // 2. CORREÇÃO DO LOGOUT: Detecta o clique no botão Sair da Conta
        else if (id == R.id.nav_logout || item.getTitle().toString().toLowerCase().contains("sair") || item.getTitle().toString().toLowerCase().contains("logout")) {
            // Se você estiver usando o Firebase Authentication para gerenciar os usuários:
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();

            // Avisa o usuário e redireciona para a sua tela de Login
            Toast.makeText(this, "Sessão encerrada!", Toast.LENGTH_SHORT).show();

            // IMPORTANTE: Substitua 'LoginActivity.class' pelo nome exato da sua classe de Login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            // Limpa o histórico de telas para o usuário não conseguir voltar usando o botão físico "voltar"
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // Fecha o menu lateral após o clique
        if (drawerLayout != null) {
            drawerLayout.closeDrawers();
        }
        return true;
    }
}