/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/** Reconhece comentários.
 *
 * @author sarah
 */
public class AutomatoComentario extends Automato {
    
    
    /** Verifica se é um comentário.
     * 
     * @param  c
     */
    public boolean isComentario(char c){
        
        switch(this.estado){
            
            case(0):{
                
                if(c == '/'){
                    this.estado = 1;
                    return true;
                }else{
                    return false;
                }
            }
            case(1):{
                
                if(c== '*'){
                    this.estado = 2;
                    return true;
                    
                } else if(c == '/'){
                    this.estado = 3;
                    return true;
                    
                }else {
                    return false;
                }
            
            }
            
            case(2):{
                
                if( c == '*'){
                    this.estado = 1;
                    return true;
                    
                } else{
                    return true;
                }
            }
            
            case (3):{
            
                return false;
            }
            
        }
        
        
        return false;
    }
    

    @Override
    public boolean isEstadoFinal() {
        return this.estado == 3;
    }
    
}
