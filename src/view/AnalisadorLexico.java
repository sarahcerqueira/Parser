/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import model.AutomatoCadeiaCaractere;
import model.AutomatoComentario;
import model.AutomatoIdentificador;
import model.AutomatoNumeros;
import util.Classe;
import util.ManipuladorDeArquivo;
import util.Modo;
import util.ModoException;
import util.Token;

/**
 * Esta classe cumpre a tarefa de um analisador Lexico
 *
 * @author User-PC
 */
public class AnalisadorLexico {

	private AutomatoCadeiaCaractere cadeia;
	private AutomatoComentario comentario;
	private AutomatoIdentificador identificador;
	private AutomatoNumeros numero;
	private ArrayList<Token> listaDeTokens;
	private ArrayList<Token> listaDeErro;
	private ArrayList<String> palavraReservada;
	private ManipuladorDeArquivo leitura;
	private String lexema;
	private char prox;
	private char atual;
	private int linha;

	public ArrayList<Token> executar(String arquivo) throws FileNotFoundException, IOException, ModoException {
		leitura = new ManipuladorDeArquivo(arquivo, Modo.LEITURA);
		inicializar();
		atual = leitura.nextCaractere();
		prox = leitura.nextCaractere();
		linha = 1;
		
		while(atual != '\0') {
			
			if(Character.toString(atual).matches("[a-zA-Z]")){
				identificador();
				
			} else if (Character.toString(atual).matches("[0-9]") | (atual == '-' && Character.toString(prox).matches("[0-9]"))) {
				numero();
			
			}else if( atual == '+' | atual == '-' | atual =='*' | atual =='/') {
				
				if(atual == '/' && (prox == '/' | prox == '*')){
					comentario();
				} else {
				
					operadorAritmetico();}
				
			} else if(atual == '!' | atual =='=' | atual == '<' | atual =='>') {
				
				if((atual == '!' && prox == '=') | atual != '!') {
					
					operadorRelacinal();
					
				} else {
					operadorLogico();
				} 
				
			} else if(atual == '|' | atual == '&') {
				
				operadorLogico();
				
			} else if(atual == ','| atual == '(' | atual == ')' | atual == '[' | atual == ']'
					| atual == '.' | atual == '{' | atual == '}' | atual == ';') {
				
				delimitador();
				
			} else if(atual == '"') {
				cadeiaCaracteres();
			
			}else if (atual == '\n' | atual == '\t' | atual ==' ' | atual=='\r') {
				
				if(atual == '\n') {
					linha = linha + 1;
					}
				this.atualizaChar();
				
			} else if ((int)atual > 31 && (int)atual < 127) {
				simbolo();
				
			}
			else {
				erro();
			}
		}
		
		leitura.fechaArquivo();
		
		escreveSaida(arquivo);
		
		return this.listaDeTokens;
			
	}

		

	private void simbolo() {
		this.concat(atual);
		this.atualizaChar();
		this.createToken(Classe.SIMBOLO, linha);
		lexema ="";
		
	}



	private void operadorLogico() {
		this.concat(atual);
		
		if((atual == '|' && prox == '|') |(atual == '&' && prox == '&') | atual == '!') {
			
			if((atual == '|' && prox == '|') |(atual == '&' && prox == '&')) {
			
			this.concat(prox);
			this.atualizaChar(); }
			
		
			if(prox == '|' | prox =='&' | prox == '!'){
				this.atualizaChar();
				erroLogico();
				
			} else {
				this.createToken(Classe.OPERADOR_LOGICO, linha);
				this.atualizaChar();
			
		}
		
		} else {
			this.atualizaChar();
			erroLogico();

		}
		
		lexema ="";
		
		
	}



