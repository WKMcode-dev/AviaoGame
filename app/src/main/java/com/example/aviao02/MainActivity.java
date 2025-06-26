package com.example.aviao02;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.aviao02.database.DBHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        EditText etNomeJogador = findViewById(R.id.etNomeJogador);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(v -> {
            String nome = etNomeJogador.getText().toString().trim();

            if (nome.isEmpty()) {
                Toast.makeText(MainActivity.this, "Digite um nome", Toast.LENGTH_SHORT).show();
                return;
            }


            try (DBHelper dbHelper = new DBHelper(MainActivity.this)) {
                dbHelper.addPlayer(nome);
                Toast.makeText(MainActivity.this, "Jogador salvo com sucesso!", Toast.LENGTH_SHORT).show();
                etNomeJogador.setText(""); // Limpa o campo
            } catch (Exception e) {

                Toast.makeText(MainActivity.this, "Erro ao salvar jogador.", Toast.LENGTH_SHORT).show();

            }
        });
    }
}