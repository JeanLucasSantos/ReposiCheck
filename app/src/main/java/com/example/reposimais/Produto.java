package com.example.reposimais;

import java.io.Serializable;

public class Produto implements Serializable {
    private String id;
    private String nome;
    private int estoqueGeral;      // Saldo total no depósito da empresa
    private int capacidadeMax;     // Limite físico de quanto cabe na gôndola
    private int qtdGondola;       // Quantidade atual que está na prateleira
    private int qtdMinima;        // O gatilho de alerta para gerar tarefa

    // Construtor padrão necessário para o Firebase
    public Produto() {
    }

    public Produto(String id, String nome, int estoqueGeral, int capacidadeMax, int qtdGondola, int qtdMinima) {
        this.id = id;
        this.nome = nome;
        this.estoqueGeral = estoqueGeral;
        this.capacidadeMax = capacidadeMax;
        this.qtdGondola = qtdGondola;
        this.qtdMinima = qtdMinima;
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public int getEstoqueGeral() { return estoqueGeral; }
    public void setEstoqueGeral(int estoqueGeral) { this.estoqueGeral = estoqueGeral; }

    public int getCapacidadeMax() { return capacidadeMax; }
    public void setCapacidadeMax(int capacidadeMax) { this.capacidadeMax = capacidadeMax; }

    public int getQtdGondola() { return qtdGondola; }
    public void setQtdGondola(int qtdGondola) { this.qtdGondola = qtdGondola; }

    public int getQtdMinima() { return qtdMinima; }
    public void setQtdMinima(int qtdMinima) { this.qtdMinima = qtdMinima; }
}