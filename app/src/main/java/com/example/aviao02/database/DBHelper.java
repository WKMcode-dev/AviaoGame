package com.example.aviao02.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.aviao02.Player;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper implements AutoCloseable {

    public static final String DB_NAME = "jogo.db";
    public static final int DB_VERSION = 1;

    public static final String TABLE_PLAYER = "player";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SCORE = "score";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_PLAYER + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_SCORE + " INTEGER DEFAULT 0)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYER);
        onCreate(db);
    }

    public long insertPlayerAndGetId(String name, int score) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_SCORE, score);

        long result = -1;
        try {
            result = db.insert(TABLE_PLAYER, null, values);
            if (result == -1) {
                Log.e("DBHelper", "Erro ao inserir jogador: " + name);
            } else {
                Log.d("DBHelper", "Jogador inserido com sucesso. ID: " + result + ", Nome: " + name);
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Exceção ao inserir jogador: " + name, e);
        }
        return result;
    }

    public void addPlayer(String name) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_SCORE, 0);

            long result = db.insert(TABLE_PLAYER, null, values);

            if (result == -1) {
                Log.e("DBHelper", "Erro ao inserir jogador (via addPlayer)");
            } else {
                Log.d("DBHelper", "Jogador inserido com sucesso (via addPlayer). ID: " + result);
            }
        } finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
    }

    public boolean updatePlayerScore(long playerId, int newScore) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCORE, newScore);

        int rowsAffected;
        try {
            rowsAffected = db.update(TABLE_PLAYER, values, COLUMN_ID + " = ?", new String[]{String.valueOf(playerId)});
        } catch (Exception e) {
            Log.e("DBHelper", "Erro ao atualizar pontuação do jogador ID: " + playerId, e);
            return false;
        }
        return rowsAffected > 0;
    }

    // **NOVO**: Atualizar o nome do jogador
    public boolean updatePlayerName(long playerId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, newName);

        int rowsAffected;
        try {
            rowsAffected = db.update(TABLE_PLAYER, values, COLUMN_ID + " = ?", new String[]{String.valueOf(playerId)});
        } catch (Exception e) {
            Log.e("DBHelper", "Erro ao atualizar nome do jogador ID: " + playerId, e);
            return false;
        }
        return rowsAffected > 0;
    }

    // **NOVO**: Deletar o jogador pelo ID
    public boolean deletePlayer(long playerId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int rowsDeleted;
        try {
            rowsDeleted = db.delete(TABLE_PLAYER, COLUMN_ID + " = ?", new String[]{String.valueOf(playerId)});
        } catch (Exception e) {
            Log.e("DBHelper", "Erro ao deletar jogador ID: " + playerId, e);
            return false;
        }
        return rowsDeleted > 0;
    }

    @Override
    public void close() {
        super.close();
        Log.d("DBHelper", "Banco de dados fechado.");
    }

    public List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_PLAYER,
                    new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_SCORE},
                    null, null, null, null, COLUMN_ID + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                    int score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
                    players.add(new Player(id, name, score));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DBHelper", "Erro ao buscar jogadores", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return players;
    }

}
