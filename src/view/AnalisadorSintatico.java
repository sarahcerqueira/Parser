/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import util.Classe;
import util.Erro;
import util.ManipuladorDeArquivo;
import util.Modo;
import util.ModoException;
import util.Token;
import util.Escopo;

/**
 *
 * @author User-PC
 */
public class AnalisadorSintatico {

    private ArrayList<Token> listaDeTokens;
    private ArrayList<String> tipo = new ArrayList<String>();
    private ArrayList<Erro> erros;
    private ArrayList<Erro> errosSemanticos;
    private Token token;
    private ArrayList<String[]> constantes;
    private ArrayList<String[]> variaveis;
    private ArrayList<Escopo> escopos;
   

    public AnalisadorSintatico(ArrayList<Token> listaDeTokens) {
        this.listaDeTokens = listaDeTokens;
        this.listaDeTokens.add(listaDeTokens.size(), new Token("$", Classe.FINALIZADOR, 0)); //add o '$' no final da lista
        this.setup();
        erros = new ArrayList<Erro>();
        constantes = new ArrayList<String[]>();
        variaveis = new ArrayList<String[]>();
        escopos = new ArrayList<Escopo>();
        errosSemanticos = new ArrayList<Erro>();
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

    private void novoErro(int linha, String erro) {
    	this.erros.add(new Erro(linha, erro));
    }
    
    private void recuperacaoDeErro() {
    	
    	int linha = this.token.getLinha();
    	linha++;
    	
    	while(!token.getValor().equals("$") && token.getLinha() != linha) {
            this.token = proximo_token();
    	}
    	
    }

    public void executar(String arquivo) {
        //estaPresenteNaListaDeTokens(":");
        this.token = proximo_token();

        programa();

        if (this.token.getValor().equals("$")) {
            System.out.println("SUCESSO: codigo encerrado com o caractere finalizador $");
        } else {
            System.out.println("ERRO: codigo encerrado sem atingir o $");
        }
        
        if (this.buscaEscopo("principal") == null) {
        	System.out.println("ERRO: metodo principal n�o existe");
    		novoErroSemantico(-1,"ERRO: metodo principal n�o existe");
        }
        
        escreveSaida(arquivo);

    }

    public Token proximo_token() {
        Token t = listaDeTokens.remove(0);
        System.out.println("Token em analise: " + t.getValor());
        return t;
    }

    private void programa() {

        if (this.token.getValor().equals("programa")) {

            this.token = proximo_token();

            if (this.token.getValor().equals("{")) {

                this.token = proximo_token();
                blocoConstantes();
                escopoPrograma();

                if (this.token.getValor().equals("}")) {
                    this.token = proximo_token();
                    return;
                    
                } else {
                    
                	System.out.println("ERRO: esta faltando o simbolo }");
                	novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo }" );
                	this.recuperacaoDeErro();
                  
                }

            } else {
            	
                System.out.println("ERRO: esta faltando o simbolo {");
                novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo {" );
            	this.recuperacaoDeErro();
            }

        } else {
        	
        	novoErro(this.token.getLinha(),"ERRO: esta faltando a palavra 'programa'" );
        	this.recuperacaoDeErro();
            System.out.println("ERRO: esta faltando a palavra 'programa'");
        }

    }
    
    private void escopoPrograma() {
        if (pertenceAoPrimeiroDe("metodo")) {
            metodo();//token = "metodo"
            escopoPrograma();
            
        } else {
        	return;
        }
    }

    private void blocoConstantes() {

        if (this.token.getValor().equals("constantes")) {
            this.token = proximo_token();

            if (this.token.getValor().equals("{")) {

                this.token = proximo_token();
                estruturaConstantes();

                if (this.token.getValor().equals("}")) {
                    this.token = proximo_token();
                    
                } else {
                    System.out.println("ERRO: esta faltando o simbolo }");
                    novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo }" );
                	this.recuperacaoDeErro();
                }

            } else {
                System.out.println("ERRO: esta faltando o simbolo {");
                novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo {" );
            	this.recuperacaoDeErro();
            }

        } else {
        	return;
        }

    }


    private void estruturaConstantes() {
    	
        if (this.tipo.contains(this.token.getValor())) { //se token == tipo
            
            String type = this.token.getValor();
            
            if(type.equals("vazio")){
                System.out.println("ERRO: o tipo vazio nao pode ser usado em declaracoes de constantes");
                novoErroSemantico(this.token.getLinha(),"ERRO: o tipo vazio nao pode ser usado em declaracoes de constantes" );	
            }
            
            this.token = proximo_token();
            constantes(type);
            
            if (this.token.getValor().equals(";")) {
                this.token = proximo_token();
                estruturaConstantes();
                
           } else {
                System.out.println("ERRO: esta faltando o simbolo ;");
            	novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo ;" );
            	this.recuperacaoDeErro();
            }
            
        } else {//mesmo problema se checar vazio
            return;
        }
    }

    private void constantes(String tipo) {
        if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) { //token == identificador
        	
        	//Erro Semantico
        	if(this.isConstante(token.getValor())) {
        		System.out.println("ERRO: identificador duplicado");
        		this.novoErroSemantico(this.token.getLinha(),"ERRO: identificador duplicado" );
        		
        	}else {
                    this.addConstantes(token.getValor(), token.getClasse().getClasse(), "constante", tipo);
        		
        	}
        		
        		
        	
            this.token = proximo_token();
            
            if (this.token.getValor().equals("=")) {
                this.token = proximo_token();
                constante(tipo);
                multiConst(tipo);
                
            } else {
                System.out.println("ERRO: faltou o caractere =");
                novoErro(this.token.getLinha(),"ERRO: faltou o caractere =" );
            	this.recuperacaoDeErro();
            }
        } else {
            System.out.println("ERRO: declaracao de constante sem identificador");
            novoErro(this.token.getLinha(),"ERRO: declaracao de constante sem identificador" );
        	this.recuperacaoDeErro();
        }
    }

