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
    
    public boolean pertenceAoPrimeiroDe(String naoTerminal){
        if(naoTerminal.equals("escopoPrograma")){
            return (this.token.getValor().equals("metodo") | this.token.getValor().equals(""));
        }
        
        return false;
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
                    System.out.println("ERRO: está faltando o simbolo }");
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
        }else if(this.token.getValor().equals("")){
            this.token = proximo_token();
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
        }else if(this.token.getValor().equals("")){
            this.token = proximo_token();
            return;
        }
    }

    private void constantes() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
