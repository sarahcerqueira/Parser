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
public enum Classe {
   
   NULL("NULL"), ERRO("ERRO"), IDENTIFICADOR("IDENTIFICADOR"), NUMERO("NUMERO"), DELIMITADOR("DELIMITADOR"), CADEIA_DE_CARACTERES("CADEIA DE CARACTERES"),
   COMENTARIO("COMENTARIO"), OPERADOR_LOGICO("OPERADOR LOGICO"), OPERADOR_ARITMETICO("OPERADOR ARITMETICO"), OPERADOR_RELACIONAL("OPERADOR RELACIONAL"),
   PALAVRA_RESERVADA("PALAVRA RESERVADA");
   //Classes disponíveis 
    
    
   private final String classe;
   
   /** @param modo
    */
   Classe(String classe){
       this.classe = classe;
   }
        
   /** Retorna a string que define a classe do lexema.
    * 
    * @return 
    */
   public String getClasse()
   {
       return this.classe;
   }

}