    private void constante(String tipo) {
    	
        if (this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                | this.token.getClasse().equals(Classe.NUMERO)) {
        	
        	//Erro sem�ntico
        	if(!token.getClasse().equals(Classe.CADEIA_DE_CARACTERES) && tipo.equals("texto")) {
        		
        		System.out.println("ERRO: constantes do tipo texto so podem receber cadeias de caracteres");
        		novoErroSemantico(this.token.getLinha(),"ERRO: constantes do tipo texto so podem receber cadeias de caracteres" );
                
        	} else if(tipo.equals("inteiro") && token.getClasse().equals(Classe.NUMERO) ) {
        		
        		if(token.getValor().contains(".")) {
        			System.out.println("ERRO: constantes do tipo inteiro nao pode receber numero real");
        			novoErroSemantico(this.token.getLinha(),"ERRO: constantes do tipo inteiro n�o pode receber numero real");
        		}
        	} else if(tipo.equals("real") && token.getClasse().equals(Classe.NUMERO)) {
        		
        		if(!token.getValor().contains(".")) {
        			System.out.println("ERRO: constantes do tipo real nao pode receber numero inteiro");
        			novoErroSemantico(this.token.getLinha(),"ERRO: constantes do tipo real nao pode receber numero inteiro");
        		}
        	} else if((tipo.equals("real") || tipo.equals("inteiro")) && !token.getClasse().equals(Classe.NUMERO) ) {
        		
        		System.out.println("ERRO: a constante do tipo "+ tipo + " aguarda um n�mero");
        		novoErroSemantico(this.token.getLinha(),"ERRO: a constante do tipo "+ tipo + " aguarda um n�mero");
        	}
            
        	this.token = proximo_token();
        	
        } else {
            System.out.println("ERRO: atribuicao de constante sem Numero/CadeiaCaracteres/Identificador");
            novoErro(this.token.getLinha(),"ERRO: atribuicao de constante sem Numero/CadeiaCaracteres/Identificador" );
        	this.recuperacaoDeErro();
        }
    }

    private void multiConst(String tipo) {
        if (pertenceAoPrimeiroDe("multiplasConstantes")) {
            multiplasConstantes(tipo);
            
        }else {
            return;
        }
    }

