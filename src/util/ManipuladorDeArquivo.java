/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Esta classe implementa métodos para manipulação de arquivo. O arquivo pode
 * ser aberto em formado de leitura ou escrita. Se aberto em formato de leitura
 * é possível ler o arquivo caracter por caracter, mas se for aberto em formato
 * de escrita um aquivo é criado e é possível escrever nesse arquivo.
 *
 * @author sarah
 */
public class ManipuladorDeArquivo {

    private InputStreamReader scanner;          //Ler arquivo
    private FileInputStream canal;              //Canal para leitura do arquivo
    private FileWriter arquivo;                 //Abre arquivo para escrita
    private PrintWriter escrevearq;             //Escreve no Arquivo
    private Modo modo;                          //Modo do manipulador de arquivo
    
    
    /**
     * 
     * @param nomedoarquivo Nome do arquivo de leitura
     * @param  modo Modo em que o manipulador de arquivo vai operar: escrita ou leitura.
     * @throws java.io.FileNotFoundException Exceção para Arquivo não encontrado
     * @throws IOException Erro na criação de arquivo
     */
    public ManipuladorDeArquivo(String nomedoarquivo, Modo modo) throws FileNotFoundException, IOException {

        if (modo.getModo().equalsIgnoreCase("leitura")) {
        	this.modo = modo;
            abreArquivo(nomedoarquivo);
            this.escrevearq = null;
            this.arquivo = null;
        } else {
        	this.modo = modo;
            criarArquivo(nomedoarquivo);
            this.canal = null;
            this.scanner = null;

        }

    }
    
    /** Abre arquivo para leitura.
     * 
     * @param nomedoarquivo Nome do arquivo de leitura
     */
    private void abreArquivo(String nomedoarquivo) throws FileNotFoundException {

        this.canal = new FileInputStream(nomedoarquivo);
        this.scanner = new InputStreamReader(canal);

    }

    /** Verifica se há mais caracteres para ler.
     * 
     * @return Verdadeiro se houver mais caracteres.
     * @throws java.io.IOException
     * @throws util.ModoException Exceção caso o método seja chamado em modo de escrita.
     */
    public boolean hasNextCaractere() throws IOException, ModoException {

        if (this.modo.getModo().equalsIgnoreCase("escrita")) {
            throw new ModoException("Esse método só pode ser usado em modo de leitura");
        }
        return this.scanner.ready();
    }

    /** Retorna o próximo caractere.
     * @return Retorna próximo caractere em formado de char.
     * @throws java.io.IOException
     * @throws util.ModoException Exceção caso o arquivo esteja em modo de escrita.
     */
    public char nextCaractere() throws IOException, ModoException {
        if (this.modo.getModo().equalsIgnoreCase("escrita")) {
            throw new ModoException("Esse método só pode ser usado em modo de leitura");
        }

        return (char) this.scanner.read();

    }

    
    /** Fecha o arquivo quando não for ser utilizado.
     * @throws java.io.IOException
     */
    public void fechaArquivo() throws IOException {

        if (this.modo.getModo().equalsIgnoreCase("escrita")) {

            this.arquivo.close();
            this.escrevearq = null;

        } else {

            this.canal.close();
            this.scanner = null;

        }
    }

    /** Cria um arquivo para escrita.
     * @param nomedoarquivo Nome do arquivo a ser criado.
     */
    private void criarArquivo(String nomedoarquivo) throws IOException {

        this.arquivo = new FileWriter(nomedoarquivo);
        this.escrevearq = new PrintWriter(this.arquivo);

    }
    
    
    /** Escreve no arquivo.
     * 
     * @param mensagem Mensagem a ser escrita no arquivo de saída.
     * @throws util.ModoException
     */
    public void escreverArquivo(String mensagem) throws ModoException {

        if (this.modo.getModo().equalsIgnoreCase("leitura")) {
            throw new ModoException("Esse método só pode ser usado em modo de escrita");
        }

        this.escrevearq.print(mensagem);

    }

}
