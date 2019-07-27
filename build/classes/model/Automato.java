/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author sarah
 */
public abstract class Automato {

    protected int estado;
    
    
    public Automato(){
        this.estado =0;
    }

    /**
     * Faz o automato voltar ao estado inicial.
     */
    public void resetAutomato() {
        this.estado = 0;
    }

    /**
     * Verifica se o automato está no estado final.
     *
     * @return
     */
    public abstract boolean isEstadoFinal() ;

}
