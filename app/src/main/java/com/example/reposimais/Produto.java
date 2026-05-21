package com.example.reposimais;

public class Produto {
    public String codigo;
    public String descricao;
    public int estoqueEmpresa; // Representa a quantidade atual na gôndola/empresa
    public int qtdMinima;      // Limite mínimo cadastrado para gerar o alerta

    // Construtor vazio obrigatório para o Firebase Realtime Database conseguir converter os dados
    public Produto() {
    }

    // Construtor completo e enxuto para criar produtos novos
    public Produto(String codigo, String descricao, int estoqueEmpresa, int qtdMinima) {
        this.codigo = codigo;
        this.descricao = descricao;
        this.estoqueEmpresa = estoqueEmpresa;
        this.qtdMinima = qtdMinima;
    }
}