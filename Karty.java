/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serwer;

/**
 *
 * @author izab
 */
// skopiowana z klienta
public class Karty {
    private String [][] karty = new String [4][13]; // zawiera nazwy kart
    private boolean [][] czyWybrana = new boolean [4][13]; // czy dana karta zostala juz wybrana
    public Karty(){
       for(int i=0;i<4;i++){
           for(int k=0;k<13;k++){               
               czyWybrana[i][k] = false; // na razie wszystkie sa wolne
           }
       } 
       for(int i=0;i<4;i++){ //  nadaje nazwy karta
           for(int k=0;k<13;k++){
               if(k==0) karty[i][k]="2";
               if(k==1) karty[i][k]="3";
               if(k==2) karty[i][k]="4";
               if(k==3) karty[i][k]="5";
               if(k==4) karty[i][k]="6";
               if(k==5) karty[i][k]="7";
               if(k==6) karty[i][k]="8";
               if(k==7) karty[i][k]="9";
               if(k==8) karty[i][k]="10";
               if(k==9) karty[i][k]="jopek";
               if(k==10) karty[i][k]="krolowa";
               if(k==11) karty[i][k]="krol";
               if(k==12) karty[i][k]="as";
               if(i==0) karty[i][k]=karty[i][k]+"pik";
               if(i==1) karty[i][k]=karty[i][k]+"kier";
               if(i==2) karty[i][k]=karty[i][k]+"karo";
               if(i==3) karty[i][k]=karty[i][k]+"trefl";
           }
       }       
    }
    public String [] losuj(){ // metoda odpowiedzialna za losowanie kart
        int licznik = 0;
        String [] wybrane = new String[5];
        while(licznik<5){ // Dla jednego gracza
            int kolor = (int) (Math.random()*4);
            int wartosc = (int) (Math.random()*13);
            if(czyWybrana[kolor][wartosc]==false){
                czyWybrana[kolor][wartosc]=true;
                wybrane[licznik]=karty[kolor][wartosc]+".jpg";
                licznik++;
            }
        }
        return wybrane;// tablica z 5 wylosowanymi kartami
    }   
}
