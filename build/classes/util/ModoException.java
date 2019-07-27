/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/** Exceção para quando métodos do manipulador de arquivos for chamado em modo 
 * errado.
 *
 * @author sarah
 */
public class ModoException extends Exception {
    
    private final String mensagem; //Mensagem da exceção
    
    /** 
     * @param  msg Mensagem de exceção.
     */
    public ModoException(String msg){
        this.mensagem = msg;
    }
    
    
    /** Retorna a mensagem de exceção
     * 
     * @return 
     */
    public String getMensagem(){
        return this.mensagem;
    }
}
