package util;

import java.util.ArrayList;

public class Escopo {
	private String nome;
	private ArrayList<String[]> variaveis;
	private ArrayList<String[]> paramentros;
	private String retorno;


	public Escopo(String nome) {
		this.nome = nome;
		variaveis = new ArrayList<String[]>();
		paramentros = new ArrayList<String[]>();
		
	}
	
	public String getNome() {
		return nome;
	}
	
	public void addVariaveis(String cadeia, String token, String tipo, String caso) {
            String [] s = new String[4];
            s[0] = cadeia;
            s[1] = token;
            s[2] = tipo;
            s[3] = caso;
		
    	variaveis.add(s);
	}
	
	public boolean isVariavel(String v) {
		int tam, aux =0;
		tam = variaveis.size();
		
		while(aux < tam) {
			if(variaveis.get(aux)[0].equals(v)) {
				return true;}
			
			aux = aux +1;
		}
		
		return false;
		
	}
	
	public String getTipo (String v) {
		int tam, aux =0;
		tam = variaveis.size();
		
		while(aux < tam) {
			String [] a = variaveis.get(aux);
			if(a[0].equals(v)) {
				return a[2]; }
			
			aux = aux +1;
		}
		
		aux =0;
		tam = this.paramentros.size();
		
		while(aux < tam) {
			String [] a = paramentros.get(aux);
			if(a[1].equals(v)) {
				return a[0]; }
			
			aux = aux +1;
		}
		
		return null;
		
	}
	
	public String[] getVariavel(String v) {
		int tam, aux =0;
		tam = variaveis.size();
		
		while(aux < tam) {
			String [] a = variaveis.get(aux);
			if(a[0].equals(v)) {
				return a; }
			
			aux = aux +1;
		}
		
		aux =0;
		tam = this.paramentros.size();
		
		while(aux < tam) {
			String [] a = paramentros.get(aux);
			if(a[1].equals(v)) {
				String [] b = new String [4];
				
				b[0] = a[1];
	            b[2] = a[0];
				return b; }
			
			aux = aux +1;
		}
				
		
		return null;
	}
	
	public void addParametros(String tipo, String cadeia) {
		String [] p = new String[2];
		
		p[0] = tipo;
		p[1] = cadeia;
		
		this.paramentros.add(p);
	}
	
	public boolean hasParamentro(String id) {
		int tam, aux=0;
		tam = this.paramentros.size();
		String [] s;
		
		while(aux < tam) {
			s = this.paramentros.get(aux);
			
			if(s[1].equals(id)) {
				return true;
			}
		}
		
		return false;
	}

	public ArrayList<String[]> getParametros() {
		return this.paramentros;
	}
	
	public String tipoParametro(String id) {
		int tam, aux=0;
		tam = this.paramentros.size();
		String [] s;
		
		while(aux < tam) {
			s = this.paramentros.get(aux);
			
			if(s[1].equals(id)) {
				return s[0];
			}
			aux = aux +1;
		}
		
		return null;
	}
	
	public void setRetorno(String tipo) {
		this.retorno = tipo;
		
		
	}
	
	public String getRetorno() {
		return this.retorno;
	}
	
	
}
