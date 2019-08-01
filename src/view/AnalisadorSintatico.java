/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.util.ArrayList;
import java.util.Collection;
import util.Classe;
import util.Token;

/**
 *
 * @author User-PC
 */
public class AnalisadorSintatico {
    
    private ArrayList<Token> listaDeTokens;
    private ArrayList<String> tipo = new ArrayList();
    private Token token;
    
    public AnalisadorSintatico(ArrayList<Token> listaDeTokens){
        this.listaDeTokens = listaDeTokens;
        this.listaDeTokens.add(listaDeTokens.size(), new Token("$",Classe.FINALIZADOR,0)); //add o '$' no final da lista
        this.setup();
    }
    
    public void setup(){
        //Tipos de variaveis
        tipo.add("inteiro");
        tipo.add("real");
        tipo.add("vazio");
        tipo.add("boleano");
        tipo.add("texto");
    
        //Identificadores
    }
    
    public void executar(){
     
        this.token = proximo_token();
        
        programa();
        
        if(this.token.getValor().equals("$")){
            System.out.println("SUCESSO: cdigo encerrado com o caractere finalizador $");
        }else{
            System.out.println("ERRO: codigo encerrado sem atingir o $");
        }
        
    }
    
    public Token proximo_token(){
        Token t = listaDeTokens.remove(0);
        System.out.println("Token em análise: "+t.getValor());
        return t;
    }

    private void programa() {
        
        if(this.token.getValor().equals("programa")){
            
            this.token = proximo_token();
            
            if(this.token.getValor().equals("{")){
                
                this.token = proximo_token();
                blocoConstantes();
                this.token= proximo_token();
                escopoPrograma();
                this.token = proximo_token();
                
                if(this.token.getValor().equals("}")){
                    return;
                }else{
                    //System.out.println("ERRO: está faltando o simbolo }");
                    return;
                }
                
            }else{
                System.out.println("ERRO: está faltando o simbolo {");
            }
            
        }else{
            System.out.println("ERRO: está faltando a palavra 'programa'");
        }
    
    }

    private void blocoConstantes() {
        
        if(this.token.getValor().equals("constantes")){
            
            this.token = proximo_token();
            
            if(this.token.getValor().equals("{")){
                
                this.token = proximo_token();
                estruturaConstantes();
                
                if(this.token.getValor().equals("}")){
                    return;
                }else{
                    System.out.println("ERRO: está faltando o simbolo }");
                }
                
            }else{
                System.out.println("ERRO: está faltando o simbolo {");
            }
            
        }else if(this.token.getValor().equals("")){
            this.token = proximo_token();
            return;
        }
        else{
            System.out.println("ERRO: está faltando a palavra 'constantes'");
        }
    
    }

    private void escopoPrograma() {
        if(this.token.pertenceAoPrimeiroDe("metodo")){
            this.token = proximo_token();
            if(this.token.pertenceAoPrimeiroDe("escopoPrograma")){
                this.token = proximo_token();
                return;
            }else{
                System.out.println("ERRO: falta a assinatura do metodo");
            }
        }else if(this.token.getValor().equals("}")){//mesmo problema checando vazio, melhor checar o }
            //this.token = proximo_token();
            return;
        }else{
            System.out.println("ERRO: falta a palavra metodo");
        }
    }

    private void estruturaConstantes() {
        if(this.tipo.contains(this.token.getValor())){ //se token == tipo
            this.token = proximo_token();
            constantes();
            if(this.token.getValor().equals(";")){
                this.token = proximo_token();
                estruturaConstantes();
            }
        }else if(this.token.getValor().equals("}")){//mesmo problema se checar vazio
            //this.token = proximo_token();
            return;
        }
    }

    private void constantes() {
        if(this.token.getValor().matches("([a-zA-Z]|[0-9]|_)*")){ //token == identificador
            this.token = proximo_token();
            if(this.token.getValor().equals("=")){
                this.token = proximo_token();
                constante();
                //this.token = proximo_token(); constante já faz isso
                multiConst();
            }else{
                System.out.println("ERRO: faltou o caractere =");
            }
        }else{
            System.out.println("ERRO: declaração de constante sem identificador");
        }
    }

    private void constante() {
        if(this.token.getValor().matches("([a-zA-Z]|[0-9]|_)*") |  this.token.getValor().matches("\"([a-zA-Z])*\"") |
                  this.token.getValor().matches("-?[0-9]+(.[0-9]+)?") ){
            this.token = proximo_token();
        }else{
            System.out.println("ERRO: atribuição de constante sem Numero/CadeiaCaracteres/Identificador");
        }
    }

    private void multiConst() {
        if(this.token.pertenceAoPrimeiroDe("multiplasConstantes")){
            //this.token = proximo_token();
            multiplasConstantes();
        }else if(this.token.getValor().equals(";")){ /*não vale a pena checar se é igual a vazio, pq se a linha inteiro a = 2;
                                              quando o token for ; ele vai querer vazio, e vai dar erro sem estar errado. Outra opção
                                                é manter só o if e tirar o resto, funciona tb.*/
            return;
        }else{
            System.out.println("ERRO: gramatica de multiplas constantes equivocada");
        }
    }

    private void multiplasConstantes() {
        if(this.token.getValor().equals(",")){
            this.token = proximo_token();
            constantes();
        }else{
            System.out.println("ERRO: faltou virugula na declaração de multiplas constantes");
        }
    }
}