    private void multiplasConstantes(String tipo) {
        if (this.token.getValor().equals(",")) {
            this.token = proximo_token();
            constantes(tipo);
            
        } else {
            System.out.println("ERRO: faltou virugula na declaracao de multiplas constantes");
            novoErro(this.token.getLinha(),"ERRO: faltou virugula na declaracao de multiplas constantes");
        	this.recuperacaoDeErro();
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
                        System.out.println("ERRO: faltou ; no fim do comando leia");
                        novoErro(this.token.getLinha(),"ERRO: faltou ; no fim do comando leia");
                    	this.recuperacaoDeErro();
                    }
                } else {
                    System.out.println("ERRO: faltou faltou o simbolo)");
                    novoErro(this.token.getLinha(),"ERRO: faltou faltou o simbolo)");
                	this.recuperacaoDeErro();
                }
            } else {
                System.out.println("ERRO: faltou o simbolo (");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo (");
            	this.recuperacaoDeErro();
            }
        } else {
            System.out.println("ERRO: faltou a palavra 'leia'");
            novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'leia'");
        	this.recuperacaoDeErro();

        }
    }

    private void conteudoLeia() {

        if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
            this.token = proximo_token();
            vetor();
            lermais();
            
        } else {
        	System.out.println("ERRO: faltou paramentros no comando leia");
            novoErro(this.token.getLinha(),"ERRO: faltou paramentros no comando leia");
        	this.recuperacaoDeErro();
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
        if (this.token.getClasse().equals(Classe.NUMERO)) {
            
            if(!isNumeroInteiro(this.token.getValor())){
                System.out.println("ERRO: Indice(s) de vetores ou matrizes devem ser um numeros inteiros");
            	novoErroSemantico(this.token.getLinha(),"ERRO: Indice(s) de vetores ou matrizes devem ser um numeros inteiros");
            }
            
            this.token = proximo_token();

        } else if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
            this.token = proximo_token();

        } else {
        	System.out.println("ERRO: falta um numero ou um identificador");
            novoErro(this.token.getLinha(),"ERRO: falta um numero ou um identificador");
        	this.recuperacaoDeErro();
        }
    }

    
    /* 
        checar se o indice é inteiro
        checar se o indice está entre 0 e o numero declarado?
        checar se o vetor não está sendo referenciado como matriz, ou o contrário
    */
    private void vetor() {
        if (this.token.getValor().equals("[")) {
            this.token = proximo_token();
            opI2();
            opIndice();

            if (this.token.getValor().equals("]")) {
                this.token = proximo_token();
                matriz();
            } else {
            	System.out.println("ERRO: esta faltando ]");
                novoErro(this.token.getLinha(),"ERRO: esta faltando ]");
            	this.recuperacaoDeErro();

            }
        }
    }

    private void matriz() {
        if (this.token.getValor().equals("[")) {
            this.token = proximo_token();
            opI2();
            opIndice();

            if (this.token.getValor().equals("]")) {
                this.token = proximo_token();

            } else {
            	System.out.println("ERRO: esta faltando ]");
                novoErro(this.token.getLinha(),"ERRO: esta faltando ]");
            	this.recuperacaoDeErro();

            }
        }
    }

    private void metodo() {
    	String escopo;
    	
        if (this.token.getValor().equals("metodo")) {
            this.token = proximo_token();

            if (this.token.getClasse().equals(Classe.IDENTIFICADOR) || this.token.getValor().equals("principal") ) {
            	
            	escopo = token.getValor();
            	
            	//Erro Semantico
            	if(this.buscaEscopo(escopo)== null) {
            		this.addEscopo(escopo);
            		
            	} else {
            		System.out.println("ERRO: metodo ja existe");
            		novoErroSemantico(this.token.getLinha(),"ERRO: metodo ja existe");
            	}
            	
            	this.token = proximo_token();                

                if (this.token.getValor().equals("(")) {
                    this.token = proximo_token();
                    listaParametros(escopo);
                    
                    if (this.token.getValor().equals(")")) {
                    	this.token = proximo_token();
                    	
                        if (this.token.getValor().equals(":")) {
                            this.token = proximo_token();
                            
                            if (this.tipo.contains(this.token.getValor())) {
                                this.token = proximo_token();

                                if (this.token.getValor().equals("{")) {
                                    this.token = proximo_token();
                                    declaracaoVariaveis(escopo);
                                    escopoMetodo(escopo);

                                    if (this.token.getValor().equals("}")) {
                                        this.token = proximo_token();

                                    } else {
                                        System.out.println("ERRO: faltou o }");
                                        novoErro(this.token.getLinha(),"ERRO: faltou o }");
                                    	this.recuperacaoDeErro();

                                    }

                                } else {
                                    System.out.println("ERRO: faltou o {");
                                    novoErro(this.token.getLinha(),"ERRO: faltou o {");
                                	this.recuperacaoDeErro();

                                }

                            } else {
                                System.out.println("ERRO:faltou o tipo do retorno");
                                novoErro(this.token.getLinha(),"ERRO:faltou o tipo do retorno");
                            	this.recuperacaoDeErro();

                            }

                        } else {
                            System.out.println("ERRO: faltou o :");
                            novoErro(this.token.getLinha(),"ERRO: faltou o :");
                        	this.recuperacaoDeErro();


                        }
                    } else {
                        System.out.println("ERRO: faltou o )");
                        novoErro(this.token.getLinha(),"ERRO: faltou o )");
                    	this.recuperacaoDeErro();


                    }
                } else {
                    System.out.println("ERRO: faltou o (");
                    novoErro(this.token.getLinha(),"ERRO: faltou o (");
                	this.recuperacaoDeErro();


                }
            } else {
                System.out.println("ERRO:falta identificacao do metodo");
                novoErro(this.token.getLinha(),"ERRO:falta identificacao do metodo");
            	this.recuperacaoDeErro();


            }
        } else {
            System.out.println("ERRO:falta a palavra 'metodo'");
            novoErro(this.token.getLinha(),"ERRO:falta a palavra 'metodo'");
        	this.recuperacaoDeErro();


        }
    }

    private void listaParametros(String escopo) {
    	String tipo, cadeia;
    	
        if (this.tipo.contains(this.token.getValor())) {
        	tipo = token.getValor();
            this.token = proximo_token();

            if (this.token.getClasse().equals(Classe.IDENTIFICADOR)) {
            	
            	if(this.hasParamentro(escopo, token.getValor())) {
            		System.out.println("ERRO: parametros com identificadores iguais");
            		novoErroSemantico(this.token.getLinha(),"ERRO: parametros com identificadores iguais");
            	
            	} else {
            		this.addParamentos(escopo, tipo, token.getValor());
            	}
            	
                this.token = proximo_token();
                maisParametros(escopo);

            } else {
            	System.out.println("ERRO:falta um identificador");
                novoErro(this.token.getLinha(),"ERRO:falta um identificador");
            	this.recuperacaoDeErro();
            }

        }
    }

    private void maisParametros(String escopo) {

        if (this.token.getValor().equals(",")) {
            this.token = proximo_token();
            listaParametros(escopo);
        }

    }

    private void escopoMetodo(String escopo) {
        if(pertenceAoPrimeiroDe("comandos")){
            comandos(escopo);
            escopoMetodo(escopo);
        }
    }

    private void chamadaDeMetodo() {
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            
            if(this.token.getValor().equals("(")){
                this.token = proximo_token();
                var();
                
                if(this.token.getValor().equals(")")){
                    this.token = proximo_token();
                    
                }else{
                    System.out.println("ERRO: faltou )");
                    novoErro(this.token.getLinha(),"ERRO: faltou )");
                	this.recuperacaoDeErro();
                }
            }else{
                System.out.println("ERRO: faltou (");
                novoErro(this.token.getLinha(),"ERRO: faltou (");
            	this.recuperacaoDeErro();
            }
        }else{
            System.out.println("ERRO: falta identificador");
            novoErro(this.token.getLinha(),"ERRO: falta identificador");
        	this.recuperacaoDeErro();
        }
    }

    private void var() {
    	
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            vetor();
            maisVariavel();
            
        }else if(pertenceAoPrimeiroDe("metodoParametro")){
        	metodoParametro();
        	
        }
    }
    
    private void maisVariavel() {
        if(this.token.getValor().equals(",")){
            this.token = proximo_token();
            var();
        }
    }

   
    private void metodoParametro() {
    	
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            
            if(this.token.getValor().equals("(")){
                this.token = proximo_token();
                var();
                
                if(this.token.getValor().equals(")")){
                    this.token = proximo_token();
                    maisVariavel();
                    
                }else{
                	System.out.println("ERRO: esta faltando o simbolo ) ");
                    novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo ) ");
                	this.recuperacaoDeErro();
                }
            }else{
            	System.out.println("ERRO: esta faltando o simbolo ( ");
                novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo ( ");
            	this.recuperacaoDeErro();
            }
        }else{
        	System.out.println("ERRO: esta faltando o identificador ");
            novoErro(this.token.getLinha(),"ERRO: esta faltando o identificador ) ");
        	this.recuperacaoDeErro();
        }
    }

    private void declaracaoVariaveis(String escopo) {
    	
        if(this.token.getValor().equals("variaveis")){
            this.token = proximo_token();
            
            if(this.token.getValor().equals("{")){
                this.token = proximo_token();
                varV(escopo);
                
                if(this.token.getValor().equals("}")){
                    this.token = proximo_token();
                    
                }else{
                	System.out.println("ERRO: esta faltando o simbolo } ");
                    novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo } ");
                	this.recuperacaoDeErro();
                }
                
            }else{
            	System.out.println("ERRO: esta faltando o simbolo { ");
                novoErro(this.token.getLinha(),"ERRO: esta faltando o simbolo { ");
            	this.recuperacaoDeErro();
            }
        }
    }

    private void varV(String escopo) {
    	
        if(this.tipo.contains(this.token.getValor())){
            String type = this.token.getValor();
            
            if(type.equals("vazio")){
                System.out.println("ERRO: o tipo vazio nao pode ser usado em declaracoes de variaveis");
                novoErroSemantico(this.token.getLinha(),"ERRO: o tipo vazio nao pode ser usado em declaracoes de variaveis" );	
            }
            
            this.token = proximo_token();
            complementoV(escopo, type);
            maisVariaveis(escopo);
            
        }else{
        	System.out.println("ERRO: aguarda-se um tipo de variavel boleano/inteiro/real/texto");
            novoErro(this.token.getLinha(),"ERRO: aguarda-se um tipo de variável boleano/inteiro/real/texto");
        	this.recuperacaoDeErro();
        }
    }
    
