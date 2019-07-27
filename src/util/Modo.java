/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/** Enum para os modos de abertura dos arquivos.
 *
 * @author sarah
 */
public enum Modo {
    
    LEITURA("leitura"), ESCRITA("escrita"); //Modos disponíveis 
    
    
   private final String modo;
   
   /** @param modo
    */
   Modo(String modo){
       this.modo = modo;
   }
        
   /** Retorna a string de define do modo.
    * 
    * @return 
    */
   public String getModo()
   {
       return this.modo;
   }


}
    
    
