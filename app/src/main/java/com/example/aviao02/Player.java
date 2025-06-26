package com.example.aviao02;

public class Player {
    private final long id;
    private String name;
    private int score;

    // Construtor
    public Player(long id, String name, int score) {
        this.id = id;
        this.name = name;
        this.score = score;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    // Setters (se quiser permitir modificar depois)
    public void setName(String name) {
        this.name = name;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
