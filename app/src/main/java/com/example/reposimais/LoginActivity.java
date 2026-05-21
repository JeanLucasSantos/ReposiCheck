package com.example.reposimais;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private EditText editNome, editEmail, editSenha;
    private CheckBox checkLembrar;
    private Button btnEntrar;
    private TextView btnCadastrar;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("usuarios");
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);

        editNome = findViewById(R.id.editNomeLogin);
        editEmail = findViewById(R.id.editEmailLogin);
        editSenha = findViewById(R.id.editSenhaLogin);
        checkLembrar = findViewById(R.id.checkLembrar);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        // Controla o estado da sessão de acordo com a preferência salva
        boolean manterLogado = sharedPreferences.getBoolean("manterLogado", false);
        if (!manterLogado) {
            mAuth.signOut();
        }

        btnEntrar.setOnClickListener(v -> logarUsuario());
        btnCadastrar.setOnClickListener(v -> cadastrarUsuario());
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        boolean manterLogado = sharedPreferences.getBoolean("manterLogado", false);

        if (currentUser != null && manterLogado) {
            irParaTelaPrincipal();
        }
    }

    private void logarUsuario() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha e-mail e senha para acessar!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("manterLogado", checkLembrar.isChecked());
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Acesso concedido!", Toast.LENGTH_SHORT).show();
                        irParaTelaPrincipal();
                    } else {
                        Toast.makeText(LoginActivity.this, "Erro ao acessar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void cadastrarUsuario() {
        String nome = editNome.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos incluindo o Nome para cadastrar!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "A senha precisa ter no mínimo 6 caracteres!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            // Vincula o nome diretamente ao ID gerado de forma segura e síncrona
                            mDatabase.child(user.getUid()).child("nome").setValue(nome);
                        }

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("manterLogado", checkLembrar.isChecked());
                        editor.apply();

                        Toast.makeText(LoginActivity.this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                        irParaTelaPrincipal();
                    } else {
                        Toast.makeText(LoginActivity.this, "Erro ao criar conta: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void irParaTelaPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}