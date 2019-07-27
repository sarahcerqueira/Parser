/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author User-PC
 */
public class AnalisadorLexico {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        //Scanner scanner = new Scanner (new FileReader("teste.txt")).useDelimiter("\\n");
        
        
     FileInputStream entrada = new FileInputStream("teste.txt");

     InputStreamReader entradaFormatada = new InputStreamReader(entrada);
     
 
     while(entradaFormatada.ready()){
         
        int c = entradaFormatada.read();
        System.out.println((char)c);}
        
        
        // TODO code application logic here
       // lerArquivo();
    }
    
    public static void lerArquivo() throws FileNotFoundException, IOException{
        try{
            //String nomeDoArquivo = JOptionPane.showInputDialog("Entre com o nome do arquivo");
            String nomeDoArquivo;
            
            JFileChooser file = new JFileChooser(); 
            file.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int i= file.showOpenDialog(null);
            
            if (i==1){
                nomeDoArquivo = "";
            } else {
                File arquivo = file.getSelectedFile();
                nomeDoArquivo = arquivo.getName();
            }
            BufferedReader br = new BufferedReader(new FileReader(nomeDoArquivo));
	
            while(br.ready()){
		String linha = br.readLine();
		System.out.println(linha);
            }
            
            br.close();
	
        }catch(IOException ioe){
            ioe.printStackTrace();
	} 
   } 
    
  }
    

