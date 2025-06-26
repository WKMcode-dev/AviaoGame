package com.example.aviao02;

import android.app.AlertDialog;

import android.os.Bundle;
import android.text.InputType;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.aviao02.database.DBHelper;

import java.util.ArrayList;
import java.util.List;

public class PlayerListActivity extends AppCompatActivity {

    private ListView listViewPlayers;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);

        dbHelper = new DBHelper(this);
        listViewPlayers = findViewById(R.id.listViewPlayers);

        loadPlayers();

        listViewPlayers.setOnItemClickListener((parent, view, position, id) -> {
            List<Player> playersList = dbHelper.getAllPlayers();
            Player player = playersList.get(position);
            showEditDeleteDialog(player);
        });
    }

    private void loadPlayers() {
        List<Player> playersList = dbHelper.getAllPlayers();
        List<String> playerNames = new ArrayList<>();
        for (Player p : playersList) {
            playerNames.add(p.getName() + " - Score: " + p.getScore());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, playerNames);
        listViewPlayers.setAdapter(adapter);
    }


    private void showEditDeleteDialog(Player player) {
        CharSequence[] options = {"Editar nome", "Apagar jogador", "Cancelar"};

        new AlertDialog.Builder(this)
                .setTitle("Ações para " + player.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Editar nome
                            showEditNameDialog(player);
                            break;
                        case 1: // Apagar jogador
                            deletePlayer(player);
                            break;
                        case 2: // Cancelar
                        default:
                            dialog.dismiss();
                            break;
                    }
                })
                .show();
    }

    private void showEditNameDialog(Player player) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar nome");

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(player.getName());
        builder.setView(input);

        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                boolean updated = dbHelper.updatePlayerName(player.getId(), newName);
                if (updated) {
                    Toast.makeText(PlayerListActivity.this, "Nome atualizado!", Toast.LENGTH_SHORT).show();
                    loadPlayers();
                } else {
                    Toast.makeText(PlayerListActivity.this, "Erro ao atualizar nome.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deletePlayer(Player player) {
        new AlertDialog.Builder(this)
                .setTitle("Confirma exclusão?")
                .setMessage("Quer apagar o jogador " + player.getName() + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    boolean deleted = dbHelper.deletePlayer(player.getId());
                    if (deleted) {
                        Toast.makeText(PlayerListActivity.this, "Jogador apagado!", Toast.LENGTH_SHORT).show();
                        loadPlayers();
                    } else {
                        Toast.makeText(PlayerListActivity.this, "Erro ao apagar jogador.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
