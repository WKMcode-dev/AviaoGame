package com.example.aviao02;
// Em CadastroActivity.java

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aviao02.database.DBHelper;

public class CadastroActivity extends AppCompatActivity {

    // Chave para passar o ID do jogador para a GameActivity
    public static final String EXTRA_PLAYER_ID = "com.example.aviao02.PLAYER_ID";
    public static final String EXTRA_PLAYER_NAME = "com.example.aviao02.PLAYER_NAME"; // Ainda pode ser útil para exibição

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro); // Certifique-se que este é o layout correto

        EditText etNomeJogador = findViewById(R.id.etNomeJogador);
        Button btnConfirmar = findViewById(R.id.btnConfirmar);

        btnConfirmar.setOnClickListener(v -> {
            String nome = etNomeJogador.getText().toString().trim();
            if (nome.isEmpty()) {
                Toast.makeText(CadastroActivity.this, "Digite o nome do jogador", Toast.LENGTH_SHORT).show();
                return;
            }

            try (DBHelper dbHelper = new DBHelper(this)) {
                // Usar o novo método que retorna o ID
                long playerId = dbHelper.insertPlayerAndGetId(nome, 0); // Pontuação inicial 0

                if (playerId != -1) { // Sucesso se o ID não for -1
                    Toast.makeText(CadastroActivity.this, "Jogador salvo! ID: " + playerId, Toast.LENGTH_SHORT).show();
                    etNomeJogador.setText("");

                    // Iniciar a GameActivity
                    Intent intent = new Intent(CadastroActivity.this, GameActivity.class);
                    intent.putExtra(EXTRA_PLAYER_ID, playerId);
                    intent.putExtra(EXTRA_PLAYER_NAME, nome); // Opcional: passar o nome também para fácil exibição
                    startActivity(intent);

                    finish(); // Opcional: finalizar CadastroActivity

                } else {
                    Toast.makeText(CadastroActivity.this, "Erro ao salvar jogador", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(CadastroActivity.this, "Erro ao interagir com o banco de dados.", Toast.LENGTH_SHORT).show();
                Log.e("CadastroActivity", "Erro ao salvar jogador", e);
            }
        });
    }
}