/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author User-PC
 */
public enum PalavraReservada {
    
   PROGRAMA("programa"), CONSTANTES("constantes"), VARIAVEIS("variaveis"), METODO("metodo"), RESULTADO("resultado"),
   PRINCIPAL("principal"), SE("se"), ENTAO("entao"), SENAO("senao"),ENQUANTO("enquanto"), LEIA("leia"), ESCREVA("escreva"),
   VAZIO("vazio"),INTEIRO("inteiro"), REAL("real"), BOOLEANO("booleano"), TEXTO("texto"),VERDADEIRO("verdadeiro"), FALSO("falso");
   //Palavras reservadas disponíveis 
    
    
   private final String palavra;
   
   /** @param modo
    */
   PalavraReservada(String palavra){
       this.palavra = palavra;
   }
        
   /** Retorna a string que define a classe do lexema.
    * 
    * @return 
    */
   public String getPalavraReservada()
   {
       return this.palavra;
   }
}
