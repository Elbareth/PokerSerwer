/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package serwer;

// printer.println() <- wyślij do jednego użytkownika
// wyslij() <- wysylam do wszystkich

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author izab
 */

class Server{
    private ServerSocket socketServer = null;
    private Socket socket = null;
    private ArrayList gracze = null; 
    private PrintWriter printer = null;
    private BufferedReader reader = null;
    private boolean czyWszystkoOK = true;
    private Logger logger = Logger.getLogger("Logs");
    private FileHandler fileHandler;
    private Integer numerUzytkownika = 1;
    private Integer liczbaWszystkich = 1;
    private Integer pula = 0;
    private Integer wymagam = 5;
    private Karty karty = new Karty();
    private int licznik = 1;
    private int licznikRund = 1;
    private List pass = new ArrayList();
    private int licznikDoPass = 0;
    private boolean gramyDalej=true;
    private Map<Integer, PrintWriter> players;
    class InformacjePodstawowe implements Runnable{ // serwer jest oparty o wątki
        private Socket socket = null;
        private String [] wiadomosc = null;
        private String stanKonta = null;
        private String nadwyzka = null;
        private String suma = null;
        private BufferedReader czytelnik = null;
        private InputStreamReader input = null;
        private Integer numerUzytkownika = 1;
        public InformacjePodstawowe(Socket socket,String [] wiadomosc, int numerUzytkownika){            
            this.wiadomosc = wiadomosc;
            this.socket = socket;
            this.numerUzytkownika = numerUzytkownika;
            try{
                input = new InputStreamReader(socket.getInputStream());
                czytelnik = new BufferedReader(input);
            }
            catch(IOException e){
                System.out.println(e.toString());
                logger.info(e.toString());
            }
        }
        @Override
        public void run(){ 
            // W przyszlosci bedzie osobny panel gdzie uzytkownik bedzie podawal swoj login
            // i za jego pomoca sie laczyl
           String message = null;
           for(int i=0;i<5;i++){               
               printer.println(wiadomosc[i]);
               printer.flush();
           }           
           printer.println("10000");
           printer.flush();
           printer.println("0");
           printer.flush();
           printer.println(numerUzytkownika.toString());
           printer.flush();
           numerUzytkownika++; // kloejny uzytkownik bedzie mial inny numer
           try{
               if((message = czytelnik.readLine()).equals("Recv")){ // klient odebral nasze infromacje podstawowe
                   czyWszystkoOK=true;
                   logger.info("Użytkownik przyjął wymagane informacje podstawowe");
                   runda(); // moze wiec zagrac w rundzie
               }
               else czyWszystkoOK = false;  // uzytkownik nie otrzymal wszystkich danych             
           }
           catch(IOException e){               
               logger.info(e.toString());
           }
        }
        public void zwiekszLicznik(){ // odpowiada za zwiekszanie licznika             
            licznik++; // ten licznik pokazuje ktory uzytkownik ma byc teraz aktywny            
            wyslij("Daj numer");
            if(licznik > liczbaWszystkich){//Zaczynamy na nowa runde                
                licznik = 1; // znowu zaczynamy od pierwszego uzytkownika               
                wymagam = wymagam -5; 
                if(wymagam < 0) wymagam = 0;
                licznikRund++; // przechodzimy do kolejnej rundy               
            }  
        }
        public void koniec(){
            try{                
                wyslij("liczymy"); // do wszyskich zeby liczyli                
                logger.info("Zaczynamy podliczać punkty");
                String tmp = null;
                Integer max = 0; 
                int boTak = 0;                
                while((tmp=czytelnik.readLine()) != null && boTak < liczbaWszystkich){                                        
                    int liczba = Integer.parseInt(tmp);
                    if(max < liczba){
                        max = liczba;                        
                    }   
                    boTak++;                    
                    if(boTak == liczbaWszystkich){
                        logger.info("Wygrał użytkownik, który zdobył "+max);
                        logger.info("Koniec gry");
                        wyslij(max.toString());
                        gramyDalej = false;
                        socket.close();
                        System.exit(0);  
                    }
                }                              
            }
            catch(IOException e){               
                logger.info(e.toString());
            }
        }
        public void zaczynamy(){ // zaczynam - przesyalam tylko numer uzytkownika
           try{               
               String tmp = czytelnik.readLine();
               if(tmp.equals("koniec")){
                   koniec();
               }
               else{                   
                    numerUzytkownika = Integer.parseInt(tmp);                    
                    logger.info("Uzytkownik przeslal swój numerek i moze teraz grac "+numerUzytkownika);
               }               
            }
            catch(IOException e){                
                logger.info(e.toString());
            }   
        }
        public void runda(){           
            wymagam = 5; // tyle na poczatku wymagam od pierwszego uzytkownika by wylozyl na stol
            while(gramyDalej){               
                while(licznikRund == 1){ // jestesmy w pierwszej rundzie.                     
                    logger.info("Jestem w rundzie pierwszej");
                    zaczynamy();                   
                   if(numerUzytkownika == licznik){ // tu okreslam kto jest aktywny a kto nie                        
                       graczAktywny();
                   }
                   else{
                       graczPauza();
                   }
                   zwiekszLicznik();
                }
                while(licznikRund == 2){                    
                    logger.info("Jestem w rundzie 2");
                    zaczynamy();                    
                    if(numerUzytkownika == licznik){                       
                        boolean ok = false;
                        if(pass != null){
                            for(int i=0;i<pass.size();i++){ // tablica uzytkownikow ktorzy spasowali
                                if(pass.get(i) == numerUzytkownika){// jesli trafilam na uzytkownika ktory spasowal
                                    logger.info("Użytkonik o numerze "+numerUzytkownika+" jest spasowany");
                                    ok = true;
                                }
                            }                                                        
                        }
                        if(ok==true){                           
                            graczPauza();
                        }
                        else{
                           graczAktywny();// jesli nie jest spasowany gra dalej 
                        }                        
                    }
                    else{
                        graczPauza();
                    }  
                    zwiekszLicznik();
                }
                while(licznikRund >= 3 && gramyDalej){                    
                    logger.info("Jestem w rudznie powyżej 3");                    
                    zaczynamy();                   
                   if(numerUzytkownika == licznik){                       
                       if(wymagam == 0){ // od 3 rundy uzytkonik moze zakonczyc gre
                            //Umazliwia odblokowanie odsloniecia kart
                            // jesli odlokuje karty to koniec gry
                            // podliczmy ile kto zdobyl punktow
                            // dla zwyciezcy tworze dyplom pdf z
                            // kwota jaka wygral, loginem i gratulacjami                            
                            logger.info("Mam zerowe wymagania więc mogę odsłonic karty");
                            printer.println("odslon");
                            printer.flush();                          
                        }                                  
                        boolean ok = false;
                        if(pass != null){
                            for(int i=0;i<pass.size();i++){ // tablica uzytkownikow ktorzy spasowali
                                if(pass.get(i) == numerUzytkownika){// jesli trafilam na uzytkownika ktory spasowal
                                    logger.info("Użytkonik o numerze "+numerUzytkownika+" został spasowany");
                                    ok = true;
                                }
                            }                            
                        }
                        if(ok==true){                            
                            graczPauza();
                        }
                        else{
                           graczAktywny(); 
                        } 
                   }
                   else{
                       graczPauza();
                   }                   
                   zwiekszLicznik();
                }               
            }
        }
        public void graczAktywny(){           
            printer.println("Go"); // otrzymuje informajce zeby dzialal                   
            printer.flush();                      
            try{   
                String[] info = czytelnik.readLine().split(",");
                if(info.length == 3){
                    stanKonta = info[0];
                    nadwyzka = info[1];
                    suma = info[2];
                }
                if(info.length == 1){
                    stanKonta = info[0];
                }                
                if(stanKonta.equals("pass")){ // jesli pierwsza wiadomosc wyslana od uzytkownika
                    // to pass to oznacza ze sie poddal. Moze tylko ogladac rozgrywke                   
                    graczSpasowany();
                    logger.info("Uzytkownik poddał się w tej rundzie");
                }   
                if(stanKonta.equals("koniec")){
                    logger.info("Użytkownicy zdecydowali, ze to koniec gry");
                    koniec();
                }
                else{                   
                    wymagam = wymagam + Integer.parseInt(nadwyzka);                                                 
                    pula = pula + Integer.parseInt(suma); // ile w sumie                   
                    String all = wymagam.toString()+","+pula.toString();
                    wyslij(all);
                    logger.info("Na koncie uzytkownika "+licznik+
                        " jest teraz "+stanKonta+
                        " teraz będę wymagać "+wymagam+" a pula całości to "+pula);
                }                            
            }
            catch(IOException e){                
                logger.info(e.toString());
            }
        }
        public void graczPauza(){// ten gracz jest na razie zestopowany przez serwer
            logger.info("Uzytkownicy o innym numerze niz "+licznik+" musz teraz spauzowac"); 
            players.forEach((player, printer) -> {
                if(player != numerUzytkownika) {
                    printer.println("Stop");
                    printer.flush();
                }
            });                    
        }
        public void graczSpasowany(){ // ten gracz sie poddal           
            pass.add(numerUzytkownika);
            logger.info("Gracz o numerze "+numerUzytkownika+" się poddał");
        }               
    }
    public void connect(){
        gracze = new ArrayList(); 
        players = new HashMap<Integer, PrintWriter>();
        try{
            fileHandler = new FileHandler("logs.txt");            
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            SimpleFormatter simple = new SimpleFormatter();
            fileHandler.setFormatter(simple);
            socketServer = new ServerSocket(7000); // port dla serwera
            while(true){
                socket = socketServer.accept(); // akceptujemy graczy
                liczbaWszystkich = gracze.size()+1; // ile mamy graczy
                logger.info("Zaakceptowano uzytkownika o sockecie "+socket.toString());
                printer = new PrintWriter(socket.getOutputStream()); 
                gracze.add(printer);  // piszacy dla kazdego uzytkownika 
                players.put(numerUzytkownika, printer);
                String [] wybraneKarty = new String[5];
                wybraneKarty = karty.losuj(); // losujemy karty
                logger.info("Wylosowano karty "+wybraneKarty[0]+" "+wybraneKarty[1]+" "+
                        wybraneKarty[2]+" "+wybraneKarty[3]+" "+wybraneKarty[4]+
                        "i ustawiono wartosc stanu kotna na 10000");
                
               Thread watek = new Thread(new InformacjePodstawowe(socket, wybraneKarty,numerUzytkownika)); // watek uzytlkownika
               // ma sobie posrt, karty i numer id
                watek.start();   
                numerUzytkownika++;
            }
        }
        catch(IOException e){           
            logger.info(e.toString());
        }        
    }
    public void wyslij(String wiadomosc){ // do wyslania wiadomosc       
        Iterator it = gracze.iterator();        
        while(it.hasNext()){ // przechodze po uzytkownikach
            try{ 
                logger.info(wiadomosc);
                PrintWriter printer = (PrintWriter) it.next();
                printer.println(wiadomosc);// wysylam im wiadomosc              
                printer.flush();
            }
            catch(Exception e){                
                logger.info(e.toString());
            }
        }
    }       
}
public class Serwer {
    
    public static void main(String[] args) {
        Server s = new Server();              
        s.connect();       
    }
    
}
