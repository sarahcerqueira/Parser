package util;

public class Token {

	private String valor;
	private Classe classe;
	private int linha;
	
	public Token (String v, Classe c, int l) {
		
		this.classe = c;
		this.linha = l;
		this.valor = v;
	}

	public String getValor() {
		return valor;
	}


	public Classe getClasse() {
		return classe;
	}


	public int getLinha() {
		return linha;
	}

	public String toString() {
		return linha + " " + valor + " " + classe;
	}
	
	
}