private void complementoV(String escopo, String tipo) {
        
       if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
    	  
           String caso = "nenhum";
           
    	   if(this.isConstante(token.getValor())) {
    		   System.out.println("ERRO: variavel com identificador igual ao identificador da constante");
    		   novoErroSemantico(this.token.getLinha(),"ERRO: variavel com identificador igual ao identificador da constante");
    	   }
    	   
    	   if(!this.hasVariarel(escopo, token.getValor())) {
    		   this.addVariaveis(escopo, token.getValor(), token.getClasse().getClasse(), tipo, caso);
    		   
    	   } else {
    		   System.out.println("ERRO: variaveis com identificadores iguais");
       			novoErroSemantico(this.token.getLinha(),"ERRO: variaveis com identificadores iguais");  
    	   }
    	   
           this.token = proximo_token();
           vetor();
           variavelMesmoTipo(escopo, tipo);
           
       }else {
    	   
    	   System.out.println("ERRO: faltou um identificador");
           novoErro(this.token.getLinha(),"ERRO: faltou um identificador");
       	   this.recuperacaoDeErro();
    	   
       }
       
    }

    private void variavelMesmoTipo(String escopo, String tipo) {
        if(this.token.getValor().equals(",")) {
            this.token = proximo_token();
            complementoV(escopo, tipo);
            
         } else if(this.token.getValor().equals(";")) {
             this.token = proximo_token();

         } else {
        	 
        	 System.out.println("ERRO: faltou , ou ;");
             novoErro(this.token.getLinha(),"ERRO: faltou , ou ;");
         	 this.recuperacaoDeErro();
         }
    }
    
    private void maisVariaveis(String escopo) {
    	
        if(this.tipo.contains(this.token.getValor())){ //Primeiro("VarV") == Tipo
            varV(escopo);
        }
    }


    private void comandos(String escopo) {
        if(pertenceAoPrimeiroDe("leia")){
            leia();
            
        }else if(pertenceAoPrimeiroDe("escreva")){
            escreva();
            
        }else if(pertenceAoPrimeiroDe("se")){
            se(escopo);
            
        }else if(pertenceAoPrimeiroDe("enquanto")){
            enquanto(escopo);
            
        }else if(pertenceAoPrimeiroDe("atribuicaoVariavel") && !this.listaDeTokens.get(0).getValor().equals("(") && 
        		!this.listaDeTokens.get(0).getClasse().equals(Classe.OPERADOR_ARITMETICO)){
            atribuicaoVariavel(escopo);
            
        }else if(pertenceAoPrimeiroDe("chamadaDeMetodo") && this.listaDeTokens.get(0).getValor().equals("(")){
            chamadaDeMetodo();
            
            if(this.token.getValor().equals(";")){
                this.token = proximo_token();
                
            }else{
            	 System.out.println("ERRO: faltou , ou ;");
                 novoErro(this.token.getLinha(),"ERRO: faltou , ou ;");
             	 this.recuperacaoDeErro();
            }
            
        }else if(pertenceAoPrimeiroDe("incrementador")){
            incrementador(escopo);
            
        }else if(this.token.getValor().equals("resultado")){
            this.token = proximo_token();
            retorno();
            
            if(this.token.getValor().equals(";")){
                this.token = proximo_token();
            }else{
            	 System.out.println("ERRO: faltou , ou ;");
                 novoErro(this.token.getLinha(),"ERRO: faltou , ou ;");
             	 this.recuperacaoDeErro();
            }
        }
            
    }

    private void escreva() {
    	
    	if(this.token.getValor().equals("escreva")) {
            this.token = proximo_token();
            
            if(this.token.getValor().equals("(")) {
                this.token = proximo_token();
                paramEscrita();
                
                if(this.token.getValor().equals(")")) {
                    this.token = proximo_token();
                    
                    if(this.token.getValor().equals(";")) {
                        this.token = proximo_token();

                    } else {
                    	 System.out.println("ERRO: faltou ;");
                         novoErro(this.token.getLinha(),"ERRO: faltou ;");
                     	 this.recuperacaoDeErro();
                    }
                    
                } else {
                	
                	 System.out.println("ERRO: faltou )");
                     novoErro(this.token.getLinha(),"ERRO: faltou )");
                 	 this.recuperacaoDeErro();
                }
                
            } else {
            	 System.out.println("ERRO: faltou (");
                 novoErro(this.token.getLinha(),"ERRO: faltou (");
             	 this.recuperacaoDeErro();
            }
            
    	} else {
    		 System.out.println("ERRO: faltou a palavra 'escreva'");
             novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'escreva'");
         	 this.recuperacaoDeErro();
    	}
        
    }

    private void paramEscrita() {
		if(pertenceAoPrimeiroDe("verificaCaso")) {
			verificaCaso();
			maisParametrosE();
			
		} else {
			System.out.println("ERRO: sintaxe de parametro incorreta no comando escrita");
            novoErro(this.token.getLinha(),"ERRO: sintaxe de parametro incorreta no comando escrita");
        	this.recuperacaoDeErro();
		}
    	
		
	}

	private void maisParametrosE() {
		
		if(this.token.getValor().equals(",")) {
            this.token = proximo_token();
            paramEscrita();

		}
		
	}

	private void se(String escopo) {
        
		if(this.token.getValor().equals("se")) {
            this.token = proximo_token();
            condSe();
            
            if(this.token.getValor().equals("entao")) {
                this.token = proximo_token();
                
                if(this.token.getValor().equals("{")) {
                    this.token = proximo_token();
                    blocoSe(escopo);
                    
                    if(this.token.getValor().equals("}")) {
                        this.token = proximo_token();
                        senao(escopo);
                        
                    } else {
                    	System.out.println("ERRO: faltou o simbolo }");
                        novoErro(this.token.getLinha(),"ERRO: faltou o simbolo }");
                    	this.recuperacaoDeErro();
                    }
               } else {
            	   System.out.println("ERRO: faltou o simbolo {");
                   novoErro(this.token.getLinha(),"ERRO: faltou o simbolo {");
               		this.recuperacaoDeErro();
               }
                
	         } else {
	        	 System.out.println("ERRO: faltou a palavra 'entao'");
	             novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'entao'");
	         	 this.recuperacaoDeErro();
	         }

		}else {
			System.out.println("ERRO: faltou a palavra 'se'");
            novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'se'");
            this.recuperacaoDeErro();
		}
    }
	
	private void negar() {
		 
		if(this.token.getValor().equals("!")) {
            this.token = proximo_token();
		}
	}
	
	private void maisCond() {
		
		if(this.token.getClasse().equals(Classe.OPERADOR_LOGICO)){
            this.token = proximo_token();
            cond();
            maisCond();
		}
		
	}

	private void cond() {
		
		if(pertenceAoPrimeiroDe("termo")) {
			termo();
			
			if(token.getClasse().equals(Classe.OPERADOR_RELACIONAL)) {
	            this.token = proximo_token();
				termo();
				
			} else {
				System.out.println("ERRO: faltou operador relacional");
	            novoErro(this.token.getLinha(),"ERRO: faltou operador relacional");
	            this.recuperacaoDeErro();
			}
			
		} else if(pertenceAoPrimeiroDe("negar")) {
			negar();
			
			if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
	            this.token = proximo_token();
	            vetor();
	            
			} else {
				System.out.println("ERRO: faltou identificador");
	            novoErro(this.token.getLinha(),"ERRO: faltou identificador");
	            this.recuperacaoDeErro();

			}
			
		} else {
			System.out.println("ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            novoErro(this.token.getLinha(),"ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            this.recuperacaoDeErro();
		}
		
	}

	private void termo() {
		
		if(pertenceAoPrimeiroDe("tipoTermo")) {
			tipoTermo();
			op();
			
		} else {
			System.out.println("ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            novoErro(this.token.getLinha(),"ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            this.recuperacaoDeErro();
		}
		
	}

	private void op() {
			
		if(this.token.getClasse().equals(Classe.OPERADOR_ARITMETICO)) {
            this.token = proximo_token();
            tipoTermo();
            op();
		}
		
	}

	private void tipoTermo() {
		
		if(token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token = proximo_token();
            vetor();
            
		} else if(token.getClasse().equals(Classe.NUMERO)) {
            this.token = proximo_token();
            
		}else if(token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)) {
            this.token = proximo_token();
			
		}else if(token.getValor().equals("verdadeiro")) {
            this.token = proximo_token();
			
		}else if(token.getValor().equals("false")) {
            this.token = proximo_token();
            
		} else {
			System.out.println("ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            novoErro(this.token.getLinha(),"ERRO: aguarda-se um identificador/numero ou Cadeia de caracter");
            this.recuperacaoDeErro();
		}
	}

	private void condSe() {
		
		if(token.getValor().equals("(")) {
            this.token = proximo_token();
            cond();
            maisCond();
            
    		if(token.getValor().equals(")")) {
                this.token = proximo_token();

    		} else {
    			System.out.println("ERRO: faltou o simbolo )");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo )");
                this.recuperacaoDeErro();
    		}


		} else {
			System.out.println("ERRO: faltou o simbolo (");
            novoErro(this.token.getLinha(),"ERRO: faltou o simbolo (");
            this.recuperacaoDeErro();
		} 
		
		
	}
	
    
	private void blocoSe(String escopo) {
		
		if(pertenceAoPrimeiroDe("comandos")) {
			comandos(escopo);
			blocoSe(escopo);
		}
		
	}

	private void senao(String escopo) {
		
		if(token.getValor().equals("senao")) {
            this.token = proximo_token();
            condSenao();
            
            if(token.getValor().equals("{")) {
                this.token = proximo_token();
                blocoSe(escopo);
                
                if(token.getValor().equals("}")) {
                    this.token = proximo_token();
                    senao(escopo);
                    
                } else {
                	System.out.println("ERRO: faltou o simbolo }");
                    novoErro(this.token.getLinha(),"ERRO: faltou o simbolo }");
                    this.recuperacaoDeErro();
                }
                
            } else {
            	System.out.println("ERRO: faltou o simbolo {");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo {");
                this.recuperacaoDeErro();
            }

            
		} 
		
	}

	
	private void condSenao() {
		
		if(this.token.getValor().equals("se")) {
            this.token = proximo_token();
			condSe();
			
			if(token.getValor().equals("entao")) {
	            this.token = proximo_token();

			} else {
				System.out.println("ERRO: faltou a palavra 'entao");
                novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'entao");
                this.recuperacaoDeErro();
			}
			
		}
		
	}

	private void enquanto(String escopo) {
		if(token.getValor().equals("enquanto")) {
            this.token = proximo_token();

    		if(token.getValor().equals("(")) {
                this.token = proximo_token();
                operacaoRelacional();
                
        		if(token.getValor().equals(")")) {
                    this.token = proximo_token();
                    
            		if(token.getValor().equals("{")) {
                        this.token = proximo_token();
                        conteudoLaco(escopo);
                        
                        if(token.getValor().equals("}")) {
                            this.token = proximo_token();
                            
                		} else {
                			System.out.println("ERRO: faltou o simbolo }");
                            novoErro(this.token.getLinha(),"ERRO: faltou o simbolo }");
                            this.recuperacaoDeErro();
                		}
                        
            		} else {
            			System.out.println("ERRO: faltou o simbolo {");
                        novoErro(this.token.getLinha(),"ERRO: faltou o simbolo {");
                        this.recuperacaoDeErro();
            		}

        		}  else {
        			System.out.println("ERRO: faltou o simbolo )");
                    novoErro(this.token.getLinha(),"ERRO: faltou o simbolo )");
                    this.recuperacaoDeErro();
        		}
                   
    		}  else {
    			System.out.println("ERRO: faltou o simbolo (");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo (");
                this.recuperacaoDeErro();
    		}

		}  else {
			System.out.println("ERRO: faltou a palavra 'enquanto'");
            novoErro(this.token.getLinha(),"ERRO: faltou a palavra 'enquanto'");
            this.recuperacaoDeErro();
		}
    }
	
	

    private void conteudoLaco(String escopo) {
    	
		if(pertenceAoPrimeiroDe("comandos")) {
			comandos(escopo);
			conteudoLaco(escopo);
		}
		
	}

	private void operacaoRelacional() {
		
		if(pertenceAoPrimeiroDe("complementoOperador")) {
			complementoOperador();
			
			if(token.getClasse().equals(Classe.OPERADOR_RELACIONAL)) {
	            this.token = proximo_token();
				complementoOperador();

			}
		} else if(pertenceAoPrimeiroDe("negar")) {
			negar();
			
			if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
	            this.token = proximo_token();
	            vetor();
	            
			} else {
				System.out.println("ERRO: faltou identificador");
	            novoErro(this.token.getLinha(),"ERRO: faltou identificador");
	            this.recuperacaoDeErro();
			}
			
		} else {
			
			System.out.println("ERRO: erro de sintaxe na condicao do enquanto");
            novoErro(this.token.getLinha(),"ERRO: erro de sintaxe na condicao do enquanto");
            this.recuperacaoDeErro();
		}
	}
		


	private void complementoOperador() {
		
		if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
            this.token = proximo_token();
            vetor();
            
		} else if(token.getClasse().equals(Classe.NUMERO)) {
            this.token = proximo_token();

		} else if(token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)) {
            this.token = proximo_token();

		} else if(token.getValor().equals("verdadeiro")) {
            this.token = proximo_token();

		} else if(token.getValor().equals("falso")) {
            this.token = proximo_token();

		} else {
			System.out.println("ERRO: erro de sintaxe na condicao do enquanto");
            novoErro(this.token.getLinha(),"ERRO: erro de sintaxe na condicao do enquanto");
            this.recuperacaoDeErro();
		}

		
		
	}

	private void atribuicaoVariavel(String escopo) {
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
        	
        	//Erro semantico
        	if(this.isConstante(token.getValor())) {
        		System.out.println("ERRO: atribui��o de constante");
        		novoErroSemantico(this.token.getLinha(),"ERRO: atribuicaoo de constante");    
        		
        	}else if(!this.hasVariarel(escopo, token.getValor())) {
        		System.out.println("ERRO: variavel nao declarada");
        		novoErroSemantico(this.token.getLinha(),"ERRO: variavel nao declarada"); 
        	}
        	
            this.token = proximo_token();
            vetor();
            
            if(this.token.getValor().equals("=")){
                this.token = proximo_token();
                verificaCaso();
                
                if(this.token.getValor().equals(";")){
                    this.token = proximo_token();
                    
                }else{
                	System.out.println("ERRO: faltou o simbolo ;");
                    novoErro(this.token.getLinha(),"ERRO: faltou o simbolo ;");
                    this.recuperacaoDeErro();
                }
            }else{
            	System.out.println("ERRO: faltou o simbolo =");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo =");
                this.recuperacaoDeErro();
            }
            
        }else{
        	System.out.println("ERRO: faltou identificador");
            novoErro(this.token.getLinha(),"ERRO: faltou identificador");
            this.recuperacaoDeErro();
        }
    }

    private void incrementador(String escopo) {
        
        if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            Escopo e = this.buscaEscopo(escopo);
            if(!e.isVariavel(this.token.getValor()) || !isConstante(this.token.getValor())) {
            	System.out.println("ERRO: variavel nao declarada");
            	novoErroSemantico(this.token.getLinha(),"ERRO: variavel nao declarada");
            		
            }else{
                System.out.println("wtf");
            }
            
            this.token = proximo_token();
            vetor();
            
            if(isIncrementador(this.token.getValor())){ //pra checar se eh ++ ou --
                this.token = proximo_token();
                
                if(this.token.getValor().equals(";")){
                    this.token = proximo_token();
                    
                }else{
                    String s = "Erro de Sintaxe: falta o ;";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
                }
            }else{
                String s = "Erro de Sintaxe: deveria ser ++ ou -- ;";
                    System.out.println(s);
                    novoErro(this.token.getLinha(),s);
                    this.recuperacaoDeErro();
            }
        }
    }

    private void retorno() {
    	
        if(pertenceAoPrimeiroDe("verificaCaso")){
            verificaCaso();
        }
    }
    
    private void verificaCaso() {
    	
    	if (token.getValor().equals("(")) {
            this.token = proximo_token();
    		verificaCaso();
    		
    		//Tratando a expressao
    		if(token.getValor().equals(")")) {
                this.token = proximo_token();
                auxiliarW();
                
    		} else {
    			System.out.println("ERRO: faltou fechar )");
                novoErro(this.token.getLinha(),"ERRO: faltou fechar )");
                this.recuperacaoDeErro();
    		}
    		
    	} else if(token.getValor().equals("++") | token.getValor().equals("--")) {
            this.token = proximo_token();
            
            if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
                this.token = proximo_token();
                vetor();
                
            } else {
            	
            	System.out.println("ERRO: faltou identificador");
                novoErro(this.token.getLinha(),"ERRO: faltou identificador");
                this.recuperacaoDeErro();
            }

    	} else if(token.getValor().equals("verdadeiro") | token.getValor().equals("falso")) {
            this.token = proximo_token();

    	} else if(token.getValor().equals("!") ) {
            this.token = proximo_token();
    		auxiliarC();
    		
    	} else if (pertenceAoPrimeiroDe("expressao")) {
    		expressao();
    		
    	} else {
    		
    		System.out.println("ERRO: erro na atribui��o de variavel");
            novoErro(this.token.getLinha(),"ERRO: erro na atribui��o de variavel");
            this.recuperacaoDeErro();
    	}
    	
    }

    
    private void auxiliarW() {
		if(token.getClasse().equals(Classe.OPERADOR_ARITMETICO)) {
            this.token = proximo_token();
            expressao();
		}
		
	}

	private void auxiliarC() {
		// TODO Auto-generated method stub
    	if(token.getValor().equals("verdadeiro") | token.getValor().equals("falso")) {
            this.token = proximo_token();

    	} else if(token.getClasse().equals(Classe.IDENTIFICADOR)) {
            this.token = proximo_token();
            vetor();
            
        } else {

    		System.out.println("ERRO: erro na declaracao de variavel");
            novoErro(this.token.getLinha(),"ERRO: erro na declaracao de variavel");
            this.recuperacaoDeErro();
        }
	}

	
	
	private void expressao() {
		
		if(this.pertenceAoPrimeiroDe("multExp")) {
			multExp();
			auxiliarK();
			
		} else {
			System.out.println("ERRO: erro na expressao");
            novoErro(this.token.getLinha(),"ERRO: erro na expressao");
            this.recuperacaoDeErro();
		}
		
	}

	private void auxiliarK() {
		
		if(token.getValor().equals("-")| token.getValor().equals("+")){
            this.token = proximo_token();
            multExp();
            auxiliarK();
		}
		
	}

	private void multExp() {
		
		if(this.pertenceAoPrimeiroDe("operador")) {
			operador();
			auxiliarY();
		} else {
			System.out.println("ERRO: aguardava-se um operador");
            novoErro(this.token.getLinha(),"ERRO: aguardava-se um operador");
            this.recuperacaoDeErro();
		}
		
	}

	private void auxiliarY() {
		
		if(token.getValor().equals('*')| token.getValor().equals('/')){
            this.token = proximo_token();
            operador();
            auxiliarY();
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
     
    
    
    private void operador() {
    	
        if( this.token.getClasse().equals(Classe.NUMERO) | this.token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)){
            this.token = proximo_token();    
            
        }else if(this.token.getClasse().equals(Classe.IDENTIFICADOR)){
            this.token =proximo_token();
            vetor();
            auxiliarF();
            
        }else if(pertenceAoPrimeiroDe("chamadaDeMetodo")){
            chamadaDeMetodo();
        
        } else if(this.token.getValor().equals("(")) {
            this.token =proximo_token();
            expressao();
            
            if(this.token.getValor().equals(")")) {
                this.token =proximo_token();
                
            } else {
            	System.out.println("ERRO: faltou o simbolo )");
                novoErro(this.token.getLinha(),"ERRO: faltou o simbolo )");
                this.recuperacaoDeErro();
            }
            
            
        	
        }else {
        	System.out.println("ERRO: operador invalido");
            novoErro(this.token.getLinha(),"ERRO: operador invalido");
            this.recuperacaoDeErro();
        }
    
    }
    
    private void auxiliarF() {
		
    	if(token.getValor().equals("++")|token.getValor().equals("--")) {
    		token = proximo_token();
    	}
		
	}

	private boolean pertenceAoPrimeiroDe(String naoTerminal){
        
        switch (naoTerminal) {
        
           case "metodo":
                return token.getValor().equals("metodo");
                
            case "multiplasConstantes":    
                return token.getValor().equals(",");
                
            case "comandos":    
                return token.getValor().equals("resultado") | token.getValor().equals("leia") | token.getValor().equals("escreva")
                		| token.getValor().equals("se") | token.getValor().equals("enquanto") | token.getClasse().equals(Classe.IDENTIFICADOR)  ;
            
            case "metodoParametro":    
                return token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "leia":    
                return token.getValor().equals("leia");
                
            case "escreva":    
                return token.getValor().equals("escreva");
                
            case "se":    
                return token.getValor().equals("se");
                
            case "enquanto":    
                return token.getValor().equals("enquanto");
                
            case "atribuicaoVariavel":    
                return token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "chamadaDeMetodo":    
                return token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "incrementador":    
                return token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "verificaCaso":    
                return  token.getValor().equals("(") | token.getValor().equals("!")| token.getValor().equals("verdadeiro")| token.getValor().equals("falso")
                		| token.getClasse().equals(Classe.IDENTIFICADOR)| token.getClasse().equals(Classe.NUMERO)
                		| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)| token.getClasse().equals(Classe.OPERADOR_ARITMETICO);
                
            case "termo":    
                return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR) | token.getValor().equals("verdadeiro")
                		| token.getValor().equals("falso");
                
            case "negar":    
                return true; //aceita vazio
                
            case "tipoTermo":    
                return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR) | token.getValor().equals("verdadeiro")
                		| token.getValor().equals("falso");
                
            case "conseSe":    
                return token.getValor().equals("(");
                
            case "complementoOperador":    
                return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR) | token.getValor().equals("verdadeiro")
                		| token.getValor().equals("falso");
                
            case "expressao":    
                return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR);
                
            case "multExp":    
            	return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR);
            	
            case "operador":    
            	return token.getClasse().equals(Classe.NUMERO)| token.getClasse().equals(Classe.CADEIA_DE_CARACTERES)
                		| token.getClasse().equals(Classe.IDENTIFICADOR) ;
                		
        }
    
        return false;
        
    }
    
    private void escreveSaida(String arquivo) {
		
		arquivo = arquivo.substring(0,  arquivo.indexOf("."));
		        
		ManipuladorDeArquivo escrita;
		try {
		            
		            escrita = new ManipuladorDeArquivo(arquivo + ".saida", Modo.ESCRITA);
		
		            if(!this.erros.isEmpty() && !this.listaDeTokens.isEmpty()){
		             
		                for (int i = 0; i < this.erros.size(); i++) {
		                        if (i == 0) {
		                                System.out.println("\nErros Sint�ticos\n");
		                                escrita.escreverArquivo("\r\n Erros Sint�ticos \r\n");
		                        }
		                        Erro e = this.erros.get(i);
		
		                        System.out.println(e.getLinha() + " - " + e.getErro());
		                        escrita.escreverArquivo(e.getLinha() + " - " + e.getErro()+ "\r\n");
		                }
		            }
		            
		           
                    
		            if (this.errosSemanticos.isEmpty()) {
		            	String s = "SUCESSO: NENHUM ERRO FOI ENCONTRADO!";
                		
                        System.out.println(s);
                        escrita.escreverArquivo(s);
                        
		            } else {
		            	
		            	 System.out.println("\nLista de Erros Semanticos\n");
		                    escrita.escreverArquivo("\nLista de Erros Semanticos\n");
		            	
		            	for (int i = 0; i < this.errosSemanticos.size(); i++) {
	                        	           Erro e = this.errosSemanticos.get(i);
	
	                        System.out.println(e.getLinha() + " - " + e.getErro());
	                        escrita.escreverArquivo(e.getLinha() + " - " + e.getErro()+ "\r\n");
	                }
		            	
		            }
		
		escrita.fechaArquivo();
		
		        
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}


    private void addConstantes(String cadeia, String token, String categoria, String tipo ) {
    	String [] s = new String[4];
    	s[0] = cadeia;
    	s[1] = token;
    	s[2] = categoria;
    	s[3] = tipo;
    	this.constantes.add(s);
    }
    
    private boolean isConstante(String id) {
    	int tam, aux =0;
    	tam = constantes.size();
    	
    	while(aux < tam) {
    		
    		if(constantes.get(aux)[0].equals(id)) {
    			return true;
    		}
    		aux = aux + 1;
    	}
    	
    	return false;
    }
    
    private void addEscopo(String nome) {
    	this.escopos.add(new Escopo(nome));
    }
    
    private boolean hasVariarel(String escopo, String cadeia) {
    	Escopo e = buscaEscopo(escopo);

    	return e.isVariavel(cadeia);
    }
    
    private void addVariaveis(String escopo, String cadeia, String token, String tipo, String caso) {
    	
        Escopo e = buscaEscopo(escopo);
    	
    	if(e != null) {
    		e.addVariaveis(cadeia, token, tipo, caso);
    	}
    	
    }
    
    private String getTipo(String escopo, String cadeia) {
    	Escopo e = buscaEscopo(escopo);
    	
    	if(e != null) {
    		return e.getTipo(cadeia);
    	}
    	
    	return null;
    	
    }
    
    private Escopo buscaEscopo(String nome) {
    	int tam, aux=0;
    	
    	tam = escopos.size();
    	
    	while(aux < tam) {
    		Escopo e = escopos.get(aux);
    		
    		if(e.getNome().equals(nome)) {
    			return e;
    		}
    		
    		aux = aux +1;
    	}
    	
    	return null;
    }
    
    private void novoErroSemantico(int linha, String erro) {
    	this.errosSemanticos.add(new Erro(linha, erro));

    }
    
    private void addParamentos(String escopo, String tipo, String cadeia) {
    	Escopo e = this.buscaEscopo(escopo);
    	
    	e.addParametros(tipo, cadeia);
    }
    
    private boolean hasParamentro(String escopo, String cadeia) {
    	Escopo e = this.buscaEscopo(escopo);
    	
    	return e.hasParamentro(cadeia);
    	
    }
    
    private boolean isNumeroInteiro(String numero){
        return numero.matches("[0-9]*");
    }
 
}