	private void erroLogico() {
				
	
		while(atual== '!' | atual =='&' | atual =='|' ){
						
			this.concat(atual);

			try {
				if(leitura.hasNextCaractere()) {
					atual = prox;
					prox = leitura.nextCaractere();					
				} else {
					atual = prox;
					prox = '\0';
					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
		
		this.createErro(linha);
		
		
	}



	private void comentario() {
		this.comentario.isComentario(atual);
		
		do {
			this.concat(atual);
			
			try {
				if(leitura.hasNextCaractere()) {
					atual = prox;
					prox = leitura.nextCaractere();					
				} else {
					
					atual = prox;
					prox = '\0';
					
					if (this.comentario.isComentario(atual)){
						concat(atual);
						this.atualizaChar();
					}
					

					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} while(this.comentario.isComentario(atual));
		
		
		
		if ((this.isSeparador(atual) | atual == '\0') && comentario.isEstadoFinal()) {
		
			this.createToken(Classe.COMENTARIO, linha);
			
		} else {
			erro();
		}
		
		this.comentario.resetAutomato();
		lexema ="";

		
	}



	private void erro() {
		
		while(!this.isSeparador(atual) &&  atual != '\0') {
					
			this.concat(atual);
					
			try {
				if(leitura.hasNextCaractere()) {
					atual = prox;
					prox = leitura.nextCaractere();	
				} else {
					atual = prox;
					prox = '\0';

					if(!this.isSeparador(atual)) {
						this.concat(atual);
						this.atualizaChar();
					}
					
					break;
				}			
		
						
				} catch (IOException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ModoException e) {
						// TODO Auto-generated catch block
					e.printStackTrace();
					}
			}
				
		this.createErro(linha);
		
	}



	private void cadeiaCaracteres() {
		this.cadeia.isCadeiaCaractere(atual);
		
		do {
			this.concat(atual);
			
			try {
				if(leitura.hasNextCaractere()) {
					atual = prox;
					prox = leitura.nextCaractere();					
				} else {
					
					atual = prox;
					prox = '\0';
					
					if(this.cadeia.isCadeiaCaractere(atual)) {
						concat(atual);
						this.atualizaChar();
					}
					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} while(this.cadeia.isCadeiaCaractere(atual));
		
		
		
		if ((this.isSeparador(atual) | atual == '\0') && cadeia.isEstadoFinal()) {
		
			this.createToken(Classe.CADEIA_DE_CARACTERES, linha);
			
		} else {
			erro();
		}
		
		this.cadeia.resetAutomato();
		lexema ="";

	}



	private void delimitador() {
		
		this.concat(atual);
		this.atualizaChar();
		this.createToken(Classe.DELIMITADOR, linha);
		lexema ="";

				
	}



	private void operadorRelacinal() {
		
		this.concat(atual);
		
		if((atual == '!' && prox == '=') |(atual == '=' && prox == '=') | (atual == '<' && prox == '=')|
				(atual == '>' && prox == '=')) {
			this.concat(prox);
			this.atualizaChar();
		} 
		
		if(prox == '!' | prox == '=' | prox == '<' | prox == '>'){
			this.atualizaChar();
			erroRelacional();
			
		} else {
			this.createToken(Classe.OPERADOR_RELACIONAL, linha);
			this.atualizaChar();

			
		}
		
		lexema ="";
		
		
	}



	private void erroRelacional() {
				
		while(atual== '=' | atual =='!' | atual =='<' | atual=='>' ){
						
			this.concat(atual);

			try {
				if(leitura.hasNextCaractere()) {
					atual = prox;
					prox = leitura.nextCaractere();					
				} else {
					atual = prox;
					prox = '\0';
					
					if(atual== '=' | atual =='!' | atual =='<' | atual=='>') {
						this.concat(atual);
						this.atualizaChar();
					}
					

					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
		
		this.createErro(linha);
	
		
	}



	private void operadorAritmetico() {
		
		this.concat(atual);
		
		if((atual == '+' && prox == '+') |(atual == '-' && prox == '-')) {
			this.concat(prox);
			this.atualizaChar();
		} 
		
		
		if(prox == '+' | prox == '-' | prox == '*' ){
			this.atualizaChar();
			erroAritmetico();
			
		} else {
			this.createToken(Classe.OPERADOR_ARITMETICO, linha);
			this.atualizaChar();

			
		}
		
		lexema ="";
		
	}



	private void erroAritmetico() {
		
		
		while(atual== '*' | atual =='+' | atual =='-' | (atual=='/' && prox != '/')){
						
			this.concat(atual);

			try {
				if(leitura.hasNextCaractere()) {
					atual = prox;
					prox = leitura.nextCaractere();					
				} else {
					atual = prox;
					prox = '\0';
					
					if (atual== '*' | atual =='+' | atual =='-' | (atual=='/' && prox != '/')) {
						this.concat(atual);
						this.atualizaChar();
					}
					
					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

		}
		
		this.createErro(linha);
	
		
	}



	private void numero() {

		this.numero.isNumero(atual);
		
		do {
			this.concat(atual);
			
			try {
				if(leitura.hasNextCaractere()) {
					atual = prox;
					prox = leitura.nextCaractere();					
				} else {
					
					atual = prox;
					prox = '\0';
					
					if(this.comentario.isComentario(atual)) {
						this.concat(atual);
						this.atualizaChar();
					}

					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} while(this.numero.isNumero(atual));
		
		
		
		if (atual !='.' && (this.isSeparador(atual) | atual == '\0') && this.numero.isEstadoFinal()) {
			
			this.createToken(Classe.NUMERO, linha);
		
		} else {
			erroNumero();
		}
		
		this.numero.resetAutomato();

		lexema ="";

	}



	private void erroNumero() {
		
		while(atual == '.' | (!this.isSeparador(atual) && atual !='\0')) {
			
			this.concat(atual);
			 
			try {
				if(leitura.hasNextCaractere()) {
					atual = prox;
					prox = leitura.nextCaractere();
					
				} else {
					atual = prox;
					prox = '\0';
					
					if(atual == '.' | (!this.isSeparador(atual) && atual !='\0')) {
						this.concat(atual);
						this.atualizaChar();
					}
					break;
				}			

				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.createErro(linha);
		
	}



	private void identificador() {
		
		this.identificador.isIdentificador(atual);
		
		do {
			this.concat(atual);
			
			try {
				if(leitura.hasNextCaractere()) {
					atual = prox;
					prox = leitura.nextCaractere();					
				} else {
					
					atual = prox;
					prox = '\0';
					
					if(this.identificador.isIdentificador(atual)){
						this.concat(atual);
						this.atualizaChar();
					}					

					break;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ModoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} while(this.identificador.isIdentificador(atual));
		
		
		
		if ((this.isSeparador(atual) | atual == '\0') && this.identificador.isEstadoFinal()) {
		
			if(this.palavraReservada.contains(lexema)){
				
				this.createToken(Classe.PALAVRA_RESERVADA, linha);
			} else {
				
				this.createToken(Classe.IDENTIFICADOR, linha);
			}
			
			
		} else {
			erro();
		}
		
		this.identificador.resetAutomato();
		lexema ="";

		
	}

	private boolean isSeparador(char c) {
		 
		if(c == ' ' | c =='\t' | c == '\n' | c =='+' | c == '-' | c =='*' | c =='/' | c =='!' | c =='=' | c =='<'|
				c =='>'| c =='&'|c =='|' |c ==';' | c ==',' | c =='('| c ==')'| c =='[' | c ==']' | c =='{' | c =='}'
				| c =='.' | c ==':' | c=='\r' | ((int)c > 31 && (int) c < 127)) {
			
			return true;
		}
		
		return false;
			 
	}


	

	public void inicializar() {
		this.cadeia = new AutomatoCadeiaCaractere();
		this.comentario = new AutomatoComentario();
		this.identificador = new AutomatoIdentificador();
		this.numero = new AutomatoNumeros();

		this.listaDeTokens = new ArrayList<>();
		this.listaDeErro = new ArrayList<>();
		this.palavraReservada = new ArrayList<>();
		this.lexema = "";		
		
		this.palavraReservada.add("programa");
		this.palavraReservada.add("constantes");
		this.palavraReservada.add("variaveis");
		this.palavraReservada.add("metodo");
		this.palavraReservada.add("resultado");
		this.palavraReservada.add("principal");
		this.palavraReservada.add("se");
		this.palavraReservada.add("entao");
		this.palavraReservada.add("senao");
		this.palavraReservada.add("enquanto");
		this.palavraReservada.add("leia");
		this.palavraReservada.add("escreva");
		this.palavraReservada.add("vazio");
		this.palavraReservada.add("inteiro");
		this.palavraReservada.add("real");
		this.palavraReservada.add("boleano");
		this.palavraReservada.add("texto");
		this.palavraReservada.add("verdadeiro");
		this.palavraReservada.add("falso");
		
	}

	private void createToken(Classe classe, int linha) {
		this.listaDeTokens.add(new Token(lexema, classe, linha));
	}
	
	private void createErro(int linha) {
		this.listaDeErro.add(new Token(lexema, Classe.ERRO, linha));
	}

	public void concat(char c) {
		this.lexema += Character.toString(c);
	}

	public void atualizaChar() {
		
		try {
			if(leitura.hasNextCaractere()) {
				atual = prox;
				prox = leitura.nextCaractere();
			} else {
				atual = prox;
				prox = '\0';

			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ModoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	private void escreveSaida(String arquivo) {
		arquivo = arquivo.substring(0,  arquivo.indexOf("."));
		
		ManipuladorDeArquivo escrita;
		try {
			escrita = new ManipuladorDeArquivo(arquivo + ".saida", Modo.ESCRITA);
		

		for (int i = 0; i < this.listaDeTokens.size(); i++) {
			if (i == 0) {
				System.out.println("Tokens Validos\n");
				escrita.escreverArquivo("Tokens Validos\r\n");
			}
			Token t = this.listaDeTokens.get(i);
			System.out.println(t.getLinha() + " - " + t.getValor() + " - " + t.getClasse().getClasse());
			escrita.escreverArquivo(t.getLinha() + " - " + t.getValor() + " - " + t.getClasse().getClasse() + "\r\n");
		}

		for (int i = 0; i < this.listaDeErro.size(); i++) {
			if (i == 0) {
				System.out.println("\nTokens Invalidos\n");
				escrita.escreverArquivo("\r\nTokens Invalidos\r\n");
			}
			Token t = this.listaDeErro.get(i);
			System.out.println(t.getLinha() + " - " + t.getValor() + " - " + t.getClasse().getClasse());
			escrita.escreverArquivo(t.getLinha() + " - " + t.getValor() + " - " + t.getClasse().getClasse() + "\r\n");

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
	
	public ArrayList<Token> getErro(){
		return this.listaDeErro;
	}

}
