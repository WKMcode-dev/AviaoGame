package com.example.aviao02;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.aviao02.database.DBHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class GameActivity extends Activity {
    public static final int REQUEST_CODE_PROFILE_UPDATE = 101;

    public static final String EXTRA_PLAYER_ID = "com.example.aviao02.PLAYER_ID";
    public static final String EXTRA_PLAYER_NAME = "com.example.aviao02.PLAYER_NAME";

    private GameView gameView;
    private FrameLayout pauseMenu;
    private FloatingActionButton menuButton;
    private Button btnResume, btnProfile, btnExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameView = findViewById(R.id.game_view);
        pauseMenu = findViewById(R.id.pause_menu);
        menuButton = findViewById(R.id.menu_button);

        btnResume = findViewById(R.id.btn_resume);
        btnProfile = findViewById(R.id.btn_profile);
        btnExit = findViewById(R.id.btn_exit);

        // Botão flutuante abre o menu de pausa
        menuButton.setOnClickListener(v -> {
            gameView.pauseGameLogic();
            pauseMenu.setVisibility(View.VISIBLE);
        });

        // "Continuar" retoma o jogo
        btnResume.setOnClickListener(v -> {
            pauseMenu.setVisibility(View.GONE);
            gameView.resumeGameLogic();
        });

        // "Meu Perfil" abre a tela de perfil com ID e nome
        btnProfile.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PerfilActivity.SHARED_PREFS_NAME, MODE_PRIVATE);
            long playerId = prefs.getLong(PerfilActivity.KEY_PLAYER_ID_LONG, -1);
            String playerName = prefs.getString(PerfilActivity.KEY_PLAYER_NAME, "Jogador");

            Intent intent = new Intent(this, PerfilActivity.class);
            intent.putExtra(EXTRA_PLAYER_ID, playerId); // Use suas constantes EXTRA_PLAYER_ID
            intent.putExtra(EXTRA_PLAYER_NAME, playerName); // Use suas constantes EXTRA_PLAYER_NAME

            startActivityForResult(intent, REQUEST_CODE_PROFILE_UPDATE);

        });

        // "Sair" finaliza a atividade
        btnExit.setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        if (pauseMenu.getVisibility() == View.VISIBLE) {
            pauseMenu.setVisibility(View.GONE);
            gameView.resumeGameLogic();
        } else {
            gameView.pauseGameLogic();
            pauseMenu.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pauseGameLogic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resumeGameLogic();
        }
    }

    public void savePlayerScore(final int score) {
        final DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = null;

        try {
            db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_SCORE, score);

            SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
            long playerId = prefs.getLong("player_id_long", -1);

            if (playerId == -1) {
                Log.e("GameActivity", "playerId inválido");
                return;
            }

            int updatedRows = db.update(
                    DBHelper.TABLE_PLAYER,
                    values,
                    DBHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(playerId)}
            );

            if (updatedRows == 0) {
                Log.e("GameActivity", "Nenhum jogador encontrado com ID: " + playerId);
            } else {
                Log.d("GameActivity", "Pontuação atualizada para o jogador ID: " + playerId);
            }

        } catch (Exception e) {
            Log.e("GameActivity", "Erro ao interagir com o banco de dados: " + e.getMessage());
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public void mostrarGameOverDialog(final int pontuacaoAtual, final int recorde) {
        runOnUiThread(() -> {
            String mensagem = getString(R.string.game_over_message, pontuacaoAtual, recorde);

            new AlertDialog.Builder(this)
                    .setTitle(R.string.game_over_title)
                    .setMessage(mensagem)
                    .setCancelable(false)
                    .setPositiveButton(R.string.continuar, (dialog, which) -> {
                        gameView.resetGame();
                        dialog.dismiss();
                    })
                    .setNegativeButton(R.string.sair, (dialog, which) -> {
                        finish();
                    })
                    .show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_perfil) {
            SharedPreferences prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
            long playerId = prefs.getLong("player_id_long", -1);
            String playerName = prefs.getString("player_name", "Jogador");

            Intent intent = new Intent(this, PerfilActivity.class);
            intent.putExtra(EXTRA_PLAYER_ID, playerId);
            intent.putExtra(EXTRA_PLAYER_NAME, playerName);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
