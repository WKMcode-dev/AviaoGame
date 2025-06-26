package com.example.aviao02;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.aviao02.database.DBHelper;

public class PerfilActivity extends AppCompatActivity {

    public static final String SHARED_PREFS_NAME = "game_prefs";
    public static final String KEY_PLAYER_ID_LONG = "player_id_long";
    public static final String KEY_PLAYER_NAME = "player_name";
    public static final String KEY_HIGH_SCORE = "high_score";
    public static final int REQUEST_CODE_PROFILE_UPDATE = 101;

    private TextView tvPlayerIdValue;
    private EditText editPlayerName;
    private TextView tvPlayerRecordValue;
    private Button btnSaveProfile;

    private SharedPreferences prefs;
    private DBHelper dbHelper;
    private long currentPlayerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        dbHelper = new DBHelper(this);
        prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);

        tvPlayerIdValue = findViewById(R.id.tv_player_id_value);
        editPlayerName = findViewById(R.id.edit_player_name);
        tvPlayerRecordValue = findViewById(R.id.tv_player_record_value);
        btnSaveProfile = findViewById(R.id.btn_save_profile);

        Intent intent = getIntent();
        if (intent != null) {
            currentPlayerId = intent.getLongExtra(GameActivity.EXTRA_PLAYER_ID, -1);
        }

        if (currentPlayerId == -1) {
            Toast.makeText(this, "Erro: ID do jogador não encontrado.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadPlayerData();

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePlayerName();
            }
        });
    }

    private void loadPlayerData() {
        if (currentPlayerId == -1) return;

        tvPlayerIdValue.setText(String.valueOf(currentPlayerId));

        String savedName = prefs.getString(KEY_PLAYER_NAME, "");
        if (TextUtils.isEmpty(savedName)) {
            loadNameFromDB(currentPlayerId);
        } else {
            editPlayerName.setText(savedName);
        }

        int record = prefs.getInt(KEY_HIGH_SCORE, 0);
        tvPlayerRecordValue.setText(String.valueOf(record));
    }

    private void loadNameFromDB(long playerId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = dbHelper.getReadableDatabase();
            cursor = db.query(
                    DBHelper.TABLE_PLAYER,
                    new String[]{DBHelper.COLUMN_NAME},
                    DBHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(playerId)},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                int nameColumnIndex = cursor.getColumnIndex(DBHelper.COLUMN_NAME);
                if (nameColumnIndex != -1) {
                    String nameDB = cursor.getString(nameColumnIndex);
                    editPlayerName.setText(nameDB);
                }
            }
        } catch (Exception e) {
            Log.e("PerfilActivity", "Error loading name from DB: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void savePlayerName() {
        String newName = editPlayerName.getText().toString().trim();

        if (TextUtils.isEmpty(newName)) {
            Toast.makeText(this, "O nome não pode ser vazio.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentPlayerId == -1) {
            Toast.makeText(this, "Erro: ID do jogador inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean dbSuccess = false;
        SQLiteDatabase db = null;
        try {
            db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(DBHelper.COLUMN_NAME, newName);

            int rowsAffected = db.update(
                    DBHelper.TABLE_PLAYER,
                    values,
                    DBHelper.COLUMN_ID + "=?",
                    new String[]{String.valueOf(currentPlayerId)}
            );
            dbSuccess = rowsAffected > 0;
        } catch (Exception e) {
            Log.e("PerfilActivity", "Error updating name in DB: " + e.getMessage());
            Toast.makeText(this, "Erro ao salvar nome no banco de dados.", Toast.LENGTH_SHORT).show();
        }

        if (dbSuccess) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_PLAYER_NAME, newName);
            editor.apply();
            Toast.makeText(this, "Nome atualizado com sucesso!", Toast.LENGTH_SHORT).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra(KEY_PLAYER_NAME, newName);
            setResult(AppCompatActivity.RESULT_OK, resultIntent);
            finish();
        } else {
            // Se dbSuccess é false mas não houve exceção, pode ser que o nome era o mesmo
            // Ainda assim, vamos garantir que SharedPreferences esteja atualizado e retornar OK
            // para que a GameActivity possa tentar recarregar.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_PLAYER_NAME, newName);
            editor.apply();
            Toast.makeText(this, "Nome salvo.", Toast.LENGTH_SHORT).show();
            Intent resultIntent = new Intent();
            resultIntent.putExtra(KEY_PLAYER_NAME, newName);
            setResult(AppCompatActivity.RESULT_OK, resultIntent);
            finish();
        }
    }
}