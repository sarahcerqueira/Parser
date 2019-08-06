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
    private final String identificador = "[a-zA-Z]([a-zA-Z]|[0-9]|_)*";
    private final String digito = "-?[0-9]+(.[0-9]+)?";
    private final String cadeiaCaracteres = "\"([a-zA-Z])*\"";
    //private final String operadoresAritmeticos = "(+ | - | * | /)";
    

    public AnalisadorSintatico(ArrayList<Token> listaDeTokens) {
        this.listaDeTokens = listaDeTokens;
        this.listaDeTokens.add(listaDeTokens.size(), new Token("$", Classe.FINALIZADOR, 0)); //add o '$' no final da lista
        this.setup();
    }

    public void setup() {
        //Tipos de variaveis
        tipo.add("inteiro");
        tipo.add("real");
        tipo.add("vazio");
        tipo.add("boleano");
        tipo.add("texto");

        //Identificadores
    }

    //fiz esse método só pra testar se tem algo na lista mesmo.
    private void estaPresenteNaListaDeTokens(String s) {

        for (Token t : this.listaDeTokens) {
            if (t.getValor().equals(s)) {
                System.out.println("Sim");
            }
        }

        System.out.println("Não");
    }

    public void executar() {
        //estaPresenteNaListaDeTokens(":");
        this.token = proximo_token();

        programa();

        if (this.token.getValor().equals("$")) {
            System.out.println("SUCESSO: cdigo encerrado com o caractere finalizador $");
        } else {
            System.out.println("ERRO: codigo encerrado sem atingir o $");
        }

    }

    public Token proximo_token() {
        Token t = listaDeTokens.remove(0);
        System.out.println("Token em análise: " + t.getValor());
        return t;
    }

    private void programa() {

        if (this.token.getValor().equals("programa")) {

            this.token = proximo_token();

            if (this.token.getValor().equals("{")) {

                this.token = proximo_token();
                blocoConstantes();
                this.token = proximo_token(); //"metodo"
                escopoPrograma();

                if (this.token.getValor().equals("}")) {
                    return;
                } else {
                    //System.out.println("ERRO: está faltando o simbolo }");
                    return;
                }

            } else {
                System.out.println("ERRO: está faltando o simbolo {");
            }

        } else {
            System.out.println("ERRO: está faltando a palavra 'programa'");
        }

    }

    private void blocoConstantes() {

        if (this.token.getValor().equals("constantes")) {

            this.token = proximo_token();

            if (this.token.getValor().equals("{")) {

                this.token = proximo_token();
                estruturaConstantes();

                if (this.token.getValor().equals("}")) {
                    return;
                } else {
                    System.out.println("ERRO: está faltando o simbolo }");
                }

            } else {
                System.out.println("ERRO: está faltando o simbolo {");
            }

        } else if (this.token.getValor().equals("")) {
            this.token = proximo_token();
            return;
        } else {
            System.out.println("ERRO: está faltando a palavra 'constantes'");
        }

    }

    private void escopoPrograma() {
        if (this.token.pertenceAoPrimeiroDe("metodo")) {
            metodo();//token = "metodo"
            this.token = proximo_token();

            if (this.token.pertenceAoPrimeiroDe("escopoPrograma")) {
                this.token = proximo_token();
                return;
            } else {
                //System.out.println("ERRO: falta a assinatura do metodo");
            }
        } else if (this.token.getValor().equals("}")) {//mesmo problema checando vazio, melhor checar o }
            //this.token = proximo_token();
            return;
        } else {
            System.out.println("ERRO: falta a palavra metodo");
        }
    }

    private void estruturaConstantes() {
        if (this.tipo.contains(this.token.getValor())) { //se token == tipo
            this.token = proximo_token();
            constantes();
            if (this.token.getValor().equals(";")) {
                this.token = proximo_token();
                estruturaConstantes();
            }
        } else if (this.token.getValor().equals("}")) {//mesmo problema se checar vazio
            //this.token = proximo_token();
            return;
        }
    }

    private void constantes() {
        if (this.token.getValor().matches("([a-zA-Z]|[0-9]|_)*")) { //token == identificador
            this.token = proximo_token();
            if (this.token.getValor().equals("=")) {
                this.token = proximo_token();
                constante();
                //this.token = proximo_token(); constante já faz isso
                multiConst();
            } else {
                System.out.println("ERRO: faltou o caractere =");
            }
        } else {
            System.out.println("ERRO: declaração de constante sem identificador");
        }
    }

    private void constante() {
        if (this.token.getValor().matches("[a-zA-Z]([a-zA-Z]|[0-9]|_)*") | this.token.getValor().matches("\"([a-zA-Z])*\"")
                | this.token.getValor().matches("-?[0-9]+(.[0-9]+)?")) {
            this.token = proximo_token();
        } else {
            System.out.println("ERRO: atribuição de constante sem Numero/CadeiaCaracteres/Identificador");
        }
    }

    private void multiConst() {
        if (this.token.pertenceAoPrimeiroDe("multiplasConstantes")) {
            //this.token = proximo_token();
            multiplasConstantes();
        } else if (this.token.getValor().equals(";")) { /*não vale a pena checar se é igual a vazio, pq se a linha inteiro a = 2;
             quando o token for ; ele vai querer vazio, e vai dar erro sem estar errado. Outra opção
             é manter só o if e tirar o resto, funciona tb.*/

            return;
        } else {
            System.out.println("ERRO: gramatica de multiplas constantes equivocada");
        }
    }

    private void multiplasConstantes() {
        if (this.token.getValor().equals(",")) {
            this.token = proximo_token();
            constantes();
        } else {
            System.out.println("ERRO: faltou virugula na declaração de multiplas constantes");
        }
    }

    private void leia() {
        if (this.token.getValor().equals("leia")) {
            this.token = proximo_token();

            if (this.token.getValor().equals("(")) {
                this.token = proximo_token();
                conteudoLeia();

                if (this.token.getValor().equals(")")) {
                    this.token = proximo_token();

                    if (this.token.getValor().equals(";")) {
                        this.token = proximo_token();

                    } else {
                        System.out.println("ERRO: faltou ponto e virgula no leia");
                    }
                } else {
                    System.out.println("ERRO: faltou )");
                }
            } else {
                System.out.println("ERRO: faltou (");
            }
        } else {
            System.out.println("ERRO: faltou leia");

        }
    }

    private void conteudoLeia() {

        if (this.token.getValor().matches(this.identificador)) {
            this.token = proximo_token();
            vetor();
            lermais();
        } else {
            System.out.println("ERRO");
        }

    }

    private void lermais() {
        if (this.token.getValor().equals(",")) {
            this.token = proximo_token();
            conteudoLeia();
        }
    }

    private void opIndice() {
        if (this.token.getClasse().equals(Classe.OPERADOR_ARITMETICO.getClasse())) {
            this.token = proximo_token();
            opI2();
            opIndice();
        }

    }

    private void opI2() {
        if (this.token.getValor().matches(this.digito)) {
            this.token = proximo_token();

        } else if (this.token.getValor().matches(this.identificador)) {
            this.token = proximo_token();

        } else {
            System.out.println("ERRO");
        }
    }

    private void vetor() {
        if (this.token.getValor().equals("[")) {
            this.token = proximo_token();
            opI2();
            opIndice();

            if (this.token.getValor().equals("]")) {
                this.token = proximo_token();
                matriz();
            } else {
                System.out.println("ERRO");

            }
        }
    }

    private void matriz() {
        if (this.token.getValor().equals("[")) {
            this.token = proximo_token();
            opI2();
            opIndice();

            if (this.token.equals(']')) {
                this.token = proximo_token();

            } else {
                System.out.println("ERRO");

            }
        }
    }

    private void metodo() {

        if (this.token.getValor().equals("metodo")) {
            this.token = proximo_token();

            if (this.token.getValor().matches(this.identificador)) {
                this.token = proximo_token();

                if (this.token.getValor().equals("(")) {
                    this.token = proximo_token();
                    listaParametros();
                    if (this.token.getValor().equals(")")) {
                        this.token = new Token(":", Classe.NULL, 0); System.out.println("Token em analise: :");//remove essa linha quando concertar o léxico
                        //this.token = proximo_token();//põe essa devolta
                        if (this.token.getValor().equals(":")) {//poe devolta quando consertar o lexico
                            this.token = proximo_token();
                            //System.out.println(token);
                            if (this.tipo.contains(this.token.getValor())) {
                                this.token = proximo_token();

                                if (this.token.getValor().equals("{")) {
                                    this.token = proximo_token();
                                    declaracaoVariaveis();
                                    escopoMetodo();

                                    if (this.token.getValor().equals("}")) {
                                        this.token = proximo_token();

                                    } else {
                                        System.out.println("ERRO: faltou o }");

                                    }

                                } else {
                                    System.out.println("ERRO: faltou o {");

                                }

                            } else {
                                System.out.println("ERRO:faltou o tipo do retorno");

                            }

                        } else {
                            System.out.println("ERRO: faltou o :");

                        }
                    } else {
                        System.out.println("ERRO: faltou o )");

                    }
                } else {
                    System.out.println("ERRO: faltou o (");

                }
            } else {
                System.out.println("ERRO:falta identificação do metodo");

            }
        } else {
            System.out.println("ERRO:falta a palavra metodo");

        }
    }

    private void listaParametros() {
        if (this.tipo.contains(this.token.getValor())) {
            this.token = proximo_token();

            if (this.token.getValor().matches(this.identificador)) {
                this.token = proximo_token();
                maisParametros();

            } else {
                System.out.println("ERRO");

            }

        }
    }

    private void maisParametros() {

        if (this.token.getValor().equals(",")) {
            this.token = proximo_token();
            listaParametros();
        }

    }

    private void escopoMetodo() {
        if(this.token.pertenceAoPrimeiroDe("comandos")){
            comandos();
            escopoMetodo();
        }
    }

    private void chamadaDeMetodo() {
        if(this.token.getValor().matches(identificador)){
            this.token = proximo_token();
            if(this.token.getValor().equals("(")){
                var();
                if(this.token.getValor().equals(")")){
                    this.token = proximo_token();
                }else{
                    System.out.println("ERRO");
                }
            }else{
                System.out.println("ERRO");
            }
        }else{
            System.out.println("ERRO");
        }
    }

    private void var() {
        if(this.token.getValor().matches(identificador)){
            this.token = proximo_token();
            vetor();
            maisVariavel();
        }else if(this.token.pertenceAoPrimeiroDe("metodoParametro")){
            
        }else{
            
        }
    }

    private void maisVariaveis() {
        if(this.tipo.contains(this.token.getValor())){ //Primeiro("VarV") == Tipo
            varV();
        }
    }

    private void metodoParametro() {
        if(this.token.getValor().matches(identificador)){
            this.token = proximo_token();
            if(this.token.getValor().equals("(")){
                this.token = proximo_token();
                var();
                if(this.token.getValor().equals(")")){
                    maisVariavel();
                }else{
                    System.out.println("ERRO");
                }
            }else{
                System.out.println("ERRO");
            }
        }else{
            System.out.println("ERRO");
        }
    }

    private void declaracaoVariaveis() {
        if(this.token.getValor().equals("variaveis")){
            this.token = proximo_token();
            if(this.token.getValor().equals("{")){
                this.token = proximo_token();
                varV();
                if(this.token.getValor().equals("}")){
                    
                }else{
                    System.out.println("ERRO");
                }
            }else{
                System.out.println("ERRO");
            }
        }else{
            System.out.println("ERRO");
        }
    }

    private void varV() {
        if(this.tipo.contains(this.token.getValor())){
            this.token = proximo_token();
            complementoV();
            maisVariaveis();
        }else{
            System.out.println("ERRO");
        }
    }

    private void complementoV() {
       if(this.token.getValor().matches(identificador)){
           this.token = proximo_token();
           vetor();
           variavelMesmoTipo();
       }
    }

    private void variavelMesmoTipo() {
        switch (this.token.getValor()) {
            case ",":
                this.token = proximo_token();
                complementoV();
                break;
            case ";":
                this.token = proximo_token();
                break;
            default:
                System.out.println("ERRO");
                break;
        }
    }

    private void maisVariavel() {
        if(this.token.getValor().equals(",")){
            this.token = proximo_token();
            var();
        }
    }

    private void comandos() {
        if(this.token.pertenceAoPrimeiroDe("leia")){
            leia();
        }else if(this.token.pertenceAoPrimeiroDe("escreva")){
            escreva();
        }else if(this.token.pertenceAoPrimeiroDe("se")){
            se();
        }else if(this.token.pertenceAoPrimeiroDe("enquanto")){
            enquanto();
        }else if(this.token.pertenceAoPrimeiroDe("atribuicaoVariavel")){
            atribuicaoVariavel();
        }else if(this.token.pertenceAoPrimeiroDe("chamadaDeMetodo")){
            chamadaDeMetodo();
            if(this.token.getValor().equals(";")){
                this.token = proximo_token();
            }else{
                System.out.println("ERRO");
            }
        }else if(this.token.pertenceAoPrimeiroDe("incrementador")){
            incrementador();
        }else if(this.token.getValor().equals("resultado")){
            this.token = proximo_token();
            retorno();
            if(this.token.getValor().equals(";")){
                this.token = proximo_token();
            }else{
                System.out.println("ERRO");
            }
        }
            
    }

    private void escreva() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void se() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void enquanto() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void atribuicaoVariavel() {
        if(this.token.getValor().matches(identificador)){
            this.token = proximo_token();
            vetor();
            if(this.token.getValor().equals("=")){
                this.token = proximo_token();
                verificaCaso();
                if(this.token.getValor().equals(";")){
                    this.token = proximo_token();
                }else{
                    System.out.println("ERRO");
                }
            }else{
                System.out.println("ERRO");
            }
        }else{
            System.out.println("ERRO");
        }
    }

    private void incrementador() {
        if(this.token.getValor().matches(identificador)){
            this.token = proximo_token();
            vetor();
            if(isIncrementador(this.token.getValor())){ //pra checar se é ++ ou --
                this.token = proximo_token();
                if(this.token.getValor().equals(";")){
                    this.token = proximo_token();
                }else{
                    System.out.println("ERRO");
                }
            }else{
                System.out.println("ERRO");
            }
        }
    }

    private void retorno() {
        if(this.token.pertenceAoPrimeiroDe("verificaCaso")){
            verificaCaso();
        }
    }

    private void verificaCaso() {
        if(this.token.pertenceAoPrimeiroDe("incremento")){
            incremento();
            this.token = proximo_token();
        }else if(this.token.pertenceAoPrimeiroDe("expressao")){
            expressao();
            this.token = proximo_token();
        }else if(this.token.pertenceAoPrimeiroDe("booleano")){
            booleano();
            this.token = proximo_token();
        }else{
            System.out.println("ERRO");
        }
        
    }

    private boolean isIncrementador(String s){
        String[] partes = s.split("");
        if(partes.length == 2){
            if( (partes[0].equals("+") && partes[1].equals("+")) | ((partes[0].equals("-") && partes[1].equals("-"))) ){
                return true;
            }
        }
        
        return false;
    }
     
    private boolean isOperadorAritmetico(String s){
                return s.equals("+") | s.equals("-") | s.equals("*") | s.equals("/");
    }

    
    
    private void incremento() {
        if(this.token.getValor().equals("(")){
            this.token = proximo_token();
            if(isIncrementador(this.token.getValor())){
                this.token = proximo_token();
                if(this.token.getValor().matches(identificador)){
                    this.token = proximo_token();
                    vetor();
                    if(this.token.getValor().equals(")")){
                        this.token = proximo_token();
                    }else{
                        System.out.println("ERRO");
                    }
                }else{
                    System.out.println("ERRO");
                }
            }else if(this.token.getValor().matches(identificador)){
                this.token = proximo_token();
                vetor();
                if(isIncrementador(this.token.getValor())){
                    this.token = proximo_token();
                    if(this.token.getValor().equals(")")){
                        this.token = proximo_token();
                    }else{
                        System.out.println("ERRO");
                    }
                }else{
                    System.out.println("ERRO");
                }
            }
        }else if(isIncrementador(this.token.getValor())){
            this.token = proximo_token();
            if(this.token.getValor().matches(identificador)){
                this.token = proximo_token();
                vetor();
            }else{
                System.out.println("ERRO");
            }
        }else if(this.token.getValor().matches(identificador)){
            this.token = proximo_token();
            vetor();
            if(isIncrementador(this.token.getValor())){
               this.token = proximo_token(); 
            }else{
                System.out.println("ERRO");
            }
        }else{
            System.out.println("ERRO");
        }
    }

    private void expressao() {
        if(this.token.getValor().equals("(")){
            this.token = proximo_token();
            expressao();
            if(this.token.getValor().equals(")")){
                this.token = proximo_token();
                if(isOperadorAritmetico(this.token.getValor())){
                    this.token = proximo_token();
                    expressao();
                }else{
                    return;
                }
            }else{
                System.out.println("ERRO");
            }
        }else if(this.token.pertenceAoPrimeiroDe("operador")){
            operador();
            maisOperacoes();
        }else{
            System.out.println("ERRO");
        }
    }

    private void booleano() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void operador() {
        if( this.token.getValor().matches(digito) | this.token.getValor().matches(cadeiaCaracteres)){
            this.token = proximo_token();    
        }else if(this.token.getValor().matches(identificador)){
            this.token =proximo_token();
            vetor();
        }else if(this.token.pertenceAoPrimeiroDe("chamadaDeMetodo")){
            chamadaDeMetodo();
        }
    
    }

    private void maisOperacoes() {
        if(isOperadorAritmetico(this.token.getValor())){
            this.token = proximo_token();
            if(this.token.pertenceAoPrimeiroDe("maisOperacoes")){
                maisOperacoes();
            }else if(this.token.pertenceAoPrimeiroDe("expressao")){
                expressao();
            }
        }
    }
    
}
